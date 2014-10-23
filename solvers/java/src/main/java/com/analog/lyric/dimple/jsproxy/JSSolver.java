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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;

/**
 * Javascript API representation for a Dimple solver factory.
 * <p>
 * Used to configure solver on a {@link JSFactorGraph}. Obtain new instances using
 * {@link DimpleApplet#solvers} factory.
 * <p>
 * This wraps an underlying Dimple {@link IFactorGraphFactory} object.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class JSSolver extends JSProxyObjectWithApplet<IFactorGraphFactory<?>>
{
	/*--------------
	 * Construction
	 */
	
	JSSolver(@Nullable DimpleApplet applet, IFactorGraphFactory<?> solver)
	{
		super(applet, solver);
	}
	
	JSSolver(DimpleApplet applet, String solverName)
	{
		this(applet, environmentForApplet(applet).solvers().instantiate(solverName));
	}
	
	private static DimpleEnvironment environmentForApplet(@Nullable DimpleApplet applet)
	{
		return applet != null ? applet.getEnvironment().getDelegate() : DimpleEnvironment.active();
	}
}
