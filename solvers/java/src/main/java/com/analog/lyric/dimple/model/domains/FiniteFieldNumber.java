/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

/**
 * An extension of Number that includes additional information needed to fully
 * define arithmetic operations on a finite field of characteristic 2 (GF(2^N)).
 * This includes a binary representation of the primitive polynomial and the
 * length in bits.
 */
@Immutable
public class FiniteFieldNumber extends Number
{
	private static final long serialVersionUID = 1L;
	private int value;
	private FiniteFieldDomain domain;

	public FiniteFieldNumber(int value, FiniteFieldDomain domain)
	{
		this.value = value;
		this.domain = domain;
	}
	
	public FiniteFieldNumber(int value, FiniteFieldNumber other)	// New value with same domain as another number
	{
		this.value = value;
		this.domain = other.domain;
	}
	
	public int getPrimativePolynomial()
	{
		return this.domain.getPrimitivePolynomial();
	}

	public int getN()
	{
		return this.domain.getN();
	}
	
	public FiniteFieldDomain getDomain()
	{
		return this.domain;
	}
	
	public boolean isCompatible(FiniteFieldNumber other)
	{
		return other.getPrimativePolynomial() == this.getPrimativePolynomial();
	}
	
	public boolean isEqual(FiniteFieldNumber other)
	{
		return isCompatible(other) && (value == other.value);
	}

	@Override
	public double doubleValue()
	{
		return this.value;
	}

	@Override
	public float floatValue()
	{
		return this.value;
	}

	@Override
	public int intValue()
	{
		return this.value;
	}

	@Override
	public long longValue()
	{
		return this.value;
	}
	
	@Override
	public FiniteFieldNumber clone()
	{
		return new FiniteFieldNumber(this.value, this.domain);
	}
	
	public FiniteFieldNumber cloneWithNewValue(int newValue)
	{
		return new FiniteFieldNumber(newValue, this.domain);
	}
	
	@Override
	public String toString()
	{
		return Integer.toString(value);
	}
}
