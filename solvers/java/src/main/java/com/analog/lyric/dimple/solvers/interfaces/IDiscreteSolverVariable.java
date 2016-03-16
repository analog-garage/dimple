/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Discrete;

/**
 * 
 * @since 0.05
 * @author Christopher Barber
 */
public interface IDiscreteSolverVariable extends ISolverVariable
{
	@Override
	public DiscreteDomain getDomain();
	
	@Override
	public @Nullable Discrete getModelObject();
	
	/**
	 * Returns index of guess within its domain.
	 * <p>
	 * Functionally equivalent to:
	 * <blockquote>
	 * <pre>
	 * getDomain().getIndex(getGuess());
	 * </pre>
	 * </blockquote>
	 * <p>
	 * @see #getGuess()
	 */
	public int getGuessIndex();

	/**
	 * Sets the guess for this variable by its domain index.
	 * <p>
	 * @param guessIndex is a valid index for the variable's {@linkplain #getDomain domain}.
	 * @see #setGuess(Object)
	 */
	public void setGuessIndex(int guessIndex);
	
	public int getValueIndex();
}
