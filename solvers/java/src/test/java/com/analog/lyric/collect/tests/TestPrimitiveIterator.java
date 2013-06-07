package com.analog.lyric.collect.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.collect.AbstractPrimitiveArrayIterator;
import com.analog.lyric.collect.DoubleArrayIterable;
import com.analog.lyric.collect.DoubleArrayIterator;
import com.analog.lyric.collect.DoubleDataInputIterator;
import com.analog.lyric.collect.FloatArrayIterable;
import com.analog.lyric.collect.FloatArrayIterator;
import com.analog.lyric.collect.IntArrayIterable;
import com.analog.lyric.collect.IntArrayIterator;
import com.analog.lyric.collect.LongArrayIterable;
import com.analog.lyric.collect.LongArrayIterator;
import com.analog.lyric.collect.PrimitiveIterable;
import com.analog.lyric.collect.PrimitiveIterator;

public class TestPrimitiveIterator
{

	@Test
	public void test()
	{
		Random random = new Random(42);
		
		{
			double[] a1 = new double[3];
			Double[] a2 = new Double[a1.length];
			for (int i = a1.length; --i >=0;)
			{
				a2[i] = a1[i] = random.nextDouble();
			}

			testIterable(new DoubleArrayIterable(a1), a2);
			testPrimitiveIterator(new DoubleArrayIterator(a1), a2);
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(bout);
			try
			{
				for (double d : a1)
				{
					dout.writeDouble(d);
				}
				dout.flush();
			}
			catch (IOException ex)
			{
				fail(ex.toString());
			}
			DataInputStream din = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()));
			testNumberIterator(new DoubleDataInputIterator(din), a2);
		}
		
		{
			float[] a1 = new float[3];
			Float[] a2 = new Float[a1.length];
			for (int i = a1.length; --i >=0;)
			{
				a2[i] = a1[i] = random.nextFloat();
			}

			testIterable(new FloatArrayIterable(a1), a2);
			testPrimitiveIterator(new FloatArrayIterator(a1), a2);
		}
		
		{
			int[] a1 = new int[3];
			Integer[] a2 = new Integer[a1.length];
			for (int i = a1.length; --i >=0;)
			{
				a2[i] = a1[i] = random.nextInt();
			}

			testIterable(new IntArrayIterable(a1), a2);
			testPrimitiveIterator(new IntArrayIterator(a1), a2);
		}
		
		{
			long[] a1 = new long[3];
			Long[] a2 = new Long[a1.length];
			for (int i = a1.length; --i >=0;)
			{
				a2[i] = a1[i] = random.nextLong();
			}

			testIterable(new LongArrayIterable(a1), a2);
			testPrimitiveIterator(new LongArrayIterator(a1), a2);
		}
		
		// Test AbstractPrimitiveArrayIterator
		PrimitiveIterator<Double> emptyIter = new AbstractPrimitiveArrayIterator<Double>(0, 0) {
				@Override
				public Double next()
				{
					nextIndex();
					return null;
				}
			};
		assertFalse(emptyIter.hasNext());
		try
		{
			emptyIter.remove();
			fail("should not get here");
		}
		catch (UnsupportedOperationException ex)
		{
		}
		try
		{
			emptyIter.next();
			fail("should not get here");
		}
		catch (NoSuchElementException ex)
		{
		}
	}

	private static enum PrimitiveType
	{
		Double,
		Float,
		Integer,
		Long
	}
	
	private <T extends Number> void testIterable(PrimitiveIterable<T> iterable, T ... expected)
	{
		PrimitiveIterator<T> iterator = iterable.iterator();
		assertNotNull(iterator);
		testNumberIterator(iterator, expected);

		iterator = iterable.iterator();
		assertNotNull(iterator);
		testPrimitiveIterator(iterator, expected);
	}
	
	private <T extends Number> void testNumberIterator(PrimitiveIterator<T> iterator, T ... expected)
	{
		int i = 0;
		for (; iterator.hasNext(); ++i)
		{
			T actual = iterator.next();
			assertEquals(expected[i], actual);
		}
		assertFalse(iterator.hasNext());
		assertEquals(expected.length, i);
	}

	private <T extends Number> void testPrimitiveIterator(PrimitiveIterator<T> iterator, T ... expected)
	{
		
		Class<?> numberClass = expected.getClass().getComponentType();
		PrimitiveType type = PrimitiveType.valueOf(numberClass.getSimpleName());
		
		int i = 0;
		for (; iterator.hasNext(); ++i)
		{
			switch (type)
			{
			case Double:
				assertEquals(expected[i].doubleValue(), ((PrimitiveIterator.OfDouble)iterator).nextDouble(), 1e-8);
				break;
			case Float:
				assertEquals(expected[i].floatValue(), ((PrimitiveIterator.OfFloat)iterator).nextFloat(), 1e-8);
				break;
			case Integer:
				assertEquals(expected[i].intValue(), ((PrimitiveIterator.OfInt)iterator).nextInt(), 1e-8);
				break;
			case Long:
				assertEquals(expected[i].longValue(), ((PrimitiveIterator.OfLong)iterator).nextLong(), 1e-8);
				break;
			}
		}
		assertFalse(iterator.hasNext());
		assertEquals(expected.length, i);
	}
}
