package com.analog.lyric.dimple.parameters;

import net.jcip.annotations.ThreadSafe;

import com.google.common.util.concurrent.AtomicDouble;

@ThreadSafe
public class ParameterValue extends AtomicDouble implements Cloneable
{
	/*--------------
	 * Construction
	 */
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParameterValue()
	{
		this(Double.NaN);
	}
	
	public ParameterValue(double value)
	{
		super(value);
	}
	
	public ParameterValue(ParameterValue that)
	{
		this(that.get());
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public ParameterValue clone()
	{
		return new ParameterValue(this);
	}
	
	/*------------------------
	 * ParameterValue methods
	 */

	/**
	 * Returns this object or null if it is not a {@link SharedParameterValue}.
	 */
	public SharedParameterValue asSharedValue()
	{
		return null;
	}
	
	/**
	 * If this object {@link #isShared()}, it is returned, otherwise an unshared
	 * copy will be returned.
	 */
	public ParameterValue cloneOrShare()
	{
		return clone();
	}

	public final boolean known()
	{
		return ! Double.isNaN(get());
	}

	public boolean isShared()
	{
		return false;
	}

	/**
	 * Returns this object if {@link #isShared()} otherwise returns
	 * a shared copy.
	 */
	public SharedParameterValue toShared()
	{
		return new SharedParameterValue(this.get());
	}

	/**
	 * Returns this object if not {@link #isShared()} otherwise returns
	 * an unshared copy.
	 */
	public ParameterValue toUnshared()
	{
		return this;
	}
}
