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
 * A basic implementation of {@link ITrainingSample}.
 */
public class BasicTrainingSample implements ITrainingSample
{
	/*-------
	 * State
	 */
	
	private final ITrainingSet _trainingSet;
	private final Iterable<ITrainingAssignment> _assignments;
	
	/*--------------
	 * Construction
	 */
	
	public BasicTrainingSample(ITrainingSet set, Iterable<ITrainingAssignment> assignments)
	{
		_trainingSet = set;
		_assignments = assignments;
	}
	
	/*
	 * 
	 */
	@Override
	public void applyAssignmentsThroughModel(FactorGraph model)
	{
		for (ITrainingAssignment assignment : _assignments)
		{
			assignment.applyToModel(model);
		}
	}

	/*
	 * 
	 */
	@Override
	public void applyAssignmentsThroughSolver(ISolverFactorGraph solver)
	{
		for (ITrainingAssignment assignment : _assignments)
		{
			assignment.applyToSolver(solver);
		}
	}

	/*
	 * 
	 */
	@Override
	public Iterable<ITrainingAssignment> getAssignments()
	{
		return _assignments;
	}

	/*
	 * 
	 */
	@Override
	public @Nullable ITrainingAssignment getAssignmentForVariable(Variable variable)
	{
		FactorGraph fg = variable.getParentGraph();
		
		if (fg != null)
		{
			for (ITrainingAssignment assignment : _assignments)
			{
				if (assignment.getVariable(fg) == variable)
				{
					return assignment;
				}
			}
		}
		
		return null;
	}

	/*
	 * 
	 */
	@Override
	public @Nullable ITrainingAssignment getAssignmentForSolverVariable(ISolverVariable variable)
	{
		final Variable modelVar = variable.getModelObject();
		return modelVar != null ? getAssignmentForVariable(modelVar) : null;
	}

	/*
	 * 
	 */
	@Override
	public ITrainingSet getTrainingSet()
	{
		return _trainingSet;
	}

}
