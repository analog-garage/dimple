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

package com.analog.lyric.dimple.matlabproxy;


import static java.util.Objects.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.util.misc.Matlab;

@Matlab
public abstract class PNodeVector extends POptionHolder
{
	/*
	 * State
	 */
	
	private Node [] _nodes = new Node[0];
	
	/*--------------
	 * Construction
	 */
	
	public PNodeVector() {}

	public PNodeVector(Node [] nodes)
	{
		_nodes = nodes.clone();
	}

	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public Node[] getDelegate()
	{
		return _nodes;
	}
	
	@Override
	public boolean isVector()
	{
		return true;
	}
	
	/*-----------------------
	 * POptionHolder methods
	 */
	
	@Override
	public final DimpleOptionHolder getOptionHolder(int i)
	{
		return _nodes[i];
	}
	
	/*---------------------
	 * PNodeVector methods
	 */

	public void setNodes(Node [] nodes)
	{
		_nodes = nodes;
	}
	
	public PNodeVector concat(Object [] varVectors, int [] varVectorIndices, int [] varIndices)
	{
		return concat(PHelpers.convertObjectArrayToNodeVectorArray(varVectors),varVectorIndices,varIndices);
	}

	public PNodeVector concat(PNodeVector [] varVectors, int [] varVectorIndices, int [] varIndices)
	{
		Node [] nodes = new Node[varIndices.length];
		for (int i = 0; i < varIndices.length; i++)
		{
			nodes[i] = varVectors[varVectorIndices[i]]._nodes[varIndices[i]];
		}
		return createNodeVector(nodes);

	}
	
	public abstract PNodeVector createNodeVector(Node [] nodes);
	
	public PNodeVector concat(Object [] varVectors)
	{
		return concat(PHelpers.convertObjectArrayToNodeVectorArray(varVectors));
	}
	
	public PNodeVector concat(PNodeVector [] varVectors)
	{
		ArrayList<Node> variables = new ArrayList<Node>();
		for (int i = 0; i < varVectors.length; i++)
		{
			for (int j =0; j < varVectors[i].size(); j++)
			{
				variables.add(varVectors[i]._nodes[j]);
			}
		}
		Node [] retval = new Node[variables.size()];
		variables.toArray(retval);
		
		return createNodeVector(retval);
	}
	
	public void replace(PNodeVector vector, int [] indices)
	{
		for (int i = 0; i < indices.length; i++)
		{
			_nodes[indices[i]] = vector._nodes[i];
		}
	}
	
	public PNodeVector getSlice(int [] indices)
	{
		Node [] variables = new Node[indices.length];
		for (int i = 0; i < indices.length; i++)
		{
			variables[i] = _nodes[indices[i]];
		}
		return createNodeVector(variables);
	}
	
	public Node getModelerNode(int index)
	{
		return _nodes[index];
	}
	public Node[] getModelerNodes()
	{
		return _nodes;
	}
	
	@Override
	public int size()
	{
		return _nodes.length;
	}
	
	public int [] getIds()
	{
		int [] ids = new int[_nodes.length];
		for (int i = 0; i < ids.length; i++)
			ids[i] = _nodes[i].getId();
		
		return ids;
	}
	
	public double getScore()
	{
		double sum = 0;

		for (int i = 0; i < _nodes.length; i++)
			sum += _nodes[i].getScore();
		
		return sum;
	}
	
	public double getBetheEntropy()
	{
		double sum = 0;
		
		for (int i = 0; i < _nodes.length; i++)
			sum += _nodes[i].getBetheEntropy();
		
		return sum;
	}
	
	public double getInternalEnergy()
	{
		double sum = 0;
		
		for (int i = 0; i < _nodes.length; i++)
			sum += _nodes[i].getInternalEnergy();
		
		return sum;
	}
	
	public void setName(String name)
	{
		for (Node variable : _nodes)
			variable.setName(name);
	}
	
	public void setNames(String baseName)
	{
		for(int i = 0; i < _nodes.length; ++i)
		{
			_nodes[i].setName(String.format("%s_vv%d", baseName, i));
		}
	}
	
	public void setLabel(String name)
	{
		for (Node variable : _nodes)
			variable.setLabel(name);
	}
	
