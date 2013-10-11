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

package com.analog.lyric.dimple.matlabproxy.repeated;

import com.analog.lyric.dimple.matlabproxy.PDiscreteDomain;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.repeated.DiscreteStream;
import com.analog.lyric.dimple.model.repeated.VariableStreamBase;

public class PDiscreteStream extends PVariableStreamBase
{
 
	public PDiscreteStream(PDiscreteDomain domain, int numVars)  
	{
		super(domain.getModelerObject(),numVars);
	}

	@Override
	protected VariableStreamBase createVariable(Domain domain) 
	{
		return new DiscreteStream((DiscreteDomain)domain);
	}

}
