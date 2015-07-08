/*******************************************************************************
*   Copyright 2013-2015 Analog Devices, Inc.
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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

/**
 * @since 0.05
 */
public abstract class SDiscreteVariableDoubleArray extends SDiscreteVariableBase
{
	/*--------------
	 * Construction
	 */
	
	public SDiscreteVariableDoubleArray(Discrete var, ISolverFactorGraph parent)
	{
		super(var, parent);
	}

	/*-------------------------
	 * ISolverVariable methods
	 */
	
	@Override
	protected DiscreteMessage cloneMessage(int edge)
	{
		return getSiblingEdgeState(edge).varToFactorMsg.clone();
	}
	
	@Deprecated
	@Override
	public Object getInputMsg(int portIndex)
	{
		return getSiblingEdgeState(portIndex).factorToVarMsg.representation();
	}

	@Deprecated
	@Override
	public Object getOutputMsg(int portIndex)
	{
		return getSiblingEdgeState(portIndex).varToFactorMsg.representation();
	}

	/**
	 * Default implementation returns an array of zeros with length matching domain size.
	 * @since 0.08
	 */
	protected double [] createDefaultMessage()
	{
		return new double[getDomain().size()];
	}

	@Deprecated
	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		final DiscreteMessage message = getSiblingEdgeState(portIndex).factorToVarMsg;
		
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
	
	@Deprecated
	@Override
	public void setOutputMsgValues(int portIndex, Object obj)
	{
		final DiscreteMessage message = getSiblingEdgeState(portIndex).varToFactorMsg;
		
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
	public SDiscreteEdge<?> getSiblingEdgeState(int siblingIndex)
	{
		return (SDiscreteEdge<?>)getSiblingEdgeState_(siblingIndex);
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	/**
	 * Gets prior from model as a {@link DiscreteMessage}
	 * <p>
	 * Returns null if prior is not a {@link DiscreteMessage}. Logs error if prior
	 * is not a {@link Value} or {@link DiscreteMessage}.
	 * @since 0.08
	 */
	@Nullable
	protected DiscreteMessage getPrior()
	{
		IDatum prior = _model.getPrior();
		
		if (prior instanceof DiscreteMessage || prior == null)
			return (DiscreteMessage)prior;

		if (!(prior instanceof Value))
		{
			DimpleEnvironment.logError("Prior %s ignored on %s: type not supported", prior, _model);
		}
		
		return null;
	}
	

}
