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

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author cbarber
 *
 */
@Immutable
public abstract class TrainingAssignment implements ITrainingAssignment, Serializable
{
	/*-------
	 * State
	 */

	private static final long serialVersionUID = 1L;
	
	protected final TrainingAssignmentType _type;
	protected final @Nullable Object _value;
	
	@Immutable
	private static class ByUUID extends TrainingAssignment
	{
		private static final long serialVersionUID = 1L;

		private final UUID _varId;
		
		private ByUUID(TrainingAssignmentType type, @Nullable Object value,  UUID varId)
		{
			super(type, value);
			_varId = varId;
		}

		@Override
		public Variable getVariable(FactorGraph model)
		{
			return Objects.requireNonNull(model.getVariableByUUID(_varId));
		}
	}
	
	@Immutable
	private static class ByName extends TrainingAssignment
	{
		private static final long serialVersionUID = 1L;
		
		private final String _varName;

		private ByName(TrainingAssignmentType type, @Nullable Object value,  String varName)
		{
			super(type, value);
			_varName = varName;
		}

		@Override
		public Variable getVariable(FactorGraph model)
		{
			return Objects.requireNonNull(model.getVariableByName(_varName));
		}
	}
	
	@Immutable
	private static class ById extends TrainingAssignment
	{
		private static final long serialVersionUID = 1L;
		
		private final int _varId;
		
		private ById(TrainingAssignmentType type, @Nullable Object value, int varId)
		{
			super(type, value);
			_varId = varId;
		}

		@Override
		public Variable getVariable(FactorGraph model)
		{
			return Objects.requireNonNull(model.getVariable(_varId));
		}
	}
	
	@Immutable
	private static class ByVariableBase extends TrainingAssignment
	{
		private static final long serialVersionUID = 1L;
		
		private final transient Variable _var;
		
		private ByVariableBase(TrainingAssignmentType type, @Nullable Object value, Variable var)
		{
			super(type, value);
			_var = var;
		}
		
		@Override
		public Variable getVariable(FactorGraph model)
		{
			return _var;
		}

		/**
		 * We don't want to actually serialize the variable itself (since it would
		 * serialize everything attached to it as well!). So we replace this with
		 * the UUID-based version.
		 * @return
		 */
		private Object writeReplace()
		{
			return new ByUUID(_type, _value, _var.getUUID());
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	protected TrainingAssignment(TrainingAssignmentType type, @Nullable Object value)
	{
		_type = type;
		_value = value;
	}
	
	public static TrainingAssignment create(UUID varId, TrainingAssignmentType type, @Nullable Object value)
	{
		return new ByUUID(type, value, varId);
	}
	
	public static TrainingAssignment create(int varId, TrainingAssignmentType type, @Nullable Object value)
	{
		return new ById(type, value, varId);
	}

	public static TrainingAssignment create(String varName, TrainingAssignmentType type, @Nullable Object value)
	{
		return new ByName(type, value, varName);
	}
	
	public static TrainingAssignment create(Variable var, TrainingAssignmentType type, @Nullable Object value)
	{
		return new ByVariableBase(type, value, var);
	}
	
	/*-----------------------------
	 * ITrainingAssignment methods
	 */
	
	@Override
	public void applyToModel(FactorGraph model)
	{
		applyToModelVariable(getVariable(model), getAssignmentType(), getValue());
	}
	
	public static void applyToModelVariable(Variable var, TrainingAssignmentType type, @Nullable Object value)
	{
		switch (type)
		{
		case FIXED:
			var.setFixedValueObject(value);
			break;
		case INPUTS:
			var.setInputObject(value);
			break;
		case MISSING:
			// FIXME: is this the right way to set a missing assignment?
			var.setGuess(null);
			break;
		case VALUE:
			// FIXME: can we use this or do we need a new method?
			// Should we rename setGuess to setAssignment?
			var.setGuess(value);
			break;
		}
	}
	
	@Override
	public void applyToSolver(ISolverFactorGraph solver)
	{
		final ISolverVariable var = getSolverVariable(solver);
		if (var != null)
		{
			applyToSolverVariable(var, getAssignmentType(), getValue());
		}
	}
	
	public static void applyToSolverVariable(ISolverVariable var, TrainingAssignmentType type, @Nullable Object value)
	{
		switch (type)
		{
		case FIXED:
			var.setInputOrFixedValue(null, value, true);
			break;
		case INPUTS:
			var.setInputOrFixedValue(value, null, false);
			break;
		case MISSING:
			// FIXME: is this the right way to set a missing assignment?
			var.setGuess(null);
			break;
		case VALUE:
			// FIXME: can we use this or do we need a new method?
			// Should we rename setGuess to setAssignment?
			var.setGuess(value);
			break;
		}
	}

	/*
	 * 
	 */
	@Override
	public TrainingAssignmentType getAssignmentType()
	{
		return _type;
	}

	/*
	 * 
	 */
	@Override
	public @Nullable ISolverVariable getSolverVariable(@Nullable ISolverFactorGraph solver)
	{
		return solver != null ? solver.getSolverVariable(this.getVariable(solver.getModelObject())) : null;
	}

	/*
	 * 
	 */
	@Override
	public @Nullable Object getValue()
	{
		return _value;
	}

}
