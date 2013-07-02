package com.analog.lyric.dimple.parameters;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class SharedParameterValue extends ParameterValue
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
	}
	
	public SharedParameterValue(SharedParameterValue that)
	{
		super(that);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public SharedParameterValue clone()
	{
		return new SharedParameterValue(this);
	}
	
	@Override
	public SharedParameterValue cloneOrShare()
	{
		return this;
	}
	
	/*-------------------------
	 * ParameterValue methods
	 */
	
	@Override
	public SharedParameterValue asSharedValue()
	{
		return this;
	}
	
	@Override
	public final boolean isShared()
	{
		return true;
	}
	
	@Override
	public SharedParameterValue toShared()
	{
		return this;
	}
	
	@Override
	public ParameterValue toUnshared()
	{
		return new ParameterValue(this.get());
	}
}


