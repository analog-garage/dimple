package com.analog.lyric.dimple.model.transform;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.google.common.collect.Iterables;

public class FactorGraphTransformMap
{
	/*-------
	 * State
	 */
	
	private final FactorGraph _sourceModel;
	private final long _sourceVersion;
	private final FactorGraph _targetModel;
	private final Map<Factor,Factor> _sourceToTargetFactors;
	private final Map<VariableBase, VariableBase> _sourceToTargetVariables;
	private final LinkedHashMap<VariableBase, AddedDeterministicVariable<?>> _addedDeterministicVariables;
	private final Set<VariableBase> _conditionedVariables;
	
	public static abstract class AddedDeterministicVariable<Var extends VariableBase>
	{
		protected final Var _variable;
		protected final Var[] _inputs;
		
		protected AddedDeterministicVariable(Var newVariable, Var[] inputVariables)
		{
			_variable = newVariable;
			_inputs = inputVariables;
		}

		public abstract void updateGuess();

		public Domain getDomain()
		{
			return _variable.getDomain();
		}
		
		public Var getVariable()
		{
			return _variable;
		}
		
		public Var getInput(int i)
		{
			return _inputs[i];
		}
		
		public final int getInputCount()
		{
			return _inputs.length;
		}
		
		public abstract void updateValue(Value newVariableValue, Value[] inputs);
	}
	
	public static class AddedJointDiscreteVariable extends AddedDeterministicVariable<Discrete>
	{

		/**
		 * @param newVariable
		 * @param inputVariables
		 */
		public AddedJointDiscreteVariable(Discrete newVariable, Discrete[] inputVariables)
		{
			super(newVariable, inputVariables);
			assert(invariantsHold());
		}
		
		private boolean invariantsHold()
		{
			JointDomainIndexer domain = getDomain().getDomainIndexer();
			assert(domain.size() == _inputs.length);
			for (int i = 0; i < _inputs.length; ++i)
			{
				assert(domain.get(i) == _inputs[i].getDomain());
			}
			return true;
		}

		@Override
		public JointDiscreteDomain<?> getDomain()
		{
			return (JointDiscreteDomain<?>) getVariable().getDomain();
		}
		
		@Override
		public void updateGuess()
		{
			final JointDomainIndexer indexer = getDomain().getDomainIndexer();
			final int[] indices = indexer.allocateIndices(null);
			for (int i = 0; i < _inputs.length; ++i)
			{
				indices[i] = getInput(i).getGuessIndex();
			}
			getVariable().setGuessIndex(indexer.jointIndexFromIndices(indices));
		}
		
		@Override
		public void updateValue(Value newVariableValue, Value[] inputs)
		{
			JointDomainIndexer indexer = getDomain().getDomainIndexer();
			newVariableValue.setIndex(indexer.jointIndexFromValues(inputs));
		}
		
	}
	
	/*--------------
	 * Construction
	 */
	
	protected FactorGraphTransformMap(FactorGraph source, FactorGraph target)
	{
		final boolean identity = (source == target);
		_sourceModel = source;
		_sourceVersion = source.getVersionId();
		_targetModel = target;
		_sourceToTargetVariables = identity? null : new HashMap<VariableBase,VariableBase>(source.getVariableCount());
		_sourceToTargetFactors = identity? null : new HashMap<Factor,Factor>(source.getFactorCount());
		_addedDeterministicVariables = new LinkedHashMap<VariableBase, AddedDeterministicVariable<?>>();
		_conditionedVariables = new LinkedHashSet<VariableBase>();
	}
	
	protected FactorGraphTransformMap(FactorGraph source)
	{
		this(source, source);
	}
	
	public static FactorGraphTransformMap create(FactorGraph source, FactorGraph target)
	{
		return new FactorGraphTransformMap(source, target);
	}
	
	public static FactorGraphTransformMap identity(FactorGraph model)
	{
		return new FactorGraphTransformMap(model);
	}
	
	/*---------
	 * Methods
	 */
	
	public void addConditionedVariable(VariableBase variable)
	{
		assert(variable.hasFixedValue());
		_conditionedVariables.add(variable);
	}
	
	public void addDeterministicVariable(AddedDeterministicVariable<?> addedVar)
	{
		_addedDeterministicVariables.put(addedVar.getVariable(), addedVar);
	}
	
	public Iterable<AddedDeterministicVariable<?>> addedDeterministicVariables()
	{
		return Iterables.unmodifiableIterable(_addedDeterministicVariables.values());
	}
	
	public void addFactorMapping(Factor sourceFactor, Factor targetFactor)
	{
		_sourceToTargetFactors.put(sourceFactor, targetFactor);
	}
	
	public void addVariableMapping(VariableBase sourceVariable, VariableBase targetVariable)
	{
		_sourceToTargetVariables.put(sourceVariable, targetVariable);
	}
	
	public <Var extends VariableBase> AddedDeterministicVariable<Var> getAddedDeterministicVariable(Var targetVariable)
	{
		return (AddedDeterministicVariable<Var>) _addedDeterministicVariables.get(targetVariable);
	}
	
	public Set<VariableBase> conditionedVariables()
	{
		return _conditionedVariables;
	}
	
	public boolean isIdentity()
	{
		return _sourceToTargetVariables == null;
	}
	
	public boolean isValid()
	{
		if (_sourceVersion != _sourceModel.getVersionId())
		{
			return false;
		}
		
		for (VariableBase sourceVar : _conditionedVariables)
		{
			if (!sourceVar.hasFixedValue())
				return false;
			VariableBase targetVar = sourceToTargetVariable(sourceVar);
			if (!targetVar.hasFixedValue())
				return false;
			if (!sourceVar.getFixedValueObject().equals(targetVar.getFixedValueObject()))
				return false;
		}
		
		return true;
	}

	public FactorGraph source()
	{
		return _sourceModel;
	}
	
	public Factor sourceToTargetFactor(Factor sourceFactor)
	{
		if (_sourceToTargetFactors == null)
		{
			return sourceFactor;
		}
		return _sourceToTargetFactors.get(sourceFactor);
	}
	
	public Map<Factor,Factor> sourceToTargetFactors()
	{
		return Collections.unmodifiableMap(_sourceToTargetFactors);
	}
	
	public VariableBase sourceToTargetVariable(VariableBase sourceVariable)
	{
		if (_sourceToTargetVariables == null)
		{
			return sourceVariable;
		}
		return _sourceToTargetVariables.get(sourceVariable);
	}
	
	public Map<VariableBase,VariableBase> sourceToTargetVariables()
	{
		return Collections.unmodifiableMap(_sourceToTargetVariables);
	}
	
	/**
	 * Value of {@link FactorGraph#getVersionId()} of {@link #source()} when
	 * transform map was created.
	 */
	public long sourceVersion()
	{
		return _sourceVersion;
	}
	
	public FactorGraph target()
	{
		return _targetModel;
	}
	
	public void updateGuesses()
	{
		for (AddedDeterministicVariable<?> added : addedDeterministicVariables())
		{
			added.updateGuess();
		}
	}
}
