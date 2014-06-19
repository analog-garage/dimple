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

package com.analog.lyric.dimple.parameters;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.options.OptionKey;

/**
 * A concrete implementation of {@link IParameterKey}.
 * <p>
 * To define a set of parameter keys for a {@link IParameterList} implementation, you need to:
 * <ul>
 * <li>Designate a public class to hold the key values.
 * <li>Define a public static final field of this type for each key.
 * <li>Each instance must specify the containing class as its {@code declaringClass}
 * and its own field name as its name.
 * <li>Each instance must specify a unique ordinal value in the range from zero to one
 * less than the number of keys defined in the class.
 * <li>Define an array of the keys values in ordinal order and return a clone of that
 * array in the {@link IParameterList#getKeys()} method.
 * </ul>
 * 
 * Here is an example:
 * 
 * <pre>
 * public class GuassianParameter
 * {
 *     public static final ParameterKey mean =
 *         new ParameterKey(0, GuassianParameter.class, "mean");
 *     public static final ParameterKey precision =
 *         new ParameterKey(1, GuassianParameter.class, "precision", 1.0, RealDomain.nonNegative());
 * 
 *     private static ParameterKey[] _keys = new ParameterKey[] { mean, precision };
 * 
 *     public static ParameterKey[] getKeys() { return _keys.clone(); }
 * }
 * </pre>
 */
@Immutable
public class ParameterKey extends OptionKey<Double> implements IParameterKey
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private final int _ordinal;
	private final double _defaultValue;
	private final RealDomain _domain;
	
	/*--------------
	 * Construction
	 */
	
	public ParameterKey(int ordinal, Class<?> declaringClass, String name,
		double defaultValue, RealDomain domain)
	{
		super(declaringClass, name);
		_ordinal = ordinal;
		_defaultValue = defaultValue;
		_domain = domain;
	}

	public ParameterKey(int ordinal, Class<?> declaringClass, String name, double defaultValue)
	{
		this(ordinal, declaringClass, name, defaultValue, RealDomain.unbounded());
	}

	public ParameterKey(int ordinal, Class<?> declaringClass, String name)
	{
		this(ordinal, declaringClass, name, 0.0, RealDomain.unbounded());
	}
	
	/*-----------------------
	 * IParameterKey methods
	 */

	@Override
	public final int ordinal()
	{
		return _ordinal;
	}

	@Override
	public Class<Double> type()
	{
		return Double.class;
	}

	@Override
	public Double defaultValue()
	{
		return _defaultValue;
	}

	@Override
	public RealDomain domain()
	{
		return _domain;
	}
}
