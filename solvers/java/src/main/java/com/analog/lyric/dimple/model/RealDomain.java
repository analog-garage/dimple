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

package com.analog.lyric.dimple.model;

import net.jcip.annotations.Immutable;

@Immutable
public class RealDomain extends Domain
{
	/*-------
	 * State
	 */
	
	private final double _lowerBound;
	private final double _upperBound;
	
	/*--------------
	 * Construction
	 */
	
	public RealDomain() { this(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY); }
	public RealDomain(double[] domain)  {this(domain[0], domain[1]);}
	public RealDomain(double lower, double upper)
	{
		if (lower > upper) throw new DimpleException("Upper bound must be greater than lower bound");
		_lowerBound = lower;
		_upperBound = upper;
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
	 * Domain including the entire real number line: [-infinity, +infinity]
	 */
	public static RealDomain full()
	{
		return StandardDomain.FULL.domain;
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

	/**
	 * Probability domain numbers between zero and one: [0.0, 1.0]
	 */
	public static RealDomain probability()
	{
		return StandardDomain.PROBABILITY.domain;
	}
	
	private static enum StandardDomain
	{
		FULL(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
		PROBABILITY(0.0, 1.0),
		NON_NEGATIVE(0.0, Double.POSITIVE_INFINITY),
		NON_POSITIVE(Double.NEGATIVE_INFINITY, 0.0);
		
		private final RealDomain domain;
		
		private StandardDomain(double lower, double upper)
		{
			domain = new RealDomain(lower, upper);
		}
	}
	
	/*----------------
	 * Domain methods
	 */
	
	@Override
	public boolean containsValue(Object value)
	{
		return value instanceof Number && inDomain(((Number)value).doubleValue());
	}
	
	@Override
	public boolean isReal()
	{
		return true;
	}
	
	/*--------------------
	 * RealDomain methods
	 */
	
	public double getLowerBound() {return _lowerBound;}
	public double getUpperBound() {return _upperBound;}
	
	// Utility to check if a value is in the domain or not
	public boolean inDomain(double value)
	{
		return (value >= _lowerBound) && (value <= _upperBound);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
			return true;

		if (!(other instanceof RealDomain))
			return false;

		RealDomain rother = (RealDomain)other;
		
		return _lowerBound == rother._lowerBound && _upperBound == rother._upperBound;
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this._lowerBound);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this._upperBound);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	@Override
	public String toString()
	{
		return "RealDomain: [" + _lowerBound + ", " + _upperBound + "]";
	}

}
