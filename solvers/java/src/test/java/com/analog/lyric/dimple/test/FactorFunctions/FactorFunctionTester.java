package com.analog.lyric.dimple.test.FactorFunctions;

import static org.junit.Assert.*;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;

public class FactorFunctionTester
{
	public static void testEvalDeterministic(FactorFunction function, Object[] in, Object[] expectedOut)
	{
		assertTrue(function.isDeterministicDirected());
		assertTrue(function.isDirected());
		
		Object[] in2 = in.clone();
		
		function.evalDeterministicFunction(in2);
		assertArrayEquals(expectedOut, in2);
		
		Object[] in3 = in.clone();
		assertEquals(expectedOut[0], function.getDeterministicFunctionValue(in3));
		
		
	}
}
