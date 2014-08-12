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

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.util.misc.Nullable;

public interface IProposalKernel
{
	public Proposal next(Value currentValue, Domain variableDomain);
	public void setParameters(Object... parameters);
	public @Nullable Object[] getParameters();
	
	/**
	 * Sets the kernel parameters from options looked up in provided {@code optionHolder}.
	 * <p>
	 * @param optionHolder a non-null option holder.
	 * @since 0.07
	 */
	public void setParametersFromOptions(IOptionHolder optionHolder);
}
