
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.model.core
import com.analog.lyric.dimple.model.domains
import com.analog.lyric.dimple.model.variables
import jpype
import typing



class MultiplexerCPD(com.analog.lyric.dimple.model.core.FactorGraph):
    @typing.overload
    def __init__(self, discreteDomain: com.analog.lyric.dimple.model.domains.DiscreteDomain, int: int): ...
    @typing.overload
    def __init__(self, discreteDomainArray: typing.Union[typing.List[com.analog.lyric.dimple.model.domains.DiscreteDomain], jpype.JArray]): ...
    @typing.overload
    def __init__(self, discreteDomainArray: typing.Union[typing.List[com.analog.lyric.dimple.model.domains.DiscreteDomain], jpype.JArray], boolean: bool, boolean2: bool): ...
    @typing.overload
    def __init__(self, objectArray: typing.Union[typing.List[typing.Any], jpype.JArray], int: int): ...
    @typing.overload
    def __init__(self, objectArray: typing.Union[typing.List[typing.Any], jpype.JArray], int: int, boolean: bool, boolean2: bool): ...
    @typing.overload
    def __init__(self, objectArray: typing.Union[typing.List[typing.MutableSequence[typing.Any]], jpype.JArray]): ...
    @typing.overload
    def __init__(self, objectArray: typing.Union[typing.List[typing.MutableSequence[typing.Any]], jpype.JArray], boolean: bool, boolean2: bool): ...
    @typing.overload
    @staticmethod
    def buildDomains(discreteDomain: com.analog.lyric.dimple.model.domains.DiscreteDomain, int: int) -> typing.MutableSequence[com.analog.lyric.dimple.model.domains.DiscreteDomain]: ...
    @typing.overload
    @staticmethod
    def buildDomains(objectArray: typing.Union[typing.List[typing.Any], jpype.JArray], int: int) -> typing.MutableSequence[com.analog.lyric.dimple.model.domains.DiscreteDomain]: ...
    def getA(self) -> com.analog.lyric.dimple.model.variables.Discrete: ...
    def getY(self) -> com.analog.lyric.dimple.model.variables.Discrete: ...
    def getZA(self) -> com.analog.lyric.dimple.model.variables.Discrete: ...
    def getZs(self) -> typing.MutableSequence[com.analog.lyric.dimple.model.variables.Discrete]: ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.nestedgraphs")``.

    MultiplexerCPD: typing.Type[MultiplexerCPD]
