package com.analog.lyric.dimple.test.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.BitSet;

import org.junit.Test;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteDomainList;
import com.analog.lyric.util.test.SerializationTester;

public class TestDiscreteDomainList
{
	@Test
	public void test()
	{
		DiscreteDomain d2 = DiscreteDomain.intRangeFromSize(2);
		DiscreteDomain d3 = DiscreteDomain.intRangeFromSize(3);
	
		try
		{
			DiscreteDomainList.create();
			fail("should not get here");
		}
		catch (DimpleException ex)
		{
		}
		try
		{
			DiscreteDomainList.create((DiscreteDomain[])null);
			fail("should not get here");
		}
		catch (DimpleException ex)
		{
		}
		
		DiscreteDomainList dl2by3 = DiscreteDomainList.create(d2, d3);
		testInvariants(dl2by3);
		assertEquals(dl2by3, DiscreteDomainList.create(d2, d3));
		assertNotEquals(dl2by3, DiscreteDomainList.create(d3, d2));
		
		DiscreteDomainList dl2to3 = DiscreteDomainList.create(new int[] { 0 }, new DiscreteDomain[] {d2, d3 });
		testInvariants(dl2to3);
		assertNotEquals(dl2by3, dl2to3);
		assertNotEquals(dl2to3, dl2by3);
		assertNotEquals(dl2by3.hashCode(), dl2to3.hashCode());
	}
	
