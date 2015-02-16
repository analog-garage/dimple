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
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.variables.RealJoint;

public class RealJointStream extends VariableStreamBase<RealJoint>
{
	public RealJointStream(RealJointDomain domain, String namePrefix)
	{
		super(domain, namePrefix);
	}
	
	public RealJointStream(int numVars)
	{
		this(RealJointDomain.create(numVars));
	}

	public RealJointStream(RealJointDomain domain)
	{
		super(domain, null);
	}
	
	@Override
	public RealJointDomain getDomain()
	{
		return (RealJointDomain)super.getDomain();
	}

	@Override
	protected RealJoint instantiateVariable(Domain domain)
	{
		return new RealJoint((RealJointDomain)domain);
	}

	@Override
	protected Class<? extends RealJoint> variableType()
	{
		return RealJoint.class;
	}
}
