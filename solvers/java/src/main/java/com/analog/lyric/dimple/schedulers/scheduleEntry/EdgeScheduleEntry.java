package com.analog.lyric.dimple.schedulers.scheduleEntry;

import java.util.ArrayList;
import java.util.HashMap;

import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;



/**
 * @author jeffb
 * 
 *         A schedule entry that contains a single edge. The update method
 *         updates just one edge of the specified node.
 */
public class EdgeScheduleEntry implements IScheduleEntry
{
	private INode _node;
	private int _portNum;
	
	public EdgeScheduleEntry(INode node, int portNum)
	{
		_node = node;
		_portNum = portNum;
	}
	public EdgeScheduleEntry(INode node, Port port)
	{
		_node = node;
		_portNum = port.getId();
	}
	
	public void update() 
	{
		_node.updateEdge(_portNum);
	}
	
	public INode getNode()
	{
		return _node;
	}
	
	public int getPortNum()
	{
		return _portNum;
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
			return new EdgeScheduleEntry(newNode,this.getPortNum());
		
		}
		return null;
	}
	
	public Iterable<Port> getPorts()
	{
		//This is just an edge, add the port.
		Port p = this.getNode().getPorts().get(this.getPortNum());
		ArrayList<Port> al = new ArrayList<Port>();
		al.add(p);
		return al;
	}
	
	public String toString()
	{
		return String.format("IScheduleEntry [%s] -> %d -> [%s]"
				,getNode().getLabel()
				,getPortNum()
				,getNode().getPorts().get(getPortNum()).getConnectedNode().getLabel());
	}
}
