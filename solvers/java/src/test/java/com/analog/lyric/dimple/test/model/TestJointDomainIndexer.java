package com.analog.lyric.dimple.test.model;

import static com.analog.lyric.math.Utilities.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.JointDomainIndexer;
import com.analog.lyric.dimple.model.JointDomainReindexer;
import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.DomainList;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.util.test.SerializationTester;

public class TestJointDomainIndexer
{
	private Random rand = new Random(1323);
	
	@Test
	public void test()
	{
		DiscreteDomain d2 = DiscreteDomain.range(0,1);
		DiscreteDomain d3 = DiscreteDomain.range(0,2);
	
		try
		{
			JointDomainIndexer.create();
			fail("should not get here");
		}
		catch (DimpleException ex)
		{
		}
		try
		{
			JointDomainIndexer.create((DiscreteDomain[])null);
			fail("should not get here");
		}
		catch (NullPointerException ex)
		{
		}
		try
		{
			BitSet bitset = new BitSet();
			bitset.set(0);
			bitset.set(1);
			bitset.set(2);
			JointDomainIndexer.create(bitset, d2, d3);
			fail("expected DimpleException");
		}
		catch (DimpleException ex)
		{
			assertThat(ex.getMessage(), containsString("Illegal output set for domain list"));
		}
		
		JointDomainIndexer dl2 = JointDomainIndexer.create(d2);
		testInvariants(dl2);
		JointDomainIndexer dl3 = JointDomainIndexer.create(d3);
		testInvariants(dl3);
		
		JointDomainIndexer dl2by3 = JointDomainIndexer.create(d2, d3);
		testInvariants(dl2by3);
		assertEquals(dl2by3, JointDomainIndexer.create(d2, d3));
		assertNotEquals(dl2by3, JointDomainIndexer.create(d3, d2));
		
		JointDomainIndexer dl2to3 = JointDomainIndexer.create(new int[] { 1 }, new DiscreteDomain[] {d2, d3 });
		testInvariants(dl2to3);
		assertNotEquals(dl2by3, dl2to3);
		assertNotEquals(dl2to3, dl2by3);
		assertNotEquals(dl2by3.hashCode(), dl2to3.hashCode());
		
		JointDomainIndexer dl2from3 = JointDomainIndexer.create(new int[] { 0 }, new DiscreteDomain[] { d2, d3 });
		testInvariants(dl2from3);
		assertNotEquals(dl2to3, dl2from3);
		assertNotEquals(dl2to3.hashCode(), dl2from3.hashCode());
		
		DomainList<?> dl2by2 = DomainList.create(new Domain[] { d2, d2 });
		assertTrue(dl2by2.isDiscrete());
		testInvariants(dl2by2.asJointDomainIndexer());
		
		// Test concat
		assertSame(dl2, JointDomainIndexer.concat(dl2, null));
		assertSame(dl2, JointDomainIndexer.concat(null, dl2));
		JointDomainIndexer dl2by2a = JointDomainIndexer.concat(dl2, dl2);
		testInvariants(dl2by2a);
		assertEquals(dl2by2, dl2by2a);
		
		JointDomainIndexer dlfoo = JointDomainIndexer.concat(dl2to3, dl2from3);
		testInvariants(dlfoo);
		assertTrue(dlfoo.isDirected());
		assertArrayEquals(new int[] { 1, 2 }, dlfoo.getOutputDomainIndices());
		assertArrayEquals(new Object[] { d2, d3, d2, d3}, dlfoo.toArray());
		
		JointDomainIndexer dlbar = JointDomainIndexer.create((BitSet)null, dlfoo);
		assertTrue(dlfoo.domainsEqual(dlbar));
		assertFalse(dlbar.isDirected());
		testInvariants(dlbar);
		
		//
		// Test DomainList
		//
		
		DomainList<?> mixed = DomainList.create(d2, RealDomain.unbounded());
		assertFalse(mixed.isDiscrete());
		assertNull(mixed.asJointDomainIndexer());
		assertFalse(DomainList.allDiscrete(d2, RealDomain.unbounded()));
		assertTrue(DomainList.allDiscrete(new DiscreteDomain[] { d2, d3 }));
	}
	
