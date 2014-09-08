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

import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.Variable;
import org.eclipse.jdt.annotation.Nullable;


/**
 * @author jeff
 * 
 * Base class for message translators, which translate between the belief-propagation messages
 * in the SumProduct solver graph to the inputs and outputs of the message graph, which uses
 * the Gibbs solver.  The inputs in the message graph are set from the input messages of the
 * sampled factor (except for the variable associated with the output edge).  The output
 * message of the sampled factor is set from the beliefs and/or samples generated for the
 * associated variable in the message graph.
 *
 */
public abstract class MessageTranslatorBase
{
	protected Port _port;
	protected Variable _variable;
	public static enum MessageDirection {INPUT, OUTPUT}
	
	public MessageTranslatorBase(Port port, Variable variable)
	{
		_port = port;
		_variable = variable;
	}

	// Abstract methods
	public abstract void setMessageDirection(MessageDirection messageDirection);
	public abstract void setVariableInputFromInputMessage();
	public abstract void setVariableInputUniform();
	public abstract void setOutputMessageFromVariableBelief();
	public abstract void initialize();
	public abstract void createInputMessage(Object msg);
	public abstract void createOutputMessage(Object msg);
	public abstract @Nullable Object getInputMessage();
	public abstract @Nullable Object getOutputMessage();
	public abstract void moveMessages(MessageTranslatorBase other);
}
