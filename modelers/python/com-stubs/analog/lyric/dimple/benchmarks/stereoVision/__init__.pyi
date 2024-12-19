
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.benchmarks.stereoVision.datasets
import typing


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.benchmarks.stereoVision")``.

    datasets: com.analog.lyric.dimple.benchmarks.stereoVision.datasets.__module_protocol__
