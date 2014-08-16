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

import java.lang.reflect.Array;
import java.util.Arrays;

import net.jcip.annotations.Immutable;

import com.analog.lyric.collect.WeakInterner;
import org.eclipse.jdt.annotation.Nullable;
import com.google.common.collect.Interner;

@Immutable
public class RealJointDomain extends Domain
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final RealDomain [] _domains;
	private final int _size;
	
	/**
	 * If true, then _domains will contain only one element representing the common domain
	 * for all dimensions.
	 */
	private final boolean _homogeneous;
	
	private static enum InternedDomains
	{
		INSTANCE;
		
		private final Interner<RealJointDomain> interner = WeakInterner.create();
	}
	
	/*--------------
	 * Construction
	 */
	
	protected RealJointDomain(RealDomain domain, int size)
	{
		super(domain.hashCode() * size);
		_domains = new RealDomain[] { domain };
		_size = size;
		_homogeneous = true;
	}
	
	protected RealJointDomain(RealDomain[] domains, boolean cloneDomains)
	{
		super(computeHashCode(domains));
		if (allEqual(domains))
		{
			_domains = new RealDomain[] { domains[0] };
			_size = domains.length;
			_homogeneous = true;
		}
		else
		{
			_domains = cloneDomains ? domains.clone() : domains;
			_size = domains.length;
			_homogeneous = false;
		}
	}
	
	@Override
	protected RealJointDomain intern()
	{
		return InternedDomains.INSTANCE.interner.intern(this);
	}
	
	public static RealJointDomain create(RealDomain... domains)
	{
		if (domains.length < 1)
		{
			throw new IllegalArgumentException("RealJointDomain requires at least one domain");
		}
		
		return new RealJointDomain(domains, true).intern();
	}
	
	public static RealJointDomain create(int size)
	{
		return create(RealDomain.unbounded(), size);
	}

	public static RealJointDomain create(RealDomain domain, int size)
	{
		return new RealJointDomain(domain, size).intern();
	}
	
	private static int computeHashCode(RealDomain[] domains)
	{
		int hashCode = 43;
		for (RealDomain domain : domains)
		{
			hashCode += domain.hashCode();
		}
		return hashCode;
	}
	
	private static boolean allEqual(RealDomain[] domains)
	{
		RealDomain domain = domains[0];
		for (int i = domains.length; --i>=1;)
		{
			if (!domain.equals(domains[i]))
			{
				return false;
			}
		}
		return true;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj)
			return true;

		if ((obj instanceof RealJointDomain))
		{
			RealJointDomain other = (RealJointDomain) obj;
			return _size == other._size &&
				_homogeneous == other._homogeneous &&
				Arrays.equals(this._domains, other._domains);
		}
		
		return false;
	}
	
	/*----------------
	 * Domain methods
	 */

	@Override
	public final RealJointDomain asRealJoint()
	{
		return this;
	}
	
	/**
	 * @return true if value is an array of length matching {@link #getNumVars()} and whose
	 * elements are contained in the correspond {@link RealDomain} in {@link #getRealDomains()}.
	 */
	@Override
	public boolean inDomain(@Nullable Object value)
	{
		if (value != null && value.getClass().isArray() && Array.getLength(value) == _size)
		{
			if (_homogeneous)
			{
				RealDomain domain = _domains[0];
				for (int i = 0, end = _size; i < end; ++i)
				{
					if (! domain.inDomain(Array.get(value, i)))
					{
						return false;
					}
				}
			}
			else
			{
				for (int i = 0, end = _size; i < end; ++i)
				{
					if (! _domains[i].inDomain(Array.get(value, i)))
					{
						return false;
					}
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean inDomain(double ... values)
	{
		int size = values.length;
		if (size != _size)
			return false;
		
		if (_homogeneous)
		{
			final RealDomain domain = _domains[0];
			for (double value : values)
				if (!domain.inDomain(value))
					return false;
		}
		else
		{
			for (int i = 0; i < size; i++)
				if (!_domains[i].inDomain(values[i]))
					return false;
		}
		
		return true;
	}

	/**
	 * True if all {@link RealDomain} subdomains are equal.
	 */
	public final boolean isHomogeneous()
	{
		return _homogeneous;
	}
	
	@Override
	public final boolean isNumber()
	{
		return false;
	}
	
	@Override
	public final boolean isRealJoint()
	{
		return true;
	}
	
	@Override
	public final boolean isScalar()
	{
		return false;
	}
	
	/*-------------------------
	 * RealJointDomain methods
	 */
	
	/**
	 * Returns the number of dimensions, or subdomains that make up this joint domain.
	 */
	@Override
	public final int getDimensions()
	{
		return _size;
	}
	
	/**
	 * Returns copy of all of the subdomains making up this joint domain.
	 * <p>
	 * If you just want the size use {@link #getDimensions()} and if you just want
	 * to access a domain at a given index use {@link #getRealDomain(int)} instead
	 * to avoid the overhead of copying the array.
	 */
	public RealDomain [] getRealDomains()
	{
		if (_homogeneous)
		{
			RealDomain[] domains = new RealDomain[_size];
			Arrays.fill(domains, _domains[0]);
			return domains;
		}
		else
		{
			return _domains.clone();
		}
	}
	
	public RealDomain getRealDomain(int dimension)
	{
		return _domains[_homogeneous ? 0 : dimension];
	}
	
	public int getNumVars()
	{
		return _size;
	}
	
	/**
	 * True if values are confined to a subset of R<sup>n</sup>
	 * <p>
	 * True if at least one subdomain in {@link #getRealDomains()} is bounded.
	 */
	@Override
	public final boolean isBounded()
	{
		// True if any dimension is bounded
		for (int i = 0; i < _domains.length; i++)
		{
			if (_domains[i].isBounded())
				return true;
		}
		
		return false;
	}
}
