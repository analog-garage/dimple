
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.solvers.core.multithreading
import typing



class SingleQueueMutlithreadingAlgorithm(com.analog.lyric.dimple.solvers.core.multithreading.MultithreadingAlgorithm):
    def __init__(self, multiThreadingManager: com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager): ...
    def iterate(self, int: int) -> None: ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.solvers.core.multithreading.singlequeuealgorithm")``.

    SingleQueueMutlithreadingAlgorithm: typing.Type[SingleQueueMutlithreadingAlgorithm]
