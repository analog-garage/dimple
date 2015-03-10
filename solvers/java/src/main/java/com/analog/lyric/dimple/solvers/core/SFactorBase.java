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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.events.SolverEvent;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraphEdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverEdgeState;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;

// TODO - add MFactor parameter

public abstract class SFactorBase extends SNode<Factor> implements ISolverFactor
{
	/*-----------
	 * Constants
	 */
	
	/**
	 * Bits in {@link #_flags} reserved by this class and its superclasses.
	 */
	@SuppressWarnings("hiding")
	protected static final int RESERVED_FLAGS = 0xFFF00000;
	
	protected final ISolverFactorGraph _parent;
	
	/*--------------
	 * Construction
	 */
	
	public SFactorBase(Factor factor, ISolverFactorGraph parent)
	{
		super(factor);
		_parent = parent;
	}
		
	/*---------------------
	 * ISolverNode methods
	 */

	@Override
	public ISolverFactorGraph getContainingSolverGraph()
	{
		return _parent;
	}

	@Override
	public ISolverVariable getSibling(int edge)
	{
		final Variable sibling = _model.getSibling(edge);
		return getSolverMapping().getSolverVariable(sibling);
	}
	
	@Override
	public SolverNodeMapping getSolverMapping()
	{
		return _parent.getSolverMapping();
	}
	
	@Override
	public @Nullable ISolverEdgeState createEdge(FactorGraphEdgeState edge)
	{
		return null;
	}
	
	public Factor getFactor()
	{
		return _model;
	}
	
	@Override
	public Object getBelief()
	{
		throw new DimpleException("not supported");
	}

	@Override
	public ISolverFactorGraph getParentGraph()
	{
		return _parent;
	}
	
	@Override
    public double getScore()
    {
		int numPorts = _model.getSiblingCount();
	    Object[] values = new Object[numPorts];

	    for (int port = 0; port < numPorts; port++)
	    {
	    	values[port] = getSibling(port).getGuess();
	    }
	    
	    return _model.getFactorFunction().evalEnergy(values);
    }

	@Override
	public int[][] getPossibleBeliefIndices()
	{
		throw new DimpleException("not implemented");
	}
	
	@Override
	public double getInternalEnergy()
	{
		throw new DimpleException("getInternalEnergy not yet supported");
	}
	
	@Override
	public double getBetheEntropy()
	{
		throw new DimpleException("getBetheEntropy not yet supported");
	}
	
	@Override
	public void setDirectedTo(int [] indices)
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
	protected @Nullable SolverEvent createMessageEvent(
		int edge,
		@Nullable IParameterizedMessage oldMessage,
		IParameterizedMessage newMessage)
	{
		return new FactorToVariableMessageEvent(this, edge, oldMessage, newMessage);
	}

	@Override
	protected Class<? extends SolverEvent> messageEventType()
	{
		return FactorToVariableMessageEvent.class;
	}
	
	/*--------------------
	 * Deprecated methods
	 */
	
	@Deprecated
	@Override
	public @Nullable Object getInputMsg(int portIndex)
	{
		final ISolverEdgeState sedge = getSiblingEdgeState(portIndex);
		return sedge != null ? sedge.getVarToFactorMsg() : null;
	}
	
	@Deprecated
	@Override
	public @Nullable Object getOutputMsg(int portIndex)
	{
		final ISolverEdgeState sedge = getSiblingEdgeState(portIndex);
		return sedge != null ? sedge.getFactorToVarMsg() : null;
	}
}