	public static void testInvariants(JointDomainIndexer domainList)
	{
		assertTrue(domainList.equals(domainList));
		assertFalse(domainList.equals("foo"));
		
		assertTrue(domainList.isDiscrete());
		assertTrue(DomainList.allDiscrete(domainList.toArray(new Domain[domainList.size()])));
		assertSame(domainList, domainList.asJointDomainIndexer());
		
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
		
		int[] inIndices = domainList.getInputDomainIndices();
		int[] outIndices = domainList.getOutputDomainIndices();

		final int[] indices = new int[size], indices2 = new int[size];
		final Object[] elements = new Object[size], elements2 = new Object[size];
		
		// Count the number of times that the undirected/directed indexes match.
		int canonicalCount = 0;
		
		for (i = 0; i < cardinality; ++i)
		{
			assertSame(indices, domainList.undirectedJointIndexToIndices(i, indices));
			assertArrayEquals(indices, domainList.undirectedJointIndexToIndices(i, null));
			assertArrayEquals(indices, domainList.undirectedJointIndexToIndices(i, new int[0]));
			assertArrayEquals(indices, domainList.undirectedJointIndexToIndices(i));
			assertSame(elements, domainList.undirectedJointIndexToElements(i, elements));
			assertArrayEquals(elements, domainList.undirectedJointIndexToElements(i));
			assertArrayEquals(elements, domainList.undirectedJointIndexToElements(i, null));
			assertArrayEquals(elements, domainList.undirectedJointIndexToElements(i, new Object[0]));
			for (int j = 0; j < size; ++ j)
			{
				assertTrue(indices[j] >= 0);
				assertTrue(indices[j] < domainList.getDomainSize(j));
				assertEquals(elements[j], domainList.get(j).getElement(indices[j]));
			}
			
			domainList.validateIndices(indices);
			
			assertEquals(i, domainList.undirectedJointIndexFromElements(elements));
			assertEquals(i, domainList.undirectedJointIndexFromIndices(indices));
			
			int ji = domainList.jointIndexFromIndices(indices);
			assertEquals(ji, domainList.jointIndexFromElements(elements));
			
			if (i == ji)
			{
				++canonicalCount;
			}
			
			int in = domainList.inputIndexFromIndices(indices);
			assertEquals(in, domainList.inputIndexFromElements(elements));
			assertEquals(in, domainList.inputIndexFromJointIndex(ji));
			
			int out = domainList.outputIndexFromIndices(indices);
			assertEquals(out, domainList.outputIndexFromElements(elements));
			assertEquals(out, domainList.outputIndexFromJointIndex(ji));
			
			assertEquals(ji, domainList.jointIndexFromInputOutputIndices(in, out));
			
			Arrays.fill(indices2, -1);
			Arrays.fill(elements2, null);
			assertSame(indices2, domainList.jointIndexToIndices(ji, indices2));
			assertArrayEquals(indices, indices2);
			assertArrayEquals(indices, domainList.jointIndexToIndices(ji));
			assertSame(elements2, domainList.jointIndexToElements(ji, elements2));
			assertArrayEquals(elements, elements2);
			
			Object[] elements3 = domainList.jointIndexToElements(ji);
			assertArrayEquals(elements, elements3);
			
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
		
		assertEquals(domainList.hasCanonicalDomainOrder(), canonicalCount == cardinality);
		
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
				assertEquals(outIndices[j], domainList.getOutputDomainIndex(j));
				assertTrue(outSet.get(outIndices[j]));
			}
			for (int j = inIndices.length; --j>=0;)
			{
				assertEquals(inIndices[j], domainList.getInputDomainIndex(j));
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
				assertEquals(i, domainList.getOutputDomainIndex(i));
			}
			
			try
			{
				domainList.getInputDomainIndex(0);
				fail("should not get here");
			}
			catch (ArrayIndexOutOfBoundsException ex)
			{
			}
			try
			{
				domainList.getOutputDomainIndex(-1);
				fail("should not get here");
			}
			catch (ArrayIndexOutOfBoundsException ex)
			{
			}
			try
			{
				domainList.getOutputDomainIndex(size);
				fail("should not get here");
			}
			catch (ArrayIndexOutOfBoundsException ex)
			{
			}
		}
		
