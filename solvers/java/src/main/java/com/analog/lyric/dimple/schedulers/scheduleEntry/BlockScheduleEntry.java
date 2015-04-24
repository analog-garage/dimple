/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.schedulers.scheduleEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.util.misc.Internal;
import com.analog.lyric.util.misc.Matlab;



/**
 * A schedule entry that contains a collection of nodes to be updated together.
 * <p>
 * This class is primarily targeted at the Gibbs solver for block Gibbs updates. For that case, all
 * of the nodes in the collection would be variables.
 *
 * @author jeffb
 * @since 0.06
 */
public class BlockScheduleEntry implements IScheduleEntry
{
	/*-------
	 * State
	 */
	
	private final IBlockUpdater _blockUpdater;
	private final VariableBlock _block;

	/*---------------
	 * Construction
	 */
	
	/**
	 * 
	 * @param blockUpdater
	 * @param block
	 * @since 0.08
	 */
	public BlockScheduleEntry(IBlockUpdater blockUpdater, VariableBlock block)
	{
		_blockUpdater = blockUpdater;
		_block = block;
		_blockUpdater.attachNodes(block.toArray(new INode[block.size()]));
	}
	
	/**
	 * @param blockUpdater
	 * @param nodeList
	 * @since 0.08
	 * @category internal
	 * @deprecated this will eventually be replaced by something in the matlabproxy package.
	 */
	@Deprecated
	@Internal
	@Matlab
	public BlockScheduleEntry(FactorGraph graph, IBlockUpdater blockUpdater, Object ... nodeList)
	{
		this(blockUpdater, graph.addVariableBlock(Arrays.copyOf(nodeList, nodeList.length, Variable[].class)));
	}

	/*------------------------
	 * IScheduleEntry methods
	 */
	
	@Override
	public BlockScheduleEntry copy(Map<Object,Object> old2newObjs, boolean copyToRoot)
	{
		return new BlockScheduleEntry(_blockUpdater.create(), (VariableBlock)old2newObjs.get(_block));
	}
	
	@Override
	public Type type()
	{
		return IScheduleEntry.Type.VARIABLE_BLOCK;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @returns parent of {@link #getBlock() variable block}, which is not necessarily the same as the
	 * parent of the variables contained in the block.
	 */
	@Override
	public @Nullable FactorGraph getParentGraph()
	{
		return _block.getParentGraph();
	}
	
	@Override
	public Iterable<? extends INode> getNodes()
	{
		return _block;
	}
	
	@Deprecated
	public void update()
	{
		_blockUpdater.update();
	}

	/*----------------------------
	 * BlockScheduleEntry methods
	 */
	
	public IBlockUpdater getBlockUpdater()
	{
		return _blockUpdater;
	}
	
	/**
	 * Variable block for entry.
	 * @since 0.08
	 */
	public VariableBlock getBlock()
	{
		return _block;
	}
	
	public INode[] getNodeList()
	{
		return _blockUpdater.getNodeList();
	}
	
	/**
	 * Returns {@link NodeScheduleEntry}s for each variable in the block.
	 * @since 0.08
	 */
	public List<NodeScheduleEntry> toNodeEntries()
	{
		ArrayList<NodeScheduleEntry> entries = new ArrayList<>(_block.size());
		for (Variable var : _block)
		{
			entries.add(new NodeScheduleEntry(var));
		}
		return entries;
	}
}
