/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model.values;

import java.lang.reflect.Array;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.IntRangeDomain;
import com.analog.lyric.dimple.model.domains.ObjectDomain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;
import com.google.common.base.Objects;

/**
 * @since 0.05
 */
public abstract class Value implements Cloneable
{
	/*--------------
	 * Construction
	 */
	
	public static Value create(Domain domain)
	{
		if (domain != null)
		{
			DiscreteDomain discrete = domain.asDiscrete();
			if (discrete != null)
			{
				return create(discrete);
			}

			RealDomain real = domain.asReal();
			if (real != null)
			{
				return create(real);
			}


			RealJointDomain realJoint = domain.asRealJoint();
			if (realJoint != null)
			{
				return new RealJointValue(realJoint);
			}

			if (domain.isIntegral())
			{
				return new IntValue();
			}

			assert(domain == ObjectDomain.instance());
		}
		
		return new ObjectValue();
	}
	
	public static RealValue create(RealDomain domain)
	{
		return new RealValue(0.0);
	}
	
	public static DiscreteValue create(DiscreteDomain domain)
	{
		if (domain.isIntegral())
		{
			if (domain instanceof IntRangeDomain)
			{
				IntRangeDomain rangeDomain = (IntRangeDomain)domain;
				if (rangeDomain.getLowerBound() == 0 && rangeDomain.getInterval() == 1)
				{
					return new SimpleIntRangeValue(rangeDomain);
				}
				else
				{
					return new IntRangeValue(rangeDomain);
				}
			}
			@SuppressWarnings("unchecked")
			TypedDiscreteDomain<Integer> intDomain = (TypedDiscreteDomain<Integer>) domain;
			return new GenericIntDiscreteValue(intDomain);
		}
		else
		{
			return new GenericDiscreteValue(domain.asDiscrete());
		}
	}
	
	public static Value create(Domain domain, Object value)
	{
		if (domain != null)
		{
			DiscreteDomain discrete = domain.asDiscrete();
			if (discrete != null)
			{
				return create(discrete, value);
			}

			RealDomain real = domain.asReal();
			if (real != null)
			{
				return create(real, ((Number)value).doubleValue());
			}

			RealJointDomain realJoint = domain.asRealJoint();
			if (realJoint != null)
			{
				return create(realJoint, (double[])value);
			}

			if (domain.isIntegral())
			{
				return new IntValue(FactorFunctionUtilities.toInteger(value));
			}

			assert(domain == ObjectDomain.instance());
		}
		
		return new ObjectValue(value);
	}
	
	public static DiscreteValue create(DiscreteDomain domain, Object value)
	{
		if (domain.isIntegral())
		{
			int intValue = FactorFunctionUtilities.toInteger(value);
			if (domain instanceof IntRangeDomain)
			{
				IntRangeDomain rangeDomain = (IntRangeDomain)domain;
				if (rangeDomain.getLowerBound() == 0 && rangeDomain.getInterval() == 1)
				{
					return new SimpleIntRangeValue(rangeDomain, intValue);
				}
				else
				{
					return new IntRangeValue(rangeDomain, intValue);
				}
			}
			@SuppressWarnings("unchecked")
			TypedDiscreteDomain<Integer> intDomain = (TypedDiscreteDomain<Integer>) domain;
			return new GenericIntDiscreteValue(intDomain, intValue, domain.getIndex(value));
		}
		else
		{
			return new GenericDiscreteValue(value, domain);
		}
	}
	
	public static RealValue create(RealDomain domain, double value)
	{
		// TODO: use domain
		return new RealValue(value);
	}

	public static RealJointValue create(RealJointDomain domain, double[] value)
	{
		// TODO: do something with domain
		return new RealJointValue(value);
	}
	
