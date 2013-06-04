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

import com.analog.lyric.dimple.model.VariableBase;

public interface ISolverVariable extends ISolverNode
{
	@Override
	public VariableBase getModelObject();
	
	public void setInputOrFixedValue(Object input,Object fixedValue, boolean hasFixedValue);
	public Object getBelief();
	public Object getValue();
    public void setGuess(Object guess);
    public Object getGuess();
    
    
    public void createNonEdgeSpecificState();
    
    //Create messages that will be passed to and from the specified factor.
    //This method should return an Object array with two elements, the first
    //being the newly created input message and the second being the newly created
    //output message.
    public Object [] createMessages(ISolverFactor factor);
    
    //Method to reset an input message's values
	public Object resetInputMessage(Object message);
	
	//Method to reset an output message's values
	public Object resetOutputMessage(Object message);
	
    //Move global state from other node to this one
    public void moveNonEdgeSpecificState(ISolverNode other);

}
