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

package com.analog.lyric.dimple.model.variables;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.FiniteFieldDomain;



public class FiniteFieldVariable extends Discrete
{
	public FiniteFieldVariable(int primitivePolynomial)
	{
		this(DiscreteDomain.finiteField(primitivePolynomial));
	}
	
	public FiniteFieldVariable(FiniteFieldDomain domain)
	{
		super(domain);
	}
	
	public FiniteFieldDomain getFiniteFieldDomain()
	{
		return getDomain();
	}
	
    @Override
	public FiniteFieldDomain getDomain()
    {
    	return (FiniteFieldDomain)super.getDomain();
    }

}
