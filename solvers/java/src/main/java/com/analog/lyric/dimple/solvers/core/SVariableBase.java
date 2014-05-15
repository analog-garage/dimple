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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.events.SolverEvent;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class SVariableBase extends SNode implements ISolverVariable
{
	/*-----------
	 * Constants
	 */
	
	/**
	 * Bits in {@link #_flags} reserved by this class and its superclasses.
	 */
	protected static final int RESERVED_FLAGS = 0xFFF00000;
	
	/*-------
	 * State
	 */
	
	protected VariableBase _var;
	
	/*--------------
	 * Construction
	 */
	
	public SVariableBase(VariableBase var)
	{
		super(var);
		_var = var;
	}
	
	/*---------------------
	 * ISolverNode methods
	 */

	@Override
	public VariableBase getModelObject()
	{
		return _var;
	}
	
	@Override
	public ISolverFactor getSibling(int edge)
	{
		return getModelObject().getSibling(edge).getSolver();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The implementation provided by this class simply returns the solver object
	 * associated with the model variable's {@link FactorGraph}.
	 * <p>
	 * This means that if the solver is changed on the model, this will return the
	 * new solver graph object, which may not be applicable to this instance.
	 */
	@Override
	public ISolverFactorGraph getParentGraph()
	{
		ISolverFactorGraph graph = null;
		FactorGraph mgraph = _var.getParentGraph();
		if(mgraph != null)
		{
			graph = mgraph.getSolver();
		}
		return graph;
	}
	
	@Override
	public ISolverFactorGraph getRootGraph()
	{
		ISolverFactorGraph graph = null;
		FactorGraph mgraph = _var.getRootGraph();
		if(mgraph != null)
		{
			graph = mgraph.getSolver();
		}
		return graph;
	}
	
	@Override
	public Object getValue()
	{
		throw new DimpleException("not supported");
	}

	@Override
	public void setGuess(Object guess)
	{
		throw new DimpleException("not supported");
	}

	@Override
	public Object getGuess()
	{
		throw new DimpleException("not supported");
	}

	@Override
	public double getScore()
	{
		throw new DimpleException("not supported");
	}

	@Override
	public double getInternalEnergy()
	{
		throw new DimpleException("not supported");
	}

	@Override
	public double getBetheEntropy()
	{
		throw new DimpleException("not supported");
	}
	
	@Override
	public Object resetOutputMessage(Object message)
	{
		return resetInputMessage(message);
	}
	
	@Override
    public void moveNonEdgeSpecificState(ISolverNode other)
    {
    	
    }
	
	@Override
	public void createNonEdgeSpecificState()
	{
		
	}
	
	/*---------------
	 * SNode methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The {@link SVariableBase} implementation returns a {@link VariableToFactorMessageEvent}.
	 */
	@Override
	protected SolverEvent createMessageEvent(
		int edge,
		IParameterizedMessage oldMessage,
		IParameterizedMessage newMessage)
	{
		return new VariableToFactorMessageEvent(this, getSibling(edge), oldMessage, newMessage);
	}


	@Override
	protected Class<? extends SolverEvent> messageEventType()
	{
		return VariableToFactorMessageEvent.class;
	}
}
