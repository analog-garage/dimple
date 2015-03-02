/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.sumproduct.sampledfactor;

import com.analog.lyric.dimple.solvers.interfaces.ISolverEdge;
import com.analog.lyric.util.misc.Internal;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 * @category internal
 */
@Internal
public interface ISumProductSampledEdge<Message> extends ISolverEdge
{
	public Message getVarToFactorMsg();
	
	public Message getFactorToVarMsg();
	
	@Internal
	public void moveMessages(ISumProductSampledEdge<?> other);
	
	@Internal
	public void setFactorToVarDirection();
	
	@Internal
	public void setVarToFactorDirection();
	
	@Internal
	public void setFactorToVarMsgFromSamples();
}
