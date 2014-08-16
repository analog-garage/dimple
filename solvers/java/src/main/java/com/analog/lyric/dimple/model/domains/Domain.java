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

import java.io.Serializable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.variables.Complex;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.RealJoint;
import org.eclipse.jdt.annotation.Nullable;
import com.google.common.math.DoubleMath;

/**
 * Base class for variable domains, which specify a set of valid values for
 * a variable type.
 */
public abstract class Domain implements Serializable
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	private final int _hashCode;
	
	/*--------------
	 * Construction
	 */
	
	Domain(int hashCode)
	{
		_hashCode = hashCode;
	}
	
	/*---------------
	 * Serialization
	 */

	/**
	 * If supported by subclass, returns interned version
	 * of domain.
	 */
	protected Domain intern()
	{
		return this;
	}
	
	protected Object readResolve()
	{
		// Replace with interned version of domain, if available.
		return intern();
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public final int hashCode()
	{
		return _hashCode;
	}

	/*----------------
	 * Domain methods
	 */
	
	/**
	 * If domain {@link #isDiscrete()} returns object cast to {@link DiscreteDomain},
	 * otherwise returns null.
	 */
	public @Nullable DiscreteDomain asDiscrete() { return null; }
	
	/**
	 * If domain {@link #isReal()} returns object cast to {@link RealDomain},
	 * otherwise returns null.
	 */
	public @Nullable RealDomain asReal() { return null; }
	
	/**
	 * If domain {@link #isRealJoint()} returns object cast to {@link RealJointDomain},
	 * otherwise returns null.
	 */
	public @Nullable RealJointDomain asRealJoint() { return null; }
	
	/**
	 * If domain {@link #isComplex()} returns object cast to {@link ComplexDomain},
	 * otherwise returns null.
	 */
	public @Nullable ComplexDomain asComplex() { return null; }
	
	/**
	 * The number of dimensions for elements of the domain.
	 * <p>
	 * Returns one for {@linkplain #isScalar() scalar} domains. For {@link RealJointDomain}
	 * and {@link JointDiscreteDomain}, returns the size of the array needed to hold the
	 * components of a single element.
	 * <p>
	 * @since 0.07
	 */
	public int getDimensions()
	{
		return 1;
	}
	
	/**
	 * True if domain only contains values that can be represented using an {@code int}.
	 */
	public boolean hasIntCompatibleValues() { return false; }
	
	/**
	 * True if the elements of the domain are bounded to some subset.
	 * <p>
	 * The default implementation returns true if not {@link #isDiscrete()}.
	 * <p>
	 * @since 0.06
	 */
	public boolean isBounded()
	{
		return !isDiscrete();
	}
	
	/**
	 * True if domain is an instance of {@link Discrete}.
	 * @see #asDiscrete()
	 */
	public boolean isDiscrete() { return false; }
	
	/**
	 * True if values of domain are (32-bit) integers.
	 * <p>
	 * @since 0.05
	 */
	public boolean isIntegral() { return false; }
	
	/**
	 * True if domain is an instance of {@link RealDomain}.
	 * @see #asReal()
	 */
	public boolean isReal() { return false; }
	
	/**
	 * True if domain is an instance of {@link RealJoint}
	 * @see #asRealJoint()
	 */
	public boolean isRealJoint() { return false; }
	
	/**
	 * True if domain is an instance of {@link Complex}
	 * @see #asComplex()
	 */
	public boolean isComplex() { return false; }
	
	/**
	 * True if all values of domain are scalar numbers.
	 * @since 0.05
	 */
	public boolean isNumber() { return true; }
	
	/**
	 * True if all values of domain are (not necessarily scalar) numbers.
	 * @since 0.05
	 */
	public boolean isNumeric() { return true; }
	
	/**
	 * True if all elements of domain are scalar values.
	 * <p>
	 * This is false for {@link RealJointDomain} and {@link JointDiscreteDomain}.
	 * @since 0.05
	 */
	public boolean isScalar() { return true; }
	
	/**
	 * @return true if {@code value} is a valid member of the domain. Implementors
	 * should not throw a cast exception.
	 */
	public abstract boolean inDomain(@Nullable Object value);
	
	/**
	 * @return true if {@code representation} corresponds to a valid member of the domain for
	 * domains that can represent values using an alternate representation, such as the index
	 * of a {@link Discrete} domain with enumerated elements.
	 * <p>
	 * The default implementation simply invokes {@link #inDomain(Object)}.
	 */
	public boolean containsValueWithRepresentation(Object representation)
	{
		return inDomain(representation);
	}

	/**
	 * @return an exception stating that {@code value} is not a member of this domain.
	 */
	public DimpleException domainError(@Nullable Object value)
	{
		return new DimpleException("'%s' is not a member of domain '%s'", value, this);
	}
	
	/*----------------
	 * Static methods
	 */
	
	/**
	 * True if {@code type} is one of: {@link Integer}, {@link Short}, {@link Byte}.
	 */
	public static boolean isIntCompatibleClass(Class<?> type)
	{
		return Number.class.isAssignableFrom(type) &&
			(type == Integer.class ||
			type == Short.class ||
			type == Byte.class);
	}
	
	public static boolean isIntCompatibleValue(Object value)
	{
		return value instanceof Number && isIntCompatibleValue((Number)value);
	}
	
	public static boolean isIntCompatibleValue(Number value)
	{
		if (isIntCompatibleClass(value.getClass()))
		{
			return true;
		}
		
		if (value instanceof Long)
		{
			return isIntCompatibleValue(((Long)value).longValue());
		}
		
		return isIntCompatibleValue(value.doubleValue());
	}
	
	public static boolean isIntCompatibleValue(double value)
	{
		return DoubleMath.isMathematicalInteger(value) && isIntCompatibleValue((long)value);
	}
	
	public static boolean isIntCompatibleValue(long value)
	{
		return Math.abs(value) <= Integer.MAX_VALUE;
	}
}
