
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.solvers.core
import com.analog.lyric.dimple.solvers.gaussian
import com.analog.lyric.dimple.solvers.gibbs
import com.analog.lyric.dimple.solvers.interfaces
import com.analog.lyric.dimple.solvers.junctiontree
import com.analog.lyric.dimple.solvers.junctiontreemap
import com.analog.lyric.dimple.solvers.lp
import com.analog.lyric.dimple.solvers.minsum
import com.analog.lyric.dimple.solvers.optimizedupdate
import com.analog.lyric.dimple.solvers.particleBP
import com.analog.lyric.dimple.solvers.sumproduct
import typing


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.solvers")``.

    core: com.analog.lyric.dimple.solvers.core.__module_protocol__
    gaussian: com.analog.lyric.dimple.solvers.gaussian.__module_protocol__
    gibbs: com.analog.lyric.dimple.solvers.gibbs.__module_protocol__
    interfaces: com.analog.lyric.dimple.solvers.interfaces.__module_protocol__
    junctiontree: com.analog.lyric.dimple.solvers.junctiontree.__module_protocol__
    junctiontreemap: com.analog.lyric.dimple.solvers.junctiontreemap.__module_protocol__
    lp: com.analog.lyric.dimple.solvers.lp.__module_protocol__
    minsum: com.analog.lyric.dimple.solvers.minsum.__module_protocol__
    optimizedupdate: com.analog.lyric.dimple.solvers.optimizedupdate.__module_protocol__
    particleBP: com.analog.lyric.dimple.solvers.particleBP.__module_protocol__
    sumproduct: com.analog.lyric.dimple.solvers.sumproduct.__module_protocol__
