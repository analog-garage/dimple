
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.model.core
import com.analog.lyric.dimple.solvers.interfaces
import com.analog.lyric.dimple.solvers.junctiontree
import com.analog.lyric.dimple.solvers.minsum
import typing



class JunctionTreeMAPSolver(com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolverBase[com.analog.lyric.dimple.solvers.minsum.MinSumSolverGraph, 'JunctionTreeMAPSolverGraph']):
    def __init__(self): ...
    @typing.overload
    def createFactorGraph(self, factorGraph: com.analog.lyric.dimple.model.core.FactorGraph) -> com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph: ...
    @typing.overload
    def createFactorGraph(self, factorGraph: com.analog.lyric.dimple.model.core.FactorGraph, iSolverFactorGraph: com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph) -> 'JunctionTreeMAPSolverGraph': ...
    def equals(self, object: typing.Any) -> bool: ...
    def hashCode(self) -> int: ...

class JunctionTreeMAPSolverGraph(com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolverGraphBase[com.analog.lyric.dimple.solvers.minsum.MinSumSolverGraph]):
    def createSubgraph(self, factorGraph: com.analog.lyric.dimple.model.core.FactorGraph) -> com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph: ...
    def getParentGraph(self) -> 'JunctionTreeMAPSolverGraph': ...
    def getRootSolverGraph(self) -> 'JunctionTreeMAPSolverGraph': ...

class Solver(JunctionTreeMAPSolver):
    def __init__(self): ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.solvers.junctiontreemap")``.

    JunctionTreeMAPSolver: typing.Type[JunctionTreeMAPSolver]
    JunctionTreeMAPSolverGraph: typing.Type[JunctionTreeMAPSolverGraph]
    Solver: typing.Type[Solver]
