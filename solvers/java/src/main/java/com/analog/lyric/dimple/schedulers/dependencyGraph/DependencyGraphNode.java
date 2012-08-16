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

package com.analog.lyric.dimple.schedulers.dependencyGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DependencyGraphNode<T>
{
	/*
	 * State
	 */
	
	protected T _object;
	protected boolean _completed = false;
	protected Set<DependencyGraphNode<T>> _dependencies;
	protected Set<DependencyGraphNode<T>> _dependents;
	
	/*
	 * Construction
	 */
	
	public DependencyGraphNode(T object)
	{
		_object = object;
	}
	
	/**
	 * @return new set to hold dependencies of this graph node. By default this will return a {@link HashSet}
	 * but it can be overridden to return a different implementation.
	 */
	protected Set<? extends DependencyGraphNode<T>> makeDependencies()
	{
		return new HashSet<DependencyGraphNode<T>>();
	}
	
	/**
	 * @return new set to hold dependent nodes of this graph node. By default this will return a {@link HashSet}
	 * but it can be overridden to return a different implementation.
	 */
	protected Set<DependencyGraphNode<T>> makeDependents()
	{
		return new HashSet<DependencyGraphNode<T>>();
	}

	/*
	 * DependencyGraphNode methods 
	 */
	
	public T getObject()
	{
		return _object;
	}
	
	/* TODO: document that these are not intended to be called directly */
	
	/**
	 * Adds dependency of this node. Note that this does not add the dependent link back to this node.
	 * 
	 * @param n is the node to be added asa new dependency of this node.
	 * @return true if dependency was added. This implementation only returns false if
	 * the dependency was already present, but subclasses could add additional cases.
	 */
	@SuppressWarnings("unchecked")
	public boolean addDependency(DependencyGraphNode<T> n)
	{
		if (this._dependencies == null)
		{
			this._dependencies = (Set<DependencyGraphNode<T>>)this.makeDependencies();
		}
		
		return _dependencies.add(n);
	}
	
	public boolean addDependent(DependencyGraphNode<T> n)
	{
		if (this._dependents == null)
		{
			this._dependents = this.makeDependents();
		}
		
		return _dependents.add(n);
	}
	
	public boolean removeDependency(DependencyGraphNode<T> n)
	{
		boolean wasRemoved = false;
		
		if (this._dependencies != null)
		{
			wasRemoved = this._dependencies.remove(n);
			if (wasRemoved && this._dependencies.size() == 0)
			{
				this._dependencies = null;
			}
		}
		
		return wasRemoved;
	}
	
	public boolean removeDependent(DependencyGraphNode<T> n)
	{
		boolean wasRemoved = false;
		
		if (this._dependents != null)
		{
			wasRemoved = _dependents.remove(n);
			if (wasRemoved && this._dependents.size() == 0)
			{
				this._dependents = null;
			}
		}
		
		return wasRemoved;
	}

	/**
	 * @return new copy of list of dependencies of this node. Modifying the returned list will
	 * not affect the actual dependencies.
	 */
	public ArrayList<? extends DependencyGraphNode<T>> getDependencies()
	{
		if (this._dependencies == null)
		{
			return new ArrayList<DependencyGraphNode<T>>();
		}
		return new ArrayList<DependencyGraphNode<T>>(this._dependencies);
	}
	
	/**
	 * @return the number of objects that will be returned by {@link #getDependencies}.
	 */
	public int getDependencyCount()
	{
		return this._dependencies == null ? 0 : this._dependencies.size();
	}
	
	/**
	 * @return new copy of list of nodes that depend on this. Modifying the returned list will
	 * not affect the actual dependents list.
	 */
	public ArrayList<? extends DependencyGraphNode<T>> getDependents()
	{
		if (this._dependents == null)
		{
			return new ArrayList<DependencyGraphNode<T>>();
		}
		return new ArrayList<DependencyGraphNode<T>>(this._dependents);
	}
	
	/**
	 * @return the number of objects that will be returned by {@link #getDependents}.
	 */
	public int getDependentsCount()
	{
		return this._dependents == null ? 0 : this._dependents.size();
	}
	
	public void markCompleted()
	{
		_completed = true;
	}
	
	public void markNotCompleted()
	{
		_completed = false;
	}

	public boolean hasCompleted()
	{
		return _completed;
	}
	
	public boolean allDependenciesMet()
	{
		if (this._dependencies != null)
		{
			for (DependencyGraphNode<T> n : _dependencies)
				if (!n.hasCompleted()) return false;
		}
		
		return true;
	}

}
