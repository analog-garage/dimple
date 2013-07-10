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

import com.google.common.math.DoubleMath;

@Immutable
public abstract class DiscreteDomain extends Domain
{
	private final int _hashCode;
	
	/*--------------
	 * Construction
	 */
	
	DiscreteDomain(int hashCode)
	{
		_hashCode = hashCode;
	}
	
	public static TypedDiscreteDomain<Boolean> forBoolean()
	{
		return new ArrayDiscreteDomain<Boolean>(false, true);
	}
	
	public static TypedDiscreteDomain<Double> forBit()
	{
		return fromElements(0.0, 1.0);
	}
	
	public static <T> TypedDiscreteDomain<T> fromElements(T ... elements)
	{
		int size = elements.length;
		Object firstElt = elements[0];
		Class<?> eltClass = firstElt.getClass();
		
		if (eltClass.isEnum())
		{
			boolean useEnumDomain = true;
			for (int i = 0; i < size; ++i)
			{
				Object elt = elements[i];
				if (elt.getClass() != eltClass || ((Enum<?>)elt).ordinal() != i)
				{
					useEnumDomain = false;
					break;
				}
			}
			
			if (useEnumDomain)
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				TypedDiscreteDomain<T> domain = new EnumDomain(eltClass);
				return domain;
			}
		}
		else if (Integer.class.isAssignableFrom(eltClass))
		{
			// We could eliminate the instanceof checks if the component type of the 'elements'
			// array is Integer...
			if (size > 1 && elements[1] instanceof Integer)
			{
				int first = ((Integer)firstElt).intValue();
				int prev = ((Integer)elements[1]).intValue();
				int interval = prev - first;
				boolean useIntRangeDomain = true;
				
				for (int i = 2; i < size; ++i)
				{
					Object element = elements[i];
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
					TypedDiscreteDomain<T> domain =
						(TypedDiscreteDomain<T>) intRangeFromSizeStartAndInterval(size, first, interval);
					return domain;
				}
			}
		}
		else if (Double.class.isAssignableFrom(eltClass))
		{
			if (size > 1 && elements[1] instanceof Double)
			{
				double first = ((Double)firstElt).doubleValue();
				double prev = ((Double)elements[1]).doubleValue();
				double interval = prev - first;
				double tolerance = DoubleRangeDomain.defaultToleranceForInterval(interval);
				boolean useDoubleRangeDomain = true;
				
				for (int i = 2; i < size; ++i)
				{
					Object element = elements[i];
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
					TypedDiscreteDomain<T> domain =
						(TypedDiscreteDomain<T>) doubleRangeFromSizeStartAndInterval(size, first, interval);
					return domain;
				}
			}
		}
		
		return new ArrayDiscreteDomain<T>(elements);
	}
	
	public static <E extends Enum<E>> EnumDomain<E> forEnum(Class<E> enumClass)
	{
		return new EnumDomain<E>(enumClass);
	}
	
	public static IntRangeDomain range(int low, int high)
	{
		return new IntRangeDomain(1 + high - low, low);
	}
	
	public static IntRangeDomain intRangeFromSize(int size)
	{
		return new IntRangeDomain(size);
	}
	
	public static IntRangeDomain intRangeFromSizeAndStart(int size, int start)
	{
		return new IntRangeDomain(size, start);
	}
	
	public static IntRangeDomain intRangeFromSizeStartAndInterval(int size, int start, int interval)
	{
		return new IntRangeDomain(size, start, interval);
	}
	
	public static DoubleRangeDomain doubleRangeFromSize(int size)
	{
		return new DoubleRangeDomain(size, 0.0, 1.0);
	}
	
	public static DoubleRangeDomain doubleRangeFromSizeAndStart(int size, double start)
	{
		return new DoubleRangeDomain(size, start);
	}
	
	public static DoubleRangeDomain doubleRangeFromSizeStartAndInterval(int size, double start, double interval)
	{
		return new DoubleRangeDomain(size, start, interval);
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
	public boolean equals(Object thatObj)
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
		if (size() != that.size())
		{
			return false;
		}
		
		for (int i = 0, end = size(); i < end; ++i)
		{
			Object thisElt = this.getElement(i);
			Object thatElt = that.getElement(i);
			
			if (thisElt != thatElt)
			{
				if (thisElt == null || !thisElt.equals(thatElt))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public final int hashCode()
	{
		return _hashCode;
	}
	
	/*----------------
	 * Domain methods
	 */
	
	@Override
	public boolean containsValue(Object value)
	{
		return (getIndex(value) >= 0);
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
	
	public abstract Object getElement(int i);
	
	public abstract Object[] getElements();

	public abstract int size();
	
	@Override
	public String toString()
	{
		int n = size();
		StringBuilder sb = new StringBuilder(String.format("DiscreteDomain - %d elements - ", n));
		if(n > 0)
		{
			for (int i = 0; i < n; i++)
			{
				if (i > 0)
				{
					sb.append(", ");
				}
				Object element = getElement(i);
				sb.append(String.format("type: %s value:%s"
						, element != null ? element.getClass() : "null"
						, element != null ? element.toString() : "null"));
			}
		}
		return sb.toString();
	}
	
	// Find the list of elements corresponding to the value; return -1 if not a valid value
	public abstract int getIndex(Object value);

	/**
	 * @deprecated Use {@link #containsValue} instead.
	 */
	@Deprecated
	public boolean isElementOf(Object value)
	{
		return (getIndex(value) >= 0);
	}

}
