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

import static java.util.Objects.*;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author jeff
 * 
 * This class is used to implement factors that have edges that
 * connect to non-discrete variables, but are not otherwise
 * implemented by a custom factor.  In this case, we use the Gibbs
 * solver as an inner loop in generating approximate messages.
 * 
 * To do this, we create a "message graph" that is a new factor graph
 * that includes a new copy of this factor and a new variable associated
 * with each edge; of the same type as the corresponding sibling variables.
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
	private final MessageTranslatorBase[] _messageTranslator;
	private final Variable[] _privateVariables;
	private final FactorGraph _messageGraph;
	
	public final static int DEFAULT_SAMPLES_PER_UPDATE = 1000;
	public final static int DEFAULT_BURN_IN_SCANS_PER_UPDATE = 10;
	public final static int DEFAULT_SCANS_PER_SAMPLE = 1;
	
	public SampledFactor(Factor factor)
	{
		super(factor);
				
		int numSiblings = factor.getSiblingCount();
		_messageTranslator = new MessageTranslatorBase[numSiblings];
		_privateVariables = new Variable[numSiblings];

		for (int edge = 0; edge < numSiblings; edge++)
		{
			// Create a private copy of each sibling variable to use in the message graph
			Variable var = factor.getSibling(edge);
			_privateVariables[edge] = var.clone();

			// Create a message translator based on the variable type
			// TODO: Allow alternative message representations for continuous variables
			if (var.getDomain().isDiscrete())
				_messageTranslator[edge] = new DiscreteMessageTranslator(factor.getPort(edge), _privateVariables[edge]);
			else if (var.getDomain().isReal())
				_messageTranslator[edge] = new NormalMessageTranslator(factor.getPort(edge), _privateVariables[edge]);
			else // Complex or RealJoint
				_messageTranslator[edge] = new MultivariateNormalMessageTranslator(factor.getPort(edge), _privateVariables[edge]);
			
			// Start with uniform input
			_messageTranslator[edge].setVariableInputUniform();
		}
		
		// Create a private message graph on which the Gibbs sampler will be run
		_messageGraph = new FactorGraph();
		_messageGraph.setSolverFactory(new GibbsSolver());
		_messageGraph.setEventAndOptionParent(this); // inherit options from this solver graph
		_messageGraph.addFactor(factor.getFactorFunction(), _privateVariables);
	}
	
	@Override
	public void doUpdateEdge(int outPortNum)
	{
		int numSiblings = _factor.getSiblingCount();
		
		// Set inputs of the message-graph variables to the incoming message value; all except the output variable
		for (int edge = 0; edge < numSiblings; edge++)
		{
			MessageTranslatorBase messageTranslator = _messageTranslator[edge];
			if (edge != outPortNum)	// Input edge
			{
				messageTranslator.setMessageDirection(MessageTranslatorBase.MessageDirection.INPUT);
				messageTranslator.setVariableInputFromInputMessage();
			}
			else					// Output edge
			{
				messageTranslator.setMessageDirection(MessageTranslatorBase.MessageDirection.OUTPUT);
				messageTranslator.setVariableInputUniform();
			}
		}

		// Run the Gibbs solver
		_messageGraph.solve();
	
		// Set the output message using the belief of the message-graph output variable
		_messageTranslator[outPortNum].setOutputMessageFromVariableBelief();

	}
	
	
	/**
	 * @deprecated Will be removed in a future release. Instead set {@link GibbsOptions#numSamples} option
	 * on this object using {@link #setOption}.
	 */
	@Deprecated
	public void setSamplesPerUpdate(int numSamples)
	{
		setOption(GibbsOptions.numSamples, numSamples);
	}

	/**
	 * @deprecated Will be removed in a future release. Instead get {@link GibbsOptions#numSamples} option
	 * from this object using {@link #getOption}.
	 */
	@Deprecated
	public int getSamplesPerUpdate()
	{
		return getOptionOrDefault(GibbsOptions.numSamples);
	}
	
	/**
	 * @deprecated Will be removed in a future release. Instead set {@link GibbsOptions#burnInScans} option
	 * on this object using {@link #setOption}.
	 */
	@Deprecated
	public void setBurnInScansPerUpdate(int burnInScansPerUpdate)
	{
		setOption(GibbsOptions.burnInScans, burnInScansPerUpdate);
	}

	/**
	 * @deprecated Will be removed in a future release. Instead get {@link GibbsOptions#burnInScans} option
	 * from this object using {@link #getOption}.
	 */
	@Deprecated
	public int getBurnInScansPerUpdate()
	{
		return getOptionOrDefault(GibbsOptions.burnInScans);
	}
	
	/**
	 * @deprecated Will be removed in a future release. Instead set {@link GibbsOptions#scansPerSample} option
	 * on this object using {@link #setOption}.
	 */
	@Deprecated
	public void setScansPerSample(int scansPerSample)
	{
		setOption(GibbsOptions.scansPerSample, scansPerSample);
	}
	
	/**
	 * @deprecated Will be removed in a future release. Instead get {@link GibbsOptions#scansPerSample} option
	 * from this object using {@link #getOption}.
	 */
	@Deprecated
	public int getScansPerSample()
	{
		return getOptionOrDefault(GibbsOptions.scansPerSample);
	}

	
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
			ISolverVariable var = requireNonNull(factor.getSibling(i).getSolver());
			Object [] messages = requireNonNull(var.createMessages(this));
			_messageTranslator[i].createInputMessage(messages[1]);
			_messageTranslator[i].createOutputMessage(messages[0]);
		}
	}

	@Override
	public void moveMessages(@NonNull ISolverNode other, int portNum, int otherPortNum)
	{
		SampledFactor s = (SampledFactor)other;
		_messageTranslator[portNum].moveMessages(s._messageTranslator[otherPortNum]);
	}

	@Override
	public @Nullable Object getInputMsg(int portIndex)
	{
		return _messageTranslator[portIndex].getInputMessage();
	}

	@Override
	public @Nullable Object getOutputMsg(int portIndex)
	{
		return _messageTranslator[portIndex].getOutputMessage();
	}

}
