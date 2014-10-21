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

package com.analog.lyric.dimple.test.jsproxy;

import static org.junit.Assert.*;

import java.awt.HeadlessException;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.jsproxy.DimpleApplet;
import com.analog.lyric.dimple.jsproxy.JSDomainFactory;
import com.analog.lyric.dimple.jsproxy.JSEnvironment;
import com.analog.lyric.dimple.jsproxy.JSFactorFunctionFactory;
import com.analog.lyric.dimple.jsproxy.JSFactorGraph;
import com.analog.lyric.dimple.jsproxy.JSSolverFactory;
import com.analog.lyric.dimple.model.core.FactorGraph;

/**
 * Holder for tests state associated with DimpleApplet.
 * <p>
 * Because it may not be possible to create an applet when running tests in "headless" mode,
 * as in automated builds, this object will hold the applet and provide alternate construction
 * of applet apis.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
class DimpleAppletTestState
{
	/*-------
	 * State
	 */
	
	final @Nullable DimpleApplet applet;
	final JSDomainFactory domains;
	final JSFactorFunctionFactory functions;
	final JSSolverFactory solvers;
	
	/*--------------
	 * Construction
	 */
	
	@SuppressWarnings("null")
	DimpleAppletTestState()
	{
		DimpleApplet _applet = null;
		JSFactorFunctionFactory _functions;
		JSDomainFactory _domains;
		JSSolverFactory _solvers;
		
		try
		{
			_applet = new DimpleApplet();
			_domains = _applet.domains;
			_functions = _applet.functions;
			_solvers = _applet.solvers;
		}
		catch (HeadlessException ex)
		{
			_domains = new JSDomainFactory();
			_functions = new JSFactorFunctionFactory(DimpleEnvironment.active().factorFunctions(), null);
			_solvers = new JSSolverFactory(null);
		}
		
		applet = _applet;
		domains = _domains;
		functions = _functions;
		solvers = _solvers;
		
		assertEquals(applet, functions.getApplet());
	}
	
	/*----------------------
	 * DimpleApplet methods
	 */
	
	@SuppressWarnings("null")
	public JSFactorGraph createGraph()
	{
		return applet != null ? applet.createGraph() : new JSFactorGraph(null, new FactorGraph());
	}
	
	@SuppressWarnings("null")
	public JSEnvironment getEnvironment()
	{
		return applet != null ? applet.getEnvironment() : new JSEnvironment(null);
	}
}
