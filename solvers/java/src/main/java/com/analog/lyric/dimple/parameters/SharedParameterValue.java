package com.analog.lyric.dimple.parameters;

import net.jcip.annotations.ThreadSafe;

import com.google.common.util.concurrent.AtomicDouble;

@ThreadSafe
public class SharedParameterValue extends AtomicDouble implements Cloneable
{
	private static final long serialVersionUID = 1L;

	/*---------------
	 * Construction
	 */
	
	public SharedParameterValue(double value)
	{
		super(value);
	}
	
	public SharedParameterValue()
	{
		this(Double.NaN);
	}
	
	public SharedParameterValue(SharedParameterValue that)
	{
		this(that.get());
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public SharedParameterValue clone()
	{
		return new SharedParameterValue(get());
	}
	
	/*------------------------------
	 * SharedParameterValue methods
	 */
	
	public final boolean known()
	{
		return ! Double.isNaN(get());
	}
}
