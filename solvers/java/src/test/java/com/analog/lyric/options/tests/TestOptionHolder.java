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

package com.analog.lyric.options.tests;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.StatelessOptionHolder;
import com.analog.lyric.options.StringOptionKey;

/**
 * 
 * @since 0.06
 * @author CBarber
 */
public class TestOptionHolder
{
	public final static StringOptionKey K = new StringOptionKey(TestOptionHolder.class, "K");
	
	@Test
	public void test()
	{
		IOptionHolder holder = new StatelessOptionHolder(){};
		assertNull(holder.getOptionParent());
		holder.clearLocalOptions(); // doesn't do anything
		expectThrow(UnsupportedOperationException.class, holder, "setOption", K, "foo");
	}
}
