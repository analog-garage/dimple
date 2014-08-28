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

package com.analog.lyric.dimple.solvers.gibbs.samplers.block;

import com.analog.lyric.util.misc.Internal;


/**
 * Sampler that updates a block of variables instead of updating one variable at a time.
 * Extends IBlockSampler specifically for MCMC-based samplers.
 * 
 * @since 0.07
 * @author jeffb
 */
public interface IBlockMCMCSampler extends IBlockSampler
{
	/**
	 * Get the rejection rate of the sampler
	 * @return rejection rate
	 * @since 0.07
	 */
	public double getRejectionRate();
	
	/**
	 * Clear the rejection rate statistics
	 * @since 0.07
	 */
	public void resetRejectionRateStats();
	
	@Internal
	public long getUpdateCount();
	@Internal
	public long getRejectionCount();

}
