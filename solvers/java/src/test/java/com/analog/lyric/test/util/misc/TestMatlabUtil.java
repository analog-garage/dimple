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

package com.analog.lyric.test.util.misc;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import com.analog.lyric.util.misc.Matlab;
import com.analog.lyric.util.misc.MatlabUtil;

/**
 * Unit tests for {@link MatlabUtil} functions
 */
public class TestMatlabUtil
{
	@Matlab(wrapper="A")
	class A
	{
	}
	
	@Matlab
	class B extends A
	{
	}
	
	class C extends B
	{
	}
	
	@Matlab(wrapper="D")
	class D extends B
	{
	}
	
	@Test
	public void testWrapper()
	{
		assertNull(MatlabUtil.wrapper("foo"));
		assertNull(MatlabUtil.wrapper(new HashSet<String>()));
		assertEquals("A", MatlabUtil.wrapper(new A()));
		assertEquals("A", MatlabUtil.wrapper(new B()));
		assertEquals("A", MatlabUtil.wrapper(new C()));
		assertEquals("D", MatlabUtil.wrapper(new D()));
	}
}
