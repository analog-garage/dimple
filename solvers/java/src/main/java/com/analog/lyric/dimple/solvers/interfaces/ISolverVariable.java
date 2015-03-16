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
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public Variable getModelObject();
	
    @Override
    public ISolverFactorGraph getParentGraph();
    
    @Override
    public ISolverFactor getSibling(int siblingNumber);
    
    /*-------------------------
     * ISolverVariable methods
     */
    
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
	
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue);
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
    
    public void createNonEdgeSpecificState();
    
    //Move global state from other node to this one
    public void moveNonEdgeSpecificState(ISolverNode other);

}