		try
		{
			domainList.validateIndices();
			fail("Expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), CoreMatchers.containsString("Wrong number of indices"));
		}
		try
		{
			Arrays.fill(indices, 0);
			indices[0] = -1;
			domainList.validateIndices(indices);
			fail("Expected IndiexOutOfBoundsException");
		}
		catch (IndexOutOfBoundsException ex)
		{
		}
		try
		{
			Arrays.fill(indices, 0);
			indices[0] = domainList.getDomainSize(0);
			domainList.validateIndices(indices);
			fail("Expected IndiexOutOfBoundsException");
		}
		catch (IndexOutOfBoundsException ex)
		{
		}
		
		JointDomainIndexer domainList2 = SerializationTester.clone(domainList);
		assertEquals(domainList, domainList2);
		assertEquals(domainList.hashCode(), domainList2.hashCode());
	}
	
	@Test
	public void testReindexer()
	{
		DiscreteDomain d2 = DiscreteDomain.range(0,1);
		DiscreteDomain d3 = DiscreteDomain.range(0,2);
		DiscreteDomain d4 = DiscreteDomain.range(0,3);
		DiscreteDomain d5 = DiscreteDomain.range(0,4);
		
		JointDomainIndexer dl2 = JointDomainIndexer.create(d2);
		JointDomainIndexer dl3 = JointDomainIndexer.create(d3);
		JointDomainIndexer dl4 = JointDomainIndexer.create(d4);
		JointDomainIndexer dl2by3 = JointDomainIndexer.create(d2, d3);
		JointDomainIndexer dl3by2 = JointDomainIndexer.create(d3, d2);
		JointDomainIndexer dl3by4 = JointDomainIndexer.create(d3, d4);
		JointDomainIndexer dl4by2 = JointDomainIndexer.create(d4, d2);
		
		JointDomainIndexer dl2to3 = JointDomainIndexer.create(new int[] {1}, new DiscreteDomain[] {d2, d3});
		JointDomainIndexer dl2from3 = JointDomainIndexer.create(new int[] {0}, new DiscreteDomain[] {d2, d3});
		
		// A simple permutation
		JointDomainReindexer dl2by3_to_dl3by2 =
			JointDomainReindexer.createPermuter(dl2by3, null,  dl3by2,  null, new int[] { 1, 0});
		assertSame(dl2by3, dl2by3_to_dl3by2.getFromDomains());
		assertSame(dl3by2, dl2by3_to_dl3by2.getToDomains());
		testInvariants(dl2by3_to_dl3by2);
		assertNotEquals(dl2by3_to_dl3by2, dl2by3_to_dl3by2.getInverse());
		assertNotEquals(dl2by3_to_dl3by2.hashCode(), dl2by3_to_dl3by2.getInverse().hashCode());
		
		JointDomainReindexer dl3by2_to_dl2by3 =
			JointDomainReindexer.createPermuter(dl3by2, null,  dl2by3,  null, new int[] { 1, 0});
		testInvariants(dl3by2_to_dl2by3);
		assertEquals(dl2by3_to_dl3by2, dl3by2_to_dl2by3.getInverse());
		assertEquals(dl3by2_to_dl2by3, dl2by3_to_dl3by2.getInverse());
		assertEquals(dl2by3_to_dl3by2.hashCode(), dl3by2_to_dl2by3.getInverse().hashCode());
		
		// Remove a domain
		JointDomainReindexer dl2by3_to_dl3 = JointDomainReindexer.createRemover(dl2by3, 0);
		assertSame(dl2by3, dl2by3_to_dl3.getFromDomains());
		assertEquals(dl2, dl2by3_to_dl3.getRemovedDomains());
		testInvariants(dl2by3_to_dl3);
		assertNotEquals(dl2by3_to_dl3by2, dl2by3_to_dl3);
		assertNotEquals(dl2by3_to_dl3by2.hashCode(), dl2by3_to_dl3.hashCode());
		
		JointDomainReindexer dl2by3_to_dl2 = JointDomainReindexer.createRemover(dl2by3, 1);
		assertSame(dl2by3, dl2by3_to_dl2.getFromDomains());
		assertEquals(dl2, dl2by3_to_dl2.getToDomains());
		testInvariants(dl2by3_to_dl2);
		
		JointDomainIndexer dl2by3by4by5 = JointDomainIndexer.create(d2, d3, d4, d5);
		JointDomainReindexer dl2by3by4by5_to_dl2by = JointDomainReindexer.createJoiner(dl2by3by4by5, 1, 2);
		JointDomainIndexer dl2by12by5 = dl2by3by4by5_to_dl2by.getToDomains();
		testInvariants(dl2by3by4by5_to_dl2by);
		assertEquals(3, dl2by12by5.size());
		assertEquals(12, dl2by12by5.getDomainSize(1));
		assertNotEquals(dl2by3_to_dl3by2, dl2by3by4by5_to_dl2by);
		assertNotEquals(dl2by3by4by5_to_dl2by, dl2by3_to_dl3);
		assertNotEquals(dl2by3by4by5_to_dl2by, dl2by3by4by5_to_dl2by.getInverse());
		assertNotEquals(dl2by3by4by5_to_dl2by.hashCode(), dl2by3by4by5_to_dl2by.getInverse().hashCode());
		
		JointDomainReindexer dl2by12by5_to_dl2by3by4by5 = JointDomainReindexer.createSplitter(dl2by12by5, 1);
		testInvariants(dl2by12by5_to_dl2by3by4by5);
		assertEquals(dl2by3by4by5_to_dl2by, dl2by12by5_to_dl2by3by4by5.getInverse());
		
		JointDomainReindexer dl2by3_to_dl3by4 =
			JointDomainReindexer.createPermuter(dl2by3, dl4, dl3by4, dl2, new int [] { 2, 0, 1 });
		testInvariants(dl2by3_to_dl3by4);
		
		JointDomainReindexer dl2by3_to_dl4by2 =
			JointDomainReindexer.createPermuter(dl2by3, dl4, dl4by2, dl3, new int [] { 1, 2, 0 });
		testInvariants(dl2by3_to_dl4by2);
		
		// Chain
		JointDomainReindexer dl3by2_to_dl3 = dl3by2_to_dl2by3.combineWith(dl2by3_to_dl3);
		testInvariants(dl3by2_to_dl3);
		
		try
		{
			dl3by2_to_dl3.combineWith(dl2by12by5_to_dl2by3by4by5);
			fail("expected exception");
		}
		catch (DimpleException ex)
		{
		}
		
		// Directed conversion
		JointDomainReindexer dl3by2_to_dl3to2 =
			JointDomainReindexer.createPermuter(dl2by3, dl2to3);
		testInvariants(dl3by2_to_dl3to2);

		JointDomainReindexer dl3by2_to_dl3from2 =
			JointDomainReindexer.createPermuter(dl2by3, dl2from3);
		testInvariants(dl3by2_to_dl3from2);
		
		//
		// Construction errors
		//
		
		try
		{
			JointDomainReindexer.createPermuter(dl2, dl3, dl3, null, new int[] { 0, 1 });
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("Combined size"));
		}
		
