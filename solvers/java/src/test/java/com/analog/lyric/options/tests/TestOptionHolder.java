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

import com.analog.lyric.options.AbstractOptionHolder;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptions;

/**
 * 
 * @since 0.06
 * @author CBarber
 */
public class TestOptionHolder
{
	@Test
	public void test()
	{
		IOptionHolder holder = new AbstractOptionHolder(){};
		assertNull(holder.getLocalOptions(false));
		assertNull(holder.getLocalOptions(true));
		assertNull(holder.getOptionParent());
		holder.clearLocalOptions(); // doesn't do anything
		assertTrue(holder.getRelevantOptionKeys().isEmpty());
		expectThrow(UnsupportedOperationException.class, holder, "createLocalOptions");
		expectThrow(UnsupportedOperationException.class, holder, "createLocalOptions");
		
		IOptions options1 = holder.options();
		assertNotNull(options1);
		assertTrue(options1.isEmpty());
		assertSame(holder, options1.getOptionHolder());

		IOptions options2 = holder.options();
		assertNotNull(options2);
		assertNotSame(options1, options2);
	}
}
