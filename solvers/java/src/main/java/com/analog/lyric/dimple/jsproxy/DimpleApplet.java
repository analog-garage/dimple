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

import java.applet.Applet;
import java.util.Calendar;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * An applet that provides a proxy for accessing Dimple from JavaScript in a web browser.
 * <p>
 * The applet simply provides an API for constructing Java API objects. It provides no
 * visual component.
 * <p>
 * This API is under active development and should be considered experimental.
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class DimpleApplet extends Applet
{
	private static final long serialVersionUID = 1L;
	
	final Cache<Object, JSProxyObject<?>> _proxyCache = CacheBuilder.newBuilder().build();

	/*----------------
	 * Applet methods
	 */
	
	@Override
	public String getAppletInfo()
	{
		// The standard way to get the version reads the VERSION resource file, but that
		// does not appear to be accessible in the applet due to sandbox security.
		String version = getClass().getPackage().getImplementationVersion();
		return String.format("DimpleApplet (v%s) Copyright %d Analog Devices Inc.",
			version != null ? version : "?",
			Calendar.getInstance().get(Calendar.YEAR));
	}
	
	@Override
	public void stop()
	{
		_proxyCache.cleanUp();
	}
	
	/*---------------
	 * Local methods
	 */
	
	/**
	 * Creates a new factor graph instance.
	 * <p>
	 * @since 0.07
	 */
	public JSFactorGraph createGraph()
	{
		return new JSFactorGraph(this, new FactorGraph());
	}

	/**
	 * Returns object representing the current Dimple environment.
	 * <p>
	 * The environment can be used to set default values for options.
	 * <p>
	 * @since 0.07
	 */
	public JSEnvironment getEnvironment()
	{
		return new JSEnvironment(this);
	}
	
	/*-----------
	 * Factories
	 */
	
	/**
	 * Instance of factory object for obtaining instances of domains.
	 * @since 0.07
	 */
	public final JSDomainFactory domains = new JSDomainFactory(this, this._proxyCache);
	
	/**
	 * Instance of factory object for obtaining instances of factor functions.
	 * @since 0.07
	 */
	public final JSFactorFunctionFactory functions = new JSFactorFunctionFactory(this);
	
	/**
	 * Instance of factory object for obtaining instances of solvers.
	 * @since 0.07
	 */
	public final JSSolverFactory solvers = new JSSolverFactory(this);
}
