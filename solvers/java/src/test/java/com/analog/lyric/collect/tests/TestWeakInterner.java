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

package com.analog.lyric.collect.tests;

import static org.junit.Assert.*;

import java.lang.ref.WeakReference;

import org.junit.Test;

import com.analog.lyric.collect.WeakInterner;
import com.google.common.collect.Interner;


/**
 * Test for {@link WeakInterner} class
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestWeakInterner
{
	@Test
	public void test()
	{
		Interner<Double> interner = WeakInterner.create();
		
		Double d42 = new Double(42);
		Double d42a = new Double(42);
		assertNotSame(d42, d42a);
		
		assertSame(d42, interner.intern(d42));
		assertSame(d42, interner.intern(d42a));
		
		Double d23 = new Double(23);

		WeakReference<Double> d23ref = new WeakReference<Double>(new Double(23));
		assertNotSame(d23, interner.intern(d23ref.get()));
		assertNotSame(d23, interner.intern(d23));
		
		System.gc();
		
		assertNull(d23ref.get());
		assertSame(d23, interner.intern(d23));
	}
}
