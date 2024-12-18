
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.model.core
import com.analog.lyric.dimple.schedulers.dependencyGraph
import com.analog.lyric.dimple.schedulers.scheduleEntry
import java.util
import typing



class Edge:
    from_: com.analog.lyric.dimple.model.core.INode = ...
    to: com.analog.lyric.dimple.model.core.INode = ...
    def __init__(self, iNode: com.analog.lyric.dimple.model.core.INode, iNode2: com.analog.lyric.dimple.model.core.INode): ...
    def equals(self, object: typing.Any) -> bool: ...
    def hashCode(self) -> int: ...

class LastUpdateGraph:
    def __init__(self): ...
    def getEdges(self, iScheduleEntry: com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry) -> java.util.ArrayList[Edge]: ...
    def getLastNode(self, edge: Edge) -> com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraphNode: ...
    def setLastNode(self, edge: Edge, staticDependencyGraphNode: com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraphNode) -> None: ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.schedulers.dependencyGraph.helpers")``.

    Edge: typing.Type[Edge]
    LastUpdateGraph: typing.Type[LastUpdateGraph]
