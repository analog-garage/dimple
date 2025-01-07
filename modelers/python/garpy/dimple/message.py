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

import math
from typing import Any, Self, Sequence

from .java import start # noqa

from com.analog.lyric.dimple.solvers.core.parameterizedMessages import (
    DiscreteEnergyMessage as JDiscreteEnergyMessage,
    DiscreteMessage as JDiscreteMessage,
    DiscreteWeightMessage as JDiscreteWeightMessage,
    NormalParameters as JNormalParameters,
    ParameterizedMessageBase as JParameterizedMessageBase
)

from .base import JWrapper, jpype_wrapper
from .value import DataRepresentationType, Datum

__all__ = [
    "DiscreteMessage",
    "DiscreteEnergyMessage",
    "DiscreteWeightMessage",
    "NormalParameters",
    "ParameterizedMessageBase",
]

@jpype_wrapper(JParameterizedMessageBase)
class ParameterizedMessageBase(Datum, JWrapper[JParameterizedMessageBase]):
    """
    Base class for parameterized factor graph messages.

    These can be thought of as a form of probability density function
    where [eval_energy][..] computes the unnormalized negative log probability
    which can be normalized by subtracting the [normalization_energy][..]
    """
    @property
    def has_deterministic_value(self) -> bool:
        return self.jobj.hasDeterministicValue()

    @property
    def is_null(self) -> bool:
        return self.jobj.isNull()

    @property
    def normalization_energy(self) -> float:
        return self.jobj.getNormalizationEnergy()

    @normalization_energy.setter
    def normalization_energy(self, energy: float) -> None:
        self.jobj.setNormalizationEnergy(energy)

    @property
    def representation_type(self) -> DataRepresentationType:
        return DataRepresentationType.MESSAGE

    def add_from(self, other: ParameterizedMessageBase) -> None:
        self.jobj.addFrom(other.jobj)

    def clone(self) -> Self:
        return self.for_jobj(self.jobj.clone())

    def compute_KL_divergence(self, other: ParameterizedMessageBase) -> float:
        return self.jobj.computeKLDivergence(other.jobj)

    def eval_energy(self, value: Any) -> float:
        """
        Computes energy for the given value for this message.

        The energy may be thought of either as a cost or as an unnormalized log
        probability.

        The returned energy value may be normalized by subtracting the
        [normalization_energy][..].
        """
        return self.jobj.evalEnergy(value)

    def eval_pdf(self, value: Any) -> float:
        """
        Evaluates the probability density function for this message for given value.
        """
        return -math.log(self.eval_energy(value) - self.normalization_energy)

    def set_from(self, other: ParameterizedMessageBase) -> None:
        self.jobj.setFrom(other.jobj)

    def set_null(self) -> None:
        self.jobj.setNull()

    def set_uniform(self) -> None:
        self.jobj.setUniform()

    def __eq__(self, other: Any) -> bool:
        if self is other:
            return True
        if not isinstance(other, ParameterizedMessageBase):
            return False
        # This forces lazy computation of normalization energy
        # to avoid confusing mismatches.
        if self.normalization_energy != other.normalization_energy:
            return False
        return self.jobj.objectEquals(other.jobj)

    def __str__(self) -> str:
        return str(self.jobj.toString())

@jpype_wrapper(JDiscreteMessage)
class DiscreteMessage(ParameterizedMessageBase, JWrapper[JDiscreteMessage]):
    @property
    def energies(self) -> tuple[float,...]:
        return tuple(self.jobj.getEnergies())

    @energies.setter
    def energies(self, values: Sequence[float]) -> None:
        self.jobj.setEnergies(*values)

    @property
    def size(self) -> int:
        return self.jobj.size()

    @property
    def stores_weights(self) -> bool:
        """True if underlying representation stores weights, otherwise energies"""
        return self.jobj.storesWeights()

    @property
    def weights(self) -> tuple[float, ...]:
        return tuple(self.jobj.getWeights())

    @weights.setter
    def weights(self, values: Sequence[float]) -> None:
        self.jobj.setWeights(*values)

    def add_energies_from(self, msg: DiscreteMessage) -> None:
        self.jobj.addEnergiesFrom(msg.jobj)

    def add_weights_from(self, msg: DiscreteMessage) -> None:
        self.jobj.addWeightsFrom(msg.jobj)

    def normalize(self) -> None:
        self.jobj.normalize()


@jpype_wrapper(JDiscreteEnergyMessage)
class DiscreteEnergyMessage(DiscreteMessage, JWrapper[JDiscreteEnergyMessage]):
    pass

@jpype_wrapper(JDiscreteWeightMessage)
class DiscreteWeightMessage(DiscreteMessage, JWrapper[JDiscreteWeightMessage]):
    pass

@jpype_wrapper(JNormalParameters)
class NormalParameters(ParameterizedMessageBase, JWrapper[JNormalParameters]):
    def __init__(
        self,
        mean: float = 0.0,
        precision: float = 1.0,
        *,
        mu: float|None = None,
        variance: float|None = None,
        sigma: float|None = None,
        std: float|None = None,
        jobj: JNormalParameters|None = None,
    ):
        if jobj is not None:
            jnorm = jobj
        else:
            jnorm = JNormalParameters(mean, precision)
            if precision == 1.0:
                if variance is not None:
                    jnorm.setVariance(variance)
                elif sigma is not None:
                    jnorm.setStandardDeviation(sigma)
                elif std is not None:
                    jnorm.setStandardDeviation(std)
            if mean == 0.0 and mu is not None:
                jnorm.setMean(mu)

        JWrapper.__init__(self, jobj=jnorm)

    @property
    def mean(self) -> float:
        return self.jobj.getMean()

    @mean.setter
    def mean(self, value: float) -> None:
        self.jobj.setMean(value)

    @property
    def precision(self) -> float:
        return self.jobj.getPrecision()

    @precision.setter
    def precision(self, value: float) -> None:
        self.jobj.setPrecision(value)

    @property
    def standard_deviation(self) -> float:
        return self.jobj.getStandardDeviation()

    @standard_deviation.setter
    def standard_deviation(self, value: float) -> None:
        self.jobj.setStandardDeviation(value)

    @property
    def variance(self) -> float:
        return self.jobj.getVariance()

    @variance.setter
    def variance(self, value: float) -> None:
        self.jobj.setVariance(value)

    def __repr__(self) -> str:
        return f"NormalParameters({self.mean}, {self.precision})"
