package com.analog.lyric.dimple.solvers.gibbs;

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;

public interface ISolverRealVariableGibbs
{
	// Internal methods
	public void getAggregateMessages(IParameterizedMessage outputMessage, int portIndex, ISampler conjugateSampler);
}
