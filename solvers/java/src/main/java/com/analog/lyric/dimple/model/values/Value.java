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

import static java.util.Objects.*;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.DomainList;
import com.analog.lyric.dimple.model.domains.DoubleRangeDomain;
import com.analog.lyric.dimple.model.domains.FiniteFieldDomain;
import com.analog.lyric.dimple.model.domains.FiniteFieldNumber;
import com.analog.lyric.dimple.model.domains.IntRangeDomain;
import com.analog.lyric.dimple.model.domains.ObjectDomain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;

/**
 * Holder for a values for a given {@link Domain}.
 * <p>
 * Subclasses are implemented to take advantage of knowledge of domain values.
 * 
 * @since 0.05
 */
public abstract class Value implements Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	/**
	 * Creates a {@code Value} instance appropriate to the given {@code domain}
	 * with a default value depending on the domain: zero for numeric domains,
	 * the first element for discrete domains, and null for {@link ObjectDomain}.
	 * <p>
	 * If {@code domain} is null, an {@link ObjectValue} will be returned.
	 */
	public static Value create(@Nullable Domain domain)
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
	
	/**
	 * Creates a {@link RealValue} instance for given {@code domain}.
	 */
	public static RealValue create(RealDomain domain)
	{
		return new RealValue(0.0);
	}
	
	/**
	 * Creates a {@link DiscreteValue} instance for given {@code domain}.
	 */
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
			else if (domain instanceof FiniteFieldDomain)
			{
				return new FiniteFieldValue((FiniteFieldDomain)domain);
			}
			@SuppressWarnings("unchecked")
			TypedDiscreteDomain<Integer> intDomain = (TypedDiscreteDomain<Integer>) domain;
			return new GenericIntDiscreteValue(intDomain);
		}
		else if (Double.class.isAssignableFrom(domain.getElementClass()))
		{
			if (domain instanceof DoubleRangeDomain)
			{
				DoubleRangeDomain rangeDomain = (DoubleRangeDomain)domain;
				if (rangeDomain.getLowerBound() == 0.0 && rangeDomain.getInterval() == 1.0)
				{
					return new SimpleDoubleRangeValue(rangeDomain);
				}
				else
				{
					return new DoubleRangeValue(rangeDomain);
				}
			}
			@SuppressWarnings("unchecked")
			TypedDiscreteDomain<Double> doubleDomain = (TypedDiscreteDomain<Double>) domain;
			return new GenericDoubleDiscreteValue(doubleDomain);
		}
		else
		{
			return new GenericDiscreteValue(domain.asDiscrete());
		}
	}
	
	/**
	 * Creates a {@link FiniteFieldValue} instance for given {@code domain}.
	 * @since 0.07
	 */
	public static FiniteFieldValue create(FiniteFieldDomain domain)
	{
		return new FiniteFieldValue(domain);
	}
	
	/**
	 * Creates a {@code Value} instance for given {@code domain} with specified initial {@code value}.
	 * <p>
	 * Simply calls {@link Value#setObject(Object)} on instance returned by {@link #create(Domain)}.
	 */
	public static Value create(Domain domain, @Nullable Object value)
	{
		Value instance = create(domain);
		instance.setObject(value);
		return instance;
	}
	
	/**
	 * Creates a {@link DiscreteValue} instance for given {@code domain} with specified initial {@code value}.
	 * <p>
	 * Simply calls {@link Value#setObject(Object)} on instance returned by {@link #create(DiscreteDomain)}.
	 */
	public static DiscreteValue create(DiscreteDomain domain, Object value)
	{
		DiscreteValue discrete = create(domain);
		discrete.setObject(value);
		return discrete;
	}
	
	/**
	 * Creates a {@link RealValue} instance for given {@code domain} with specified initial {@code value}.
	 */
	public static RealValue create(RealDomain domain, double value)
	{
		// TODO: use domain
		return new RealValue(value);
	}

	/**
	 * Creates a {@link RealJointValue} instance for given {@code domain} with specified initial {@code value}.
	 */
	public static RealJointValue create(RealJointDomain domain, double[] value)
	{
		// TODO: use domain
		return new RealJointValue(value);
	}
	
	/**
	 * Creates a {@code Value} instance with specified {@code initialValue}.
	 */
	public static Value create(@Nullable Object initialValue)
	{
		if (initialValue instanceof Number)
		{
			Number number = (Number)initialValue;
			if (number instanceof Integer || number instanceof Short || number instanceof Byte)
			{
				return new IntValue(number.intValue());
			}
			else if (number instanceof FiniteFieldNumber)
			{
				return new FiniteFieldValue((FiniteFieldNumber)number);
			}
			else
			{
				return new RealValue(number.doubleValue());
			}
		}
		else if (initialValue instanceof double[])
		{
			return new RealJointValue((double[])initialValue);
		}
		else
		{
			return new ObjectValue(initialValue);
		}
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public abstract Value clone();
	
	@Override
	public String toString()
	{
		return getObject() + "";
	}
	
	/*---------------
	 * Value methods
	 */
	
	/**
	 * Domain for valid contents of value object.
	 */
	public abstract Domain getDomain();
	
	/**
	 * Returns current value as a {@link Object}.
	 * <p>
	 * This may create a new object if underlying representation is a primitive type such as {@code int} or
	 * {@code double}; in such cases it is preferable to use {@link #getInt()}, {@link #getDouble()}, etc.
	 */
	public abstract @Nullable Object getObject();
	
	/**
	 * Sets current value from an {@link Object}.
	 */
	public abstract void setObject(@Nullable Object value);
	
	/**
	 * Returns current value as a {@code boolean}.
	 */
	public boolean getBoolean()
	{
		return FactorFunctionUtilities.toBoolean(getObject());
	}
	
	/**
	 * Sets current value as a {@code double}.
	 */
	public double getDouble()
	{
		return FactorFunctionUtilities.toDouble(getObject());
	}
	
	public double[] getDoubleArray()
	{
		Object val = getObject();
		if (val instanceof double[])
		{
			return (double[])val;
		}
		return new double[] { getDouble() };
	}
	
	/**
	 * Sets current value from a {@code double}.
	 */
	public void setDouble(double value)
	{
		setObject(value);
	}
	
	
	/**
	 * Gets the current value as a {@code FiniteFieldNumber}
	 * @since 0.07
	 */
	public FiniteFieldNumber getFiniteField()
	{
		return (FiniteFieldNumber)requireNonNull(getObject());
	}
	
	/**
	 * Sets the current value from a {@code FiniteFieldNumber}
	 * @since 0.07
	 */
	public void setFiniteField(FiniteFieldNumber value)
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

	/**
	 * Returns current value as a {@code int}.
	 */
	public int getInt()
	{
		return FactorFunctionUtilities.toInteger(getObject());
	}
	
	/**
	 * Sets contents from another value.
	 * <p>
	 * Default implementation uses {@link #getObject()}/{@link #setObject(Object)}. Subclasses implement
	 * this more efficiently to avoid {@link Object} conversion.
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
	
	/**
	 * Sets contents from a {@code int}.
	 */
	public void setInt(int value)
	{
		setObject(value);
	}
	
	/**
	 * Sets contents from a {@code boolean}
	 * Subclasses should override this to set a boolean in the appropriate form
	 * @since 0.07
	 */
	public void setBoolean(boolean value)
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
		return Objects.equals(getObject(), other.getObject());
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
		
		@SuppressWarnings("unchecked")
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

	/**
	 * Creates {@code Value} array with empty or default content from array of Domains.
	 * 
	 * @param domains specifies the domains of the objects. This must have between 1 and {@code objs.length}
	 * elements. If less than the maximum, the last domain will be applied to all remaining objects.
	 * @return array of {@code Value} instances each with the corresponding domain.
	 * @since 0.07
	 */
	public static Value[] createFromDomains(Domain... domains)
	{
		final int nDomains = domains.length;
		
		final Value[] output = new Value[nDomains];
		for (int i = 0; i < nDomains; ++i)
		{
			Value value = create(domains[i]);
			output[i] = value;
		}
		return output;
	}

	/**
	 * Creates {@code Value} array with empty or default content from a DomainList.
	 * 
	 * @param domains specifies the domains of the objects. This must have between 1 and {@code objs.length}
	 * elements. If less than the maximum, the last domain will be applied to all remaining objects.
	 * @return array of {@code Value} instances each with the corresponding domain.
	 * @since 0.07
	 */
	public static Value[] createFromDomains(DomainList<?> domains)
	{
		final int nDomains = domains.size();
		
		final Value[] output = new Value[nDomains];
		for (int i = 0; i < nDomains; ++i)
		{
			Value value = create(domains.get(i));
			output[i] = value;
		}
		return output;
	}

	/**
	 * Copies current contents of {@code values} into {@code output} array.
	 */
	public static <T extends Value> Object[] toObjects(T[] values, Object[] output)
	{
		for (int i = values.length; --i>=0;)
		{
			output[i] = values[i].getObject();
		}
		return output;
	}
	
	/**
	 * Converts an array of {@code Value} to an array of {@link Object} containing the current
	 * contents of the values.
	 */
	public static <T extends Value> Object[] toObjects(T[] values)
	{
		return toObjects(values, new Object[values.length]);
	}
}
