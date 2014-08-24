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

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.ACos;
import com.analog.lyric.dimple.factorfunctions.ASin;
import com.analog.lyric.dimple.factorfunctions.ATan;
import com.analog.lyric.dimple.factorfunctions.Abs;
import com.analog.lyric.dimple.factorfunctions.Cos;
import com.analog.lyric.dimple.factorfunctions.Cosh;
import com.analog.lyric.dimple.factorfunctions.Divide;
import com.analog.lyric.dimple.factorfunctions.Exp;
import com.analog.lyric.dimple.factorfunctions.GreaterThan;
import com.analog.lyric.dimple.factorfunctions.GreaterThanOrEqual;
import com.analog.lyric.dimple.factorfunctions.LessThan;
import com.analog.lyric.dimple.factorfunctions.LessThanOrEqual;
import com.analog.lyric.dimple.factorfunctions.Log;
import com.analog.lyric.dimple.factorfunctions.Negate;
import com.analog.lyric.dimple.factorfunctions.Product;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.values.Value;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestSimpleDeterministicFunctions extends FactorFunctionTester
{
	private final int N = 10;
	private final Random _rand = new Random(42);
	
	@Test
	public void testAbs()
	{
		testSimple(new Abs(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble();
				return new Value[] { Value.create(Math.abs(x)), Value.create(x) };
			}
		});
	}
	
	@Test
	public void testACos()
	{
		testSimple(new ACos(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble();
				return new Value[] { Value.create(Math.acos(x)), Value.create(x) };
			}
		});
	}

	@Test
	public void testASin()
	{
		testSimple(new ASin(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble();
				return new Value[] { Value.create(Math.asin(x)), Value.create(x) };
			}
		});
	}

	@Test
	public void testATan()
	{
		testSimple(new ATan(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble();
				return new Value[] { Value.create(Math.atan(x)), Value.create(x) };
			}
		});
	}

	@Test
	public void testCos()
	{
		testSimple(new Cos(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble();
				return new Value[] { Value.create(Math.cos(x)), Value.create(x) };
			}
		});
	}

	@Test
	public void testACosh()
	{
		testSimple(new Cosh(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble();
				return new Value[] { Value.create(Math.cosh(x)), Value.create(x) };
			}
		});
	}

	@Test
	public void testDivide()
	{
		testSimple(new Divide(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble(), y = _rand.nextDouble();
				return new Value[] { Value.create(x / y) , Value.create(x), Value.create(y) };
			}
		});
	}

	@Test
	public void testExp()
	{
		testSimple(new Exp(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble();
				return new Value[] { Value.create(Math.exp(x)), Value.create(x) };
			}
		});
	}

	@Test
	public void testGreaterThan()
	{
		testSimple(new GreaterThan(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble(),  y = _rand.nextDouble();
				return new Value[] { Value.create(x > y ? 1.0 : 0.0), Value.create(x), Value.create(y) };
			}
		});
	}

	@Test
	public void testGreaterThanOrEqual()
	{
		testSimple(new GreaterThanOrEqual(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble(),  y = _rand.nextDouble();
				return new Value[] { Value.create(x >= y ? 1.0 : 0.0), Value.create(x), Value.create(y) };
			}
		});
	}

	@Test
	public void testLessThan()
	{
		testSimple(new LessThan(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble(),  y = _rand.nextDouble();
				return new Value[] { Value.create(x < y ? 1.0 : 0.0), Value.create(x), Value.create(y) };
			}
		});
	}

	@Test
	public void testLessThanOrEqual()
	{
		testSimple(new LessThanOrEqual(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble(),  y = _rand.nextDouble();
				return new Value[] { Value.create(x <= y ? 1.0 : 0.0), Value.create(x), Value.create(y) };
			}
		});
	}

	@Test
	public void testLog()
	{
		testSimple(new Log(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble();
				return new Value[] { Value.create(Math.log(x)), Value.create(x) };
			}
		});
	}

	@Test
	public void testNegate()
	{
		testSimple(new Negate(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble();
				return new Value[] { Value.create(-x), Value.create(x) };
			}
		});
	}

	@Test
	public void testProduct()
	{
		Product product = new Product();
		
		testSimple(product,new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble(), y = _rand.nextDouble();
				return new Value[] { Value.create(x * y), Value.create(x), Value.create(y) };
			}
		});
	}
	
	private static interface TestCaseGenerator
	{
		Value[] createTestCase();
	}
	
	private void testSimple(FactorFunction function, TestCaseGenerator testCaseGenerator)
	{
		Value[][] testCases = new Value[N][];
		for (int i = 0; i < N; ++i)
		{
			testCases[i] = testCaseGenerator.createTestCase();
			assertArrayEquals(new int[] { 0 }, function.getDirectedToIndices(testCases[i].length));
		}
		testEvalDeterministic(function, RealDomain.unbounded(), testCases);
	}
}
