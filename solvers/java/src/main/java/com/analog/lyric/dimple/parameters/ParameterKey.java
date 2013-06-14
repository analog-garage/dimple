package com.analog.lyric.dimple.parameters;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.RealDomain;
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
 *         new ParameterKey(0, Parameter2.class, "mean");
 *     public static final ParameterKey precision =
 *         new ParameterKey(1, Parameter2.class, "precision", 1.0, RealDomain.nonNegative());
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
		this(ordinal, declaringClass, name, defaultValue, RealDomain.full());
	}

	public ParameterKey(int ordinal, Class<?> declaringClass, String name)
	{
		this(ordinal, declaringClass, name, 0.0, RealDomain.full());
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
