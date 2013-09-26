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

package com.analog.lyric.dimple.schedulers.dependencyGraph.crossiteration;
import java.util.ArrayList;


public class BasicDependencyGraph<ObjectType,NodeType extends DependencyGraphNode<ObjectType>> extends ArrayList<NodeType>
{
	private static final long serialVersionUID = 1L;

	protected ArrayList<NodeType> _rootList = new ArrayList<NodeType>();	// All nodes that don't depend on any other node
	protected ArrayList<NodeType> _leafList = new ArrayList<NodeType>();	// All nodes that don't have any dependent nodes

	
	public void initialize()
	{
		for (NodeType n : this)
			n.markNotCompleted();
	}

	@Override
	public boolean add(NodeType value)
	{
		if (value.getDependencyCount() == 0)
			_rootList.add(value);
		if (value.getDependentsCount() == 0)
			_leafList.add(value);
		return super.add(value);
	}

	public void addDependency(NodeType dependent, NodeType dependency)
	{
		dependent.addDependency(dependency);
		dependency.addDependent(dependent);
		_rootList.remove(dependent);					// Dependent is not a root node
		_leafList.remove(dependency);					// Dependency is not a leaf node
	}
	
	public void addDependencies(NodeType dependent, Iterable<? extends NodeType> dependencies)
	{
		for (NodeType dependency : dependencies)
			addDependency(dependent, dependency);
	}

	public void removeDependency(NodeType dependent, NodeType dependency)
	{
		dependent.removeDependency(dependency);
		dependency.removeDependent(dependent);
		if (dependent.getDependencyCount() == 0)
			_rootList.add(dependent);					// Dependent no longer has any dependencies
		if (dependency.getDependentsCount() == 0)
			_leafList.add(dependency);					// Dependency no longer has any dependents
	}

	public void removeDependencies(NodeType dependent, Iterable<? extends NodeType> dependencies)
	{
		for (NodeType dependency : dependencies)
			removeDependency(dependent, dependency);
	}

	public void insertDependency(NodeType dependent, NodeType newIntermediary, NodeType dependency)
	{
		removeDependency(dependent, dependency);
		addDependency(dependent, newIntermediary);
		addDependency(newIntermediary, dependency);
		_rootList.remove(newIntermediary);				// New intermediary is not a root node
		_leafList.remove(newIntermediary);				// New intermediary is not a leaf node
		_rootList.remove(dependent);					// Dependent is not a root node (should already be off the list, but just making sure)
		_leafList.remove(dependency);					// Dependency is not a leaf node (should already be off the list, but just making sure)
	}

	public ArrayList<NodeType> getRootList()
	{
		return _rootList;
	}
	
	public ArrayList<NodeType> getLeafList()
	{
		return _leafList;
	}

}
