/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.matlabproxy;

import java.util.UUID;

import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.dimple.options.DimpleOptionHolder;

/**
 * MATLAB proxy object for {@link VariableBlock}s.
 * @since 0.08
 * @author Christopher Barber
 */
public class PVariableBlock extends POptionHolder
{
	/*-------
	 * State
	 */
	
	private final VariableBlock _block;
	
	/*--------------
	 * Construction
	 */
	
	public PVariableBlock(VariableBlock block)
	{
		_block = block;
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public VariableBlock getDelegate()
	{
		return _block;
	}

	@Override
	public VariableBlock getModelerObject()
	{
		return _block;
	}

	/*
	 * 
	 */
	@Override
	public DimpleOptionHolder getOptionHolder(int i)
	{
		if (i != 0)
			throw new IndexOutOfBoundsException();
		return _block;
	}
	
	@Override
	public boolean isVariableBlock()
	{
		return true;
	}

	@Override
	public int size()
	{
		return 1;
	}

	/*------------------------
	 * PVariableBlock methods
	 */
	
	/**
	 * Returns global id of underlying block
	 * @since 0.08
	 */
	public long getId()
	{
		return _block.getGlobalId();
	}
	
	public long[] getIds()
	{
		return new long[] { getId() };
	}
	
	public UUID getUUID()
	{
		return _block.getUUID();
	}
	
	public UUID[] getUUIDs()
	{
		return new UUID[] { getUUID() };
	}
	
	/**
	 * Returns local id of underlying block with respect to its parent graph.
	 * @since 0.08
	 */
	public int getLocalId()
	{
		return _block.getLocalId();
	}
	
	public PFactorGraphVector getParent()
	{
		return new PFactorGraphVector(_block.getParentGraph());
	}
	
	/**
	 * Returns new variable vector containing variables in block in order.
	 * @since 0.08
	 */
	public PVariableVector getVariables()
	{
		return new PVariableVector(_block.toArray(new Node[_block.size()]));
	}
}
