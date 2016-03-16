/*******************************************************************************
*   Copyright 2016 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core.proposalKernels;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.Option;

/**
 * Used as a placeholder if there is no default proposal kernel and one must be explicitly specified.
 * 
 * @since 0.07.1
 */
public class NullProposalKernel implements IProposalKernel
{
	@Override
	public Proposal next(Value currentValue, Domain variableDomain)
	{
		throw new DimpleException("Invalid proposal kernel; must explicitly set a valid kernel");
	}
	
	@Deprecated
	@Override
	public void setParameters(Object... parameters)
	{
	}
	
	@SuppressWarnings("null")
	@Deprecated
	@Override
	public Object[] getParameters()
	{
		return null;
	}
	
	@Override
	public List<Option<?>> getOptionConfiguration(@Nullable List<Option<?>> list)
	{
		return new LinkedList<Option<?>>();
	}
	
	@Override
	public void configureFromOptions(IOptionHolder optionHolder)
	{
	}
}
