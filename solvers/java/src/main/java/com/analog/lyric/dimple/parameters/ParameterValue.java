package com.analog.lyric.dimple.parameters;


import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * Simple holder for a {@link IParameterKey}/value pair.
 */
@Immutable
public final class ParameterValue<Key extends IParameterKey> implements Serializable
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final Key _key;
	private final int _index;
	private final double _value;
	private final boolean _fixed;
	
	/*--------------
	 * Construction
	 */

	/**
	 * Construct with given attributes.
	 */
	public ParameterValue(Key key, int index, double value, boolean fixed)
	{
		_key = key;
		_index = index;
		_value = value;
		_fixed = fixed;
	}
	
	public ParameterValue<Key> fixedCopy()
	{
		return fixed() ? this : new ParameterValue<Key>(_key, _index, _value, true);
	}
	
	public ParameterValue<Key> unfixedCopy()
	{
		return fixed() ? new ParameterValue<Key>(_key, _index, _value, false) : this;
	}

	/**
	 * Returns a {@link #fixed} parameter value with given {@link #key} and {@link #value}.
	 * The {@link #index} will be set to the value of {@link IParameterKey#ordinal()} of {@code key}.
	 * <p>
	 * @see #unfixed(IParameterKey, double)
	 */
	public static <Key extends IParameterKey> ParameterValue<Key> fixed(Key key, double value)
	{
		return new ParameterValue<Key>(key, key.ordinal(), value, true);
	}
	
	/**
	 * Returns a non-{@link #fixed} parameter value with null {@link #key} and given
	 * {@link #index} and {@link #value}.
	 * <p>
	 * @see #unfixed(IParameterKey, double)
	 * @see #fixed(int, double)
	 * @see #missing(int)
	 */
	public static <Key extends IParameterKey> ParameterValue<Key> fixed(int index, double value)
	{
		return new ParameterValue<Key>(null, index, value, false);
	}

	/**
	 * Returns a non-{@link #fixed} parameter value with given {@link #key} and {@link #value}.
	 * The {@link #index} will be set to the value of {@link IParameterKey#ordinal()} of {@code key}.
	 * <p>
	 * @see #unfixed(IParameterKey)
	 * @see #fixed(IParameterKey, double)
	 * @see #missing(IParameterKey)
	 */
	public static <Key extends IParameterKey> ParameterValue<Key> unfixed(Key key, double value)
	{
		return new ParameterValue<Key>(key, key.ordinal(), value, false);
	}

	/**
	 * Returns a non-{@link #fixed} parameter value with null {@link #key} and given
	 * {@link #index} and {@link #value}.
	 * <p>
	 * @see #unfixed(IParameterKey, double)
	 * @see #fixed(int, double)
	 * @see #missing(int)
	 */
	public static <Key extends IParameterKey> ParameterValue<Key> unfixed(int index, double value)
	{
		return new ParameterValue<Key>(null, index, value, false);
	}

	/**
	 * Returns a non-{@link #fixed} parameter value with given {@link #key} and {@link #value} taken
	 * from {@link IParameterKey#defaultValue()} of {@code key}.
	 * <p>
	 * @see #unfixed(IParameterKey, double)
	 */
	public static <Key extends IParameterKey> ParameterValue<Key> unfixed(Key key)
	{
		return unfixed(key, key.defaultValue());
	}

	public static <Key extends IParameterKey> ParameterValue<Key> missing(Key key)
	{
		return unfixed(key, Double.NaN);
	}
	
	public static <Key extends IParameterKey> ParameterValue<Key> missing(int index)
	{
		return unfixed(index, Double.NaN);
	}
	
	/*----------
	 * Methods
	 */

	public boolean fixed()
	{
		return _fixed;
	}
	
	/**
	 * Key identifying the parameter. May be null in which case only the {@link #index()} is relevant.
	 */
	public Key key()
	{
		return _key;
	}

	/**
	 * The index identifying the parameter. Must be non-negative and less than the size of the
	 * parameter list to which it refers.
	 * <p>
	 * If {@link #key()} is non-null, this is expected to be the same as {@link IParameterKey#ordinal()}.
	 */
	public int index()
	{
		return _index;
	}
	
	/**
	 * True if parameter has missing value, i.e. {@link #value()} is a NaN.
	 */
	public boolean isMissing()
	{
		return Double.isNaN(_value);
	}
	
	public double value()
	{
		return _value;
	}
}
