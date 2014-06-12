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

package com.analog.lyric.dimple.schedulers;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.util.misc.IMapList;

/**
 * @author jeffb
 * 
 *         If this graph is a tree, or any of it's sub-graphs are trees, this
 *         class generates a tree-schedule. Otherwise it generates uses a
 *         scheduler to be defined in a sub-class.
 * 
 *         This scheduler respects any schedulers already assigned to
 *         sub-graphs. That is, if a sub-graph already has a scheduler
 *         associated with it, that scheduler will be used for that sub-graph
 *         instead of this one.
 */
public abstract class TreeSchedulerAbstract implements IScheduler
{
	protected int _nodeUpdateThreshold = 1;		// Will use node-update if number of edges to update is greater than threshold, otherwise will use edge update
	
	
	@Override
	public ISchedule createSchedule(FactorGraph g)
	{
		if (g.isForest())		// The graph is a tree
			return createTreeSchedule(g);
		else				// Not a tree
			return createNonTreeSchedule(g);
	}
	
	
	// To be overridden to specify the desired non-tree scheduler.
	// Note that sub-graphs in the non-tree schedule should be scheduled using
	// the tree-scheduler sub-class.
	protected abstract ISchedule createNonTreeSchedule(FactorGraph g) ;
	
	
	@SuppressWarnings("unchecked")
	protected ISchedule createTreeSchedule(FactorGraph g)
	{
		FixedSchedule schedule = new FixedSchedule();
		

		HashMap<Integer,NodeUpdateState> updateState = new HashMap<Integer,NodeUpdateState>();
		
		@SuppressWarnings("all")
		IMapList allIncludedNodes = g.getNodes();
		ArrayList<INode> startingNodes = new ArrayList<INode>();

		// For all nodes, set up the node update state
		// Edges connected to nodes outside the graph have already been updated
		if (g.hasParentGraph())
		{
			for (INode node : (IMapList<INode>)allIncludedNodes)
			{
				List<? extends INode> siblings = node.getSiblings();
				int numSiblings = siblings.size();
				NodeUpdateState nodeState = new NodeUpdateState(numSiblings);
				int numSiblingsInSubGraph = 0;
				for (int index = 0; index < numSiblings; index++)
					if (!allIncludedNodes.contains(siblings.get(index)))
						nodeState.inputUpdated(index);
					else
						numSiblingsInSubGraph++;
				updateState.put(node.getId(), nodeState);
				if (numSiblingsInSubGraph <= 1)
					startingNodes.add(node);
			}
		}
		else	// If there's no parent, then nothing has already been updated
		{
			for (INode node : (IMapList<INode>)allIncludedNodes)
			{
				int numSiblings = node.getSiblingCount();
				updateState.put(node.getId(), new NodeUpdateState(numSiblings));
				if (numSiblings <= 1)
					startingNodes.add(node);
			}
		}

		
		// Start with leaf nodes
		int numStartingNodes = startingNodes.size();
		for (int i = 0; i < numStartingNodes; i++)
		{
			INode node = startingNodes.get(i);

			boolean moreInThisPath = true;
			while (moreInThisPath)
			{
				NodeUpdateState nodeState = updateState.get(requireNonNull(node).getId());
				INode nextNode = null;
				if (!nodeState.doneUpdatingAllOutputs() && nodeState.readyToUpdateAllOutputs())
				{
					// Update all output edges that have not already been updated
					if (nodeState.getNumOutputPortsNotUpdated() > _nodeUpdateThreshold)
					{
						// Use node update
						schedule.add(new NodeScheduleEntry(node));
						int nextNodeCount = 0;
						List<? extends INode> siblings = node.getSiblings();
						int numSiblings = siblings.size();
						for (int index = 0; index < numSiblings; index++)
						{
							if (!nodeState.isOutputUpdated(index))
							{
								nodeState.outputUpdated(index);
								INode sibling = siblings.get(index);
								NodeUpdateState siblingNodeState = updateState.get(sibling.getId());
								if (siblingNodeState != null)
								{
									siblingNodeState.inputUpdated(sibling.getPortNum(node));
									if (nextNodeCount++ == 0)
										nextNode = sibling;			// Do the first one next
									else
									{
										startingNodes.add(sibling);	// Will need to come back and revisit these paths
										numStartingNodes++;
									}
								}
							}
						}
						if (nextNodeCount == 0)
							moreInThisPath = false;	// No variables that aren't already done or boundary variables
					}
					else
					{
						// Use edge update
						int nextNodeCount = 0;
						List<? extends INode> siblings = node.getSiblings();
						int numSiblings = siblings.size();
						for (int index = 0; index < numSiblings; index++)
						{
							if (!nodeState.isOutputUpdated(index))
							{
								schedule.add(new EdgeScheduleEntry(node, index));
								nodeState.outputUpdated(index);
								INode sibling = siblings.get(index);
								NodeUpdateState siblingNodeState = updateState.get(sibling.getId());
								if (siblingNodeState != null)
								{
									siblingNodeState.inputUpdated(sibling.getPortNum(node));
									if (nextNodeCount++ == 0)
										nextNode = sibling;			// Do the first one next
									else
									{
										startingNodes.add(sibling);	// Will need to come back and revisit these paths
										numStartingNodes++;
									}
								}
							}
						}
						if (nextNodeCount == 0)
							moreInThisPath = false;	// No variables that aren't already done or boundary variables
					}
				}
				else if (!nodeState.doneUpdatingSingleOutput() && nodeState.readyToUpdateSingleOutput())
				{
					// We're ready to update one output, so update that output
					int portId = nodeState.outputToUpdate();
					schedule.add(new EdgeScheduleEntry(node, portId));
					nodeState.outputUpdated(portId);
					INode sibling = node.getSibling(portId);
					NodeUpdateState siblingNodeState = updateState.get(sibling.getId());
					if (siblingNodeState != null)
					{
						siblingNodeState.inputUpdated(sibling.getPortNum(node));
						nextNode = sibling;
					}
					else	// No node state, must be a boundary variable
						moreInThisPath = false;
				}
				else	// Next node isn't ready to update
					moreInThisPath = false;
				
				node = nextNode;
			}
		}

		return schedule;
	}
	
	
	// Set/get the threshold determining when to use node vs. edge updates when a node is ready to update all its remaining output edges
	public void setNodeUpdateThreshold(int threshold) {_nodeUpdateThreshold = threshold;}
	public int getNodeUpdateThreshold() {return _nodeUpdateThreshold;}
	public void useOnlyEdgeUpdates() {_nodeUpdateThreshold = Integer.MAX_VALUE;}
	public void useDefaultUpdateRule() {_nodeUpdateThreshold = 1;}




