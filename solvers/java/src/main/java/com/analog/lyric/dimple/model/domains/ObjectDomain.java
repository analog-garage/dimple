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

package com.analog.lyric.dimple.model.domains;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Domain of all Java objects.
 * <p>
 * NOTE: currently there is no random variable type that corresponds to this domain.
 * 
 *  @since 0.05
 */
@Immutable
public final class ObjectDomain extends Domain
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private static final ObjectDomain INSTANCE = new ObjectDomain();
	
	// Currently no support for bounds, since you can usually use IntRangeDomain for that.
	
	/*--------------
	 * Construction
	 */
	
	private ObjectDomain()
	{
		super(617);
	}
	
	public static ObjectDomain instance()
	{
		return INSTANCE;
	}
	
	@Override
	protected ObjectDomain intern()
	{
		return INSTANCE;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object other)
	{
		return this == other || other instanceof ObjectDomain;
	}
	
	/*----------------
	 * Domain methods
	 */
	
	@Override
	public boolean inDomain(@Nullable Object value)
	{
		return true;
	}
	
	@Override
	public boolean isNumber()
	{
		return false;
	}
	
	@Override
	public boolean isNumeric()
	{
		return false;
	}
	
	@Override
	public boolean isScalar()
	{
		return false;
	}
}