	public String [] getNames()
	{
		String [] retval = new String[_nodes.length];
		for (int i = 0; i < _nodes.length; i++)
		{
			retval[i] = _nodes[i].getName();
		}
		return retval;
	}
	public String [] getQualifiedNames()
	{
		String [] retval = new String[_nodes.length];
		for (int i = 0; i < _nodes.length; i++)
		{
			retval[i] = _nodes[i].getQualifiedName();
		}
		return retval;
	}
	public String [] getExplicitNames()
	{
		String [] retval = new String[_nodes.length];
		for (int i = 0; i < _nodes.length; i++)
		{
			retval[i] = _nodes[i].getExplicitName();
		}
		return retval;
	}
	public String [] getNamesForPrint()
	{
		String [] retval = new String[_nodes.length];
		for (int i = 0; i < _nodes.length; i++)
		{
			retval[i] = _nodes[i].getLabel();
		}
		return retval;
	}
	public String [] getQualifiedNamesForPrint()
	{
		String [] retval = new String[_nodes.length];
		for (int i = 0; i < _nodes.length; i++)
		{
			retval[i] = _nodes[i].getQualifiedLabel();
		}
		return retval;
	}
	public UUID [] getUUIDs()
	{
		UUID [] retval = new UUID[_nodes.length];
		for (int i = 0; i < _nodes.length; i++)
		{
			retval[i] = _nodes[i].getUUID();
		}
		return retval;
	}
	
	Port [][] getModelerPorts()
	{
		Port [][] ports = new Port[_nodes.length][];
		
		for (int i = 0; i < _nodes.length; i++)
		{
			ArrayList<Port> tmp = _nodes[i].getPorts();
			ports[i] = new Port[tmp.size()];
			tmp.toArray(ports[i]);
		}
		return ports;
	}

	public int getPortNum(PNodeVector nodeVector)
	{
		Node thisNode = PHelpers.convertToNode(this);
		Node n = PHelpers.convertToNode(nodeVector);
		
		int num = thisNode.getPortNum(n);
		return num;

	}
	
	public Port [] getPorts(int index)
	{
		Port [] ports;
		ArrayList<Port> alports = getModelerNode(index).getPorts();
		ports = new Port[alports.size()];
		alports.toArray(ports);
		return ports;
	}
	
	public void update()
	{
		for (int i = 0; i < _nodes.length; i++)
			_nodes[i].update();
	}
	
	public void updateEdge(PNodeVector nodeVector)
	{
		updateEdge(getPortNum(nodeVector));
	}
	
	public void updateEdge(int portNum)
	{
		for (int i = 0; i < _nodes.length; i++)
			_nodes[i].updateEdge(portNum);
	}
	
	public @Nullable ISolverNode getSolver(int index)
	{
		Node n = getModelerNode(index);
		return n.getSolver();
	}
	
	public ISolverNode [] getSolvers(int [] indices)
	{
		ISolverNode [] retval = new ISolverNode[indices.length];
		for (int i = 0; i < indices.length; i++)
			retval[i] = getSolver(indices[i]);
		return retval;
	}
	
	public ISolverNode [] getSolvers()
	{
		ISolverNode[] retval = new ISolverNode[_nodes.length];
		for (int i = 0; i < _nodes.length; i++)
		{
			retval[i] = _nodes[i].getSolver();
		}
		return retval;
	}
	
	public void invokeSolverMethod(String methodName,Object ... args)
	{
		ISolverNode sn = requireNonNull(_nodes[0].getSolver());
		
		Method [] ms = sn.getClass().getMethods();
		for (Method m : ms)
		{
			if (m.getName().equals(methodName))
			{
				boolean keepTrying = false;
				for (int i = 0; i < _nodes.length; i++)
				{
					try
					{
						m.invoke(_nodes[i].getSolver(),args);
					}
					catch (IllegalArgumentException e) {keepTrying = true;}	// Might be another version with different signature
					catch (Exception e)
					{
						throw new DimpleException(e);
					}
				}
				if (!keepTrying) return;
			}
		}

		
		throw new DimpleException("method not found");
	}
	
	public Object [] invokeSolverMethodWithReturnValue(String methodName,Object ... args)
	{
		ISolverNode sn = requireNonNull(_nodes[0].getSolver());
		
		Method [] ms = sn.getClass().getMethods();
		for (Method m : ms)
		{
			if (m.getName().equals(methodName))
			{
				boolean keepTrying = false;
				Object [] retval = new Object[_nodes.length];
				for (int i = 0; i < _nodes.length; i++)
				{
					try
					{
						retval[i] = m.invoke(_nodes[i].getSolver(),args);
					}
					catch (IllegalArgumentException e) {keepTrying = true;}	// Might be another version with different signature
					catch (Exception e)
					{
						throw new DimpleException(e);
					}
				}
				if (!keepTrying) return retval;
			}
		}

		
		throw new DimpleException("method not found");
	}
	
}
