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

package com.analog.lyric.dimple.matlabproxy;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.util.misc.Matlab;

@Immutable
@Matlab
public class PDiscreteDomain extends PDomain
{
	public PDiscreteDomain(DiscreteDomain domain)
	{
		super(domain);
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public DiscreteDomain getDelegate()
	{
		return (DiscreteDomain)super.getDelegate();
	}
	
	@Override
	public DiscreteDomain getModelerObject()
	{
		return (DiscreteDomain)super.getModelerObject();
	}
	
	@Override
	public boolean isDiscrete()
	{
		return true;
	}
	
	/*-------------------------
	 * PDiscreteDomain methods
	 */
	
	public Object [] getElements()
	{
		DiscreteDomain domain = getModelerObject();
		// For some reason MATLAB cares about the component type of the array and for example will
		// implicitly convert strings from an Object[] but not a String[]!
		return domain.getElements(new Object[domain.size()]);
	}
}
