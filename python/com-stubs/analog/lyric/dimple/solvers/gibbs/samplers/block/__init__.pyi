
import sys
if sys.version_info >= (3, 8):
    from typing import Protocol
else:
    from typing_extensions import Protocol

import com.analog.lyric.dimple.schedulers.scheduleEntry
import com.analog.lyric.dimple.solvers.core.proposalKernels
import com.analog.lyric.dimple.solvers.gibbs
import com.analog.lyric.dimple.solvers.gibbs.samplers
import com.analog.lyric.dimple.solvers.interfaces
import java.lang
import java.util
import typing



class IBlockInitializer:
    def initialize(self) -> None: ...

class IBlockSampler(com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler, com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater): ...

class IBlockMCMCSampler(IBlockSampler): ...

class BlockMHSampler(IBlockMCMCSampler):
    @typing.overload
    def __init__(self): ...
    @typing.overload
    def __init__(self, iBlockProposalKernel: com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel): ...
    def copy(self, map: typing.Union[java.util.Map[typing.Any, typing.Any], typing.Mapping[typing.Any, typing.Any]]) -> 'BlockMHSampler': ...
    def getProposalKernel(self) -> com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel: ...
    def getProposalKernelName(self) -> java.lang.String: ...
    @typing.overload
    def setProposalKernel(self, iBlockProposalKernel: com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel) -> None: ...
    @typing.overload
    def setProposalKernel(self, string: typing.Union[java.lang.String, str]) -> None: ...
    def update(self, iSolverVariableBlock: com.analog.lyric.dimple.solvers.interfaces.ISolverVariableBlock) -> bool: ...

class BlockMHInitializer(BlockMHSampler, IBlockInitializer):
    def __init__(self, gibbsVariableBlock: com.analog.lyric.dimple.solvers.gibbs.GibbsVariableBlock, iBlockProposalKernel: com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel): ...
    def initialize(self) -> None: ...


class __module_protocol__(Protocol):
    # A module protocol which reflects the result of ``jp.JPackage("com.analog.lyric.dimple.solvers.gibbs.samplers.block")``.

    BlockMHInitializer: typing.Type[BlockMHInitializer]
    BlockMHSampler: typing.Type[BlockMHSampler]
    IBlockInitializer: typing.Type[IBlockInitializer]
    IBlockMCMCSampler: typing.Type[IBlockMCMCSampler]
    IBlockSampler: typing.Type[IBlockSampler]
