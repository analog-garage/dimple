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

package com.analog.lyric.dimple.test.FactorFunctions;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.google.common.math.DoubleMath;

/**
 * Contains utility methods for testing {@link FactorFunction} implementations.
 */
public class FactorFunctionTester extends DimpleTestBase
{
	/**
	 * Tests given factor function over set of test cases.
	 * <p>
	 * Exercises the following methods:
	 * <ul>
	 * <li>{@link FactorFunction#evalDeterministic(Object[])}
	 * <li>{@link FactorFunction#updateDeterministicLimit(int)}
	 * <li>{@link FactorFunction#updateDeterministic(Value[], Collection, AtomicReference)}
	 * </ul>
	 * <p>
	 * @param function is the function to be tested.
	 * @param domains specifies the domains of all of the values in each test case. The domains list must have
	 * at least one entry, but may be shorter than the test case length, in which case the last domain in the
	 * list is used for all remaining entries.
	 * @param outputIndices specifies the subindexes of each test cases that represent the output values. The
	 * other values are input values.
	 * @param testCases one or more object arrays specifying the inputs and expected outputs for deterministic
	 * evaluation. Each test case must be the same length.
	 * 
	 * @see #testEvalDeterministic(FactorFunction, Domain, int[], Object[][])
	 * @see #testEvalDeterministic(FactorFunction, Domain, Object[][])
	 */
	public static void testEvalDeterministic(FactorFunction function, Domain[] domains, int[] outputIndices,
		Object[] ... testCases)
	{
		assertTrue(function.isDeterministicDirected());
		assertTrue(function.isDirected());
		
		final int caseSize = testCases[0].length;
		final int nInputs = caseSize - outputIndices.length;
		final BitSet inputSet = BitSetUtil.bitsetFromIndices(caseSize, outputIndices);
		inputSet.flip(0, caseSize);
		final int[] inputIndices = new int[nInputs];
		for (int i = 0, j = -1; (j = inputSet.nextSetBit(j+1)) >= 0; ++i)
		{
			inputIndices[i] = j;
		}
		
		AtomicReference<int[]> changedOutputsHolder = new AtomicReference<int[]>();

		for (int i = 0, end = testCases.length; i < end; ++i)
		{
			Object[] prevTestCase = i > 0 ? testCases[i - 1] : null;
			Object[] testCase = testCases[i];
			Object[] objects = copyInputs(inputIndices, testCase);
			function.evalDeterministic(objects);
			assertArrayEquals(testCase, objects);
			
			assertEquals(0.0, function.evalEnergy(testCase), 0.0);
			assertEquals(1.0, function.eval(testCase), 0.0);
			
			if (prevTestCase != null)
			{
				boolean outputsDiffer = false;
				for (int outputIndex : outputIndices)
				{
					objects[outputIndex] = prevTestCase[outputIndex];
					
					if (!prevTestCase[outputIndex].equals(testCase[outputIndex]))
					{
						outputsDiffer = true;
					}
				}
				
				if (outputsDiffer)
				{
					// If one of the outputs is different, then using the inputs from one
					// test case with the outputs from another should result in a zero weight/ infinite energy.
					assertEquals(Double.POSITIVE_INFINITY, function.evalEnergy(objects), 0.0);
					assertEquals(0.0, function.eval(objects), 0.0);
				}

				Collection<IndexedValue> oldValues = new HashSet<IndexedValue>();
				
				if (function.updateDeterministicLimit(caseSize) <= 0)
				{
					// This should just do a full update
					Value[] values = Value.createFromObjects(objects, domains);
					changedOutputsHolder.set(null);
					function.updateDeterministic(values, oldValues, changedOutputsHolder);
					assertNull(changedOutputsHolder.get());
					for (int inputIndex : inputIndices)
					{
						assertEquals(testCase[inputIndex], values[inputIndex].getObject());
					}
					for (int outputIndex : outputIndices)
					{
						assertTrue(valueFuzzyEquals(values[outputIndex], testCase[outputIndex], 1e-10));
					}
				}
				else
				{
					// Incrementally update starting with previous case.
					Value[] values = Value.createFromObjects(prevTestCase, domains);

					// Test index exception
					for (int out : outputIndices)
					{
						IndexedValue.SingleList badIndexes = IndexedValue.SingleList.create(out, values[out]);
						try
						{
							function.updateDeterministic(values, badIndexes, changedOutputsHolder);
						}
						catch (IndexOutOfBoundsException ex)
						{
						}
						badIndexes.release();
					}

					// Alternate between steps of size 1 and 2 to exercise multi-value updates
					for (int step = 1, j = 0; j < nInputs; j += step, step ^= 3)
					{
						oldValues.clear();
						for (int k = j, endk = Math.min(nInputs, j + step); k < endk; ++k)
						{
							int index = inputIndices[k];
							Value oldValue = values[index].clone();
							values[index].setObject(testCase[index]);
							oldValues.add(new IndexedValue(index, oldValue));
						}
						changedOutputsHolder.set(null);
						function.updateDeterministic(values, oldValues, changedOutputsHolder);
						int[] changedOutputs = changedOutputsHolder.get();
						if (changedOutputs != null)
						{
							for (int outputIndex : changedOutputs)
							{
								assertTrue(outputIndex >= 0);
								assertTrue(outputIndex < caseSize);
								assertFalse(inputSet.get(outputIndex));
							}
						}
					}
					for (int inputIndex : inputIndices)
					{
						assertEquals(testCase[inputIndex], values[inputIndex].getObject());
					}
					for (int outputIndex : outputIndices)
					{
						assertTrue(valueFuzzyEquals(values[outputIndex], testCase[outputIndex], 1e-10));
					}
				}
			}
		}
	}
	
