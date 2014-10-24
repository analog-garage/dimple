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
import com.analog.lyric.dimple.model.variables.Variable;

/**
 * Base interface for solver representation of a variable.
 */
public interface ISolverVariable extends ISolverNode
{
	/**
	 * The domain of the variable.
	 * <p>
	 * Equivalent to:
	 * <blockquote>
	 * <pre>
	 * getModelObject().getDomain();
	 * </pre>
	 * </blockquote>
	 * 
	 * @since 0.07
	 */
	public Domain getDomain();
	
	@Override
	public @Nullable Variable getModelObject();
	
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue, boolean hasFixedValue);
	public @Nullable Object getBelief();
	
	public Object getValue();
	
	/**
	 * Sets guess to specified value.
	 * <p>
	 * @param guess is either null to unset the guess or a valid value for the
	 * {@linkplain #getDomain domain} of the variable.
	 * @see #getGuess()
	 * @see #guessWasSet()
	 */
    public void setGuess(@Nullable Object guess);
    
    /**
     * Current value of guess for this variable.
     * <p>
     * Returns value previously assigned by {@link #setGuess(Object)} or else
     * returns result of {@link #getValue()}.
     * @since 0.07
     */
    public Object getGuess();
    
    /**
     * True if guess has been set.
     * <p>
     * @see #getGuess()
     * @see #setGuess(Object)
     * @since 0.07
     */
    public boolean guessWasSet();
    
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
