
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.schedulers.scheduleEntry
import com.analog.lyric.dimple.solvers.core.multithreading
import java.util
import java.util.concurrent
import typing



class PhaseMultithreadingAlgorithm(com.analog.lyric.dimple.solvers.core.multithreading.MultithreadingAlgorithm):
    def __init__(self, multiThreadingManager: com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager): ...
    def iterate(self, int: int) -> None: ...
    def updateScheduleEntries(self, executorService: java.util.concurrent.ExecutorService, arrayList: java.util.ArrayList[com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry], int: int, boolean: bool) -> None: ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.solvers.core.multithreading.phasealgorithm")``.

    PhaseMultithreadingAlgorithm: typing.Type[PhaseMultithreadingAlgorithm]
