
package com.analog.lyric.dimple.solvers.sumproduct;

import static java.util.Objects.*;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

/**
 * Solver variable for finite field factors under Sum-Product solver
 * 
 * @since 0.07
 */
public abstract class SumProductFiniteFieldFactor extends SFactorBase
{

	protected double [][] _inputMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
	protected double [][] _outputMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
	
	public SumProductFiniteFieldFactor(Factor factor)
	{
		super(factor);
	}

	@Override
	public void createMessages()
	{
		final Factor factor = _factor;
		final int nVars = factor.getSiblingCount();
		
	    _inputMsgs = new double[nVars][];
	    _outputMsgs = new double[nVars][];
	    
	    for (int index = 0; index < nVars; index++)
	    {
	    	ISolverVariable svar =  requireNonNull(factor.getSibling(index).getSolver());
	    	Object [] messages = requireNonNull(svar.createMessages(this));
	    	_outputMsgs[index] = (double[])messages[0];
	    	_inputMsgs[index] = (double[])messages[1];
	    }
	    
	}


	@SuppressWarnings("null")
	@Override
	public void resetEdgeMessages(int i)
	{
		SumProductDiscrete sv = (SumProductDiscrete)_factor.getSibling(i).getSolver();
		_inputMsgs[i] = sv.resetInputMessage(_inputMsgs[i]);
	}

	@Override
	public Object getInputMsg(int portIndex)
	{
		return _inputMsgs[portIndex];
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outputMsgs[portIndex];
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum)
	{

		SumProductFiniteFieldFactor sother = (SumProductFiniteFieldFactor)other;
	    _inputMsgs[thisPortNum] = sother._inputMsgs[otherPortNum];
	    _outputMsgs[thisPortNum] = sother._outputMsgs[otherPortNum];
	}

	/*---------------
	 * SNode methods
	 */
	
	@Override
	public DiscreteMessage cloneMessage(int edge)
	{
		return new DiscreteWeightMessage(_outputMsgs[edge]);
	}
	
	@Override
	public boolean supportsMessageEvents()
	{
		return true;
	}
}
