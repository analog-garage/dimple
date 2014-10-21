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

import com.analog.lyric.dimple.model.domains.DiscreteDomain;

/**
 * Javascript API representation of Dimple discrete domain.
 * <p>
 * Delegates to underlying {@link DiscreteDomain}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class JSDiscreteDomain extends JSDomain<DiscreteDomain>
{
	JSDiscreteDomain(JSDomainFactory factory, DiscreteDomain domain)
	{
		super(factory, domain);
	}

	/*------------------
	 * JSDomain methods
	 */
	
	@Override
	public int discreteSize()
	{
		return _delegate.size();
	}
	
	@Override
	public JSDomain.Type getDomainType()
	{
		return JSDomain.Type.DISCRETE;
	}
	
	/**
	 * Returns the indexed element in the domain.
	 * @param i is in the range 0 to {@link #discreteSize()} - 1.
	 * @since 0.07
	 */
	public Object getElement(int i)
	{
		return _delegate.getElement(i);
	}
}