	/**
	 * Shorthand for call to {@link #testEvalDeterministic(FactorFunction, Domain[], int[], Object[][])} like
	 * following:
	 * <pre>
	 *    testEvalDeterministic(function, new Domain[] { domain }, inputIndices, testCases)
	 * </pre>
	 */
	public static void testEvalDeterministic(FactorFunction function, Domain domain, int[] inputIndices,
		Object[] ... testCases)
	{
		testEvalDeterministic(function, new Domain[] { domain }, inputIndices, testCases);
	}
	
	/**
	 * Shorthand for call to {@link #testEvalDeterministic(FactorFunction, Domain[], int[], Object[][])}
	 * like following:
	 * <pre>
	 *    testEvalDeterministic(function, new Domain[] { domain }, new int[] { 0 }, testCases)
	 * </pre>
	 */
	public static void testEvalDeterministic(FactorFunction function, Domain domain, Object[] ... testCases)
	{
		testEvalDeterministic(function, domain, new int[] { 0 }, testCases);
	}
	
	public static void testEvalDeterministic(FactorFunction function, Domain[] domains, Object[] ... testCases)
	{
		testEvalDeterministic(function, domains, new int[] { 0 }, testCases);
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private static boolean valueFuzzyEquals(Value value, Object object, double tolerance)
	{
		Object valueObj = requireNonNull(value.getObject());
		if (valueObj instanceof Number && object instanceof Number)
		{
			return DoubleMath.fuzzyEquals(((Number)valueObj).doubleValue(), ((Number)object).doubleValue(), tolerance);
		}
		
		return valueObj.equals(object);
	}
	
	/**
	 * Returns a new Object array with same length as {@code objects} and shallow copying only
	 * the entries specified by {@code inputIndices}.
	 */
	private static Object[] copyInputs(int[] inputIndices, Object[] objects)
	{
		Object[] copy = new Object[objects.length];
		for (int i : inputIndices)
		{
			copy[i] = objects[i];
		}
		return copy;
	}
}
