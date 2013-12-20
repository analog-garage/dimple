package com.analog.lyric.dimple.model.domains;

import net.jcip.annotations.Immutable;

/**
 * Domain of all Java objects.
 * <p>
 * NOTE: currently there is no random variable type that corresponds to this domain.
 * 
 *  @since 0.05
 */
@Immutable
public final class ObjectDomain extends Domain
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private static final ObjectDomain INSTANCE = new ObjectDomain();
	
	// Currently no support for bounds, since you can usually use IntRangeDomain for that.
	
	/*--------------
	 * Construction
	 */
	
	private ObjectDomain()
	{
		super(617);
	}
	
	public static ObjectDomain instance()
	{
		return INSTANCE;
	}
	
	/*----------------
	 * Domain methods
	 */
	
	@Override
	public boolean inDomain(Object value)
	{
		return true;
	}
	
	@Override
	public boolean isNumber()
	{
		return false;
	}
	
	@Override
	public boolean isNumeric()
	{
		return false;
	}
	
	@Override
	public boolean isScalar()
	{
		return false;
	}
}
