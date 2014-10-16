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

import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;

/**
 * Javascript API representation for a table-based discrete factor.
 * @since 0.07
 * @author Christopher Barber
 */
public class JSTableFactorFunction extends JSFactorFunction
{
	private final JSFactorTable _table;
	
	/*--------------
	 * Construction
	 */
	
	private JSTableFactorFunction(JSFactorFunctionFactory factory, TableFactorFunction function, JSFactorTable table)
	{
		super(factory, function);
		_table = table;
	}
	
	JSTableFactorFunction(JSFactorFunctionFactory factory, TableFactorFunction function)
	{
		super(factory, function);
		_table = new JSFactorTable(factory.getApplet(), function.getFactorTable());
	}

	JSTableFactorFunction(JSFactorFunctionFactory factory, JSFactorTable table)
	{
		super(factory, new TableFactorFunction("table", table.getDelegate()));
		_table = table;
	}
	
	/*--------------------------
	 * JSFactorFunction methods
	 */
	
	@Override
	public boolean isTableFactor()
	{
		return true;
	}
	
	/*-------------------------------
	 * JSTableFactorFunction methods
	 */
	
	/**
	 * Returns the underlying factor table.
	 * @since 0.07
	 */
	public JSFactorTable getTable()
	{
		return _table;
	}
}