	public static void testInvariants(DiscreteDomainList domainList)
	{
		assertTrue(domainList.equals(domainList));
		assertFalse(domainList.equals("foo"));
		
		final int size = domainList.size();
		assertTrue(size > 0);
		
		final int inSize = domainList.getInputSize();
		assertTrue(inSize >= 0);
		assertTrue(inSize < size);
		
		final int outSize = domainList.getOutputSize();
		assertTrue(outSize >= 0);
		assertTrue(outSize <= size);
		assertEquals(size, inSize + outSize);
		
		final int cardinality = domainList.getCardinality();
		assertTrue(cardinality > 1);
		
		final int inCardinality = domainList.getInputCardinality();
		assertTrue(inCardinality >= 1);
		assertTrue(inCardinality <= cardinality);
		
		final int outCardinality = domainList.getOutputCardinality();
		assertTrue(outCardinality >= 1);
		assertTrue(outCardinality <= cardinality);
		assertEquals(cardinality, inCardinality * outCardinality);
		
		int i = 0;
		for (DiscreteDomain domain : domainList)
		{
			assertSame(domain, domainList.get(i));
			assertEquals(domain.size(), domainList.getDomainSize(i));
			++i;
		}
		assertEquals(size, i);
		
		BitSet inSet = domainList.getInputSet();
		BitSet outSet = domainList.getOutputSet();
		
		int[] inIndices = domainList.getInputIndices();
		int[] outIndices = domainList.getOutputIndices();

		final int[] indices = new int[size], indices2 = new int[size];
		final Object[] elements = new Object[size], elements2 = new Object[size];
		
		for (i = 0; i < cardinality; ++i)
		{
			assertSame(indices, domainList.undirectedJointIndexToIndices(i, indices));
			assertArrayEquals(indices, domainList.undirectedJointIndexToIndices(i, null));
			assertArrayEquals(indices, domainList.undirectedJointIndexToIndices(i, new int[0]));
			assertSame(elements, domainList.undirectedJointIndexToElements(i, elements));
			assertArrayEquals(elements, domainList.undirectedJointIndexToElements(i, null));
			assertArrayEquals(elements, domainList.undirectedJointIndexToElements(i, new Object[0]));
			for (int j = 0; j < size; ++ j)
			{
				assertTrue(indices[j] >= 0);
				assertTrue(indices[j] < domainList.getDomainSize(j));
				assertEquals(elements[j], domainList.get(j).getElement(indices[j]));
			}
			
			assertEquals(i, domainList.undirectedJointIndexFromElements(elements));
			assertEquals(i, domainList.undirectedJointIndexFromIndices(indices));
			
			int in = domainList.inputIndexFromIndices(indices);
			assertEquals(in, domainList.inputIndexFromElements(elements));
			
			int out = domainList.outputIndexFromIndices(indices);
			assertEquals(out, domainList.outputIndexFromElements(elements));
			
			int ji = domainList.jointIndexFromIndices(indices);
			assertEquals(ji, domainList.jointIndexFromElements(elements));
			
			Arrays.fill(indices2, -1);
			Arrays.fill(elements2, null);
			assertSame(indices2, domainList.jointIndexToIndices(ji, indices2));
			assertArrayEquals(indices, indices2);
			assertSame(elements2, domainList.jointIndexToElements(ji, elements2));
			assertArrayEquals(elements, elements2);
			
			if (!domainList.isDirected())
			{
				assertEquals(ji, i);
			}
			
			assertEquals(out + in * domainList.getOutputCardinality(), ji);
			
			Arrays.fill(indices2, -1);
			Arrays.fill(elements2, null);
			domainList.inputIndexToIndices(in, indices2);
			assertEquals(in, domainList.inputIndexFromIndices(indices2));
			domainList.inputIndexToElements(in, elements2);
			assertEquals(in, domainList.inputIndexFromElements(elements2));
			if (domainList.isDirected())
			{
			}
			else
			{
				for (int j : indices2)
				{
					assertEquals(-1, j);
				}
			}
			
			Arrays.fill(indices2, -1);
			Arrays.fill(elements2, null);
			domainList.outputIndexToIndices(out, indices2);
			assertEquals(out, domainList.outputIndexFromIndices(indices2));
			domainList.outputIndexToElements(out, elements2);
			assertEquals(out, domainList.outputIndexFromElements(elements2));
			if (domainList.isDirected())
			{
			}
			else
			{
				for (int j = 0; j < size; ++j)
				{
					assertEquals(indices[j], indices2[j]);
				}
			}
		}
		
		if (domainList.isDirected())
		{
			assertFalse(inSet.intersects(outSet));
			assertEquals(size, inSet.cardinality(), outSet.cardinality());
			assertNotSame(inSet, domainList.getInputSet());
			assertEquals(inSet, domainList.getInputSet());
			assertNotSame(outSet, domainList.getOutputSet());
			assertEquals(outSet, domainList.getOutputSet());
			
			assertEquals(inIndices.length, inSet.cardinality());
			assertEquals(outIndices.length, outSet.cardinality());
			assertEquals(inIndices.length, domainList.getInputSize());
			assertEquals(outIndices.length, domainList.getOutputSize());
			
			for (int j = outIndices.length; --j>=0;)
			{
				assertEquals(outIndices[j], domainList.getOutputIndex(j));
				assertTrue(outSet.get(outIndices[j]));
			}
			for (int j = inIndices.length; --j>=0;)
			{
				assertEquals(inIndices[j], domainList.getInputIndex(j));
				assertTrue(inSet.get(inIndices[j]));
			}
		}
		else
		{
			assertEquals(1, inCardinality);
			assertEquals(0, inSize);
			assertNull(inSet);
			assertNull(outSet);
			assertNull(inIndices);
			assertNull(outIndices);
			
			for (i = 0; i < size; ++i)
			{
				assertEquals(i, domainList.getOutputIndex(i));
			}
			
			try
			{
				domainList.getInputIndex(0);
				fail("should not get here");
			}
			catch (ArrayIndexOutOfBoundsException ex)
			{
			}
			try
			{
				domainList.getOutputIndex(-1);
				fail("should not get here");
			}
			catch (ArrayIndexOutOfBoundsException ex)
			{
			}
			try
			{
				domainList.getOutputIndex(size);
				fail("should not get here");
			}
			catch (ArrayIndexOutOfBoundsException ex)
			{
			}
		}
		
		DiscreteDomainList domainList2 = SerializationTester.clone(domainList);
		assertEquals(domainList, domainList2);
		assertEquals(domainList.hashCode(), domainList2.hashCode());
	}
}
