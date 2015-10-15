/*******************************************************************************
*   Copyright 2012-2015 Analog Devices, Inc.
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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;

/**
 * As of release 0.08 all functionality moved to {@link DimpleEnvironment}
 */
@Deprecated
public class Model
{
	private Model()
	{
	}

	private static class ModelerHolder
	{
		static final Model INSTANCE = new Model();
	}

	@Deprecated
	public static Model getInstance()
	{
		return ModelerHolder.INSTANCE;
	}

	/**
	 * Restores system default solver factory on {@link DimpleEnvironment#active()}
	 * @deprecated as of release 0.08 use {@link DimpleEnvironment#restoreSystemDefaultSolver()} instead.
	 */
	@Deprecated
	public void restoreDefaultDefaultGraphFactory()
	{
		DimpleEnvironment.active().restoreSystemDefaultSolver();
	}
	
	/**
	 * Returns default solver factory from {@link DimpleEnvironment#active()}.
	 * @deprecated as of release 0.08 use {@link DimpleEnvironment#defaultSolver()} instead.
	 */
	@Deprecated
	public @Nullable IFactorGraphFactory<?> getDefaultGraphFactory()
	{
		return DimpleEnvironment.active().defaultSolver();
	}

	/**
	 * Sets default solver factory for {@link DimpleEnvironment#active()}
	 * @deprecated as of release 0.08
	 */
	@Deprecated
	public void setDefaultGraphFactory(@Nullable IFactorGraphFactory<?> graphFactory)
	{
		DimpleEnvironment.active().setDefaultSolver(graphFactory);
	}
	
	/**
	 * @deprecated as of release 0.08 use {@link DimpleEnvironment#getVersion()} instead
	 */
	@Deprecated
	public static String getVersion()
	{
		return DimpleEnvironment.getVersion();
	}
}
