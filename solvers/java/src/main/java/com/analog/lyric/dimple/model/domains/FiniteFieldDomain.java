package com.analog.lyric.dimple.model.domains;

import net.jcip.annotations.Immutable;

import com.analog.lyric.math.Utilities;

/**
 * A finite field of characteristic 2 (GF(2^N)).  The domain is determined by the length, N,
 * as well as the primitive polynomial.
 */
@Immutable
public class FiniteFieldDomain extends TypedDiscreteDomain<FiniteFieldNumber>
{
	private static final long serialVersionUID = 1L;
	
	private int _primitivePolynomial;
	private int _N;
	private int _size;

	/*--------------
	 * Construction
	 */
	
	FiniteFieldDomain(int primitivePolynomial)
	{
		super(computeHashCode(primitivePolynomial));
		
		_primitivePolynomial = primitivePolynomial;
		_N = Utilities.findMSB(primitivePolynomial) - 1;
		_size = 1 << _N;
	}
		
	private static int computeHashCode(int primitivePolynomial)
	{
		return primitivePolynomial;
	}
	
	public final int getPrimitivePolynomial()
	{
		return _primitivePolynomial;
	}
	
	public int getN()
	{
		return _N;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public final boolean equals(Object that)
	{
		if (this == that)
			return true;
		
		if (that instanceof FiniteFieldDomain)
		{
			FiniteFieldDomain thatFF = (FiniteFieldDomain)that;
			return (_primitivePolynomial == thatFF._primitivePolynomial) && (_N == thatFF._N);
		}
		
		return false;
	}
	
	/*------------------------
	 * DiscreteDomain methods
	 */

	@Override
	public final int size()
	{
		return _size;
	}


	/*------------------------
	 * TypedDiscreteDomain methods
	 */

	@Override
	public final FiniteFieldNumber getElement(int i)
	{
		return new FiniteFieldNumber(i, this);
	}

	@Override
	public final Class<?> getElementClass()
	{
		return FiniteFieldNumber.class;
	}

	@Override
	public final int getIndex(Object value)
	{
		if (value instanceof FiniteFieldNumber)
			return ((FiniteFieldNumber)value).intValue();
		else if (value instanceof Integer)
			return (Integer)value;	// Already an index
		else
			return -1;
	}
}
