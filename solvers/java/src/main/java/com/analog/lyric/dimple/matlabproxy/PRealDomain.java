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

import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.util.misc.Matlab;

@Matlab
public class PRealDomain extends PDomain
{
	public PRealDomain(RealDomain domain)
	{
		super(domain);
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public RealDomain getDelegate()
	{
		return (RealDomain)super.getDelegate();
	}
	
	@Override
	public RealDomain getModelerObject()
	{
		return (RealDomain)super.getModelerObject();
	}
	
	@Override
	public boolean isReal()
	{
		return true;
	}
	
	/*---------------------
	 * PRealDomain methods
	 */
	
	public double getUpperBound()
	{
		return getModelerObject().getUpperBound();
	}
	public double getLowerBound()
	{
		return getModelerObject().getLowerBound();
	}
}
