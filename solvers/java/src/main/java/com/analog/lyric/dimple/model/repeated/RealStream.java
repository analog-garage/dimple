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

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;

public class RealStream extends VariableStreamBase
{
	public RealStream()
	{
		this(RealDomain.unbounded());
	}

	public RealStream(RealDomain domain)
	{
		super(domain);
	}
	
	/**
	 * Construct with bounded domain.
	 * @param lower is the lower bound of the stream's domain
	 * @param upper is the upper bound of the stream's domain
	 * @since 0.07
	 */
	public RealStream(double lower, double upper)
	{
		this(RealDomain.create(lower,upper));
	}

	
	@Override
	public RealDomain getDomain()
	{
		return (RealDomain)super.getDomain();
	}

	@Override
	protected Variable instantiateVariable(Domain domain)
	{
		return new Real((RealDomain)domain);
	}


}
