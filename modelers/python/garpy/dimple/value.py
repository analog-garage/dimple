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

import enum
from typing import Any, Self, Protocol

from .java import start # noqa

from com.analog.lyric.dimple.model.values import (
    Value as JValue
)

from .base import JWrapper, jpype_wrapper
from .domain import Domain

__all__ = [
    "DataRepresentationType",
    "Datum",
    "Value"
]

class DataRepresentationType(enum.IntEnum):
    FUNCTION = 0
    MESSAGE = 1
    VALUE = 2

class Datum(Protocol):
    @property
    def representation_type(self) -> DataRepresentationType:
        ...

    def clone(self) -> Self:
        ...

    def eval_energy(self, value: Value) -> float:
        ...


@jpype_wrapper(JValue)
class Value(Datum, JWrapper[JValue]):

    @classmethod
    def constant(cls, domain_or_value: Domain|Any, value:Any=None, *, index:int=-1) -> Self:
        if isinstance(domain_or_value, Domain):
            domain = domain_or_value
            jdomain = domain.jdomain
            if value is not None:
                jvalue = JValue.constant(jdomain, value)
            elif domain.is_discrete and index >= 0:
                jvalue = JValue.constantWithIndex(jdomain, index)
            else:
                jvalue = JValue.constant(jdomain, None)
        elif isinstance(domain_or_value, float):
            jvalue = JValue.constantReal(domain_or_value)
        else:
            jvalue = JValue.constant(domain_or_value)
        return cls(jobj=jvalue)

    @classmethod
    def create(cls, domain_or_value: Domain|Any, value:Any=None, *, index:int=-1) -> Self:
        if isinstance(domain_or_value, Domain):
            domain = domain_or_value
            jdomain = domain.jdomain
            if value is not None:
                jvalue = JValue.create(jdomain, value)
            elif domain.is_discrete and index >= 0:
                jvalue = JValue.createWithIndex(jdomain, index)
            else:
                jvalue = JValue.create(jdomain)
        elif isinstance(domain_or_value, float):
            jvalue = JValue.createReal(domain_or_value)
        else:
            jvalue = JValue.create(domain_or_value)
        return cls(jobj=jvalue)

    @property
    def bool_value(self) -> bool:
        return self.jobj.getBoolean()

    @bool_value.setter
    def bool_value(self, value: bool) -> None:
        self.jobj.setBoolean(value)

    @property
    def domain(self) -> Domain:
        return Domain.for_jobj(self.jobj.getDomain())

    @property
    def float_value(self) -> float:
        return self.jobj.getDouble()

    @float_value.setter
    def float_value(self, value: float) -> None:
        self.jobj.setDouble(value)

    @property
    def index_value(self) -> int:
        return self.jobj.getIndex()

    @index_value.setter
    def index_value(self, index:int) -> None:
        self.jobj.setIndex(index)

    @property
    def int_value(self) -> int:
        return self.jobj.getInt()

    @int_value.setter
    def int_value(self, value: int) -> None:
        self.jobj.setInt(value)

    @property
    def is_mutable(self) -> bool:
        return self.jobj.isMutable()

    @property
    def representation_type(self) -> DataRepresentationType:
        return DataRepresentationType.VALUE

    @property
    def value(self) -> Any:
        return self.jobj.getObject()

    @value.setter
    def value(self, value: Any) -> None:
        self.jobj.setObject(value)

    def clone(self) -> Self:
        return self.for_jobj(self.jobj.clone())

    def eval_energy(self, value: Value) -> float:
        return self.jobj.evalEnergy(value.jobj)

    def __eq__(self, other: Any) -> bool:
        return isinstance(other, Value) and self.jobj.valueEquals(other.jobj)

    def __str__(self) -> str:
        return str(self.jobj.toString())


