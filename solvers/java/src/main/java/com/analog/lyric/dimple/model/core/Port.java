/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model.core;

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class Port
{
	public INode node;
	public int index;
	public Port(INode node, int index)
	{
		this.node = node;
		this.index = index;
	}
	
	public static Port createFactorPort(FactorGraphEdgeState edge, FactorGraph fg)
	{
		return new Port(edge.getFactor(fg), edge._factorToVariableIndex);
	}
	
	public static Port createVariablePort(FactorGraphEdgeState edge, FactorGraph fg)
	{
		return new Port(edge.getVariable(fg), edge._variableToFactorIndex);
	}
	
	public static Port createPortFromNode(FactorGraphEdgeState edge, Node node)
	{
		return new Port(node, node.isVariable() ? edge.getVariableToFactorIndex() : edge.getFactorToVariableIndex());
	}
	
	@Override
	public int hashCode()
	{
		return node.hashCode()+index;
	}
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (obj instanceof Port)
		{
			Port p = (Port)obj;
			return p.node == this.node && p.index == this.index;
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return node.toString() + " index: " + index;
	}
	
	public INode getConnectedNode()
	{
		return node.getSibling(index);
	}

	public void setInputMsgValues(Object obj)
	{
		requireNonNull(node.getSolver()).setInputMsgValues(index, obj);
	}

	public void setOutputMsgValues(Object obj)
	{
		requireNonNull(node.getSolver()).setOutputMsgValues(index,obj);
	}
	
	public @Nullable Object getInputMsg()
	{
		final ISolverNode snode = node.getSolver();
		return snode != null ? snode.getInputMsg(index) : null;
	}
	public @Nullable Object getOutputMsg()
	{
		final ISolverNode snode = node.getSolver();
		return snode != null ? snode.getOutputMsg(index) : null;
	}
	public INode getSibling()
	{
		return node.getSibling(index);
	}
	public Port getSiblingPort()
	{
		return new Port(getSibling(),node.getSiblingPortIndex(index));
	}
	
	public FactorGraphEdgeState toEdgeState()
	{
		return node.getSiblingEdgeState(index);
	}
}
