/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.options.IOptionConfigurable;

public interface IProposalKernel extends IOptionConfigurable
{
	public Proposal next(Value currentValue, Domain variableDomain);
	
	/**
	 * @deprecated Will be removed in future release. Instead set options on
	 * corresponding option holder (e.g. the variable or the graph).
	 */
	@Deprecated
	public void setParameters(Object... parameters);
	
	/**
	 * @deprecated Will be removed in future release. Instead use {@link #getOptionConfiguration}.
	 */
	@Deprecated
	public @Nullable Object[] getParameters();
}
