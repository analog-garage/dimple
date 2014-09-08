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
 */
public interface ITrainingSample
{
	public void applyAssignmentsThroughModel(FactorGraph model);
	
	public void applyAssignmentsThroughSolver(ISolverFactorGraph solver);
	
	public Iterable<ITrainingAssignment> getAssignments();
	
	public @Nullable ITrainingAssignment getAssignmentForVariable(Variable variable);
	
	public @Nullable ITrainingAssignment getAssignmentForSolverVariable(ISolverVariable variable);
	
	/**
	 * @return the training set from which this sample was obtained.
	 */
	public ITrainingSet getTrainingSet();
}