	protected class NodeUpdateState
	{
		protected int _portCount = 0;
		protected int _inputUpdateCount = 0;
		protected int _outputUpdateCount = 0;
		protected boolean[] _inputUpdated;
		protected boolean[] _outputUpdated;
		protected int _outputToUpdate = -1;
		protected boolean _readyToUpdateSingleOutput = false;
		protected boolean _doneUpdatingSingleOutput = false;
		protected boolean _readyToUpdateAllOutputs = false;
		protected boolean _doneUpdatingAllOutputs = false;
		

		public NodeUpdateState(int portCount)
		{
			_portCount = portCount;
			_inputUpdated = new boolean[portCount];		// Note: assumes ports are indexed sequentially from 0
			_outputUpdated = new boolean[portCount];
			for (int i = 0; i < portCount; i++)
			{
				_inputUpdated[i] = false;
				_outputUpdated[i] = false;
			}
			if (portCount == 1)
			{
				_readyToUpdateSingleOutput = true;
				_outputToUpdate = 0;
			}
		}
		
		public final void inputUpdated(int portId)
		{
			if (!_inputUpdated[portId])		// Make sure it hasn't already been marked as updated
			{
				_inputUpdated[portId] = true;
				_inputUpdateCount++;

				if (_inputUpdateCount == _portCount)
				{
					// If all ports have been updated, then we can update any of the outputs
					_readyToUpdateAllOutputs = true;
				}
				else if (_inputUpdateCount == _portCount - 1)
				{
					// If all ports but one have been updated, we're ready to update the
					// output on the port that hasn't yet been updated
					_readyToUpdateSingleOutput = true;
					for (int i = 0; i < _portCount; i++)
					{
						if (!_inputUpdated[i])
						{
							_outputToUpdate = i;
							break;
						}
					}
				}
			}
		}
		
		public final void outputUpdated(int portId)
		{
			if (!_outputUpdated[portId])		// Make sure it hasn't already been marked as updated
			{
				_outputUpdated[portId] = true;
				_outputUpdateCount++;

				_doneUpdatingSingleOutput = true;
				if (_outputUpdateCount == _portCount) _doneUpdatingAllOutputs = true;
			}
		}
		
		public final int outputToUpdate() {return _outputToUpdate;}
		public final boolean readyToUpdateSingleOutput() {return _readyToUpdateSingleOutput;}
		public final boolean doneUpdatingSingleOutput() {return _doneUpdatingSingleOutput;}
		public final boolean readyToUpdateAllOutputs() {return _readyToUpdateAllOutputs;}
		public final boolean doneUpdatingAllOutputs() {return _doneUpdatingAllOutputs;}
		
		public final boolean isInputUpdated(int portId) {return _inputUpdated[portId];}
		public final boolean isOutputUpdated(int portId) {return _outputUpdated[portId];}
		
		public final int getPortCount() {return _portCount;}
		public final int getInputUpdateCount() {return _inputUpdateCount;}
		public final int getOutputUpdateCount() {return _outputUpdateCount;}
		public final int getNumInputPortsNotUpdated() {return _portCount - _inputUpdateCount;}
		public final int getNumOutputPortsNotUpdated() {return _portCount - _outputUpdateCount;}
	}

}
