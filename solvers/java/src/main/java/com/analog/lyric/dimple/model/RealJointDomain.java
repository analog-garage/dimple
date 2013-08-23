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

import java.lang.reflect.Array;
import java.util.Arrays;

import net.jcip.annotations.Immutable;

@Immutable
public class RealJointDomain extends Domain
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final RealDomain [] _domains;
	
	/*--------------
	 * Construction
	 */
	
	RealJointDomain(RealDomain[] domains)
	{
		super(Arrays.hashCode(domains) * 3 + 31);
		_domains = domains;
	}

	public static RealJointDomain create(RealDomain... domains)
	{
		if (domains.length < 2)
		{
			throw new IllegalArgumentException("RealJointDomain requires at least two domains");
		}
		return new RealJointDomain(domains.clone());
	}

	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if ((obj instanceof RealJointDomain))
		{
			RealJointDomain other = (RealJointDomain) obj;
			return Arrays.equals(this._domains, other._domains);
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
	 * @returns true if value is an array of length matching {@link #getNumVars()} and whose
	 * elements are contained in the correspond {@link RealDomain} in {@link #getRealDomains()}.
	 */
	@Override
	public boolean inDomain(Object value)
	{
		if (value.getClass().isArray() && Array.getLength(value) == _domains.length)
		{
			for (int i = 0, end = _domains.length; i < end; ++i)
			{
				if (! _domains[i].inDomain(Array.get(value, i)))
				{
					return false;
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean inDomain(double ... value)
	{
		int size = value.length;
		if (size != _domains.length)
			return false;
		
		for (int i = 0; i < size; i++)
			if (!_domains[i].inDomain(value[i]))
				return false;
		
		return true;
	}

	@Override
	public final boolean isRealJoint()
	{
		return true;
	}
	
	/*-------------------------
	 * RealJointDomain methods
	 */
	
	/**
	 * Returns the number of dimensions, or subdomains that make up this joint domain.
	 */
	public final int getDimensions()
	{
		return _domains.length;
	}
	
	public RealDomain [] getRealDomains()
	{
		return _domains.clone();
	}
	
	public RealDomain getRealDomain(int dimension)
	{
		return _domains[dimension];
	}
	
	public int getNumVars()
	{
		return _domains.length;
	}
}
