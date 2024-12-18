#  Copyright 2024 Analog Devices Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
"""
Interfaces for Dimple random variables
"""

from __future__ import annotations

from abc import ABC
from typing import Any, Type, TypeVar, cast

from com.analog.lyric.dimple.model.domains import Domain as JDomain, DiscreteDomain as JDiscreteDomain
from com.analog.lyric.dimple.model.variables import Variable as JVariable, Discrete as JDiscrete
from garpy.dimple.base import jpype_wrapper, _register_wrapper, _lookup_wrapper, _lookup_wrapper_type, ValueType, Node

__all__ = [
    "Discrete",
    "DiscreteDomain",
    "Domain",
    "JDiscrete",
    "JDomain",
    "JVariable",
    "Variable",
]

T_co = TypeVar("T_co", covariant=True)

@jpype_wrapper(JDomain)
class Domain(ABC):
    __slots__ = ["_jdomain"]

    _jdomain: JDomain

    def __init__(self, *, jdomain: JDomain) -> None:
        self._jdomain = jdomain
        _register_wrapper(self, jdomain)

    @classmethod
    def for_jdomain(cls: Type[T_co], jdomain: JDomain) -> T_co:
        domain = _lookup_wrapper(cls, jdomain)
        if domain is None:
            jpype_cls = type(jdomain)
            wrapper_cls = _lookup_wrapper_type(jpype_cls)
            if wrapper_cls is None:
                raise TypeError(f"Cannot find Domain class for '{jpype_cls}'")
            domain = wrapper_cls(jdomain=jdomain)
        return domain

    @property
    def jdomain(self) -> JDomain:
        return self._jdomain

    @property
    def is_discrete(self) -> bool:
        return self._jdomain.isDiscrete()

    def __hash__(self) -> int:
        return self._jdomain.hashCode()

    def __eq__(self, other: Any) -> bool:
        return isinstance(other, Domain) and self._jdomain.equals(other._jdomain)

    # TODO more methods


ET = TypeVar("ET", bound=ValueType)


@jpype_wrapper(JDiscreteDomain)
class DiscreteDomain[ET](Domain):
    def __init__(self, *, jdomain: JDiscreteDomain) -> None:
        super().__init__(jdomain=jdomain)

    @classmethod
    def create(cls, *elts: ET) -> DiscreteDomain:
        return DiscreteDomain(jdomain=JDiscreteDomain.create(*elts))

    @property
    def jdomain(self) -> JDiscreteDomain:
        return cast(JDiscreteDomain, self._jdomain)

    @property
    def size(self) -> int:
        return self.jdomain.size()

    def __getitem__(self, i: int) -> ET:
        return self.jdomain.getElement(i)

    def __len__(self) -> int:
        return self.jdomain.size()


@jpype_wrapper(JVariable)
class Variable(Node):
    @property
    def jnode(self) -> JVariable:
        return self.jvariable

    @property
    def jvariable(self) -> JVariable:
        return cast(JVariable, self._jnode)

    @property
    def domain(self) -> Domain:
        return Domain.for_jdomain(self.jvariable.getDomain())


@jpype_wrapper(JDiscrete)
class Discrete[ET](Variable):
    def __init__(self, domain: DiscreteDomain[ET]):
        super().__init__(jnode=JDiscrete(domain.jdomain))

    @property
    def domain(self) -> DiscreteDomain[ET]:
        return DiscreteDomain.for_jdomain(self.jvariable.getDomain())

    @property
    def jvariable(self) -> JDiscrete:
        return self._jnode

    @property
    def belief(self) -> tuple[float,...]:
        return tuple(self.jvariable.getBelief())

    @property
    def prior(self) -> tuple[float,...]:
        return tuple(self.jvariable.getPrior())

    @prior.setter
    def prior(self, *weights: float) -> None:
        self.jvariable.setPrior(*weights)

    @property
    def value(self) -> ET:
        return self.jvariable.getValue()

