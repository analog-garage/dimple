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

package com.analog.lyric.dimple.solvers.interfaces;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.variables.VariableBase;

public interface ISolverVariable extends ISolverNode
{
	public Domain getDomain();
	
	@Override
	public @Nullable VariableBase getModelObject();
	
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue, boolean hasFixedValue);
	public @Nullable Object getBelief();
	public Object getValue();
    public void setGuess(@Nullable Object guess);
    public Object getGuess();
    
    @Override
    public ISolverFactor getSibling(int edge);
    
    public void createNonEdgeSpecificState();
    
    //Create messages that will be passed to and from the specified factor.
    //This method should return an Object array with two elements, the first
    //being the newly created input message and the second being the newly created
    //output message.
    public @Nullable Object [] createMessages(ISolverFactor factor);
    
    //Method to reset an input message's values
	public @Nullable Object resetInputMessage(Object message);
	
	//Method to reset an output message's values
	public @Nullable Object resetOutputMessage(Object message);
	
    //Move global state from other node to this one
    public void moveNonEdgeSpecificState(ISolverNode other);

}
