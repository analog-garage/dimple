/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.core.proposalKernels;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.Proposal;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.util.misc.Nullable;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class MyProposalKernel implements IProposalKernel
{

	@Override
	public Proposal next(Value currentValue, Domain variableDomain)
	{
		return new Proposal(currentValue);
	}

	@Override
	public void setParameters(Object... parameters)
	{
	}

	@Override
	public @Nullable Object[] getParameters()
	{
		return null;
	}

	@Override
	public void setParametersFromOptions(IOptionHolder optionHolder)
	{
	}
}
