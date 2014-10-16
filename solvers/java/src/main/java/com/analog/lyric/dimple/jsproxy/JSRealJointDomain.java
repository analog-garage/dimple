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

import com.analog.lyric.dimple.model.domains.RealJointDomain;

/**
 * Javascript API representation for a multi-dimensional, continuous real domain with optional bounds.
 * <p>
 * This wraps an underlying Dimple {@link RealJointDomain} object.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class JSRealJointDomain extends JSDomain<RealJointDomain>
{
	JSRealJointDomain(JSDomainFactory factory, RealJointDomain domain)
	{
		super(factory, domain);
	}

	@Override
	public JSDomain.Type getDomainType()
	{
		return JSDomain.Type.REAL_JOINT;
	}
	
	/**
	 * Returns the scalar real domain for the specified dimension
	 * @param dimension must be non-negative and less than {@link #dimensions()}.
	 * @since 0.07
	 */
	public JSRealDomain getRealDomain(int dimension)
	{
		return _factory.wrap(_delegate.getRealDomain(dimension));
	}
}
