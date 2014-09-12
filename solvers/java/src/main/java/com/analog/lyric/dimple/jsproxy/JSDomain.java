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

package com.analog.lyric.dimple.jsproxy;

import com.analog.lyric.dimple.model.domains.Domain;
import org.eclipse.jdt.annotation.Nullable;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public abstract class JSDomain<D extends Domain> extends JSProxyObject<D>
{
	private final JSDomainFactory _factory;
	
	JSDomain(JSDomainFactory factory, D domain)
	{
		super(domain);
		_factory = factory;
	}

	public enum Type
	{
		DISCRETE,
		REAL,
		REAL_JOINT;
	}
	
	/*-----------------------
	 * JSProxyObject methods
	 */
	
	@Override
	public DimpleApplet getApplet()
	{
		return _factory._applet;
	}

	/*------------------
	 * JSDomain methods
	 */
	
	public boolean contains(@Nullable Object value)
	{
		return _delegate.inDomain(value);
	}
	
	/**
	 * Number of dimensions of domain elements.
	 * <p>
	 * Returns one for scalar domains, or a value greater than one for
	 * continuous or discrete domains with more than one dimension.
	 * 
	 * @since 0.07
	 */
	public int dimensions()
	{
		return _delegate.getDimensions();
	}
	
	/**
	 * Returns number of elements if this is a discrete domain, otherwise -1.
	 * @since 0.07
	 */
	public int discreteSize()
	{
		return -1;
	}
	
	public abstract Type getDomainType();
	
	public boolean isDiscrete()
	{
		return _delegate.isDiscrete();
	}
	
	public boolean isReal()
	{
		return _delegate.isReal();
	}
	
	public boolean isRealJoint()
	{
		return _delegate.isRealJoint();
	}
}
