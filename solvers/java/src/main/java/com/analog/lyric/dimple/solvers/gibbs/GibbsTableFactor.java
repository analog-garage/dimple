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

package com.analog.lyric.dimple.solvers.gibbs;

import static java.util.Objects.*;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * Solver table factor for Gibbs solver.
 * 
 * @since 0.07
 */
public class GibbsTableFactor extends STableFactorBase implements ISolverFactorGibbs
{
	/*-------
	 * State
	 */
	
    protected DiscreteValue[] _inPortMsgs = new DiscreteValue[0];
    protected double[][] _outPortMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    protected int _numPorts;
    protected boolean _isDeterministicDirected;
    private boolean _visited = false;
	private int _topologicalOrder = 0;
    
    /*--------------
     * Construction
     */
    
	public GibbsTableFactor(Factor factor)
	{
		super(factor);
		_isDeterministicDirected = _factor.getFactorFunction().isDeterministicDirected();
	}
	
	
	@Override
	public void doUpdateEdge(int outPortNum)
	{
		// The Gibbs solver doesn't directly update factors, but the equivalent is instead done calls from variables
		// This is ignored and doesn't throw an error so that a custom schedule that updates factors won't cause a problem
	}
	@Override
	protected void doUpdate()
	{
		// The Gibbs solver doesn't directly update factors, but the equivalent is instead done calls from variables
		// This is ignored and doesn't throw an error so that a custom schedule that updates factors won't cause a problem
	}
	

	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		// Generate message representing conditional distribution of selected edge variable
		// This should be called only for a table factor that is not a deterministic directed factor

		if (_isDeterministicDirected) throw new DimpleException("Invalid call to updateEdge");
		
		double[] outMessage = _outPortMsgs[outPortNum];
		IFactorTable factorTable = getFactorTable();

		final int numPorts = _numPorts;
		int[] inPortMsgs = new int[numPorts];
		for (int port = 0; port < numPorts; port++)
			inPortMsgs[port] = _inPortMsgs[port].getIndex();

		inPortMsgs[outPortNum] = 0;
		factorTable.getEnergySlice(outMessage, outPortNum, inPortMsgs);
	}
	
	
	

	@Override
	public double getPotential()
	{
		if (_isDeterministicDirected)
			return 0;

		if (getFactorTableIfComputed() == null)
		{
			// Avoid creating table because it may be very large.
			// FIXME - think more about this. Should this be conditional on something?
			final double energy = getFactor().getFactorFunction().evalEnergy(_inPortMsgs);
			if (energy != energy)	// Faster isNaN
				return Double.POSITIVE_INFINITY;
			return energy;
		}
		
		int[] inPortMsgs = new int[_numPorts];
		for (int port = 0; port < _numPorts; port++)
			inPortMsgs[port] = _inPortMsgs[port].getIndex();

		return getPotential(inPortMsgs);
	}
	public double getPotential(int[] inputs)
	{
		return getFactorTable().getEnergyForIndices(inputs);
	}
		
	@Override
	public final int getTopologicalOrder()
	{
		return _topologicalOrder ;
	}
	
	@Override
	public final void setTopologicalOrder(int order)
	{
		_topologicalOrder = order;
	}
	
	
	@Override
	public void updateNeighborVariableValue(int variableIndex, Value oldValue)
	{
		((GibbsSolverGraph)requireNonNull(getRootGraph())).scheduleDeterministicDirectedUpdate(this, variableIndex, oldValue);
	}
	
	@Override
	public void updateNeighborVariableValuesNow(@Nullable Collection<IndexedValue> oldValues)
	{
		// Compute the output values of the deterministic factor function from the input values
		final Factor factor = _factor;
		factor.getFactorFunction().evalDeterministic(_inPortMsgs);
		
		// Update the directed-to variables with the computed values
		int[] directedTo = factor.getDirectedTo();
		if (directedTo != null)
		{
			for (int outputIndex : directedTo)
			{
				Variable variable = factor.getSibling(outputIndex);
				// FIXME: is sample value already set? Just need to handle side effects?
				ISolverVariableGibbs svar =
					(ISolverVariableGibbs)variable.requireSolver("updateNeighborVariableValuesNow");
				svar.setCurrentSample(_inPortMsgs[outputIndex]);
			}
		}
	}



	@Override
	public void createMessages()
	{
		final Factor factor = _factor;
    	int size = factor.getSiblingCount();
    	_numPorts= size;
    	
	    _inPortMsgs = new DiscreteValue[_numPorts];
	    _outPortMsgs = new double[_numPorts][];
	    
	    for (int port = 0; port < _numPorts; port++)
	    {
	    	ISolverVariable svar = requireNonNull(factor.getSibling(port).getSolver());
	    	Object [] messages = requireNonNull(svar.createMessages(this));
	    	_inPortMsgs[port] = (DiscreteValue)messages[1];
	    	_outPortMsgs[port] = (double[])messages[0];
	    }
	}


	@Override
	public void initialize()
	{
		super.initialize();
		_isDeterministicDirected = _factor.getFactorFunction().isDeterministicDirected();
	}
	
	@Override
	public void resetEdgeMessages(int portNum)
	{

	}


	@Override
	public Object getInputMsg(int portIndex)
	{
		return _inPortMsgs[portIndex];
	}


	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outPortMsgs[portIndex];
	}


	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		GibbsTableFactor tf = (GibbsTableFactor)other;
		this._inPortMsgs[thisPortNum] = tf._inPortMsgs[otherPortNum];
		this._outPortMsgs[thisPortNum] = tf._outPortMsgs[otherPortNum];
	}

	/*--------------------------
	 * STableFactorBase methods
	 */

	@Override
	protected void setTableRepresentation(IFactorTable table)
	{
		if (_isDeterministicDirected)
		{
			table.setRepresentation(FactorTableRepresentation.DETERMINISTIC);
		}
		else
		{
			table.setRepresentation(FactorTableRepresentation.DENSE_ENERGY);
		}
	}

	@Override
	public boolean setVisited(boolean visited)
	{
		boolean changed = _visited ^ visited;
		_visited = visited;
		return changed;
	}
}
