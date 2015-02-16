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

package com.analog.lyric.dimple.model.repeated;

import com.analog.lyric.dimple.model.domains.ComplexDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.variables.Complex;

/**
 * Variable stream for complex variables.
 * @since 0.07
 */
public class ComplexStream extends RealJointStream
{
	public ComplexStream()
	{
		super(ComplexDomain.create());
	}

	public ComplexStream(ComplexDomain domain)
	{
		super(domain);
	}
	
	@Override
	public ComplexDomain getDomain()
	{
		return (ComplexDomain)super.getDomain();
	}

	@Override
	protected Complex instantiateVariable(Domain domain)
	{
		return new Complex((ComplexDomain)domain);
	}
	
	@Override
	protected Class<Complex> variableType()
	{
		return Complex.class;
	}

	@Override
	public Complex get(int index)
	{
		return (Complex)super.get(index);
	}
	
	@Override
	public Complex get(int index, boolean createIfDoesntExist)
	{
		return (Complex)super.get(index, createIfDoesntExist);
	}
	
	@Override
	public Complex[] getVariables()
	{
		return (Complex[])super.getVariables();
	}
}
