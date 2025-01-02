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
Interfaces for Dimple factors and factor functions
"""
from __future__ import annotations

import enum
import math
from typing import (
    Any,
    Literal,
    Protocol,
    Self,
    Sequence,
    TypeAlias,
    TYPE_CHECKING,
    cast,
    runtime_checkable,
)

from jpype import JArray, JImplements, JOverride, JString

from com.analog.lyric.dimple.model.values import Value as JValue
from com.analog.lyric.dimple.model.factors import Factor as JFactor
from com.analog.lyric.dimple.factorfunctions.core import IFactorFunction as JIFactorFunction
from com.analog.lyric.dimple.factorfunctions.core import FactorFunction as JFactorFunction
from com.analog.lyric.dimple.factorfunctions.core import DelegatingFactorFunction as JDelegatingFactorFunction

from .base import Node, JWrapper, ValueType, jpype_wrapper

if TYPE_CHECKING:
    from .variable import Variable

__all__ = [
    "FactorFuncSignature",
    "FactorFunction",
    "FactorFunctionBase",
    "FFuncType",
    "JFactorFunction",
]

@runtime_checkable
class FactorFuncSignature(Protocol):
    """
    Type signature for python factor function that can be used with
    FactorFunction and Factor.
    """
    def __call__(self, *values: ValueType) -> float:
        ...

    @property
    def __name__(self) -> str:
        ...


class FFuncType(enum.StrEnum):
    """
    Factor function type

    * energy: computes "energy" of inputs (i.e. -log of weights/probabilities)
    * weight: computes "weight/probality" of inputs
    """
    energy = "energy"
    weight = "weight"

FFuncArg: TypeAlias = FFuncType|Literal["energy", "weight"]
"""
FFuncType and its literal string values.
"""

@JImplements(JIFactorFunction)
class _JIFactorFuncImpl:
    """
    Implements Java IFactorFunction interface for use with
    DelegatingFactorFunction.
    """
    _func: FactorFuncSignature
    _func_type: FFuncType
    _jname: JString
    _is_deterministic_directed: bool
    _is_directed: bool
    _is_parameteric: bool

    def __init__(
        self,
        name: str,
        func: FactorFuncSignature,
        func_type: FFuncType = FFuncType.energy,
        *,
        is_directed: bool = False,
        is_deterministic_directed: bool = False,
        is_parametric: bool = False,
    ) -> None:
        self._jname = JString(name)
        self._func = func
        self._func_type = func_type
        self._is_deterministic_directed = is_deterministic_directed
        self._is_directed = is_directed
        self._is_parameteric = is_parametric

    @JOverride
    def evalDeterministic(self, values:list[JValue]|JArray) -> None:
        pass # TODO

    @JOverride
    def eval(
        self,
        jvalues: list[JValue]|JArray
    ) -> float:
        result = self._func(*[v.getObject() for v in jvalues])
        if self._func_type == FFuncType.energy:
            result = math.exp(-result)
        return result

    @JOverride
    def evalEnergy(
        self,
        jvalues: list[JValue]|JArray
    ) -> float:
        result = self._func(*[v.getObject() for v in jvalues])
        if self._func_type == FFuncType.weight:
            result = -math.log(result)
        return result

    @JOverride
    def getName(self) -> JString:
        return self._jname

    @JOverride
    def isDeterministicDirected(self) -> bool:
        return self._is_deterministic_directed

    @JOverride
    def isDirected(self) -> bool:
        return self._is_directed

    @JOverride
    def isParametric(self) -> bool:
        return self._is_parameteric

@jpype_wrapper(JFactorFunction)
class FactorFunctionBase(JWrapper[JFactorFunction]):
    @property
    def name(self) -> str:
        return str(self.jobj.getName())

    def clone(self) -> Self:
        return self.__class__.for_jobj(self.jobj.clone())

    def eval_weight(self, *values: ValueType) -> float:
        return self.jobj.eval(*values)

    def eval_energy(self, *values: ValueType) -> float:
        return self.jobj.evalEnergy(*values)

    def __eq__(self, other: Any) -> bool:
        return isinstance(other, JWrapper) and other.jobj.objectEquals(other.jobj)


@jpype_wrapper(JDelegatingFactorFunction)
class FactorFunction(FactorFunctionBase, JWrapper[JDelegatingFactorFunction]):
    _impl: _JIFactorFuncImpl

    def __init__(
        self,
        func: FactorFuncSignature,
        *,
        func_type: FFuncArg = FFuncType.energy,
        name: str = "",
    ) -> None:
        name = name or str(func.__name__)
        if name == '<lambda>':
            name = f"lambda_{id(func):x}"
        self._impl = _JIFactorFuncImpl(name, func, func_type=FFuncType(func_type))
        super().__init__(jobj=JDelegatingFactorFunction(self._impl))  # type: ignore

    # TODO - intern instances and provide appropriate factory

    @property
    def func(self) -> FactorFuncSignature:
        return self._impl._func

    @property
    def func_type(self) -> FFuncType:
        return self._impl._func_type

    @property
    def name(self) -> str:
        return self.jfunction.getName()

    @property
    def jfunction(self) -> JDelegatingFactorFunction:
        return cast(JDelegatingFactorFunction, self.jobj)

    def eval_weight(self, *values: ValueType) -> float:
        return self._impl.eval(*values)

    def eval_energy(self, *values: ValueType) -> float:
        return self._impl.evalEnergy(*values)

@jpype_wrapper(JFactor)
class Factor(Node, JWrapper[JFactor]):
    @property
    def can_be_directed(self) -> bool:
        return self.jfactor.canBeDirected()

    @property
    def is_directed(self) -> bool:
        return self.jfactor.isDirected()

    @property
    def is_discrete(self) -> bool:
        return self.jfactor.isDiscrete()

    @property
    def jfactor(self) -> JFactor:
        return cast(JFactor, self.jobj)

    @property
    def siblings(self) -> Sequence[Variable]:
        return super().siblings # type: ignore

