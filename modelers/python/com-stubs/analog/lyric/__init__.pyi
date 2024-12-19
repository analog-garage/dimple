
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.benchmarking
import com.analog.lyric.collect
import com.analog.lyric.dimple
import com.analog.lyric.math
import com.analog.lyric.options
import com.analog.lyric.util
import typing


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric")``.

    benchmarking: com.analog.lyric.benchmarking.__module_protocol__
    collect: com.analog.lyric.collect.__module_protocol__
    dimple: com.analog.lyric.dimple.__module_protocol__
    math: com.analog.lyric.math.__module_protocol__
    options: com.analog.lyric.options.__module_protocol__
    util: com.analog.lyric.util.__module_protocol__
