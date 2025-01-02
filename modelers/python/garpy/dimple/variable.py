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

import math
from typing import Sequence, TypeVar, TYPE_CHECKING, cast

from .java import start # noqa

from com.analog.lyric.dimple.solvers.core.parameterizedMessages import (
    DiscreteWeightMessage as JDiscreteWeightMessage,
    DiscreteMessage as JDiscreteMessage, DiscreteMessage
)

from com.analog.lyric.dimple.model.domains import (
    Domain as JDomain,
    RealDomain as JRealDomain,
)
from com.analog.lyric.dimple.model.values import Value as JValue
from com.analog.lyric.dimple.model.variables import (
    Variable as JVariable,
    Discrete as JDiscrete,
    Bit as JBit,
    Real as JReal,
)
from garpy.dimple.base import jpype_wrapper, Node, JWrapper
from .domain import Domain, DiscreteDomain, ET, RealDomain
from .value import Datum

if TYPE_CHECKING:
    from .factor import Factor

__all__ = [
    "Discrete",
    "JDiscrete",
    "JDomain",
    "JReal",
    "JRealDomain",
    "JVariable",
    "Real",
    "Variable",
]

T_co = TypeVar("T_co", covariant=True)

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
    def prior(self) -> Datum|None:
        jprior = self.jvariable.getPrior()
        return jprior and cast(Datum, JWrapper.for_jobj(jprior))

    @prior.setter
    def prior(self, prior: Datum|None) -> None:
        assert prior is None or isinstance(prior, JWrapper)
        jprior = prior and prior.jobj
        self.jvariable.setPrior(jprior)

    @property
    def siblings(self) -> Sequence[Factor]: # type: ignore
        return super().siblings # type: ignore


@jpype_wrapper(JDiscrete)
class Discrete[ET](Variable, JWrapper[JDiscrete]):
    def __init__(self, domain_or_value: DiscreteDomain[ET] | ET, *values: ET):
        if isinstance(domain_or_value, JDiscrete):
            jdiscrete = domain_or_value
        elif isinstance(domain_or_value, Discrete):
            jdiscrete = JDiscrete(domain_or_value.domain.jdomain)
        else:
            domain = DiscreteDomain.create(domain_or_value, *values)
            jdiscrete = JDiscrete(domain.jdomain)
        super().__init__(jobj=jdiscrete)

    @classmethod
    def for_jobj(cls, jobj: JDiscrete) -> Discrete[ET]: # type: ignore
        # TODO is there some way to use __new__ to avoid the
        # type check hack in __init__ ?
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
    def prior_weights(self) -> tuple[float,...]:
        jprior = self.jvariable.getPrior()
        if isinstance(jprior, JDiscreteMessage):
            return tuple(jprior.getWeights())
        if isinstance(jprior, JValue):
            jvalue = cast(JValue, jprior)
            index = jvalue.getIndex()
            weights = [0.0] * len(self.domain)
            weights[index] = 1.0
            return tuple(weights)
        return (1.0/len(self.domain),) * len(self.domain)

    @prior_weights.setter
    def prior_weights(self, *weights: float) -> None:
        self.jvariable.setPrior(*weights)

    @property
    def value(self) -> ET:
        return self.jvariable.getValue()

@jpype_wrapper(JBit)
class Bit(Discrete[int]):
    def __init__(self) -> None:
        super().__init__(DiscreteDomain.bit())

    @property
    def p1_prior(self) -> float:
        return self.prior_weights[1]

    @p1_prior.setter
    def p1_prior(self, p1: float) -> None:
        self.prior_weights = (1.0-p1, p1)

    @property
    def p1_belief(self) -> float:
        return self.belief[1]


@jpype_wrapper(JReal)
class Real(Variable, JWrapper[JReal]):
    def __init__(
        self,
        domain_or_lower: RealDomain | float | None=None,
        upper:float = math.inf, /,
    ) -> None:
        if isinstance(domain_or_lower, RealDomain):
            jdomain = domain_or_lower.jdomain
        elif isinstance(domain_or_lower, float):
            jdomain = JRealDomain.create(domain_or_lower, upper)
        else:
            jdomain = JRealDomain.unbounded()
        super().__init__(jobj=jdomain)

    @property
    def domain(self) -> RealDomain:
        return RealDomain.for_jobj(self.jvariable.getDomain())
