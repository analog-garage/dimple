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

import java.io.Serializable;

public abstract class Domain implements Serializable
{
	private static final long serialVersionUID = 1L;

	public DiscreteDomain asDiscrete() { return null; }
	public RealDomain asReal() { return null; }
	
	public boolean isDiscrete() { return false; }
	public boolean isJoint() { return false; }
	public boolean isReal() { return false; }
	
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
