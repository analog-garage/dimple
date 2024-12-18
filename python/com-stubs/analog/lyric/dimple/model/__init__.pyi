
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.model.core
import com.analog.lyric.dimple.model.domains
import com.analog.lyric.dimple.model.factors
import com.analog.lyric.dimple.model.repeated
import com.analog.lyric.dimple.model.sugar
import com.analog.lyric.dimple.model.transform
import com.analog.lyric.dimple.model.values
import com.analog.lyric.dimple.model.variables
import typing


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.model")``.

    core: com.analog.lyric.dimple.model.core.__module_protocol__
    domains: com.analog.lyric.dimple.model.domains.__module_protocol__
    factors: com.analog.lyric.dimple.model.factors.__module_protocol__
    repeated: com.analog.lyric.dimple.model.repeated.__module_protocol__
    sugar: com.analog.lyric.dimple.model.sugar.__module_protocol__
    transform: com.analog.lyric.dimple.model.transform.__module_protocol__
    values: com.analog.lyric.dimple.model.values.__module_protocol__
    variables: com.analog.lyric.dimple.model.variables.__module_protocol__
