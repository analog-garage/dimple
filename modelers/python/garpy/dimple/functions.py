#  Copyright 2025 Analog Devices Inc.
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
Built-in factor function implementations.
"""

from __future__ import annotations

from typing import cast

from .java import start # noqa

from com.analog.lyric.dimple.factorfunctions import (
    Equals as JEquals,
    Normal as JNormal,
    NotEquals as JNotEquals,
    Uniform as JUniform,
)

from .base import JWrapper, jpype_wrapper
from .factor import FactorFunctionBase
from .message import NormalParameters

__all__ =[
    "Equals",
    "Normal",
    "NotEquals",
]

@jpype_wrapper(JEquals)
class Equals(FactorFunctionBase, JWrapper[JEquals]):
    def __init__(self) -> None:
        super().__init__(jobj=JEquals())  # type: ignore

@jpype_wrapper(JNotEquals)
class NotEquals(FactorFunctionBase, JWrapper[JNotEquals]):
    def __init__(self) -> None:
        super().__init__(jobj=JNotEquals())  # type: ignore

class UnaryFactorFunction(FactorFunctionBase):
    pass

@jpype_wrapper(JNormal)
class Normal(UnaryFactorFunction, JWrapper[JNormal]):
    def __init__(
        self,
        mean: NormalParameters|float|None = None,
        precision: float|None = None,
    ) -> None:
        if isinstance(mean, NormalParameters):
            jnorm = JNormal(mean.jobj)
        elif isinstance(mean, (float,int)):
            precision = float(precision) if isinstance(precision, (float,int)) else 0.0
            jnorm = JNormal(float(mean), precision)
        else:
            jnorm = JNormal()
        super().__init__(jobj=jnorm)

    @property
    def jnormal(self) -> JNormal:
        return cast(JNormal, self.jobj)

    @property
    def parameters(self) -> NormalParameters:
        return NormalParameters.for_jobj(self.jnormal.getParameters())

    @property
    def mean(self) -> float:
        return self.jnormal.getMean()

    @mean.setter
    def mean(self, value: float) -> None:
        self.jnormal.setMean(value)

    @property
    def precision(self) -> float:
        return self.jnormal.getPrecision()

    @precision.setter
    def precision(self, value: float) -> None:
        self.jnormal.setPrecision(value)

    @property
    def standard_deviation(self) -> float:
        return self.jnormal.getStandardDeviation()

    @property
    def variance(self) -> float:
        return self.jnormal.getVariance()

@jpype_wrapper(JUniform)
class Uniform(UnaryFactorFunction, JWrapper[JUniform]):
    def __init__(self) -> None:
        super().__init__(jobj=JUniform())  # type: ignore
