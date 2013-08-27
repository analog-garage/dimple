package com.analog.lyric.collect.tests;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.BitSet;

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
	
}
