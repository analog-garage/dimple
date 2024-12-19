
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.util.misc
import com.analog.lyric.util.test
import typing


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.util")``.

    misc: com.analog.lyric.util.misc.__module_protocol__
    test: com.analog.lyric.util.test.__module_protocol__
