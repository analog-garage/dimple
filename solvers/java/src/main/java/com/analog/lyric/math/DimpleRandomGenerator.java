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

package com.analog.lyric.math;

import static com.analog.lyric.dimple.environment.DimpleEnvironment.*;

import org.apache.commons.math3.random.RandomGenerator;

import com.analog.lyric.dimple.environment.DimpleEnvironment;

/**
 * @deprecated as of release 0.08 use {@link DimpleEnvironment#activeRandom()}
 */
@Deprecated
public class DimpleRandomGenerator
{
	/**
	 * @deprecated as of release 0.08 use {@link DimpleEnvironment#activeRandom()}.
	 */
	@Deprecated
	public static RandomGenerator rand = activeRandom()._randGenerator;
	
	/**
	 * @deprecated as of release 0.08 use {@link DimpleRandom#setSeed(long)}
	 * on {@link DimpleEnvironment#activeRandom} instance.
	 */
	@Deprecated
	public static void setSeed(long seed)
	{
		rand.setSeed(seed);
		activeRandom().setSeed(seed);
	}
	
	/**
	 * @deprecated as of release 0.08 use {@link DimpleRandom#nextBinomial}
	 * from {@link DimpleEnvironment#activeRandom} instance.
	 */
	@Deprecated
	public static final int randomBinomial(int N, double p)
	{
		return activeRandom().nextBinomial(N,p);
	}

}
