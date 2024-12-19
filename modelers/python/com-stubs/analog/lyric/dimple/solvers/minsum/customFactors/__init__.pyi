
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.model.factors
import com.analog.lyric.dimple.solvers.minsum
import typing



class CustomXor(com.analog.lyric.dimple.solvers.minsum.STableFactor):
    def __init__(self, factor: com.analog.lyric.dimple.model.factors.Factor, minSumSolverGraph: com.analog.lyric.dimple.solvers.minsum.MinSumSolverGraph): ...
    def doUpdateEdge(self, int: int) -> None: ...
    def initialize(self) -> None: ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.solvers.minsum.customFactors")``.

    CustomXor: typing.Type[CustomXor]
