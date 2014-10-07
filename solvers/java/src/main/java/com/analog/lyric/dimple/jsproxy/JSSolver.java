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

import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class JSSolver extends JSProxyObject<IFactorGraphFactory<?>>
{
	/*-------
	 * State
	 */
	
	private final DimpleApplet _applet;
	
	/*--------------
	 * Construction
	 */
	
	JSSolver(DimpleApplet applet, IFactorGraphFactory<?> solver)
	{
		super(solver);
		_applet = applet;
	}
	
	JSSolver(DimpleApplet applet, String solverName)
	{
		this(applet, applet.getEnvironment().getDelegate().solvers().instantiate(solverName));
	}

	/*-----------------------
	 * JSProxyObject methods
	 */
	
	@Override
	public DimpleApplet getApplet()
	{
		return _applet;
	}
}
