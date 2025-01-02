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

from __future__ import annotations

from typing import Any, Iterator, TypeVar, overload

from jpype import JDouble, JLong

from .java import start as __start # noqa

from com.analog.lyric.dimple.model.domains import Domain as JDomain, DiscreteDomain as JDiscreteDomain, \
    RealDomain as JRealDomain

from .base import jpype_wrapper, JWrapper, ValueType

__all__ = [
    "Domain",
    "DiscreteDomain",
    "JDiscreteDomain",
    "JDomain",
    "JRealDomain",
    "RealDomain",
]

@jpype_wrapper(JDomain)
class Domain(JWrapper[JDomain]):
    @property
    def jdomain(self) -> JDomain:
        return self._jobj

    @property
    def is_discrete(self) -> bool:
        return self._jobj.isDiscrete()

    @property
    def is_real(self) -> bool:
        return self._jobj.isReal()

    def __hash__(self) -> int:
        return self._jobj.hashCode()

    def __eq__(self, other: Any) -> bool:
        return isinstance(other, Domain) and self._jobj.equals(other._jobj)

    def __contains__(self, val: Any) -> bool:
        return self.jdomain.inDomain(val)

    # TODO more methods


ET = TypeVar("ET", bound=ValueType)

@jpype_wrapper(JDiscreteDomain)
class DiscreteDomain[ET](Domain, JWrapper[JDiscreteDomain]):
    @classmethod
    def bit(cls) -> DiscreteDomain[int]:
        """
        Discrete domain with values 0 or 1
        """
        return DiscreteDomain.for_jobj(JDiscreteDomain.bit())

    @classmethod
    def bool(cls) -> DiscreteDomain[bool]:
        """
        Discrete domain with boolean values
        """
        return DiscreteDomain.for_jobj(JDiscreteDomain.bool())

    @classmethod
    def create(cls, *elts: ET) -> DiscreteDomain:
        return DiscreteDomain.for_jobj(jobj=JDiscreteDomain.create(*elts))


    @overload
    @classmethod
    def range(cls, start:int|float, stop:int|float, /, step:int|float=1) -> DiscreteDomain:
        ... # pragma: no cover

    @overload
    @classmethod
    def range(cls, stop: int | float, /, step: int | float = 1) -> DiscreteDomain:
        ... # pragma: no cover

    @classmethod  # type: ignore
    def range(
        cls,
        start:int|float,
        stop:int|float|None=None,
        /,
        step:int|float=1,
    ) -> DiscreteDomain:
        """
        A discrete domain over a numeric range.

        Has same signature as [range][] built-in function.

        Note that this sticks to the Python convention for the
        upper bound and therefore difers from the inclusive upper-bound
        used in the MATLAB and Java APIs.

        Args:
            start: starting value
            stop: exclusive upper-bound
            step: amount to increment
        """
        if stop is None:
            stop = start
            start = 0
        return DiscreteDomain.for_jobj(JDiscreteDomain.range(start, stop-1, step))

    # @property
    # def jdomain(self) -> JDiscreteDomain:
    #     return self._jobj

    @property
    def size(self) -> int:
        return self.jdomain.size()

    def __getitem__(self, i: int) -> ET:
        return self.jdomain.getElement(i)

    def __len__(self) -> int:
        return self.jdomain.size()

    def __iter__(self) -> Iterator[ET]:
        for i in range(len(self)):
            yield self[i]


@jpype_wrapper(JRealDomain)
class RealDomain(Domain, JWrapper[JRealDomain]):
    @classmethod
    def create(cls, lower_bound: float, upper_bound: float) -> RealDomain:
        return cls(jobj=JRealDomain.create(lower_bound, upper_bound))

    @classmethod
    def non_negative(cls) -> RealDomain:
        return cls(jobj=JRealDomain.nonNegative())

    @classmethod
    def non_positive(cls) -> RealDomain:
        return cls(jobj=JRealDomain.nonPositive())

    @classmethod
    def unbounded(cls) -> RealDomain:
        return cls(jobj=JRealDomain.unbounded())

    @property
    def is_bounded(self) -> bool:
        """
        True if either upper or lower bound is finite.
        """
        return self.jobj.isBounded()

    @property
    def jdomain(self) -> JRealDomain:
        return super().jdomain

    @property
    def lower_bound(self) -> float:
        return self.jdomain.getLowerBound()

    @property
    def upper_bound(self) -> float:
        return self.jdomain.getUpperBound()

    def subset_of(self, other: RealDomain) -> bool:
        return isinstance(other, RealDomain) and self.jdomain.isSubsetOf(other.jdomain)

    def __contains__(self, val: Any) -> bool:
        if isinstance(val, RealDomain):
            return val.subset_of(self)
        if isinstance(val, (float,int)):
            val = JDouble@val
        return super().__contains__(val)
