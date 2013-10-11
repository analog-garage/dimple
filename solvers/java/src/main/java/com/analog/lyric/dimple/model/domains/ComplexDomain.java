package com.analog.lyric.dimple.model.domains;

import java.util.Arrays;

import com.analog.lyric.dimple.exceptions.DimpleException;

public class ComplexDomain extends RealJointDomain
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	ComplexDomain(RealDomain[] domains)
	{
		super(domains);
		
		if (domains.length != 2)
			throw new DimpleException("Complex domain must have exactly two components");
	}
	
	public static ComplexDomain create(RealDomain... domains)
	{
		return new ComplexDomain(domains.clone());
	}
	
	public static ComplexDomain create()
	{
		RealDomain[] domains = new RealDomain[2];
		Arrays.fill(domains, RealDomain.unbounded());
		return create(domains);
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


}
