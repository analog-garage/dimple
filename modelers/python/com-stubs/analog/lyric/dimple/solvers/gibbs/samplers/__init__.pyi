
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.solvers.gibbs.samplers.block
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic
import typing



class ISampler: ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.solvers.gibbs.samplers")``.

    ISampler: typing.Type[ISampler]
    block: com.analog.lyric.dimple.solvers.gibbs.samplers.block.__module_protocol__
    conjugate: com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.__module_protocol__
    generic: com.analog.lyric.dimple.solvers.gibbs.samplers.generic.__module_protocol__
