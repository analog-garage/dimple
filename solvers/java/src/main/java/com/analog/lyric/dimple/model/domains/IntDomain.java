package com.analog.lyric.dimple.model.domains;

import net.jcip.annotations.Immutable;

/**
 * Non-discrete integral domain.
 * <p>
 * NOTE: currently there is no random variable type that corresponds to this domain.
 * 
 * @since 0.05
 */
@Immutable
public class IntDomain extends Domain
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private static final IntDomain UNBOUNDED = new IntDomain();
	
	// Currently no support for bounds, since you can usually use IntRangeDomain for that.
	
	/*--------------
	 * Construction
	 */
	
	private IntDomain()
	{
		super(14237);
	}
	
	public static IntDomain unbounded()
	{
		return UNBOUNDED;
	}
	
	/*----------------
	 * Domain methods
	 */
	
	@Override
	public boolean isIntegral()
	{
		return true;
	}
	
	@Override
	public boolean inDomain(Object value)
	{
		if (value instanceof Number)
		{
			Number number = (Number)value;
			return number.intValue() == number.doubleValue();
		}
		
		return false;
	}
}
