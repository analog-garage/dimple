/*******************************************************************************
*   Copyright 2012-2013 Analog Devices, Inc.
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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.WeakInterner;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.variables.Bit;
import com.google.common.collect.Interner;
import com.google.common.math.DoubleMath;

/**
 * Domain for discrete variables that can take on a values from a predefined finite set
 * in a specified order.
 * <p>
 * While from a set-theoretic perspective two domains with the same elements in different order
 * are equivalent, these objects are only considered equal if the elements occur in the same
 * order because the order is intrinsic in the representation of variable values using the domain.
 */
@Immutable
public abstract class DiscreteDomain extends Domain
{
	private static final long serialVersionUID = 1L;
	
	private static enum InternedDomains
	{
		INSTANCE;
		
		private final Interner<DiscreteDomain> interner = WeakInterner.create();
	}

	@Override
	protected DiscreteDomain intern()
	{
		return InternedDomains.INSTANCE.interner.intern(this);
	}
	
	@SuppressWarnings("unchecked")
	static <D extends DiscreteDomain> D intern(D domain)
	{
		return (D)domain.intern();
	}
	
	/*--------------
	 * Construction
	 */
	
	DiscreteDomain(int hashCode)
	{
		super(hashCode);
	}
	
	/**
	 * Domain consisting of the integers 0 and 1, which is the domain of {@link Bit} variables.
	 */
	public static IntRangeDomain bit()
	{
		return range(0,1);
	}
	
	/**
	 * Domain consisting of the values false and true.
	 */
	public static TypedDiscreteDomain<Boolean> bool()
	{
		return create(false, true);
	}
	
	/**
	 * Returns a discrete domain consisting of the specified elements in the specified order.
	 * <p>
	 * @param elements must either be immutable or must implement {@link Object#equals(Object)} and
	 * {@link Object#hashCode()} methods that do not depend on any mutable state.
	 */
	public static <T> TypedDiscreteDomain<T> create(T[] elements)
	{
		return create(elements[0], 1, elements);
	}
	
	public static <T> TypedDiscreteDomain<T> create(T firstElement, Object ... moreElements)
	{
		return create(firstElement, 0, moreElements);
	}
	
	private static <T> TypedDiscreteDomain<T> create(T firstElt, int offset, Object[] moreElements)
	{
		int size = moreElements.length;
		Class<?> eltClass = firstElt.getClass();
		
		if (eltClass.isEnum() && ((Enum<?>)firstElt).ordinal() == 0)
		{
			boolean useEnumDomain = true;
			for (int i = offset; i < size; ++i)
			{
				Object elt = moreElements[i];
				if (elt.getClass() != eltClass || ((Enum<?>)elt).ordinal() != (i + 1))
				{
					useEnumDomain = false;
					break;
				}
			}
			
			if (useEnumDomain)
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				TypedDiscreteDomain<T> domain = intern(new EnumDomain(eltClass));
				return domain;
			}
		}
		else if (Integer.class.isAssignableFrom(eltClass))
		{
			// We could eliminate the instanceof checks if the component type of the 'elements'
			// array is Integer...
			if (size > offset && moreElements[offset] instanceof Integer)
			{
				int first = ((Integer)firstElt).intValue();
				int prev = ((Integer)moreElements[offset]).intValue();
				int interval = prev - first;
				
				if (interval > 0)
				{
					boolean useIntRangeDomain = true;

					for (int i = offset+1; i < size; ++i)
					{
						Object element = moreElements[i];
						if (element instanceof Integer)
						{
							int next = prev + interval;
							if (next == ((Integer)element).intValue())
							{
								prev = next;
								continue;
							}
						}
						useIntRangeDomain = false;
						break;
					}

					if (useIntRangeDomain)
					{
						@SuppressWarnings("unchecked")
						TypedDiscreteDomain<T> domain = (TypedDiscreteDomain<T>) range(first, prev, interval);
						return domain;
					}
				}
			}
		}
		else if (Double.class.isAssignableFrom(eltClass))
		{
			if (size > offset && moreElements[offset] instanceof Double)
			{
				double first = ((Double)firstElt).doubleValue();
				double prev = ((Double)moreElements[offset]).doubleValue();
				double interval = prev - first;
				
				if (interval > 0.0)
				{
					double tolerance = DoubleRangeDomain.defaultToleranceForInterval(interval);
					boolean useDoubleRangeDomain = true;

					for (int i = 1+offset; i < size; ++i)
					{
						Object element = moreElements[i];
						if (element instanceof Double)
						{
							double next = prev + interval;
							if (DoubleMath.fuzzyEquals(next, (Double)element, tolerance))
							{
								prev = next;
								continue;
							}
						}
						useDoubleRangeDomain = false;
						break;
					}

					if (useDoubleRangeDomain)
					{
						@SuppressWarnings("unchecked")
						TypedDiscreteDomain<T> domain = (TypedDiscreteDomain<T>) range(first, prev, interval, tolerance);
						return domain;
					}
				}
			}
		}
		
