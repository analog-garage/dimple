/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.learning;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents an assignment (or lack thereof) to a random variable for the purpose
 * of training for parameter or structure learning.
 */
public interface ITrainingAssignment
{
	/**
	 * Apply the training assignment to the appropriate variable in the given {@code model}.
	 * <p>
	 * Implementors should make use of static utility methods in {@link TrainingAssignment}.
	 */
	public void applyToModel(FactorGraph model);
	
	/**
	 * Apply the training assignment to the appropriate variable in the given {@code solver}.
	 * <p>
	 * Implementors should make use of static utility methods in {@link TrainingAssignment}.
	 */
	public void applyToSolver(ISolverFactorGraph solver);
	
	public TrainingAssignmentType getAssignmentType();
	
	/**
	 * Lookup the model variable to which the assignment should be
	 * applied. The assignment does not necessarily contain a direct
	 * pointer to the variable.
	 */
	public Variable getVariable(FactorGraph model);
	
	/**0
	 * Lookup the solver variable to which the assignment should be
	 * applied. The assignment does not necessarily contain a direct
	 * pointer to the variable.
	 */
	public @Nullable ISolverVariable getSolverVariable(@Nullable ISolverFactorGraph solver);
	
	/**
	 * The type of value will depend on {@link #getAssignmentType()}:
	 * 
	 * <dl>
	 * <dt>MISSING
	 * <dd>Returns null.
	 * <dt>VALUE
	 * <dd>Returns a value compatible with the variable's domain and inputs.
	 * For discrete variables, this should return the index of the domain element,
	 * not the element itself.
	 * <dt>INPUTS
	 * <dd>Returns an inputs object suitable for use with {@link Variable#setInputObject(Object)}
	 * on this variable.
	 * <dt>FIXED
	 * <dd>Returns a value compatible with the variable's domain and inputs.
	 * For discrete variables, this should return the index of the domain element,
	 * not the element itself.
	 * </d>
	 */
	public @Nullable Object getValue();
}
