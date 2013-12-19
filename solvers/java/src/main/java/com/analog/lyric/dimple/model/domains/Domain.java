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
	public DiscreteDomain asDiscrete() { return null; }
	
	/**
	 * If domain {@link #isReal()} returns object cast to {@link RealDomain},
	 * otherwise returns null.
	 */
	public RealDomain asReal() { return null; }
	
	/**
	 * If domain {@link #isRealJoint()} returns object cast to {@link RealJointDomain},
	 * otherwise returns null.
	 */
	public RealJointDomain asRealJoint() { return null; }
	
	/**
	 * If domain {@link #isComplex()} returns object cast to {@link ComplexDomain},
	 * otherwise returns null.
	 */
	public ComplexDomain asComplex() { return null; }
	
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
	 * @return true if {@code value} is a valid member of the domain. Implementors
	 * should not throw a cast exception.
	 */
	public abstract boolean inDomain(Object value);
	
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
	public DimpleException domainError(Object value)
	{
		return new DimpleException("'%s' is not a member of domain '%s'", value, this);
	}
}
