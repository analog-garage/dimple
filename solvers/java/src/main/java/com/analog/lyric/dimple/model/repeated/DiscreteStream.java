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

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Discrete;

public class DiscreteStream extends VariableStreamBase<Discrete>
{
	public DiscreteStream(DiscreteDomain domain, String namePrefix)
	{
		super(domain, namePrefix);
	}
	
	public DiscreteStream(DiscreteDomain domain)
	{
		super(domain, null);
	}
	
	public DiscreteStream(Object ... domain)
	{
		this(DiscreteDomain.create(domain));
	}
	
	@Override
	protected Discrete instantiateVariable(Domain domain)
	{
		return new Discrete((DiscreteDomain)domain);
	}

	@Override
	protected Class<? extends Discrete> variableType()
	{
		return Discrete.class;
	}
	
	@Override
	public Bit get(int index)
	{
		return (Bit)super.get(index);
	}
	
	@Override
	public Bit get(int index, boolean createIfDoesntExist)
	{
		return (Bit)super.get(index, createIfDoesntExist);
	}
	
	@Override
	public Bit[] getVariables()
	{
		return (Bit[])super.getVariables();
	}
}
