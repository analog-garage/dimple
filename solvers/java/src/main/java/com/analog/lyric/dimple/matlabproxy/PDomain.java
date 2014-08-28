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

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.util.misc.Matlab;

@Immutable
@Matlab
public class PDomain extends PObject
{
	private final Domain _domain;
	
	PDomain(Domain domain)
	{
		_domain = domain;
	}
	
	/*-----------------
	 * PObject methods
	 */

	@Override
	public boolean isDomain()
	{
		return true;
	}
	
	@Override
	public Domain getDelegate()
	{
		return _domain;
	}
	
	@Override
	public Domain getModelerObject()
	{
		return _domain;
	}
}
