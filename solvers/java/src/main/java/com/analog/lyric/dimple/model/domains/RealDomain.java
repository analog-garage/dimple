/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.model.domains;

import com.analog.lyric.dimple.exceptions.DimpleException;

import net.jcip.annotations.Immutable;

@Immutable
public class RealDomain extends Domain
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final double _lowerBound;
	private final double _upperBound;
	
	/*--------------
	 * Construction
	 */
	
	RealDomain() { this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY); }

	RealDomain(double[] domain)  {this(domain[0], domain[1]);}

	public RealDomain(double lower, double upper)
	{
		super(computeHashCode(lower, upper));
		if (lower > upper) throw new DimpleException("Upper bound must be greater than lower bound");
		_lowerBound = lower;
		_upperBound = upper;
	}
	
	private static int computeHashCode(double lowerBound, double upperBound)
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lowerBound);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(upperBound);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Returns domain with given lower and upper bounds. May return a previously
	 * interned instance.
	 */
	public static RealDomain create(double lower, double upper)
	{
		for (StandardDomain standard : StandardDomain.values())
		{
			RealDomain domain = standard.domain;
			if (domain.getLowerBound() == lower && domain.getUpperBound() == upper)
			{
				return domain;
			}
		}
		
		// TODO: cache other domains as well...
		
		return new RealDomain(lower, upper);
	}
	
	/**
	 * Same as #unbounded()
	 */
	public static RealDomain create()
	{
		return StandardDomain.UNBOUNDED.domain;
	}
	
	/**
	 * Domain including the entire real number line: [-infinity, +infinity]
	 */
	public static RealDomain unbounded()
	{
		return StandardDomain.UNBOUNDED.domain;
	}
	
	/**
	 * Domain consisting of zero plus all positive numbers: [0.0, +infinity]
	 */
	public static RealDomain nonNegative()
	{
		return StandardDomain.NON_NEGATIVE.domain;
	}
	
	/**
	 * Domain consisting of zero plus all negative numbers: [-infinity, 0.0]
	 */
	public static RealDomain nonPositive()
	{
		return StandardDomain.NON_POSITIVE.domain;
	}

	private static enum StandardDomain
	{
		UNBOUNDED(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
		NON_NEGATIVE(0.0, Double.POSITIVE_INFINITY),
		NON_POSITIVE(Double.NEGATIVE_INFINITY, 0.0);
		
		private final RealDomain domain;
		
		private StandardDomain(double lower, double upper)
		{
			domain = new RealDomain(lower, upper);
		}
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public final boolean equals(Object other)
	{
		if (this == other)
			return true;

		if (!(other instanceof RealDomain))
			return false;

		RealDomain rother = (RealDomain)other;
		
		return _lowerBound == rother._lowerBound && _upperBound == rother._upperBound;
		
	}
	
	/*----------------
	 * Domain methods
	 */
	
	@Override
	public final RealDomain asReal()
	{
		return this;
	}
	
	@Override
	public final boolean inDomain(Object value)
	{
		return value instanceof Number && inDomain(((Number)value).doubleValue());
	}
	
	@Override
	public final boolean isReal()
	{
		return true;
	}
	
	/*--------------------
	 * RealDomain methods
	 */
	
	public final double getLowerBound() {return _lowerBound;}
	public final double getUpperBound() {return _upperBound;}
	
	/**
	 * True if {@code value} is in the range [{@link #getLowerBound()}, {@link #getUpperBound()}].
	 */
	public final boolean inDomain(double value)
	{
		return (value >= _lowerBound) && (value <= _upperBound);
	}
	
	public final boolean isBounded()
	{
		return !isUnbounded();
	}
	
	public final boolean isUnbounded()
	{
		return (_lowerBound == Double.NEGATIVE_INFINITY) && (_upperBound == Double.POSITIVE_INFINITY);
	}
	
	
	@Override
	public String toString()
	{
		return "RealDomain: [" + _lowerBound + ", " + _upperBound + "]";
	}

}
