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

package com.analog.lyric.dimple.options;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.solvers.core.proposalKernels.CircularNormalProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeOptions;
import com.analog.lyric.dimple.solvers.lp.LPOptions;
import com.analog.lyric.dimple.solvers.minsum.MinSumOptions;
import com.analog.lyric.dimple.solvers.particleBP.ParticleBPOptions;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductOptions;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.OptionRegistry;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Registry of option keys for known dimple options.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public enum DimpleOptionRegistry
{
	INSTANCE;
	
	private final OptionRegistry _registry = new OptionRegistry();
	
	private DimpleOptionRegistry()
	{
		addFromClasses(
			DimpleOptions.class,
			SolverOptions.class,
		
			// Solver option classes
			GibbsOptions.class,
			JunctionTreeOptions.class,
			LPOptions.class,
			MinSumOptions.class,
			ParticleBPOptions.class,
			SumProductOptions.class,
		
			// Proposal kernels
			NormalProposalKernel.class,
			CircularNormalProposalKernel.class
			);
	}
	
	private void addFromClasses(Class<?> ... classes)
	{
		for (Class<?> c : classes)
		{
			_registry.addFromClass(c);
		}
	}
	
	/**
	 * Underlying option registry.
	 * @since 0.07
	 */
	public static OptionRegistry getRegistry()
	{
		return INSTANCE._registry;
	}
	
	/**
	 * Returns key for given qualified name or null.
	 * <p>
	 * @see OptionRegistry#get(String)
	 * @param name is a non-null string containing either the fully qualified name of the option
	 * or the name qualified with just the option class name (e.g. "SolverOptions.iterations").
	 * @return key for name or null if not found.
	 * @since 0.07
	 */
	public static @Nullable IOptionKey<?> getKey(String name)
	{
		return INSTANCE._registry.get(name);
	}
	
	/**
	 * Returns key for given qualified name or throws an error.
	 * <p>
	 * @param keyOrName either a {@link IOptionKey} instance which will simply be returned or
	 * a {@link String} compatible with {@link #getKey(String)}.
	 * @throws DimpleException if key not found or input argument is not the correct type.
	 * @throws IllegalArgumentException if {@code keyOrName} does not have the correct type.
	 * @since 0.07
	 */
	public static IOptionKey<?> asKey(Object keyOrName)
	{
		IOptionKey<?> key;
		
		if (keyOrName instanceof String)
		{
			String name = (String)keyOrName;
			key = getKey(name);
			if (key == null)
			{
				throw new DimpleException("Unknown option key '%s'", name);
			}
			else
			{
				return key;
			}
		}
		else if (keyOrName instanceof IOptionKey)
		{
			key = (IOptionKey<?>)keyOrName;
		}
		else
		{
			throw new IllegalArgumentException(
				String.format("Expected String or IOptionKey instead of '%s'", keyOrName.getClass()));
		}
		
		return key;
	}
}
