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

package com.analog.lyric.dimple.solvers.gibbs.samplers.generic;

import com.analog.lyric.dimple.model.values.Value;

public interface IMCMCSampler extends IGenericSampler
{
	/**
	 * Generate a new sample value.
	 * 
	 * @param sampleValue
	 * @param samplerClient
	 * @return true if a new sample was generated. False if sample was rejected.
	 * @since 0.06 - boolean return added
	 */
	public boolean nextSample(Value sampleValue, ISamplerClient samplerClient);
}
