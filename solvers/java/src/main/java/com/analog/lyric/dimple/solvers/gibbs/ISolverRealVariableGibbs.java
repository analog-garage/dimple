package com.analog.lyric.dimple.solvers.gibbs;

import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IParameterizedMessage;

public interface ISolverRealVariableGibbs
{
	// Internal methods
	public void getAggregateMessages(IParameterizedMessage outputMessage, int portIndex, ISampler conjugateSampler);
}
