/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.RealJointDomain;

public class PRealJointDomain extends PDomain 
{
	public PRealJointDomain(RealJointDomain domain)
	{
		super(domain);
	}

	public PRealJointDomain(Object [] domains) 
	{		
		super(new RealJointDomain(unwrapRealDomains(domains)));
		// TODO Auto-generated constructor stub
	}
	
	private static RealDomain [] unwrapRealDomains(Object [] domains)
	{
		RealDomain [] rdomains = new RealDomain[domains.length];
		for (int i = 0; i < rdomains.length; i++)
			rdomains[i] = (RealDomain) ((PRealDomain)domains[i]).getModelerObject();
		return rdomains;
		
	}

	public int getNumVars()
	{
		return ((RealJointDomain)getModelerObject()).getNumVars();
	}
}
