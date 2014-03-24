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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.BitSet;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.collect.BitSetUtil;

public class TestBitSetUtil
{
	@Test
	public void bitsetFromIndices()
	{
		BitSet bits = BitSetUtil.bitsetFromIndices(100, 1,4,7,50);
		assertEquals(4, bits.cardinality());
		assertTrue(bits.get(1));
		assertTrue(bits.get(4));
		assertTrue(bits.get(7));
		assertTrue(bits.get(50));
		
		try
		{
			BitSetUtil.bitsetFromIndices(4, 1, 0, -1);
			fail("expected IndexOutOfBoundsException");
		}
		catch (IndexOutOfBoundsException ex)
		{
			assertThat(ex.getMessage(), containsString("-1 out of range [0, 3]"));
		}
		try
		{
			BitSetUtil.bitsetFromIndices(4, 0, 2, 4);
			fail("expected IndexOutOfBoundsException");
		}
		catch (IndexOutOfBoundsException ex)
		{
			assertThat(ex.getMessage(), containsString("4 out of range [0, 3]"));
		}
		try
		{
			BitSetUtil.bitsetFromIndices(4, 1, 0, 1);
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Duplicate index 1"));
		}
	}
	
	@Test
	public void testIntegerOperations()
	{
		Random rand = new Random(123);
		
		for (int i = 0; i < 100; ++i)
		{
			testIntegerOperations(rand.nextInt());
			testIntegerOperations(rand.nextLong());
		}
	}
	
	public void testIntegerOperations(int x)
	{
		assertEquals(0, BitSetUtil.clearMask(x, x));
		assertEquals(x, BitSetUtil.setMask(0, x));
		assertTrue(BitSetUtil.isMaskSet(x, x));
		assertEquals(-1, BitSetUtil.setMask(-1, x));
		assertEquals(-1, BitSetUtil.setMask(BitSetUtil.clearMask(-1, x), x));
		
		int mask = 1;
		for (int bit = 0; bit < 32; ++bit, mask <<= 1)
		{
			if ((mask & x) != 0)
			{
				assertTrue(BitSetUtil.isBitSet(x, bit));
				assertTrue(BitSetUtil.isMaskSet(x, mask));
				assertEquals(x ^ mask, BitSetUtil.clearBit(x, bit));
				assertEquals(x ^ mask, BitSetUtil.clearMask(x, mask));
				assertEquals(x, BitSetUtil.setBit(x, bit));
				assertEquals(x, BitSetUtil.setMask(x, mask));
			}
			else
			{
				assertFalse(BitSetUtil.isBitSet(x, bit));
				assertFalse(BitSetUtil.isMaskSet(x, mask));
				assertEquals(x, BitSetUtil.clearBit(x, bit));
				assertEquals(x, BitSetUtil.clearMask(x, mask));
				assertEquals(x|mask, BitSetUtil.setBit(x, bit));
				assertEquals(x|mask, BitSetUtil.setMask(x, mask));
			}
		}
	}

	public void testIntegerOperations(long x)
	{
		assertEquals(0, BitSetUtil.clearMask(x, x));
		assertEquals(x, BitSetUtil.setMask(0, x));
		assertTrue(BitSetUtil.isMaskSet(x, x));
		assertEquals(-1, BitSetUtil.setMask(-1, x));
		assertEquals(-1, BitSetUtil.setMask(BitSetUtil.clearMask(-1, x), x));
		
		long mask = 1;
		for (int bit = 0; bit < 64; ++bit, mask <<= 1)
		{
			if ((mask & x) != 0)
			{
				assertTrue(BitSetUtil.isBitSet(x, bit));
				assertTrue(BitSetUtil.isMaskSet(x, mask));
				assertEquals(x ^ mask, BitSetUtil.clearBit(x, bit));
				assertEquals(x ^ mask, BitSetUtil.clearMask(x, mask));
				assertEquals(x, BitSetUtil.setBit(x, bit));
				assertEquals(x, BitSetUtil.setMask(x, mask));
			}
			else
			{
				assertFalse(BitSetUtil.isBitSet(x, bit));
				assertFalse(BitSetUtil.isMaskSet(x, mask));
				assertEquals(x, BitSetUtil.clearBit(x, bit));
				assertEquals(x, BitSetUtil.clearMask(x, mask));
				assertEquals(x|mask, BitSetUtil.setBit(x, bit));
				assertEquals(x|mask, BitSetUtil.setMask(x, mask));
			}
		}
	}
}
