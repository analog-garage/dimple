package com.analog.lyric.dimple.model.transform;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;

public class FactorGraphTransformMap
{
	/*-------
	 * State
	 */
	
	private final FactorGraph _sourceModel;
	private final long _sourceVersion;
	private final FactorGraph _targetModel;
	private final Map<Node, Node> _sourceToTargetMap;
	private final List<AddedDeterministicVariable> _addedDeterministicVariables;
	private final Set<VariableBase> _conditionedVariables;
	
	public static abstract class AddedDeterministicVariable
	{
		protected final VariableBase _variable;
		protected final VariableBase[] _inputs;
		
		public AddedDeterministicVariable(VariableBase newVariable, VariableBase[] inputVariables)
		{
			_variable = newVariable;
			_inputs = inputVariables;
		}

		public Domain getDomain()
		{
			return _variable.getDomain();
		}
		
		public VariableBase getVariable()
		{
			return _variable;
		}
		
		public final VariableBase getInput(int i)
		{
			return _inputs[i];
		}
		
		public final int getInputCount()
		{
			return _inputs.length;
		}
		
		public abstract void updateValue(Value newVariableValue, Value[] inputs);
	}
	
	public static class AddedJointDiscreteVariable extends AddedDeterministicVariable
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
		public void updateValue(Value newVariableValue, Value[] inputs)
		{
			JointDomainIndexer indexer = getDomain().getDomainIndexer();
			newVariableValue.setIndex(indexer.jointIndexFromValues(inputs));
		}
		
		@Override
		public JointDiscreteDomain<?> getDomain()
		{
			return (JointDiscreteDomain<?>) getVariable().getDomain();
		}
		
		@Override
		public Discrete getVariable()
		{
			return _variable.asDiscreteVariable();
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	protected FactorGraphTransformMap(FactorGraph source, FactorGraph target, Map<Node,Node> sourceToTargetMap)
	{
		_sourceModel = source;
		_sourceVersion = source.getVersionId();
		_targetModel = target;
		_sourceToTargetMap = sourceToTargetMap;
		_addedDeterministicVariables = new LinkedList<AddedDeterministicVariable>();
		_conditionedVariables = new LinkedHashSet<VariableBase>();
	}
	
	protected FactorGraphTransformMap(FactorGraph source)
	{
		this(source, source, null);
	}
	
	public static FactorGraphTransformMap create(
		FactorGraph source,	FactorGraph target,	Map<Node,Node> sourceToTargetMap)
	{
		return new FactorGraphTransformMap(source, target, sourceToTargetMap);
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
	
	public void addDeterministicVariable(AddedDeterministicVariable addedVar)
	{
		_addedDeterministicVariables.add(addedVar);
	}
	
	public List<AddedDeterministicVariable> addedDeterministicVariables()
	{
		return _addedDeterministicVariables;
	}
	
	public Set<VariableBase> conditionedVariables()
	{
		return _conditionedVariables;
	}
	
	public boolean isIdentity()
	{
		return _sourceToTargetMap == null;
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
	
	public Map<Node,Node> sourceToTarget()
	{
		return _sourceToTargetMap;
	}
	
	public Factor sourceToTargetFactor(Factor sourceFactor)
	{
		if (_sourceToTargetMap == null)
		{
			return sourceFactor;
		}
		return (Factor) _sourceToTargetMap.get(sourceFactor);
	}
	
	public VariableBase sourceToTargetVariable(VariableBase sourceVariable)
	{
		if (_sourceToTargetMap == null)
		{
			return sourceVariable;
		}
		return (VariableBase) _sourceToTargetMap.get(sourceVariable);
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
}
