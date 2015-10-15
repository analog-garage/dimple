/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.solvers.core.CustomFactors;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.util.misc.Matlab;

/**
 * MATLAB proxy wrapper for {@link CustomFactors}
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@Matlab(wrapper="CustomFactors")
public class PCustomFactors extends PObject
{
	/*-------
	 * State
	 */
	
	private final CustomFactors<?,?> _customFactors;
	
	/*--------------
	 * Construction
	 */

	PCustomFactors(CustomFactors<?,?> customFactors)
	{
		_customFactors = customFactors;
	}

	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return _customFactors.toString();
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public CustomFactors<ISolverFactor,ISolverFactorGraph> getDelegate()
	{
		return (CustomFactors<ISolverFactor, ISolverFactorGraph>) _customFactors;
	}

	/*------------------------
	 * PCustomFactors options
	 */
	
	/**
	 * Adds custom factor mapping for named factor function.
	 * @prepend specifies whether to insert mapping ahead of others for the same function
	 * @param factorFunction
	 * @param customFactorClass
	 * @see CustomFactors#add(String, String)
	 * @see CustomFactors#addFirst(String, String)
	 * @since 0.08
	 */
	public void add(boolean prepend, String factorFunction, String customFactorClass)
	{
		if (prepend)
		{
			_customFactors.addFirst(factorFunction, customFactorClass);
		}
		else
		{
			_customFactors.add(factorFunction, customFactorClass);
		}
	}

	/**
	 * Adds multiple custom factor mappings.
	 * <p>
	 * @prepend specifies whether to insert mapping ahead of others for the same function
	 * @param mappings is a n-by-2 multidimensional array, where each array {@code mappings[m]}
	 * contains the factor function and custom factor class arguments for {@link #add(boolean, String, String)}.
	 * @since 0.08
	 */
	public void add(boolean prepend, String[][] mappings)
	{
		if (prepend)
		{
			for (int i = mappings.length; --i>=0;)
			{
				final String[] mapping = mappings[i];
				_customFactors.addFirst(mapping[0], mapping[1]);
			}
		}
		else
		{
			for (String[] mapping : mappings)
			{
				_customFactors.add(mapping[0], mapping[1]);
			}
		}
	}
	
	/**
	 * Adds built-in mappings.
	 * <p>
	 * @see CustomFactors#addBuiltins()
	 * @since 0.08
	 */
	public void addBuiltins()
	{
		_customFactors.addBuiltins();
	}
}
