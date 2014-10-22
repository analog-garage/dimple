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

package com.analog.lyric.dimple.solvers.core;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;



/*
 * This class is more like a Factory than a Solver.  It simply generates
 * FactorGraphs and Variables
 */
public abstract class SolverBase<SolverGraph extends ISolverFactorGraph>
	implements IFactorGraphFactory<SolverGraph>
{
	@Override
	@NonNullByDefault(false)
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		
		final Class<? extends SolverBase<?>> superclass = equalitySuperClass();
		return superclass != null && superclass.isInstance(obj);
	}
	
	@Override
	public int hashCode()
	{
		final Class<? extends SolverBase<?>> superclass = equalitySuperClass();
		return superclass != null ? superclass.hashCode() : super.hashCode();
	}
	
	/**
	 * Subclasses can override this to return a class to be used for equality checks.
	 * <p>
	 * This is used to determine the behavior of {@link #equals} and {@link #hashCode}. If
	 * null (the default), then these methods will have their default behavior of same-object
	 * equality semantics. If this returns a non-null class, then instances of that class will
	 * 
	 * @since 0.07
	 */
	protected @Nullable Class<? extends SolverBase<?>> equalitySuperClass()
	{
		return null;
	}
}
