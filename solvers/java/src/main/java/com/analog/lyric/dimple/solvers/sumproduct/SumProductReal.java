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

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;
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
	public SumProductReal(Real var, SumProductSolverGraph parent)
    {
		super(var, parent);
	}

    @Override
	protected void doUpdateEdge(int outPortNum)
    {
    	final NormalParameters outMsg = getSiblingEdgeState(outPortNum).varToFactorMsg;
    	final IDatum prior = _model.getPrior();
    	
    	if (prior instanceof Value)
    	{
    		outMsg.setDeterministic((Value)prior);
    		return;
    	}
    	
    	
    	NormalParameters input = priorToNormal(prior);
   	
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
    	IDatum prior = _model.getPrior();
    	
    	if (prior instanceof Value)
    	{
        	// If fixed value, just return the input, which has been set to a zero-variance message
    		return new NormalParameters(((Value)prior).getDouble(), Double.POSITIVE_INFINITY);
    	}
    	
    	NormalParameters input = priorToNormal(prior);
    	
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
	
	@Deprecated
	@Override
	public double getScore()
	{
		IDatum prior = _model.getPrior();
		
		if (prior == null)
			return 0;
		else if (prior instanceof Value)
			return Objects.equals(getGuess(), ((Value)prior).getObject()) ? 0 : Double.POSITIVE_INFINITY;
		else
			return ((IUnaryFactorFunction)prior).evalEnergy(getGuess());
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
	
    private @Nullable NormalParameters priorToNormal(@Nullable IDatum prior)
    {
    	if (prior instanceof NormalParameters)
    	{
    		return (NormalParameters)prior;
    	}
    	else if (prior instanceof Normal)
    	{
    		return ((Normal)prior).getParameters();
    	}
    	else if (prior != null)
    	{
    		DimpleEnvironment.logError(
    			"Ignoring prior on %s: sum-product reals only supports NormalParameters for priors but got %s",
    			_model, prior);
    	}
    	
    	return null;
    }
    
}
