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

import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.factorfunctions.MultivariateNormal;
import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.solvers.core.SMultivariateNormalEdge;
import com.analog.lyric.dimple.solvers.core.SRealJointVariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;

/**
 * Solver variable for RealJoint variables under Sum-Product solver.
 * 
 * @since 0.07
 */
public class SumProductRealJoint extends SRealJointVariableBase
{

	private int _numVars;

	public SumProductRealJoint(RealJoint var, SumProductSolverGraph parent)
	{
		super(var, parent);
		
		_numVars = _model.getDomain().getNumVars();
	}
	
	@Override
	public Object getBelief()
	{
		MultivariateNormalParameters m = new MultivariateNormalParameters(getDomain().getDimensions());
		doUpdate(m,-1);
		return m;
	}
	
	@Override
	public Object getValue()
	{
		MultivariateNormalParameters m = (MultivariateNormalParameters)getBelief();
		return m.getMean();
	}
	
	@Deprecated
	@Override
	public double getScore()
	{
		IDatum prior = _model.getPrior();
		
		if (prior == null)
			return 0;
		else if (prior instanceof Value)
		{
			double[] value = ((Value) prior).getDoubleArray();
			return _guessValue.length == 0 || Arrays.equals(_guessValue,  value) ? 0 : Double.POSITIVE_INFINITY;
		}
		else
			return ((IUnaryFactorFunction)prior).evalEnergy(getGuess());
	}
	

	@Override
	protected void doUpdateEdge(int outPortNum)
	{
		doUpdate(getSiblingEdgeState(outPortNum).varToFactorMsg, outPortNum);
	}

	private void doUpdate(MultivariateNormalParameters outMsg, int outPortNum)
	{
		IDatum prior = _model.getPrior();
		
		if (prior instanceof Value)
		{
	    	// If fixed value, just return the input, which has been set to a zero-variance message
			outMsg.setDeterministic(((Value)prior));
			return;
		}
		
		final MultivariateNormalParameters input = priorToNormal(prior);
		
		if (input != null)
		{
			outMsg.set(input);
		}
		else
		{
			outMsg.setNull();
		}

		for (int i = 0, n = getSiblingCount(); i < n; i++ )
		{
			if (i != outPortNum)
			{
				final MultivariateNormalParameters inMsg = getSiblingEdgeState(i).factorToVarMsg;
				outMsg.addFrom(inMsg);
			}
		}
	}

	public MultivariateNormalParameters createDefaultMessage()
	{
		return new MultivariateNormalParameters(_numVars);
	}

	@Deprecated
	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		getSiblingEdgeState(portIndex).factorToVarMsg.set((MultivariateNormalParameters)obj);
	}


	public MultivariateNormalParameters createFixedValueMessage(double[] fixedValue)
	{
		double[] variance = new double[_numVars];
		MultivariateNormalParameters message = new MultivariateNormalParameters(fixedValue, variance);
		return message;
	}

	/*-----------------------
	 * SVariableBase methods
	 */
	
	@Override
	protected MultivariateNormalParameters cloneMessage(int edge)
	{
		return getSiblingEdgeState(edge).varToFactorMsg.clone();
	}
	
	@Override
	protected boolean supportsMessageEvents()
	{
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public SMultivariateNormalEdge getSiblingEdgeState(int siblingIndex)
	{
		return (SMultivariateNormalEdge)getSiblingEdgeState_(siblingIndex);
	}
	
	/*-----------------
	 * Private methods
	 */
	
    private @Nullable MultivariateNormalParameters priorToNormal(@Nullable IDatum prior)
    {
    	if (prior instanceof MultivariateNormalParameters)
    	{
    		return (MultivariateNormalParameters)prior;
    	}
    	else if (prior instanceof MultivariateNormal)
    	{
    		return ((MultivariateNormal)prior).getParameters();
    	}
    	else if (prior != null)
    	{
    		DimpleEnvironment.logError(
    			"Ignoring prior on %s: sum-product reals only supports MultivariateNormalParameters for priors but got %s",
    			_model, prior);
    	}
    	
    	return null;
    }
    
}
