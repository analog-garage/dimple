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

package com.analog.lyric.dimple.solvers.sumproduct.sampledfactor;

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class SampledFactor extends SFactorBase
{
	private int _numSamples = 1;
	private int _burnInScans = 0;
	private int _scansPerSample = 1;
	private IMessageGenerator[] _messageGenerator;
	private VariableBase[] _privateVariables;
	private FactorGraph _messageGraph;
	private com.analog.lyric.dimple.solvers.gibbs.SFactorGraph _solverGraph;

	
	public SampledFactor(Factor factor)
	{
		super(factor);
				
		int numSiblings = factor.getSiblingCount();
		_messageGenerator = new IMessageGenerator[numSiblings];
		_privateVariables = new VariableBase[numSiblings];

		for (int edge = 0; edge < numSiblings; edge++)
		{
			// Choose message generator based on variable type
			// TODO: Allow more than one message representation for Real variables
			VariableBase var = factor.getSibling(edge);
			if (var.getDomain().isDiscrete())
				_messageGenerator[edge] = new DiscreteMessageGenerator(factor.getPort(edge));
			else
				_messageGenerator[edge] = new GaussianMessageGenerator(factor.getPort(edge));
			
			
			// Create a private copy of each sibling variable to use in the message graph
			_privateVariables[edge] = var.clone();
			_privateVariables[edge].setInputObject(null);
		}
		
		// Create a private message graph on which the Gibbs sampler will be run
		_messageGraph = new FactorGraph();
		_messageGraph.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		_solverGraph = (com.analog.lyric.dimple.solvers.gibbs.SFactorGraph)(_messageGraph.getSolver());
		_messageGraph.addFactor(factor.getFactorFunction(), _privateVariables);
	}
	
	@Override
	public void updateEdge(int outPortNum)
	{
		int numSiblings = _factor.getSiblingCount();
		_solverGraph.setNumSamples(_numSamples);
		_solverGraph.setBurnInScans(_burnInScans);
		_solverGraph.setScansPerSample(_scansPerSample);
		_solverGraph.saveAllSamples();		// FIXME: Only enable on output variable, and then turn off after solving
		VariableBase outputVar = _privateVariables[outPortNum];
		
		// Set inputs to the incoming message value for all variables except the output variable
		for (int edge = 0; edge < numSiblings; edge++)
		{
			if (edge != outPortNum)
			{
				VariableBase var = _privateVariables[edge];
				// FIXME Do this in a way that doesn't require knowing the variable type
				if (var.getDomain().isDiscrete())
				{
					var.setInputObject(_messageGenerator[edge].getInputMsg());
				}
				else
				{
					NormalParameters inputMessage = (NormalParameters)_messageGenerator[edge].getInputMsg();
					Normal inputFactorFunction = new Normal(inputMessage.getMean(), inputMessage.getPrecision());	// FIXME make more efficient
					var.setInputObject(inputFactorFunction);
				}
			}
			else
				_privateVariables[edge].setInputObject(null);
		}

		// Run the Gibbs solver
		_messageGraph.solve();
	
		// Get the resulting samples
		// FIXME: Get the original ArrayLists without first copying to arrays
		Object samples = null;
		if (outputVar.getDomain().isDiscrete())
			// FIXME: Get the beliefs in this case instead, and don't bother to save samples
			samples = ((com.analog.lyric.dimple.solvers.gibbs.SDiscreteVariable)(outputVar.getSolver())).getAllSampleIndices();
		else
			samples = ((com.analog.lyric.dimple.solvers.gibbs.SRealVariable)(outputVar.getSolver())).getAllSamples();
		
		
		// For all output sample values, compute the output message
		_messageGenerator[outPortNum].generateOutputMessageFromSamples(samples);
	}
	
	// Set/get operating parameters
	public void setNumSamples(int numSamples)
	{
		_numSamples = numSamples;
	}

	public int getNumSamples()
	{
		return _numSamples;
	}
	
	// FIXME Set the other parameters

	
	@Override
	public void resetEdgeMessages(int i)
	{
		_messageGenerator[i].initialize();
	}
	
	@Override
	public void createMessages()
	{
		final Factor factor = _factor;
		for (int i = 0, nVars = factor.getSiblingCount(); i < nVars; i++)
		{
			ISolverVariable var = factor.getSibling(i).getSolver();
			Object [] messages = var.createMessages(this);
			_messageGenerator[i].createInputMessage(messages[1]);
			_messageGenerator[i].createOutputMessage(messages[0]);
		}
	}

	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPortNum)
	{
		SampledFactor s = (SampledFactor)other;
		_messageGenerator[portNum].moveMessages(s._messageGenerator[otherPortNum]);
	}

	@Override
	public Object getInputMsg(int portIndex)
	{
		return _messageGenerator[portIndex].getInputMsg();
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _messageGenerator[portIndex].getOutputMsg();
	}

}
