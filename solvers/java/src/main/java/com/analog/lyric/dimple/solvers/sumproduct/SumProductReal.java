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

import static java.util.Objects.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.variables.Real;
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
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
	private @Nullable NormalParameters _input;
    
	public SumProductReal(Real var, SumProductSolverGraph parent)
    {
		super(var, parent);
	}


	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue)
	{
		if (fixedValue != null)
			_input = createFixedValueMessage((Double)fixedValue);
		else if (input == null)
    		_input = null;
    	else
    	{
    		if (input instanceof Normal)	// Input is a Normal factor function with fixed parameters
    		{
    			Normal normalInput = (Normal)input;
    			if (!normalInput.hasConstantParameters())
    				throw new DimpleException("Normal factor function used as Input must have constant parameters");
    			final NormalParameters newInput = _input = new NormalParameters();
    			newInput.setMean(normalInput.getMean());
    			newInput.setPrecision(normalInput.getPrecision());
    		}
    		else	// Input is array in the form [mean, standard deviation]
    		{
    			double[] vals = (double[])input;
    			if (vals.length != 2)
    				throw new DimpleException("Expect a two-element vector of mean and standard deviation");

    			if (vals[1] < 0)
    				throw new DimpleException("Expect standard deviation to be >= 0");

    			final NormalParameters newInput = _input = new NormalParameters();
    			newInput.setMean(vals[0]);
    			newInput.setStandardDeviation(vals[1]);
    		}
    	}
    	
    }
	
    @Override
	protected void doUpdateEdge(int outPortNum)
    {
    	final NormalParameters input = _input;
    	final NormalParameters outMsg = getSiblingEdgeState(outPortNum).varToFactorMsg;
   	
    	// If fixed value, just return the input, which has been set to a zero-variance message
    	if (_model.hasFixedValue())
    	{
    		outMsg.set(Objects.requireNonNull(input));
        	return;
    	}
    	
    	final int nEdges = _model.getSiblingCount();
    	
    	double mu = 0;
    	double tau = 0;
    	double muTau = 0;
    	
    	if (input != null)
    	{
        	mu = input.getMean();
        	tau = input.getPrecision();
        	muTau = mu * tau;
    	}
    	
    	boolean anyTauIsInfinite = false;
    	double muOfInf = 0;
    	
    	if (tau == Double.POSITIVE_INFINITY)
    	{
    		anyTauIsInfinite = true;
    		muOfInf = mu;
    	}
    	
    	for (int i = 0; i < nEdges; i++)
    	{
    		if (i != outPortNum)
    		{
    			final NormalParameters msg = getSiblingEdgeState(i).factorToVarMsg;
    			double tmpTau = msg.getPrecision();
    			
    			if (tmpTau == Double.POSITIVE_INFINITY)
    			{
    				if (!anyTauIsInfinite)
    				{
	    				anyTauIsInfinite = true;
	    				muOfInf = msg.getMean();
    				}
    				else
    				{
    					if (muOfInf != msg.getMean())
    						throw new DimpleException("Real variable failed because two incoming messages were certain of conflicting things.");
    				}
    			}
    			else
    			{
	    			tau += tmpTau;
	    			muTau += tmpTau * msg.getMean();
    			}
    		}
    	}
    	
    	if (tau == Double.POSITIVE_INFINITY && !anyTauIsInfinite)
    		throw new DimpleException("This case isn't handled yet.");
    	
    	if (anyTauIsInfinite)
    	{
    		muTau = muOfInf;
    		tau = Double.POSITIVE_INFINITY;
    	}
    	else
    	{
	    	if (tau != 0)
	    		muTau /= tau;
	    	else
	    		muTau = 0;
    	}
    	
    	outMsg.setMean(muTau);
    	outMsg.setPrecision(tau);
    }
    

    
    @Override
	public Object getBelief()
    {
    	final NormalParameters input = _input;

    	// If fixed value, just return the input, which has been set to a zero-variance message
    	if (_model.hasFixedValue())
    		return requireNonNull(input).clone();
    	
    	double mu = 0;
    	double tau = 0;
    	double muTau = 0;
    	if (input != null)
    	{
        	mu = input.getMean();
        	tau = input.getPrecision();
        	muTau = mu * tau;
    	}
    	
    	boolean anyTauIsInfinite = false;
    	double muOfInf = 0;
    	
    	if (tau == Double.POSITIVE_INFINITY)
    	{
    		anyTauIsInfinite = true;
    		muOfInf = mu;
    	}
    	
    	for (int i = 0, n = getSiblingCount(); i < n; i++)
    	{
    		final NormalParameters msg = getSiblingEdgeState(i).factorToVarMsg;
			double tmpTau = msg.getPrecision();
			
			
			if (tmpTau == Double.POSITIVE_INFINITY)
			{
				if (!anyTauIsInfinite)
				{
    				anyTauIsInfinite = true;
    				muOfInf = msg.getMean();
				}
				else
				{
					if (muOfInf != msg.getMean())
						throw new DimpleException("Real variable failed because two incoming messages were certain of conflicting things.");
				}
			}
			else
			{
    			tau += tmpTau;
    			muTau += tmpTau * msg.getMean();
			}
    	}

    	if (tau == Double.POSITIVE_INFINITY && ! anyTauIsInfinite)
    		throw new DimpleException("This case isn't handled yet.");

    	if (anyTauIsInfinite)
    	{
    		mu = muOfInf;
    		tau = Double.POSITIVE_INFINITY;
    	}
    	else
    	{
	    	if (tau != 0)
	    		mu = muTau /= tau;
	    	else
	    		mu = 0;
    	}
    	
    	return new NormalParameters(mu, tau);
    }
    
    
	@Override
	public Object getValue()
	{
		NormalParameters belief = (NormalParameters)getBelief();
		return new Double(belief.getMean());
	}
	
	@Override
	public double getScore()
	{
		final NormalParameters input = _input;
		if (input == null)
			return 0;
		else
			return (new Normal(input)).evalEnergy(getGuess());
	}
	
	public NormalParameters createDefaultMessage()
	{
		NormalParameters message = new NormalParameters();
		return (NormalParameters)resetInputMessage(message);
	}

	@Override
	public Object resetInputMessage(Object message)
	{
		((NormalParameters)message).setUniform();
		return message;
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
		return (SNormalEdge)super.getSiblingEdgeState(siblingIndex);
	}
}
