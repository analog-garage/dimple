package com.analog.lyric.dimple.learning;

import java.util.Iterator;

import com.analog.lyric.collect.DoubleArrayIterable;
import com.analog.lyric.collect.PrimitiveIterable;
import com.analog.lyric.collect.PrimitiveIterator;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * A training sample represented as an ordered sequence of double values in the
 * same order as the training set's variable set. Where NaN denotes a missing value
 * and discrete variable values are represented as the index into their enumerated
 * elements.
 */
public class DoubleListTrainingSample implements IVariableListTrainingSample
{
	/*-------
	 * State
	 */
	
	private final IVariableListTrainingSet _trainingSet;
	private final PrimitiveIterable.OfDouble _assignments;
	
	/*--------------
	 * Construction
	 */
	
	public DoubleListTrainingSample(IVariableListTrainingSet trainingSet, PrimitiveIterable.OfDouble assignments)
	{
		_trainingSet = trainingSet;
		_assignments = assignments;
	}
	
	public DoubleListTrainingSample(IVariableListTrainingSet trainingSet, double[] assignments)
	{
		this(trainingSet, new DoubleArrayIterable(assignments));
	}

	/*-------------------------
	 * ITrainingSample methods
	 */
	
	@Override
	public void applyAssignmentsThroughModel(FactorGraph model)
	{
		PrimitiveIterator.OfDouble assignmentIter = _assignments.iterator();
		for (VariableBase var : _trainingSet.getVariableList())
		{
			applyToVariable(var, assignmentIter.nextDouble());
		}
	}

	@Override
	public void applyAssignmentsThroughSolver(ISolverFactorGraph solver)
	{
		PrimitiveIterator.OfDouble assignmentIter = _assignments.iterator();
		for (VariableBase var : _trainingSet.getVariableList())
		{
			applyToVariable(var, assignmentIter.nextDouble());
		}
	}

	@Override
	public Iterable<ITrainingAssignment> getAssignments()
	{
		return new AssignmentIterable();
	}

	@Override
	public ITrainingAssignment getAssignmentForVariable(VariableBase variable)
	{
		Iterator<VariableBase> variables = getTrainingSet().getVariableList().iterator();
		PrimitiveIterator.OfDouble assignments = _assignments.iterator();

		while (variables.hasNext())
		{
			VariableBase curVar = variables.next();
			double assignment = assignments.nextDouble();
			if (curVar == variable)
			{
				return makeAssignment(curVar, assignment);
			}
		}
		
		return null;
	}

	@Override
	public ITrainingAssignment getAssignmentForSolverVariable(ISolverVariable variable)
	{
		return getAssignmentForVariable(variable.getModelObject());
	}

	/*-------------------------------------
	 * IVariableListTrainingSample methods
	 */
	
	@Override
	public IVariableListTrainingSet getTrainingSet()
	{
		return _trainingSet;
	}
	
	/*---------------------------------
	 * AllDoubleTrainingSample methods
	 */
	
	public static void applyToVariable(VariableBase var, double value)
	{
		TrainingAssignmentType type = assignmentTypeForVariable(var, value);
		Object objValue = type != TrainingAssignmentType.MISSING ? value : null;
		TrainingAssignment.applyToModelVariable(var, type, objValue);
	}
	
	/**
	 * Determines the training assignment type for {@code value} for given {@code variable}.
	 * 
	 * @return {@link TrainingAssignmentType#MISSING} if {@code value} is a NaN and otherwise returns
	 * {@link TrainingAssignmentType#VALUE}.
	 * @throws DimpleException if {@code value} is not a valid member of {@code variable}'s domain.
	 */
	public static TrainingAssignmentType assignmentTypeForVariable(VariableBase variable, double value)
	{
		TrainingAssignmentType type = TrainingAssignmentType.MISSING;
		
		if (!Double.isNaN(value))
		{
			Domain domain = variable.getDomain();
			if (!domain.containsValueWithRepresentation(value))
			{
				throw domain.domainError(value);
			}
			type = TrainingAssignmentType.VALUE;
		}
		
		return type;
	}
	
	protected ITrainingAssignment makeAssignment(VariableBase var, double value)
	{
		TrainingAssignmentType type = assignmentTypeForVariable(var, value);
		Object objValue = type != TrainingAssignmentType.MISSING ? value : null;
		return TrainingAssignment.create(var, type, objValue);
	}
	
	/*-----------------
	 * Private members
	 */
	
	private class AssignmentIterator implements Iterator<ITrainingAssignment>
	{
		private final Iterator<VariableBase> _variables = getTrainingSet().getVariableList().iterator();
		private final PrimitiveIterator.OfDouble _values = _assignments.iterator();
		
		@Override
		public boolean hasNext()
		{
			return _variables.hasNext()  & _values.hasNext();
		}

		@Override
		public ITrainingAssignment next()
		{
			return makeAssignment(_variables.next(), _values.next());
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("Iterator.remove");
		}
	}
	
	private class AssignmentIterable implements Iterable<ITrainingAssignment>
	{
		@Override
		public Iterator<ITrainingAssignment> iterator()
		{
			return new AssignmentIterator();
		}
		
	}
}
