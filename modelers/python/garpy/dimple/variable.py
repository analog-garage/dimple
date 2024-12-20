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

from typing import Any, Sequence, Type, TypeVar, TYPE_CHECKING, cast

from com.analog.lyric.dimple.model.domains import Domain as JDomain, DiscreteDomain as JDiscreteDomain
from com.analog.lyric.dimple.model.variables import Variable as JVariable, Discrete as JDiscrete
from garpy.dimple.base import jpype_wrapper, ValueType, Node, JWrapper

if TYPE_CHECKING:
    from .factor import Factor

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
class Domain(JWrapper[JDomain]):
    @property
    def jdomain(self) -> JDomain:
        return self._jobj

    @property
    def is_discrete(self) -> bool:
        return self._jobj.isDiscrete()

    def __hash__(self) -> int:
        return self._jobj.hashCode()

    def __eq__(self, other: Any) -> bool:
        return isinstance(other, Domain) and self._jobj.equals(other._jobj)

    # TODO more methods


ET = TypeVar("ET", bound=ValueType)


@jpype_wrapper(JDiscreteDomain)
class DiscreteDomain[ET](Domain, JWrapper[JDiscreteDomain]):
    @classmethod
    def create(cls, *elts: ET) -> DiscreteDomain:
        return DiscreteDomain(jobj=JDiscreteDomain.create(*elts))

    @property
    def jdomain(self) -> JDiscreteDomain:
        return self._jobj

    @property
    def size(self) -> int:
        return self.jdomain.size()

    def __getitem__(self, i: int) -> ET:
        return self.jdomain.getElement(i)

    def __len__(self) -> int:
        return self.jdomain.size()


@jpype_wrapper(JVariable)
class Variable(Node, JWrapper[JVariable]):
    @property
    def jnode(self) -> JVariable:
        return self.jvariable

    @property
    def jvariable(self) -> JVariable:
        return cast(JVariable, self._jobj)

    @property
    def domain(self) -> Domain:
        return Domain.for_jobj(self.jvariable.getDomain())

    @property
    def siblings(self) -> Sequence[Factor]: # type: ignore
        return super().siblings # type: ignore


@jpype_wrapper(JDiscrete)
class Discrete[ET](Variable, JWrapper[JDiscrete]):
    def __init__(self, domain: DiscreteDomain[ET]):
        if isinstance(domain, JDiscrete):
            jdiscrete = domain
        else:
            jdiscrete = JDiscrete(domain.jdomain)
        super().__init__(jobj=jdiscrete)

    @classmethod
    def for_jobj(cls, jobj: JDiscrete) -> Discrete[ET]: # type: ignore
        return cls(jobj)

    @property
    def domain(self) -> DiscreteDomain[ET]:
        return DiscreteDomain.for_jobj(self.jvariable.getDomain())

    @property
    def jvariable(self) -> JDiscrete:
        return cast(JDiscrete, self._jobj)

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

