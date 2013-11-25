package com.analog.lyric.dimple.model.domains;


public class ComplexDomain extends RealJointDomain
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	ComplexDomain(RealDomain realDomain, RealDomain imaginaryDomain)
	{
		super(new RealDomain[] { realDomain, imaginaryDomain }, false);
	}
	
	ComplexDomain(RealDomain domain)
	{
		super(domain, 2);
	}
	
	public static ComplexDomain create(RealDomain realDomain, RealDomain imaginaryDomain)
	{
		return new ComplexDomain(realDomain, imaginaryDomain);
	}
	
	public static ComplexDomain create(RealDomain domain)
	{
		return new ComplexDomain(domain);
	}
	
	public static ComplexDomain create()
	{
		return create(RealDomain.unbounded());
	}
	
	/*----------------
	 * Domain methods
	 */

	@Override
	public final ComplexDomain asComplex()
	{
		return this;
	}

	@Override
	public final boolean isComplex()
	{
		return true;
	}

	/*-----------------
	 * Complex methods
	 */
	
	public final RealDomain realDomain()
	{
		return getRealDomain(0);
	}
	
	public final RealDomain imaginaryDomain()
	{
		return getRealDomain(1);
	}
}
