
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.model.core
import typing



class RepeatedSolve:
    def __init__(self): ...
    @staticmethod
    def solveRepeated(factorGraph: com.analog.lyric.dimple.model.core.FactorGraph, int: int) -> None: ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.matlabproxy.testutil")``.

    RepeatedSolve: typing.Type[RepeatedSolve]
