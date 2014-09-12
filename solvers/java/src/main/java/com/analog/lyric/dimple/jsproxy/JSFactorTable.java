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

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class JSFactorTable extends JSFactorFunction
{

	/**
	 * @param function
	 * @since 0.07
	 */
	JSFactorTable(JSFactorFunctionFactory factory, TableFactorFunction function)
	{
		super(factory, function);
	}

	/*-----------------------
	 * JSProxyObject methods
	 */
	
	@Override
	public TableFactorFunction getDelegate()
	{
		return (TableFactorFunction)_delegate;
	}
	
	/*--------------------------
	 * JSFactorFunction methods
	 */
	
	@Override
	public boolean isFactorTable()
	{
		return true;
	}
	
	/*-----------------------
	 * JSFactorTable methods
	 */
	
	public int getDimensions()
	{
		return table().getDimensions();
	}
	
	public JSDiscreteDomain[] getDomains()
	{
		JSDomainFactory factory = getApplet().domains;
		final JointDomainIndexer domains = table().getDomainIndexer();
		final int size = domains.size();
		final JSDiscreteDomain[] jsdomains = new JSDiscreteDomain[size];
		for (int i = size; --i>=0;)
		{
			jsdomains[i] = factory.wrap(domains.get(i));
		}
		return jsdomains;
	}
	
	/*------------------
	 * Internal methods
	 */
	
	public IFactorTable table()
	{
		return getDelegate().getFactorTable();
	}
}
