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

import net.jcip.annotations.Immutable;

import com.analog.lyric.collect.WeakInterner;
import com.analog.lyric.dimple.exceptions.DimpleException;
import org.eclipse.jdt.annotation.Nullable;
import com.google.common.collect.Interner;

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
		return new RealDomain(lower, upper).intern();
	}
	
	/**
	 * Same as #unbounded()
	 */
	public static RealDomain create()
	{
		return InternedDomains.INSTANCE.UNBOUNDED;
	}
	
	/**
	 * Domain including the entire real number line: [-infinity, +infinity]
	 */
	public static RealDomain unbounded()
	{
		return InternedDomains.INSTANCE.UNBOUNDED;
	}
	
	/**
	 * Domain consisting of zero plus all positive numbers: [0.0, +infinity]
	 */
	public static RealDomain nonNegative()
	{
		return InternedDomains.INSTANCE.NON_NEGATIVE;
	}
	
	/**
	 * Domain consisting of zero plus all negative numbers: [-infinity, 0.0]
	 */
	public static RealDomain nonPositive()
	{
		return InternedDomains.INSTANCE.NON_POSITIVE;
	}

	private static enum InternedDomains
	{
		INSTANCE;

		private final Interner<RealDomain> interner;
		private final RealDomain UNBOUNDED;
		private final RealDomain NON_NEGATIVE;
		private final RealDomain NON_POSITIVE;
		
		private InternedDomains()
		{
			interner = WeakInterner.create();
			UNBOUNDED = interner.intern(new RealDomain(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
			NON_NEGATIVE = interner.intern(new RealDomain(0.0, Double.POSITIVE_INFINITY));
			NON_POSITIVE = interner.intern(new RealDomain(Double.NEGATIVE_INFINITY, 0.0));
		}
	}

	@Override
	protected RealDomain intern()
	{
		return InternedDomains.INSTANCE.interner.intern(this);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public final boolean equals(@Nullable Object other)
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
	public final boolean inDomain(@Nullable Object value)
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
	
	/**
	 * True if values are confined to a subset of the reals.
	 * <p>
	 * True when {@link #getLowerBound} is greater than negative infinity or
	 * {@link #getUpperBound()} is less than positive infinity.
	 */
	@Override
	public final boolean isBounded()
	{
		return _lowerBound > Double.NEGATIVE_INFINITY || _upperBound < Double.POSITIVE_INFINITY;
	}
	
	/**
	 * True if bounds are within the bounds of other domain.
	 * <p>
	 * True if {@link #getLowerBound()} and {@link #getUpperBound()} are within
	 * the bounds of the {@code other} domain.
	 * <p>
	 * @param other is non-null real domain.
	 * @since 0.07
	 */
	public final boolean isSubsetOf(RealDomain other)
	{
		// TODO: we could generalize this to method to other domains...
		return other._lowerBound <= _lowerBound && _upperBound <= other._upperBound;
	}
	
	@Override
	public String toString()
	{
		return "RealDomain: [" + _lowerBound + ", " + _upperBound + "]";
	}

}
