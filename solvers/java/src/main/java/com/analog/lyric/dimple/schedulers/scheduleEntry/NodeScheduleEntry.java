package com.analog.lyric.dimple.schedulers.scheduleEntry;

import java.util.ArrayList;
import java.util.HashMap;

import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;



/**
 * @author jeffb
 * 
 *         A schedule entry that contains an entire node. The update method
 *         updates the entire node (all edges).
 */
public class NodeScheduleEntry implements IScheduleEntry
{
	private INode _node;
	
	public NodeScheduleEntry(INode node)
	{
		_node = node;
	}
	
	public void update() 
	{
		_node.update();
	}
	
	public INode getNode()
	{
		return _node;
	}
	
	public IScheduleEntry copy(HashMap<Object,Object> old2newObjs) 
	{
		return copy(old2newObjs, false);
	}
	public IScheduleEntry copyToRoot(HashMap<Object,Object> old2newObjs) 
	{
		return copy(old2newObjs, true);
	}
	
	public IScheduleEntry copy(HashMap<Object,Object> old2newObjs, boolean copyToRoot)
	{
		boolean skip = false;
		boolean isBoundaryVariable = false;
		
		
		if (this.getNode() instanceof VariableBase)
		{
			isBoundaryVariable = this.getNode().getParentGraph().getBoundaryVariables().contains((VariableBase)this.getNode());
			
			if(copyToRoot)
			{
				skip = isBoundaryVariable && 
				   this.getNode().getParentGraph().hasParentGraph();
			}
			else
			{
				skip = isBoundaryVariable;
			}
		}
		
		if (!skip)
		{
			INode newNode = (INode)old2newObjs.get(this.getNode());
			return new NodeScheduleEntry(newNode);
		
		}
		return null;
	}

	public Iterable<Port> getPorts() 
	{
		ArrayList<Port> ports = new ArrayList<Port>();
		
		//Add each port of this node to the list.
		for (Port p : this.getNode().getPorts())
		{
			ports.add(p);
		}
		return ports;
	}

	public String toString()
	{
		return String.format("IScheduleEntry [%s]"
				,getNode().getName());
	}
}
