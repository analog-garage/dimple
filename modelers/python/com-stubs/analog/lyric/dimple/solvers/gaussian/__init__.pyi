
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.solvers.sumproduct
import typing



class Solver(com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver):
    def __init__(self): ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.solvers.gaussian")``.

    Solver: typing.Type[Solver]
