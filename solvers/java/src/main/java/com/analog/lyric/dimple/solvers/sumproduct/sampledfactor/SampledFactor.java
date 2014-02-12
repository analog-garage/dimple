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

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * @author jeff
 * 
 * This class is used to implement factors that include Real variables
 * (and potentially also discrete variables), but are not otherwise
 * implemented by a custom factor.  In this case, we use the Gibbs
 * solver as an inner loop in generating approximate messages.
 * 
 * To do this, we create a "message graph" that is a new factor graph
 * that includes a new copy of this factor and a new variable associated
 * with each edge; of the same type as the corresponding sibling variables.
 * (Currently, Complex and RealJoint variables are not supported.)
 * 
 * To compute an approximate output message, the Input for each variable
 * in the message graph is set to the same value as the input message
 * to this factor.  The form of this depends on the type of variable:
 * discrete or real.  For the edge that we're computing the output
 * message, we don't use the input message, but instead set the input
 * of that variable to uniform.
 * 
 * When we perform inference on this message graph using the Gibbs solver,
 * the resulting samples estimate the belief on the variable in the message
 * graph corresponding to the output edge of the factor.  If inference were
 * perfect, this belief would exactly equal the desired output message.
 * Since the inference is approximate, the output message is an approximation
 * of the desired output message.  The accuracy depends on the number of
 * samples used in each update.
 * 
 */
public class SampledFactor extends SFactorBase
{
	private int _samplesPerUpdate = 100;
	private int _burnInScansPerUpdate = 0;
	private int _scansPerSample = 1;
	private MessageTranslatorBase[] _messageTranslator;
	private VariableBase[] _privateVariables;
	private FactorGraph _messageGraph;
	private com.analog.lyric.dimple.solvers.gibbs.SFactorGraph _solverGraph;

	
	public SampledFactor(Factor factor)
	{
		super(factor);
				
		int numSiblings = factor.getSiblingCount();
		_messageTranslator = new MessageTranslatorBase[numSiblings];
		_privateVariables = new VariableBase[numSiblings];

		for (int edge = 0; edge < numSiblings; edge++)
		{
			// Create a private copy of each sibling variable to use in the message graph
			VariableBase var = factor.getSibling(edge);
			_privateVariables[edge] = var.clone();
			_privateVariables[edge].setInputObject(null);

			// Create a message translator based on the variable type 
			// TODO: Allow more than one message representation for Real variables
			if (var.getDomain().isDiscrete())
				_messageTranslator[edge] = new DiscreteMessageTranslator(factor.getPort(edge), _privateVariables[edge]);
			else
				_messageTranslator[edge] = new GaussianMessageTranslator(factor.getPort(edge), _privateVariables[edge]);
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
		_solverGraph.setNumSamples(_samplesPerUpdate);
		_solverGraph.setBurnInScans(_burnInScansPerUpdate);
		_solverGraph.setScansPerSample(_scansPerSample);
		_solverGraph.saveAllSamples();		// FIXME: Only enable on output variable, and then turn off after solving
		
		// Set inputs of the message-graph variables to the incoming message value; all except the output variable
		for (int edge = 0; edge < numSiblings; edge++)
		{
			if (edge != outPortNum)
				_messageTranslator[edge].setVariableInputFromInputMessage();
			else
				_messageTranslator[edge].setVariableInputUniform();
		}

		// Run the Gibbs solver
		_messageGraph.solve();
	
		// Set the output message using the belief of the message-graph output variable
		_messageTranslator[outPortNum].setOutputMessageFromVariableBelief();

	}
	
	// Set/get operating parameters
	public void setNumSamples(int numSamples)
	{
		_samplesPerUpdate = numSamples;
	}

	public int getNumSamples()
	{
		return _samplesPerUpdate;
	}
	
	// FIXME Set the other parameters

	
	@Override
	public void resetEdgeMessages(int i)
	{
		_messageTranslator[i].initialize();
	}
	
	@Override
	public void createMessages()
	{
		final Factor factor = _factor;
		for (int i = 0, nVars = factor.getSiblingCount(); i < nVars; i++)
		{
			ISolverVariable var = factor.getSibling(i).getSolver();
			Object [] messages = var.createMessages(this);
			_messageTranslator[i].createInputMessage(messages[1]);
			_messageTranslator[i].createOutputMessage(messages[0]);
		}
	}

	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPortNum)
	{
		SampledFactor s = (SampledFactor)other;
		_messageTranslator[portNum].moveMessages(s._messageTranslator[otherPortNum]);
	}

	@Override
	public Object getInputMsg(int portIndex)
	{
		return _messageTranslator[portIndex].getInputMessage();
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _messageTranslator[portIndex].getOutputMessage();
	}

}
