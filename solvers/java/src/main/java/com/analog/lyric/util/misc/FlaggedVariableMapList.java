package com.analog.lyric.util.misc;

import com.analog.lyric.dimple.model.variables.VariableBase;


public class FlaggedVariableMapList extends FlaggedMapList<VariableBase> implements IVariableMapList
{
	public FlaggedVariableMapList(int initialCapacity)
	{
		super(initialCapacity);
	}
}