	public static Value create(Object object)
	{
		if (object instanceof Number)
		{
			Number number = (Number)object;
			if (number instanceof Integer || number instanceof Short || number instanceof Byte)
			{
				return new IntValue(number.intValue());
			}
			else
			{
				return new RealValue(number.doubleValue());
			}
		}
		else if (object instanceof double[])
		{
			return new RealJointValue((double[])object);
		}
		else
		{
			return new ObjectValue(object);
		}
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public abstract Value clone();
	
	/*---------------
	 * Value methods
	 */
	
	public abstract Domain getDomain();
	
	public abstract Object getObject();
	public abstract void setObject(Object value);
	
	public boolean getBoolean()
	{
		return FactorFunctionUtilities.toBoolean(getObject());
	}
	
	public double getDouble()
	{
		return FactorFunctionUtilities.toDouble(getObject());
	}
	
	public void setDouble(double value)
	{
		setObject(value);
	}

	/**
	 * If value is known to be a member of a {@link DiscreteDomain}, returns its index within its domain otherwise -1.
	 */
	public int getIndex()
	{
		return -1;
	}

	public int getInt()
	{
		return FactorFunctionUtilities.toInteger(getObject());
	}
	
	/**
	 * Sets contents from another value.
	 * <p>
	 * Default implementation uses {@link #getObject()}/{@link #setObject(Object)}.
	 */
	public void setFrom(Value value)
	{
		setObject(value.getObject());
	}
	
	/**
	 * If value is known to be a member of a {@link DiscreteDomain}, sets value to element with
	 * given index within the domain.
	 */
	public void setIndex(int index)
	{
		throw DimpleException.unsupportedMethod(getClass(), "setIndex");
	}
	
	public void setInt(int value)
	{
		setObject(value);
	}
	
	/**
	 * True if {@link #setFrom(Value)} on {@code other} would not change the state of this value.
	 * <p>
	 * Default implementation compares {@link #getObject()} values.
	 */
	public boolean valueEquals(Value other)
	{
		return Objects.equal(getObject(), other.getObject());
	}
	
	/*-----------------------
	 * Static helper methods
	 */
	
	/**
	 * Fills in and returns {@code Value} array from array of objects.
	 * 
	 * @param objs must have values that are compatible with members of output array.
	 * @param output must be the same length as object array and contain concrete {@link Value}
	 * instances compatible with corresponding objects.
	 * @return {@code output} argument
	 * 
	 * @see #createFromObjects(Object[], Class, Domain...)
	 */
	public static <T extends Value> T[] copyFromObjects(Object[] objs, T[] output)
	{
		for (int i = objs.length; --i>=0;)
		{
			output[i].setObject(objs[i]);
		}
		return output;
	}
	
	/**
	 * Creates {@code Value} array from array of objects using declared subclass type and provided
	 * domains.
	 * 
	 * @param objs
	 * @param valueClass specifies the component type of the the returned array.
	 * @param domains specifies the domains of the objects. This must have between 1 and {@code objs.length}
	 * elements. If less than the maximum, the last domain will be applied to all remaining objects.
	 * @return array of {@code Value} instances containing the corresponding values from the object array.
	 * 
	 * @see #createFromObjects(Object[], Domain...)
	 * @see #copyFromObjects(Object[], Value[])
	 */
	public static <T extends Value> T[] createFromObjects(Object[] objs, Class<T> valueClass, Domain ... domains)
	{
		final int size = objs.length;
		final int nDomains = domains.length;
		assert(nDomains > 0 && nDomains <= size);
		
		final T[] output = (T[]) Array.newInstance(valueClass, size);
		for (int i = 0; i < size; ++i)
		{
			final Domain domain = i < nDomains ? domains[i] : domains[nDomains - 1];
			
			@SuppressWarnings("unchecked")
			T value = (T) create(domain, objs[i]);
			output[i] = value;
		}
		return output;
	}
	
	/**
	 * Same as {@link #createFromObjects(Object[], Class, Domain...)}
	 */
	public static Value[] createFromObjects(Object[] objs, Domain ... domains)
	{
		return createFromObjects(objs, Value.class, domains);
	}

	public static <T extends Value> Object[] toObjects(T[] values, Object[] output)
	{
		for (int i = values.length; --i>=0;)
		{
			output[i] = values[i].getObject();
		}
		return output;
	}
	
	public static <T extends Value> Object[] toObjects(T[] values)
	{
		return toObjects(values, new Object[values.length]);
	}
}
