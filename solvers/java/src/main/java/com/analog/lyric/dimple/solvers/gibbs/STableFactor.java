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

import java.util.ArrayList;

import com.analog.lyric.dimple.FactorFunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.dimple.solvers.gibbs.sample.DiscreteSample;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;


public class STableFactor extends STableFactorBase implements ISolverFactorGibbs
{
    protected DiscreteSample[] _inPortMsgs = null;
    protected double[][] _outPortMsgs = null;
    protected int _numPorts;
    protected boolean _isDeterministicDirected;

    
	public STableFactor(Factor factor)
	{
		super(factor);
		_isDeterministicDirected = _factor.getFactorFunction().isDeterministicDirected();
	}
	
	
	@Override
	public void updateEdge(int outPortNum)
	{
		// The Gibbs solver doesn't directly update factors, but the equivalent is instead done calls from variables
		// This is ignored and doesn't throw an error so that a custom schedule that updates factors won't cause a problem
	}
	@Override
	public void update()
	{
		// The Gibbs solver doesn't directly update factors, but the equivalent is instead done calls from variables
		// This is ignored and doesn't throw an error so that a custom schedule that updates factors won't cause a problem
	}
	

	@Override
	public double getConditionalPotential(int portIndex)
	{
		double result = getPotential();
		
		// If this is a deterministic directed factor, and the request is from a directed-from variable,
		// Then propagate the request through the directed-to variables and sum up the results
		if (_isDeterministicDirected && !_factor.isDirectedTo(portIndex))
		{
			ArrayList<INode> siblings = _factor.getSiblings();
		    for (int port = 0; port < _numPorts; port++)
		    {
		    	VariableBase v = (VariableBase)siblings.get(port);
		    	if (_factor.isDirectedTo(v))
		    		result += ((ISolverVariableGibbs)v.getSolver()).getConditionalPotential(_factor.getSiblingPortIndex(port));
		    }
		}

		return result;
	}
	


	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		// Generate message representing conditional distribution of selected edge variable
		// This should be called only for a table factor that is not a deterministic directed factor

		if (_isDeterministicDirected) throw new DimpleException("Invalid call to updateEdge");
		
		double[] outMessage = _outPortMsgs[outPortNum];
		int outputMsgLength = outMessage.length;

		IFactorTable factorTable = getFactorTable();
		double[] factorTableWeights = factorTable.getPotentials();

		int[] inPortMsgs = new int[_numPorts];
		for (int port = 0; port < _numPorts; port++)
			inPortMsgs[port] = _inPortMsgs[port].getIndex();

		for (int outIndex = 0; outIndex < outputMsgLength; outIndex++)
		{
			inPortMsgs[outPortNum] = outIndex;
		
			int weightIndex = factorTable.getWeightIndexFromTableIndices(inPortMsgs);
			if (weightIndex >= 0)
				outMessage[outIndex] = factorTableWeights[weightIndex];
			else
				outMessage[outIndex] = Double.POSITIVE_INFINITY;
			
		}
	}
	
	
	

	@Override
	public double getPotential()
	{
	    int[] inPortMsgs = new int[_numPorts];
	    for (int port = 0; port < _numPorts; port++)
	    	inPortMsgs[port] = _inPortMsgs[port].getIndex();
	    
	    return getPotential(inPortMsgs);
	}
	public double getPotential(int[] inputs)
	{
		IFactorTable factorTable = getFactorTable();
		int weightIndex = factorTable.getWeightIndexFromTableIndices(inputs);
		if (weightIndex >= 0)
			return factorTable.getPotentials()[weightIndex];
		else
			return Double.POSITIVE_INFINITY;
	}
		
	
	// Set the value of a neighboring variable
	// If this is a deterministic directed factor, and this variable is a directed input (directed-from)
	// then re-compute the directed outputs and propagate the result to the directed-to variables
	@Override
	public void updateNeighborVariableValue(int portIndex)
	{
		if (!_isDeterministicDirected) return;
		if (_factor.isDirectedTo(portIndex)) return;
		
		// Compute the output values of the deterministic factor function from the input values
	    Object[] values = new Object[_numPorts];
	    for (int port = 0; port < _numPorts; port++)
	    	values[port] = _inPortMsgs[port].getObject();
		_factor.getFactorFunction().evalDeterministicFunction(values);
		
		// Update the directed-to variables with the computed values
		ArrayList<INode> siblings = _factor.getSiblings();
	    for (int port = 0; port < _numPorts; port++)
	    {
	    	VariableBase v = (VariableBase)siblings.get(port);
	    	if (_factor.isDirectedTo(v))
	    		((SDiscreteVariable)v.getSolver()).setCurrentSample(values[port]);
	    }
	}



	@Override
	public void createMessages()
	{
    	int size = _factor.getSiblings().size();
    	_numPorts= size;
    	
	    _inPortMsgs = new DiscreteSample[_numPorts];
	    _outPortMsgs = new double[_numPorts][];
	    
	    for (int port = 0; port < _numPorts; port++)
	    {
	    	ISolverVariable svar = _factor.getVariables().getByIndex(port).getSolver();
	    	Object [] messages = svar.createMessages(this);
	    	_inPortMsgs[port] = (DiscreteSample)messages[1];
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
		STableFactor tf = (STableFactor)other;
		this._inPortMsgs[thisPortNum] = tf._inPortMsgs[otherPortNum];
		this._outPortMsgs[thisPortNum] = tf._outPortMsgs[otherPortNum];
	}

	@Override
	public void setDirectedTo(int [] indices)
	{
		for (VariableBase vb : _factor.getVariables())
		{
			((ISolverVariableGibbs)vb.getSolver()).updateDirectedCache();
		}
	}

}
