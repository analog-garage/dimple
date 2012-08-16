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

import java.util.HashMap;

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.util.misc.MapList;

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
	
	
	public ISchedule createSchedule(FactorGraph g) 
	{
		if (g.isTree())		// The graph is a tree
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
		MapList allIncludedNodes = g.getNodes();

		// For all nodes, set up the node update state
		// Edges connected to nodes outside the graph have already been updated
		if (g.hasParentGraph())
		{
			for (INode node : (MapList<INode>)allIncludedNodes)
			{
				NodeUpdateState nodeState = new NodeUpdateState(node.getPorts().size());
				for (Port p : node.getPorts())
					if (!allIncludedNodes.contains(p.getConnectedNode())) nodeState.inputUpdated(p);
				updateState.put(node.getId(), nodeState);
			}
		}
		else	// If there's no parent, then nothing has already been updated
		{
			for (INode node : (MapList<INode>)allIncludedNodes)
				updateState.put(node.getId(), new NodeUpdateState(node.getPorts().size()));
		}


		// Loop until all edges have been updated
		boolean done = false;
		while (!done)
		{
			done = true;
			for (INode node : (MapList<INode>)allIncludedNodes)
			{
				NodeUpdateState nodeState = updateState.get(node.getId());
				if (!nodeState.doneUpdatingAllOutputs())
				{
					done = false;
					if (nodeState.readyToUpdateAllOutputs())
					{
						// Update all output edges that have not already been updated
						if (nodeState.getNumOutputPortsNotUpdated() > _nodeUpdateThreshold)
						{
							// Use node update
							schedule.add(new NodeScheduleEntry(node));
							for (Port p : node.getPorts())
							{
								if (!nodeState.isOutputUpdated(p))
								{
									nodeState.outputUpdated(p);
									NodeUpdateState siblingNodeState = updateState.get(p.getConnectedNode().getId());
									if (siblingNodeState != null)
										siblingNodeState.inputUpdated(p.getSibling().getId());
								}
							}
						}
						else
						{
							// Use edge update
							for (Port p : node.getPorts())
							{
								if (!nodeState.isOutputUpdated(p))
								{
									schedule.add(new EdgeScheduleEntry(node, p));
									nodeState.outputUpdated(p);
									NodeUpdateState siblingNodeState = updateState.get(p.getConnectedNode().getId());
									if (siblingNodeState != null)
										siblingNodeState.inputUpdated(p.getSibling().getId());
								}
							}
						}
					}
					else if (!nodeState.doneUpdatingSingleOutput() && nodeState.readyToUpdateSingleOutput())
					{
						// We're ready to update one output, so update that output
						int portId = nodeState.outputToUpdate();
						schedule.add(new EdgeScheduleEntry(node, portId));
						nodeState.outputUpdated(portId);
						Port p = node.getPorts().get(portId);
						NodeUpdateState siblingNodeState = updateState.get(p.getConnectedNode().getId());
						if (siblingNodeState != null)
							siblingNodeState.inputUpdated(p.getSibling().getId());
					}
				}
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
		
		public void inputUpdated(Port port) {inputUpdated(port.getId());}
		public void inputUpdated(int portId)
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
		
		public void outputUpdated(Port port) {outputUpdated(port.getId());}
		public void outputUpdated(int portId)
		{
			if (!_outputUpdated[portId])		// Make sure it hasn't already been marked as updated
			{
				_outputUpdated[portId] = true;
				_outputUpdateCount++;

				_doneUpdatingSingleOutput = true;
				if (_outputUpdateCount == _portCount) _doneUpdatingAllOutputs = true;
			}
		}
		
		public int outputToUpdate() {return _outputToUpdate;}
		public boolean readyToUpdateSingleOutput() {return _readyToUpdateSingleOutput;}
		public boolean doneUpdatingSingleOutput() {return _doneUpdatingSingleOutput;}
		public boolean readyToUpdateAllOutputs() {return _readyToUpdateAllOutputs;}
		public boolean doneUpdatingAllOutputs() {return _doneUpdatingAllOutputs;}
		
		public boolean isInputUpdated(Port port) {return _inputUpdated[port.getId()];}
		public boolean isInputUpdated(int portId) {return _inputUpdated[portId];}
		public boolean isOutputUpdated(Port port) {return _outputUpdated[port.getId()];}
		public boolean isOutputUpdated(int portId) {return _outputUpdated[portId];}
		
		public int getPortCount() {return _portCount;}
		public int getInputUpdateCount() {return _inputUpdateCount;}
		public int getOutputUpdateCount() {return _outputUpdateCount;}
		public int getNumInputPortsNotUpdated() {return _portCount - _inputUpdateCount;}
		public int getNumOutputPortsNotUpdated() {return _portCount - _outputUpdateCount;}
	}

}
