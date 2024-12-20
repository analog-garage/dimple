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
Base definitions for Dimple
"""

from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Callable,
    ClassVar,
    Iterator,
    Sequence,
    Type,
    TypeVar,
    TYPE_CHECKING,
    cast
)
from uuid import UUID
from weakref import WeakValueDictionary

from jpype import JObject

import garpy.dimple.java # noqa

from com.analog.lyric.dimple.environment import DimpleEnvironment as JDimpleEnvironment
from com.analog.lyric.dimple.model.core import Node as JNode
from com.analog.lyric.dimple.model.core import Ids as JIds
from com.analog.lyric.dimple.options import DimpleOptionHolder as JDimpleOptionHolder
from com.analog.lyric.dimple.options import SolverOptions as JSolverOptions
from com.analog.lyric.dimple.options import DimpleOptionRegistry as JDimpleOptionRegistry
from com.analog.lyric.options import OptionKey as JOptionKey

if TYPE_CHECKING:
    from .factorgraph import FactorGraph

__all__ = [
    "DimpleEnvironment",
    "DimpleOptionHolder",
    "DimpleOptionRegistry",
    "Node",
    "OptionKey",
    "SolverOptions",
    "ValueType",
    "jpype_wrapper",
]


type ValueType = bool|str|int|float

J = TypeVar('J', bound=JObject)
W = TypeVar('W', bound='JWrapper')

class JWrapper[J]:
    """
    Base class for all Dimple classes implemented on top of the
    base Java implementation of dimple.

    Each wraps an instance of a JPype wrapper object that in turn
    wraps the actual Java object. We avoid exposing the JPype classes
    to provide a more pythonic interface and to control what parts of
    the interface we want to expose.

    You can use the [jobj][.] property to access the underlying JPype
    object and use its methods, if necessary.

    Except where documented, we try to limit the state held by these wrapper
    classes to have no direct state other than the pointer to the underling
    JPype instance. In general, there can be more than one wrapper instance
    for the same JPype object, so you should use `==` rather than `is` to
    test for the same object (it also appears that JPype may also produce
    more than one wrapper instance for the same underlying Java object).
    """
    __slots__ = ["_jobj", "__weakref__"]

    _jpype_to_wrapper: ClassVar[WeakValueDictionary[int, JWrapper]] = WeakValueDictionary()
    """
    Weak value table mapping JObject instances to a known wrapper instance.
    """

    _jpype_wrapper_types: ClassVar[dict[Type[JWrapper], Type[JObject]]] = {}
    """
    Mapping from JObject type to corresponding JWrapper subtype. These entries
    are created by the @jpype_wrapper decorator.
    """

    _jobj: J

    def __init__(self, *, jobj: J) -> None:
        """
        Construct a wrapper instance.

        This also registers the wrapper instance in an interning table
        so that it can be returned by the [for_jobj][.] constructor.

        This is mostly for use by subclass implementations. You should
        prefer the [for_jobj][.] constructor, since it can return an
        interned instance.

        Arguments:
            jobj: the underlying JPype object to wrap
        """
        self._jobj = jobj
        self._jpype_to_wrapper.setdefault(id(jobj), self)

    @classmethod
    def for_jobj(cls:Type[W], jobj: J) -> W:
        """
        Returns an appropriate wrapper instance for the JPype
        object.

        This will return an existing wrapper instance, if possible,
        otherwise a new one will be constructed.
        """
        if wrapper := cls._jpype_to_wrapper.get(id(jobj)):
            assert isinstance(wrapper, cls), f"{wrapper} is not a {cls}"
            return wrapper
        if wrapper_cls := cls.wrapper_type_for(type(jobj)):
            if wrapper_cls != cls:
                # Recurse to subclass implementation. This allows subclass
                # to override constructor.
                return cast(W, wrapper_cls.for_jobj(jobj))
        return cls(jobj=jobj)

    @classmethod
    def wrapper_type_for(cls, jtype_cls: Type[JObject]) -> Type[JWrapper]|None:
        """
        Returns corresponding wrapper class for given JPype class,
        as registered by the @jpype_wrapper class decorator.
        """
        return cls._jpype_wrapper_types.get(jtype_cls)

    @property
    def jobj(self) -> J:
        """The underlying JPype object"""
        return self._jobj


WT = TypeVar("WT", bound=Type[JWrapper])

def jpype_wrapper(jpype_cls: type) -> Callable[[WT],WT]:
    """
    Decorator used before wrapper classes.

    Arguments:
        jpype_cls: Java wrapper class generated by JPype that is
            wrapped by the decorated class
    """
    def _register(wrapper_cls: WT) -> WT:
        JWrapper._jpype_wrapper_types[jpype_cls] = wrapper_cls
        return wrapper_cls
    return _register


@jpype_wrapper(JOptionKey)
class OptionKey(JWrapper[JOptionKey]):
    @property
    def jkey(self) -> JOptionKey:
        return self._jobj

    @property
    def name(self) -> str:
        """Returns the qualified name of the option (e.g. BPOptions.damping)"""
        return str(self.jkey.qualifiedName())

    def get(self, holder: DimpleOptionHolder, default: bool = True) -> Any:
        """Gets option value from holder

        Arguments:
            holder: the option holder on which to lookup the option value
            default: if True, a default value may be returned, otherwise
                None will be returned if not set.
        """
        return self.jkey.get(holder.jobj)

    def set(self, holder: DimpleOptionHolder, value: Any) -> None:
        """
        Sets the option value on the option holder object.
        """
        self.jkey.convertAndSet(holder.jobj, value)

    def unset(self, holder: DimpleOptionHolder) -> None:
        """Unsets this option on the option holder object"""
        self.jkey.unset(holder.jobj)

    def __hash__(self) -> int:
        return self.jkey.hashCode()

    def __eq__(self, other: Any) -> bool:
        return isinstance(other, OptionKey) and other.jkey == self.jkey

    def __repr__(self) -> str:
        return f"<OptionKey '{self}'>"

    def __str__(self) -> str:
        return str(self._jobj.toString())


@jpype_wrapper(JDimpleOptionRegistry)
class DimpleOptionRegistry(JWrapper[JDimpleOptionRegistry], Mapping[str, OptionKey]):
    @property
    def jregistry(self) -> JDimpleOptionRegistry:
        return self._jobj

    def __len__(self) -> int:
        return self.jregistry.size()

    def __getitem__(self, key: str) -> OptionKey:
        return OptionKey.for_jobj(self.jregistry.get(key))

    def __iter__(self) -> Iterator[str]:
        for key in self.jregistry.iterator():
            yield key.qualifiedName()


@jpype_wrapper(JDimpleEnvironment)
class DimpleEnvironment(JWrapper[JDimpleEnvironment]):
    @classmethod
    def active(cls) -> DimpleEnvironment:
        return cls.for_jobj(JDimpleEnvironment.active())

    @property
    def jenvironment(self) -> JDimpleEnvironment:
        return self._jobj

    @property
    def option_keys(self) -> DimpleOptionRegistry:
        return DimpleOptionRegistry.for_jobj(self._jobj.optionRegistry())

    @property
    def version(self) -> str:
        """Return version string for running version of dimple"""
        return self.jenvironment.getVersion()


@jpype_wrapper(JDimpleOptionHolder)
class DimpleOptionHolder(JWrapper[JDimpleOptionHolder]):

    @property
    def environment(self) -> DimpleEnvironment:
        return DimpleEnvironment.for_jobj(self.jobj.getEnvironment())

    @property
    def option_parent(self) -> DimpleOptionHolder|None:
        if jparent := self.jobj.getOptionParent():
            return DimpleOptionHolder.for_jobj(cast(JDimpleOptionHolder, jparent))
        return None

    @property
    def supports_local_options(self) -> bool:
        return self.jobj.supportsLocalOptions()

    def clear_local_options(self) -> None:
        self._jobj.clearLocalOptions()

    def get_local_option(self, key: str|OptionKey) -> Any:
        return self.jobj.getLocalOption(self.option_key(key).jkey)

    def get_local_options(self) -> dict[OptionKey,Any]:
        options: dict[OptionKey,Any] = {}
        for jopt in self.jobj.getLocalOptions():
            options[OptionKey.for_jobj(jopt.key())] = jopt.externalValue()
        return options

    def get_option(self, key: str|OptionKey, default: bool = True) -> Any:
        option = self.option_key(key)
        if default:
            return self.jobj.getOptionOrDefault(option.jkey)
        return self.jobj.getOption(option.jkey)

    def get_option_and_source(self, key: str|OptionKey) -> tuple[Any, DimpleOptionHolder]|None:
        jsource = cast(list[JDimpleOptionHolder], JDimpleOptionHolder[1]) # type: ignore
        if value := self.jobj.getOptionAndSource(self.option_key(key).jkey, jsource):
            source = DimpleOptionHolder.for_jobj(jsource[0])
            return (value, source)
        return None

    def option_key(self, key: str|OptionKey) -> OptionKey:
        if isinstance(key, OptionKey):
            return key
        return self.environment.option_keys[key]

    def set_option(self, key: str|OptionKey, value: Any) -> None:
        self.option_key(key).set(self, value)

    def unset_option(self, key: str|OptionKey) -> None:
        self.jobj.unsetOption(self.option_key(key).jkey)


class SolverOptions:
    """
    General option keys applicable to many solver types.
    """
    enableMultithreading = OptionKey.for_jobj(JSolverOptions.enableMultithreading)

    maxAutomaticFactorTableSize = OptionKey.for_jobj(
        JSolverOptions.maxAutomaticFactorTableSize
    )


N = TypeVar('N', bound='Node')

@jpype_wrapper(JNode)
class Node(DimpleOptionHolder, JWrapper[JNode]):
    """
    Base node type for Dimple factor graphs.
    """

    @staticmethod
    def is_global_id(id: int) -> bool:
        return JIds.isGlobalId(id)

    @property
    def graph(self) -> FactorGraph:
        from .factorgraph import FactorGraph
        jgraph = self._jobj.getContainingGraph()
        return FactorGraph.for_jobj(jgraph) # type: ignore

    @property
    def jnode(self) -> JNode:
        return cast(JNode, self._jobj)

    @property
    def global_id(self) -> int:
        return self.jnode.getGlobalId()

    @property
    def local_id(self) -> int:
        return self.jnode.getLocalId()

    @property
    def label(self) -> str:
        return str(self.jnode.getLabel())

    @label.setter
    def label(self, label: str|None) -> None:
        self.jnode.setLabel(label)

    @property
    def name(self) -> str:
        return str(self.jnode.getName())

    @name.setter
    def name(self, name: str) -> None:
        self.jnode.setName(name)

    @property
    def siblings(self) -> Sequence[Node]:
        parent = self
        class _SiblingSequence(Sequence[Node]):
            def __len__(self) -> int:
                return parent.jnode.getSiblingCount()
            def __getitem__(self, index: int) -> Node:  # type: ignore
                return Node.for_jobj(parent.jnode.getSibling(index))
        return _SiblingSequence()

    @property
    def uuid(self) -> UUID:
        return UUID(str(self.jnode.getUUID()))

    def __eq__(self, other: Any) -> bool:
        return isinstance(other, Node) and other.jnode == self.jnode


