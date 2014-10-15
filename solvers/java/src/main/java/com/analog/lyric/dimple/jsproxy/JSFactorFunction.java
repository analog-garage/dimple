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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class JSFactorFunction extends JSProxyObject<FactorFunction>
{
	final JSFactorFunctionFactory _factory;
	
	/*--------------
	 * Construction
	 */
	
	JSFactorFunction(JSFactorFunctionFactory factory, FactorFunction function)
	{
		super(function);
		_factory = factory;
	}
	
	/*---------
	 * Methods
	 */
	
	@Override
	public DimpleApplet getApplet()
	{
		return _factory._applet;
	}

	public String getName()
	{
		return _delegate.getName();
	}
	
	public @Nullable int[] getDirectedToIndices(int numEdges)
	{
		return _delegate.getDirectedToIndices(numEdges);
	}
	
	public @Nullable Object getParameter(String name)
	{
		FactorFunction function = _delegate;
		if (function instanceof IParametricFactorFunction)
		{
			return ((IParametricFactorFunction)function).getParameter(name);
		}
		return null;
	}
	
	public boolean isDeterministicDirected()
	{
		return _delegate.isDeterministicDirected();
	}
	
	public boolean isFactorTable()
	{
		return false;
	}

	public boolean isParametric()
	{
		return _delegate.isParametric();
	}
}
