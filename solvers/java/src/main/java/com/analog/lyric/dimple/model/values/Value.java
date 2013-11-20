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

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.domains.Domain;

public abstract class Value implements Cloneable
{
	/*--------------
	 * Construction
	 */
	
	/**
	 * Returns concrete {@link Value} subclass to be used for values of given {@code domain}.
	 */
	public static Class<? extends Value> classForDomain(Domain domain)
	{
		if (domain.isDiscrete())
		{
			return DiscreteValue.class;
		}
		else if (domain.isReal())
		{
			return RealValue.class;
		}
		else if (domain.isRealJoint())
		{
			return RealJointValue.class;
		}
		else
		{
			return ObjectValue.class;
		}
	}
	
	public static Value create(Domain domain)
	{
		if (domain.isDiscrete())
		{
			return new DiscreteValue(domain.asDiscrete());
		}
		else if (domain.isReal())
		{
			return new RealValue(0.0);
		}
		else if (domain.isRealJoint())
		{
			return new RealJointValue(ArrayUtil.EMPTY_DOUBLE_ARRAY);
		}
		else
		{
			return new ObjectValue();
		}
	}
	
	public static Value create(Domain domain, Object value)
	{
		if (domain.isDiscrete())
		{
			return new DiscreteValue(value, domain.asDiscrete());
		}
		else if (domain.isReal())
		{
			return new RealValue(((Number)value).doubleValue());
		}
		else if (domain.isRealJoint())
		{
			return new RealJointValue((double[])value);
		}
		else
		{
			return new ObjectValue(value);
		}
	}
	
	public static Value create(Object object)
	{
		if (object instanceof Number)
		{
			return new RealValue(((Number)object).doubleValue());
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
	
	public abstract Object getObject();
	public abstract void setObject(Object value);
	
	public double getDouble()
	{
		return FactorFunctionUtilities.toDouble(getObject());
	}
	
	public void setDouble(double value)
	{
		setObject(value);
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
			assert(valueClass.isAssignableFrom(Value.classForDomain(domain)));
			
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
