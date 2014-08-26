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
import com.analog.lyric.dimple.factorfunctions.And;
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
import com.analog.lyric.dimple.factorfunctions.Or;
import com.analog.lyric.dimple.factorfunctions.Product;
import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
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
				return testCase(Math.abs(x), x);
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
				return testCase(Math.acos(x), x);
			}
		});
	}

	@Test
	public void testAnd()
	{
		And and = new And();
		assertArrayEquals(new int[] { 0 }, and.getDirectedToIndices());
		testEvalDeterministic(and, RealDomain.unbounded(),
			testCase(1.0, 1.0, 1.0),
			testCase(1.0, 1.0, 1.0, 1.0));
		testEvalDeterministic(and, RealDomain.unbounded(),
			testCase(0.0, 1.0, 0.0),
			testCase(0.0, 1.0, 1.0, 0.0),
			testCase(0.0, 0.0, 0.0));
		testEvalDeterministic(and, DiscreteDomain.bool(),
			testCase(false, true, false ,true),
			testCase(false, false, false),
			testCase(false, true, true, true, false)
		);
		testEvalDeterministic(and, DiscreteDomain.bool(),
			testCase(true, true, true ,true),
			testCase(true, true),
			testCase(true, true, true, true, true)
		);
	}
	
	@Test
	public void testASin()
	{
		testSimple(new ASin(), new TestCaseGenerator() {
			@Override
			public Value[] createTestCase()
			{
				double x = _rand.nextDouble();
				return testCase(Math.asin(x), x);
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
				return testCase(Math.atan(x), x);
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
				return testCase(Math.cos(x), x);
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
				return testCase(Math.cosh(x), x);
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
				return testCase(x / y, x, y);
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
				return testCase(Math.exp(x), x);
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
				return testCase(x > y ? 1.0 : 0.0, x, y);
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
				return testCase(x >= y ? 1.0 : 0.0, x, y);
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
				return testCase(x < y ? 1.0 : 0.0, x, y);
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
				return testCase(x <= y ? 1.0 : 0.0, x, y);
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
				return testCase(Math.log(x), x);
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
				return testCase(-x, x);
			}
		});
	}

	@Test
	public void testOr()
	{
		Or or = new Or();
		assertArrayEquals(new int[] { 0 }, or.getDirectedToIndices());
		testEvalDeterministic(or, RealDomain.unbounded(),
			testCase(1.0, 1.0, 1.0),
			testCase(1.0, 0.0, 0.0, 1.0),
			testCase(1.0, 1.0, 1.0, 1.0));
		testEvalDeterministic(or, RealDomain.unbounded(),
			testCase(0.0, 0.0, 0.0),
			testCase(0.0, 0.0, 0.0, 0.0),
			testCase(0.0, 0.0));
		testEvalDeterministic(or, DiscreteDomain.bool(),
			testCase(false, false, false ,false),
			testCase(false, false, false),
			testCase(false, false, false, false, false)
		);
		testEvalDeterministic(or, DiscreteDomain.bool(),
			testCase(true, true, false ,true),
			testCase(true, true),
			testCase(true, true, false, true, true)
		);
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
				return testCase(x * y, x, y);
			}
		});
	}
	
	@Test
	public void testXor()
	{
		Xor xor = new Xor();
		assertArrayEquals(new int[] { 0 }, xor.getDirectedToIndices());
		testEvalDeterministic(xor, RealDomain.unbounded(),
			testCase(0.0, 1.0, 1.0),
			testCase(0.0, 0.0, 0.0, 1.0, 1.0),
			testCase(0.0, 0.0, 0.0));
		testEvalDeterministic(xor, RealDomain.unbounded(),
			testCase(0.0, 0.0, 0.0),
			testCase(0.0, 0.0, 0.0, 0.0),
			testCase(0.0, 0.0));
		testEvalDeterministic(xor, DiscreteDomain.bool(),
			testCase(false, false, false ,false),
			testCase(false, false, false),
			testCase(false, false, false, false, false)
		);
		testEvalDeterministic(xor, DiscreteDomain.bool(),
			testCase(true, true),
			testCase(true, true, false),
			testCase(true, false, false, true)
		);
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
	
	private Value[] testCase(Object ... args)
	{
		Value[] out = new Value[args.length];
		for (int i = 0; i < args.length; i++)
			out[i] = Value.create(args[i]);
		return out;
	}
}
