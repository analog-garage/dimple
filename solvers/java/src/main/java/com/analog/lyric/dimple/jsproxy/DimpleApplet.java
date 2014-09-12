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
		// TODO: figure out how to get version programatically. When I tried to do this
		// the regular way, the sandbox security prevented access to the VERSION resource file.
		return "DimpleApplet (v0.07). Copyright 2014 Analog Devices Inc.";
	}
	
	@Override
	public void stop()
	{
		_proxyCache.cleanUp();
	}
	
	/*---------------
	 * Local methods
	 */
	
	public JSFactorGraph createGraph()
	{
		return new JSFactorGraph(this, new FactorGraph());
	}

	/*-----------
	 * Factories
	 */
	
	public final JSDomainFactory domains = new JSDomainFactory(this, this._proxyCache);
	
	public final JSFactorFunctionFactory functions = new JSFactorFunctionFactory(this);
	
	public final JSSolverFactory solvers = new JSSolverFactory();
}
