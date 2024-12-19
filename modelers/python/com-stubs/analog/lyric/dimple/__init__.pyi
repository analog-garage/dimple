
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.benchmarks
import com.analog.lyric.dimple.data
import com.analog.lyric.dimple.environment
import com.analog.lyric.dimple.events
import com.analog.lyric.dimple.examples
import com.analog.lyric.dimple.exceptions
import com.analog.lyric.dimple.factorfunctions
import com.analog.lyric.dimple.matlabproxy
import com.analog.lyric.dimple.model
import com.analog.lyric.dimple.nestedgraphs
import com.analog.lyric.dimple.options
import com.analog.lyric.dimple.parameters
import com.analog.lyric.dimple.schedulers
import com.analog.lyric.dimple.solvers
import typing


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple")``.

    benchmarks: com.analog.lyric.dimple.benchmarks.__module_protocol__
    data: com.analog.lyric.dimple.data.__module_protocol__
    environment: com.analog.lyric.dimple.environment.__module_protocol__
    events: com.analog.lyric.dimple.events.__module_protocol__
    examples: com.analog.lyric.dimple.examples.__module_protocol__
    exceptions: com.analog.lyric.dimple.exceptions.__module_protocol__
    factorfunctions: com.analog.lyric.dimple.factorfunctions.__module_protocol__
    matlabproxy: com.analog.lyric.dimple.matlabproxy.__module_protocol__
    model: com.analog.lyric.dimple.model.__module_protocol__
    nestedgraphs: com.analog.lyric.dimple.nestedgraphs.__module_protocol__
    options: com.analog.lyric.dimple.options.__module_protocol__
    parameters: com.analog.lyric.dimple.parameters.__module_protocol__
    schedulers: com.analog.lyric.dimple.schedulers.__module_protocol__
    solvers: com.analog.lyric.dimple.solvers.__module_protocol__
