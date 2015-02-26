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

package com.analog.lyric.dimple.solvers.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

/**
 * @since 0.05
 */
public abstract class SDiscreteVariableDoubleArray extends SDiscreteVariableBase
{
	/*-------
	 * State
	 */
	
	// FIXME - turn this into a DiscreteMessage
	protected double [] _input;
	
	/*--------------
	 * Construction
	 */
	
	public SDiscreteVariableDoubleArray(Discrete var, ISolverFactorGraph parent)
	{
		super(var, parent);
		_input = createDefaultMessage();
	}

	/*-------------------------
	 * ISolverVariable methods
	 */
	
	/**
	 * This method is called by the model object when the solver object is first created
	 * or any time input or fixedValue changes.  The method can be ignored if the solver
	 * wants to pull down the input object or fixedValue when it needs it, but, for  performance
	 * reasons, it'soften good to cache the input.
	 */
	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue)
	{
		if (input == null)
		{
			_input = createDefaultMessage();
		}
		else
		{
	    	double[] vals = (double[])input;
	    	if (vals.length != _model.asDiscreteVariable().getDiscreteDomain().size())
	    		throw new DimpleException("length of priors does not match domain");
	    	
	    	_input = vals;
		}
	}

	@Override
	protected DiscreteMessage cloneMessage(int edge)
	{
		return getEdge(edge).varToFactorMsg.clone();
	}
	
	/**
	 * This method is called during initialize() to reset the input and output
	 * messages.
	 */
	@Override
	public void resetEdgeMessages(int portNum)
	{
		getEdge(portNum).reset();
	}

	@Override
	public abstract @NonNull double[] resetInputMessage(Object message);
	
	@Override
	public Object getInputMsg(int portIndex)
	{
		// FIXME return actual message object
		return getEdge(portIndex).factorToVarMsg.representation();
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		// FIXME return actual message object
		return getEdge(portIndex).varToFactorMsg.representation();
	}

	/**
	 * This method must be implemented if the solver is to support streaming graphs.
	 */
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		final SDiscreteVariableDoubleArray sother = (SDiscreteVariableDoubleArray) other;

		final SDiscreteEdge<?> thisEdge = getEdge(thisPortNum);
		final SDiscreteEdge<?> otherEdge = sother.getEdge(otherPortNum);
		
		thisEdge.factorToVarMsg.setFrom(otherEdge.factorToVarMsg);
		thisEdge.varToFactorMsg.setFrom(otherEdge.varToFactorMsg);
		otherEdge.reset();
	}

	public double [] createDefaultMessage()
	{
		//TODO: both variable and factor do this.  Why doesn't factor just ask variable?
		int domainLength = _model.asDiscreteVariable().getDiscreteDomain().size();
    	double[] retVal = new double[domainLength];
    	return resetInputMessage(retVal);
    }
	
	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		setInputMsgValues(portIndex, obj);
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		final DiscreteMessage message = getEdge(portIndex).factorToVarMsg;
		
		if (obj instanceof DiscreteMessage)
		{
			message.setFrom((DiscreteMessage)obj);
		}
		else
		{
			double[] target  = message.representation();
			System.arraycopy(obj, 0, target, 0, target.length);
		}
	}
	
	@Override
	public void setOutputMsgValues(int portIndex, Object obj)
	{
		final DiscreteMessage message = getEdge(portIndex).varToFactorMsg;
		
		if (obj instanceof DiscreteMessage)
		{
			message.setFrom((DiscreteMessage)obj);
		}
		else
		{
			double[] target  = message.representation();
			System.arraycopy(obj, 0, target, 0, target.length);
		}
	}
	

	@SuppressWarnings("null")
	@Override
	protected SDiscreteEdge<?> getEdge(int siblingIndex)
	{
		return (SDiscreteEdge<?>) super.getEdge(siblingIndex);
	}
}
