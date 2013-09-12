package com.analog.lyric.dimple.parameters;


import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * Simple holder for a {@link IParameterKey}/value pair.
 */
@Immutable
public final class Parameter<Key extends IParameterKey> implements Serializable
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final Key _key;
	private final int _index;
	private final SharedParameterValue _value;
	private final boolean _fixed;
	private final boolean _shared;
	
	/*--------------
	 * Construction
	 */

	Parameter(Key key, int index, SharedParameterValue value, boolean fixed, boolean shared)
	{
		_key = key;
		_index = index;
		_value = value;
		_fixed = fixed;
		_shared = shared;
	}
	
	public Parameter(Key key, SharedParameterValue value, boolean fixed)
	{
		this(key, key.ordinal(), value, fixed, true);
	}
	
	public Parameter(Key key, double value, boolean fixed)
	{
		this(key, key.ordinal(), new SharedParameterValue(value), fixed, false);
	}

	public Parameter(int index, SharedParameterValue value, boolean fixed)
	{
		this(null, index, value, fixed, true);
	}
	
	public Parameter(int index, double value, boolean fixed)
	{
		this(null, index, new SharedParameterValue(value), fixed, false);
	}
	
	/**
	 * Returns a {@link #fixed} parameter value with given {@link #key} and {@link #value}.
	 * The {@link #index} will be set to the value of {@link IParameterKey#ordinal()} of {@code key}.
	 * <p>
	 * @see #unfixed(IParameterKey, double)
	 */
	public static <Key extends IParameterKey> Parameter<Key> fixed(Key key, double value)
	{
		return new Parameter<Key>(key, value, true);
	}
	
	public static <Key extends IParameterKey> Parameter<Key> fixed(Key key, SharedParameterValue value)
	{
		return new Parameter<Key>(key, value, true);
	}
	
	/**
	 * Returns a non-{@link #fixed} parameter value with null {@link #key} and given
	 * {@link #index} and {@link #value}.
	 * <p>
	 * @see #unfixed(IParameterKey, double)
	 * @see #fixed(int, double)
	 * @see #unknown(int)
	 */
	public static <Key extends IParameterKey> Parameter<Key> fixed(int index, double value)
	{
		return new Parameter<Key>(index, value, false);
	}

	public static <Key extends IParameterKey> Parameter<Key> fixed(int index, SharedParameterValue value)
	{
		return new Parameter<Key>(index, value, true);
	}
	
	/**
	 * Returns a non-{@link #fixed} parameter value with given {@link #key} and {@link #value}.
	 * The {@link #index} will be set to the value of {@link IParameterKey#ordinal()} of {@code key}.
	 * <p>
	 * @see #unfixed(IParameterKey)
	 * @see #fixed(IParameterKey, double)
	 * @see #unknown(IParameterKey)
	 */
	public static <Key extends IParameterKey> Parameter<Key> unfixed(Key key, double value)
	{
		return new Parameter<Key>(key, value, false);
	}

	/**
	 * Returns a non-{@link #fixed} parameter value with null {@link #key} and given
	 * {@link #index} and {@link #value}.
	 * <p>
	 * @see #unfixed(IParameterKey, double)
	 * @see #fixed(int, double)
	 * @see #unknown(int)
	 */
	public static <Key extends IParameterKey> Parameter<Key> unfixed(int index, double value)
	{
		return new Parameter<Key>(index, value, false);
	}

	/**
	 * Returns a non-{@link #fixed} parameter value with given {@link #key} and {@link #value} taken
	 * from {@link IParameterKey#defaultValue()} of {@code key}.
	 * <p>
	 * @see #unfixed(IParameterKey, double)
	 */
	public static <Key extends IParameterKey> Parameter<Key> unfixed(Key key)
	{
		return unfixed(key, key.defaultValue());
	}

	public static <Key extends IParameterKey> Parameter<Key> unknown(Key key)
	{
		return unfixed(key, Double.NaN);
	}
	
	public static <Key extends IParameterKey> Parameter<Key> unknown(int index)
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
	 * True if parameter has known value, i.e. {@link #value()} is not NaN.
	 */
	public boolean known()
	{
		return _value.known();
	}
	
	public double value()
	{
		return _value.get();
	}
	
	public SharedParameterValue sharedValue()
	{
		return _shared ? _value : null;
	}
}