		try
		{
			JointDomainReindexer.createPermuter(dl2, null, dl2, null, new int[] { 0, 1, 2 });
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("does not match domain sizes"));
		}
		
		try
		{
			JointDomainReindexer.createPermuter(dl2, null, dl2, null, new int[] { -1 });
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("out-of-range value -1"));
		}
		
		try
		{
			JointDomainReindexer.createPermuter(dl2, null, dl2, null, new int[] { 2 });
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("out-of-range value 2"));
		}
		
		try
		{
			JointDomainReindexer.createPermuter(dl2, dl3, dl2by3, null, new int[] { 0, 0 });
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("two entries mapping to 0"));
		}
		
		try
		{
			JointDomainReindexer.createPermuter(dl2, null, dl3, null, new int[] { 0 });
			fail("expected IllegalArgumentException");
		}
		catch (IllegalArgumentException ex)
		{
			assertThat(ex.getMessage(), containsString("domain size mismatch at index 0"));
		}

	}
	
	public void testInvariants(JointDomainReindexer converter)
	{
		testInvariants(converter, true);
	}
	
	private void testInvariants(JointDomainReindexer converter, boolean testInverse)
	{
		assertEquals(converter, converter);

		JointDomainReindexer inverse = converter.getInverse();
		assertEquals(converter, inverse.getInverse());
		
		JointDomainReindexer.Indices indices = converter.getScratch();
		assertSame(converter, indices.converter);
		assertEquals(converter.getFromDomains().size(), indices.fromIndices.length);
		assertEquals(converter.getToDomains().size(), indices.toIndices.length);
		if (converter.getAddedDomains() == null)
		{
			assertSame(ArrayUtil.EMPTY_INT_ARRAY, indices.addedIndices);
		}
		else
		{
			assertEquals(converter.getAddedDomains().size(), indices.addedIndices.length);
		}
		if (converter.getRemovedDomains() == null)
		{
			assertSame(ArrayUtil.EMPTY_INT_ARRAY, indices.removedIndices);
		}
		else
		{
			assertEquals(converter.getRemovedDomains().size(), indices.removedIndices.length);
		}
		
		indices.release();
		assertSame(indices, converter.getScratch());
		assertNotSame(indices, converter.getScratch());
		indices = converter.getScratch();
		
		final int maxFrom = converter.getFromDomains().getCardinality();
		final int maxAdded = converter.getAddedCardinality();
		
		final AtomicInteger removedRef = new AtomicInteger();
		final AtomicInteger removedRef2 = new AtomicInteger();
		final AtomicInteger addedRef = new AtomicInteger();
		
		double[] fromDenseWeights = new double[maxFrom];
		double[] fromDenseEnergies = new double[maxFrom];
		for (int i = 0; i < maxFrom; ++i)
		{
			double w = rand.nextDouble();
			fromDenseWeights[i] = w;
			fromDenseEnergies[i] = weightToEnergy(w);
		}
		
		final int maxTo = converter.getToDomains().getCardinality();
		final int maxRemoved = converter.getRemovedCardinality();
		
		final double[] toDenseWeights = converter.convertDenseWeights(fromDenseWeights);
		assertEquals(maxTo, toDenseWeights.length);
		double[] toDenseEnergies = converter.convertDenseEnergies(fromDenseEnergies);
		assertEquals(maxTo, toDenseEnergies.length);
		
		for (int from = 0; from < maxFrom; ++from)
		{
			double fromWeight = fromDenseWeights[from];
			double fromEnergy = fromDenseEnergies[from];
			
			for (int added = 0; added < maxAdded; ++added)
			{
				int to = converter.convertJointIndex(from,  added, null);
				assertEquals(to, converter.convertJointIndex(from, added));
				assertEquals(to, converter.convertJointIndex(from, added, removedRef));
				
				assertEquals(from, inverse.convertJointIndex(to, removedRef.get(), null));
				assertEquals(from, inverse.convertJointIndex(to, removedRef.get(), addedRef));
				assertEquals(added, addedRef.get());
				
				indices.writeIndices(from, added);
				converter.convertIndices(indices);
				int to2 = indices.readIndices(null);
				assertEquals(to, to2);
				assertEquals(to, indices.readIndices(removedRef2));
				assertEquals(removedRef.get(), removedRef2.get());
				
				if (maxRemoved == 1)
				{
					assertEquals(fromWeight, toDenseWeights[to], 0.0);
					assertEquals(fromEnergy, toDenseEnergies[to], 0.0);
				}
				else
				{
					// Weight must be equal sum of entries mapping to this one
					double weightSum = 0.0;
					for (int removed = 0; removed < maxRemoved; ++removed)
					{
						int fromInverse = inverse.convertJointIndex(to, removed);
						weightSum += fromDenseWeights[fromInverse];
					}
					assertEquals(weightSum, toDenseWeights[to], 1e-12);
					assertEquals(weightToEnergy(weightSum), toDenseEnergies[to], 1e-12);
				}
			}
		}
		
		//
		// Test sparse conversions
		//
		
		// A "dense" sparse to joint index.
		final int[] fromDenseSparseToJoint = new int[maxFrom];
		for (int i = 0; i < maxFrom; ++i)
		{
			fromDenseSparseToJoint[i] = i;
		}
		final int[] toDenseSparseToJoint = converter.convertSparseToJointIndex(fromDenseSparseToJoint);
		assertEquals(maxTo, toDenseSparseToJoint.length);
		for (int i = toDenseSparseToJoint.length; --i>=0;)
		{
			assertEquals(i, toDenseSparseToJoint[i]);
		}
		assertArrayEquals(
			toDenseWeights,
			converter.convertSparseWeights(fromDenseWeights, fromDenseSparseToJoint, toDenseSparseToJoint),
			1e-12);
		assertArrayEquals(
			toDenseEnergies,
			converter.convertSparseEnergies(fromDenseEnergies, fromDenseSparseToJoint, toDenseSparseToJoint),
			1e-12);
		
		// Test a random sparse selection
		BitSet sparseSet = new BitSet(maxFrom);
		for (int i = maxFrom/2; --i>=0;)
		{
			sparseSet.set(rand.nextInt(maxFrom));
		}
		final int[] fromSparseToJoint = new int[sparseSet.cardinality()];
		for (int i = 0, sparseIndex = -1; i < fromSparseToJoint.length; ++i)
		{
			sparseIndex = sparseSet.nextSetBit(sparseIndex+1);
			fromSparseToJoint[i] = sparseIndex;
		}
		final int[] toSparseToJoint = converter.convertSparseToJointIndex(fromSparseToJoint);
		for (int oldSparse : fromSparseToJoint)
		{
			for (int added = 0; added < maxAdded; ++added)
			{
				int newSparse = converter.convertJointIndex(oldSparse, added);
				assertTrue(Arrays.binarySearch(toSparseToJoint, newSparse) >= 0);
			}
		}
		final double[] fromSparseWeights = new double[fromSparseToJoint.length];
		final double[] fromSparseEnergies = new double[fromSparseToJoint.length];
		for (int si = fromSparseToJoint.length; --si>=0;)
		{
			int ji = fromSparseToJoint[si];
			fromSparseWeights[si] = fromDenseWeights[ji];
			fromSparseEnergies[si] = fromDenseEnergies[ji];
		}
		final double[] toSparseWeights =
			converter.convertSparseWeights(fromSparseWeights,  fromSparseToJoint, toSparseToJoint);
		final double[] toSparseEnergies =
			converter.convertSparseEnergies(fromSparseEnergies,  fromSparseToJoint, toSparseToJoint);
		for (int si = toSparseToJoint.length; --si>=0;)
		{
			int ji = toSparseToJoint[si];
			if (maxRemoved == 1)
			{
				assertEquals(toDenseWeights[ji], toSparseWeights[si], 1e-12);
				assertEquals(toDenseEnergies[ji], toSparseEnergies[si], 1e-12);
			}
			else
			{
				// Weight must be equal sum of entries mapping to this one
				double weightSum = 0.0;
				for (int removed = 0; removed < maxRemoved; ++removed)
				{
					int fromInverse = inverse.convertJointIndex(ji, removed);
					if (Arrays.binarySearch(fromSparseToJoint, fromInverse) >= 0)
					{
						weightSum += fromDenseWeights[fromInverse];
					}
				}
				assertEquals(weightSum, toSparseWeights[si], 1e-12);
				assertEquals(weightToEnergy(weightSum), toSparseEnergies[si], 1e-12);
			}
		}
		
		if (testInverse)
		{
			testInvariants(inverse, false);
		}
	}
}
