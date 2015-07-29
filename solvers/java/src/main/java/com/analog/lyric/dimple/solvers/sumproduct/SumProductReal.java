/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.sumproduct;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.solvers.core.PriorAndCondition;
import com.analog.lyric.dimple.solvers.core.SNormalEdge;
import com.analog.lyric.dimple.solvers.core.SRealVariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;

/**
 * Solver variable for Real variables under Sum-Product solver.
 * 
 * @since 0.07
 */
public class SumProductReal extends SRealVariableBase
{
	public SumProductReal(Real var, SumProductSolverGraph parent)
    {
		super(var, parent);
	}

    @Override
	protected void doUpdateEdge(int outPortNum)
    {
    	doUpdate(getSiblingEdgeState(outPortNum).varToFactorMsg, outPortNum);
    }
    
    @Override
	public NormalParameters getBelief()
    {
    	return doUpdate(new NormalParameters(), -1);
    }
    
    
	@Override
	public Object getValue()
	{
		NormalParameters belief = getBelief();
		return new Double(belief.getMean());
	}
	
	public NormalParameters createDefaultMessage()
	{
		return new NormalParameters();
	}

	@Deprecated
	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		getSiblingEdgeState(portIndex).factorToVarMsg.set((NormalParameters)obj);
	}
	
	public NormalParameters createFixedValueMessage(double fixedValue)
	{
		NormalParameters message = new NormalParameters();
		message.setMean(fixedValue);
		message.setPrecision(Double.POSITIVE_INFINITY);
		return message;
	}
	
	/*-----------------------
	 * SVariableBase methods
	 */
	
	@Override
	protected NormalParameters cloneMessage(int edge)
	{
		return getSiblingEdgeState(edge).varToFactorMsg.clone();
	}
	
	@Override
	protected boolean supportsMessageEvents()
	{
		return true;
	}

	@Override
	@SuppressWarnings("null")
	public SNormalEdge getSiblingEdgeState(int siblingIndex)
	{
		return (SNormalEdge)getSiblingEdgeState_(siblingIndex);
	}
	
	/*-----------------
	 * Private methods
	 */
	
    private NormalParameters doUpdate(NormalParameters outMsg, int excludeEdge)
    {
    	
    	PriorAndCondition known = getPriorAndCondition();
    	Value fixedValue = known.value();
    	
    	if (fixedValue != null)
    	{
    		outMsg.setDeterministic(fixedValue);
    	}
    	else
    	{
    		outMsg.setNull();

    		for (IDatum datum : known)
    		{
    			NormalParameters input = priorToNormal(datum);
    			if (input != null)
    			{
    				outMsg.addFrom(input);
    			}
    		}

    		for (int i = getSiblingCount(); --i>=0;)
    		{
    			if (i != excludeEdge)
    			{
    				outMsg.addFrom(getSiblingEdgeState(i).factorToVarMsg);
    			}
    		}
    	}

    	known.release();
    	
    	return outMsg;
    }
    

    private @Nullable NormalParameters priorToNormal(@Nullable IDatum prior)
    {
    	NormalParameters result = null;
    	
    	if (prior != null)
    	{
    		result = NormalParameters.fromDatum(prior);

    		if (result == null)
    		{
    			DimpleEnvironment.logError(
    				"Ignoring prior on %s: sum-product reals only supports NormalParameters for priors but got %s",
    				_model, prior);
    		}
    	}
    	
    	return result;
    }
    
}