		return intern(new ArrayDiscreteDomain<T>(firstElt, offset, moreElements));
	}
	
	public static <E extends Enum<E>> EnumDomain<E> forEnum(Class<E> enumClass)
	{
		return intern(new EnumDomain<E>(enumClass));
	}
	
	public static <T> JointDiscreteDomain<T> joint(JointDomainIndexer domains)
	{
		return intern(new JointDiscreteDomain<T>(domains));
	}
	
	public static <T> JointDiscreteDomain<T> joint(TypedDiscreteDomain<?>... domains)
	{
		return joint(JointDomainIndexer.create(domains));
	}
	
	public static JointDiscreteDomain<?> joint(DiscreteDomain ... domains)
	{
		return joint(JointDomainIndexer.create(domains));
	}

	/**
	 * Returns discrete domain containing all integers from {@code low} to {@code high}.
	 * @param low is the first element of the domain
	 * @param high is the last element of the domain, which must be greater than or equal to {@code low}
	 * @see #range(int, int, int)
	 */
	public static IntRangeDomain range(int low, int high)
	{
		return intern(new IntRangeDomain(low, high, 1));
	}
	
	/**
	 * Returns discrete domain containing all integers from {@code low} to {@code high} in increments of
	 * specified interval.
	 * <p>
	 * @param low is the first element of the domain
	 * @param high is the upper bound of the domain, which must be greater than {@code low}. Will only
	 * be included in the domain itself if {@code (high - low)} is a multiple of {@code interval}.
	 * @param interval is a positive integer specifying the increment between adjacent elements in the domain.
	 * @see #range(int, int)
	 */
	public static IntRangeDomain range(int low, int high, int interval)
	{
		return intern(new IntRangeDomain(low, high, interval));
	}

	public static DoubleRangeDomain range(double low, double high)
	{
		return DoubleRangeDomain.create(low, high, 1.0, Double.NaN);
	}
	
	public static DoubleRangeDomain range(double low, double high, double interval)
	{
		return DoubleRangeDomain.create(low, high, interval, Double.NaN);
	}

	public static DoubleRangeDomain range(double low, double high, double interval, double tolerance)
	{
		return DoubleRangeDomain.create(low, high, interval, tolerance);
	}
	
	public static FiniteFieldDomain finiteField(int primitivePolynomial)
	{
		return intern(new FiniteFieldDomain(primitivePolynomial));
	}

	/*----------------
	 * Object methods
	 */
	
	/**
	 * True if elements of domain are the same and in the same order.
	 * <p>
	 * The default implementation simply does an elementwise comparison.
	 */
	@Override
	public boolean equals(@Nullable Object thatObj)
	{
		if (this == thatObj)
		{
			return true;
		}
		
		if (!(thatObj instanceof DiscreteDomain))
		{
			return false;
		}
		
		DiscreteDomain that = (DiscreteDomain)thatObj;
		if (size() != that.size() || hashCode() != that.hashCode())
		{
			return false;
		}
		
		for (int i = 0, end = size(); i < end; ++i)
		{
			if (!this.getElement(i).equals(that.getElement(i)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/*----------------
	 * Domain methods
	 */
	
	@Override
	public final DiscreteDomain asDiscrete()
	{
		return this;
	}
	
	@Override
	public boolean hasIntCompatibleValues()
	{
		return isIntCompatibleClass(getElementClass());
	}
	
	@Override
	public boolean inDomain(@Nullable Object value)
	{
		return (getIndex(value) >= 0);
	}
	
	@Override
	public boolean isIntegral()
	{
		return isIntCompatibleClass(getElementClass());
	}
	
	/**
	 * @return true if {@code value} is a {@link Number} representing a valid
	 * integer index into the array of elements returned by {@link #getElements()}.
	 */
	@Override
	public boolean containsValueWithRepresentation(Object value)
	{
		if (value instanceof Number)
		{
			Number number = (Number)value;
			int index = number.intValue();
			return index >=0 && index < size();
		}
		
		return false;
	}
	
	@Override
	public final boolean isDiscrete()
	{
		return true;
	}
	
	/*------------------------
	 * DiscreteDomain methods
	 */
	
	/**
	 * If this is a {@link TypedDiscreteDomain} with element type that extends {@code elementClass}
	 * returns this object, otherwise null.
	 */
	public abstract @Nullable <T> TypedDiscreteDomain<T> asTypedDomain(Class<T> elementClass);
	
	/**
	 * Common superclass of all elements.
	 */
	public abstract Class<?> getElementClass();
	
	/**
	 * Returns the value of the ith element in the domain.
	 * @param i must be in the range [0,{@link #size()}-1].
	 */
	public abstract Object getElement(int i);
	
	/**
	 * Returns an array containing all the elements of the domain in their canonical order.
	 * <p>
	 * Because this allocates memory and costs linear time to compute, it is usually better to use
	 * {@link #getElement(int)} to access elements one at a time and {@link #size()} to get the
	 * number of elements.
	 * <p>
	 * @see #getElements(Object[])
	 */
	public Object[] getElements()
	{
		return getElements(null);
	}
	
	/**
	 * Returns an array containing all of the elements of the domain in their canonical
	 * order using provided {@code array} if possible.
	 * <p>
	 * If {@code array} has a component type compatible with {@link #getElementClass()} and
	 * length at least as large as {@link #size()} it will be filled in with the domain elements
	 * and returned. If {@code array} has a compatible type but is not long enough, a new
	 * array will be returned with the same component type as {@code array}. Otherwise a new
	 * array will be returned with component type same as {@link #getElementClass()}.
	 */
	public <T> T[] getElements(@Nullable T[] array)
	{
		final int length = size();
		
		array = ArrayUtil.allocateArrayOfType(getElementClass(), array, length);
		
		for (int i = 0; i < length; ++i)
		{
			@SuppressWarnings("unchecked")
			T element = (T) getElement(i);
			array[i] = element;
		}
		
		return array;
	}

	/**
	 * The number of elements in the domain.
	 */
	public abstract int size();
	
	@Override
	public String toString()
	{
		final int maxToShow = 10;
		final int size = size();
		final int n = Math.min(size, maxToShow) - 1;
		StringBuilder sb = new StringBuilder("{");
		for (int i = 0; i < n; ++i)
		{
			sb.append(getElement(i));
			sb.append(",");
		}
		if (n < size - 1)
		{
			sb.append("...,");
		}
		sb.append(getElement(size-1));
		sb.append("}");
		return sb.toString();
	}
	
	// Find the list of elements corresponding to the value; return -1 if not a valid value
	/**
	 * Returns the index of {@code value} in this domains elements or else -1 if
	 * {@code value} is not an element of this domain.
	 * @see #getIndexOrThrow(Object)
	 */
	public abstract int getIndex(@Nullable Object value);
	
	/**
	 * Like {@link #getIndex(Object)} but throws a {@link DimpleException} instead of returning
	 * -1 on failure.
	 */
	public int getIndexOrThrow(@Nullable Object value)
	{
		int index = getIndex(value);
		if (index < 0)
		{
			throw domainError(value);
		}
		return index;
	}

	/**
	 * @deprecated Use {@link #inDomain} instead.
	 */
	@Deprecated
	public boolean isElementOf(Object value)
	{
		return (getIndex(value) >= 0);
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	/**
	 * Verifies that {@code index} is in the range [0,size-1].
	 * @throws IndexOutOfBoundsException if index is not in range.
	 */
	protected static void assertIndexInBounds(int index, int size)
	{
		// We want this check to be fast. This converts index to a long and masks the low-32 bits.
		// As a result, all negative index values are going to end up as large positive numbers
		// allowing us to do this in a single compare.
		if ((index & 0xFFFFFFFFL) >= size)
		{
			throw new IndexOutOfBoundsException(String.format("Index '%d' is not in range [%d,%d]", index, 0, size -1));
		}
	}
}
