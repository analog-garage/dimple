/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

import java.lang.reflect.Constructor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ConstructorRegistry;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolver;
import com.analog.lyric.dimple.solvers.junctiontreemap.JunctionTreeMAPSolver;
import com.analog.lyric.dimple.solvers.lp.LPSolver;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.particleBP.ParticleBPSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.util.misc.Internal;


/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class DimpleSolverRegistry extends ConstructorRegistry<IFactorGraphFactory<?>>
{
	/*--------------
	 * Construction
	 */
	
	/**
	 * For internal use. Users should instead use {@link DimpleEnvironment#solvers}.
	 * @since 0.07
	 */
	@Internal
	public DimpleSolverRegistry()
	{
		super(IFactorGraphFactory.class);
		
		addClass(GibbsSolver.class);
		addClass(JunctionTreeSolver.class);
		addClass(JunctionTreeMAPSolver.class);
		addClass(LPSolver.class);
		addClass(MinSumSolver.class);
		addClass(ParticleBPSolver.class);
		addClass(SumProductSolver.class);
	}
	
	/*
	 * 
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This extends the default behavior by appending "Solver" to the end of the name if no match
	 * is found using the original argument.
	 */
	@NonNullByDefault(false)
	@Override
	public synchronized @Nullable Constructor<IFactorGraphFactory<?>> get(Object simpleClassName)
	{
		Constructor<IFactorGraphFactory<?>> constructor = super.get(simpleClassName);
		if (constructor == null)
		{
			String name = (String)simpleClassName;
			if (!name.endsWith("Solver"))
			{
				// Try again with name appended with "Solver"
				constructor = super.get(name + "Solver");
			}
		}
		return constructor;
	}
}
