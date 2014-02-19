package com.analog.lyric.dimple.model.transform;

import java.util.Map;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.google.common.collect.BiMap;

public class FactorGraphTransformMap
{
	/*-------
	 * State
	 */
	
	private final FactorGraph _sourceModel;
	private final FactorGraph _targetModel;
	private final BiMap<Node, Node> _nodeBiMap;
	
	/*--------------
	 * Construction
	 */
	
	public FactorGraphTransformMap(FactorGraph source, FactorGraph target, BiMap<Node,Node> nodeBiMap)
	{
		_sourceModel = source;
		_targetModel = target;
		_nodeBiMap = nodeBiMap;
	}
	
	/*---------
	 * Methods
	 */
	
	public FactorGraph source()
	{
		return _sourceModel;
	}
	
	public FactorGraph target()
	{
		return _targetModel;
	}

	public Map<Node,Node> sourceToTarget()
	{
		return _nodeBiMap;
	}
	
	public Map<Node,Node> targetToSource()
	{
		return _nodeBiMap.inverse();
	}
}
