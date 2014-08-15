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

package com.analog.lyric.dimple.test;

import org.junit.Before;

import com.analog.lyric.dimple.environment.DimpleEnvironment;

/**
 * Provides shared standard setup for Dimple tests
 * <p>
 * All dimple tests should inherit from this class.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class DimpleTestBase
{
	@Before
	public void setup()
	{
		// Reset the environment for each test in case a previous test has modified it.
		DimpleEnvironment env = new DimpleEnvironment();
		DimpleEnvironment.setActive(env);
		DimpleEnvironment.setDefaultEnvironment(env);
	}
}
