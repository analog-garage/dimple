
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.benchmarks.imageDenoising
import com.analog.lyric.dimple.benchmarks.stereoVision
import typing


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.benchmarks")``.

    imageDenoising: com.analog.lyric.dimple.benchmarks.imageDenoising.__module_protocol__
    stereoVision: com.analog.lyric.dimple.benchmarks.stereoVision.__module_protocol__
