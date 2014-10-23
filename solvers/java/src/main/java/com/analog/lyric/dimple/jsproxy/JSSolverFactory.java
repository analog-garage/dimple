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
	
	package com.analog.lyric.dimple.jsproxy;

import com.analog.lyric.util.misc.Internal;

	
	
/**
 * This provides the solvers that are available for use with {@link JSFactorGraph#setSolver(Object)}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class JSSolverFactory
{
	private final DimpleApplet _applet;

	/**
	 * For tests purposes. Instead use {@link DimpleApplet#solvers}.
	 */
	@Internal
	public JSSolverFactory(DimpleApplet applet)
	{
		_applet = applet;
	}
	
	/**
	 * Returns solver instance with specified name.
	 * <p>
	 * Currently supported solvers include:
	 * <ul>
	 * <li>Gibbs
	 * <li>JunctionTree
	 * <li>JunctionTreeMAP
	 * <li>LP
	 * <li>MinSum
	 * <li>ParticleBP
	 * <li>SumProduct
	 * </ul>
	 * @since 0.07
	 */
	public JSSolver get(String name)
	{
		return new JSSolver(_applet, name);
	}
}
