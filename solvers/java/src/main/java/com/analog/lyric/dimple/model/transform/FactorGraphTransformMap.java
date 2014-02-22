package com.analog.lyric.dimple.model.transform;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.google.common.collect.BiMap;

public class FactorGraphTransformMap
{
	/*-------
	 * State
	 */
	
	private final FactorGraph _sourceModel;
	private final FactorGraph _targetModel;
	private final BiMap<Node, Node> _nodeBiMap;
	private final List<AddedDeterministicVariable> _addedDeterministicVariables;
	
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
	
	public FactorGraphTransformMap(FactorGraph source, FactorGraph target, BiMap<Node,Node> nodeBiMap)
	{
		_sourceModel = source;
		_targetModel = target;
		_nodeBiMap = nodeBiMap;
		_addedDeterministicVariables = new LinkedList<AddedDeterministicVariable>();
	}
	
	/*---------
	 * Methods
	 */
	
	public void addDeterministicVariable(AddedDeterministicVariable addedVar)
	{
		_addedDeterministicVariables.add(addedVar);
	}
	
	public List<AddedDeterministicVariable> addedDeterministicVariables()
	{
		return _addedDeterministicVariables;
	}
	
	public FactorGraph source()
	{
		return _sourceModel;
	}
	
	public Map<Node,Node> sourceToTarget()
	{
		return _nodeBiMap;
	}
	
	public FactorGraph target()
	{
		return _targetModel;
	}

	public Map<Node,Node> targetToSource()
	{
		return _nodeBiMap.inverse();
	}
}
