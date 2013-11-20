package com.analog.lyric.dimple.test.FactorFunctions;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.VectorInnerProduct;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;

public class TestVectorInnerProduct extends FactorFunctionTester
{

	@Test
	public void test()
	{
		VectorInnerProduct function = new VectorInnerProduct();
		
		// Some simple hand-coded cases
		testEvalDeterministic(function, RealDomain.unbounded(),
			new Object[] { 23.0, 2.0, 3.0, 4.0, 5.0 },
			new Object[] { 1.0, 7.0, 3.0, -2.0, 5.0 },
			new Object[] { 23.5, 2.5, 3.5, 1.0, 6.0 }
			);

		Random rand = new Random(123456);
		int nCases = 5;
		
		RealDomain rd = RealDomain.unbounded();
		RealJointDomain rd2 = RealJointDomain.create(2);
		
		for (int size = 2; size < 5; ++size)
		{
			double[] v1 = new double[size];
			double[] v2 = new double[size];
			double output = 0.0;
			
			for (int i = 0; i < size; ++i)
			{
				v1[i] = rand.nextDouble() * 10;
				v2[i] = rand.nextDouble() * 10;
				output += v1[i] * v2[i];
			}
			
			Object[][] testCases = new Object[nCases][];
			for (int i = 0; i < nCases; ++i)
			{
				Object[] testCase = testCases[i] = new Object[3];
				testCase[0] = output;
				testCase[1] = v1.clone();
				testCase[2] = v2.clone();
			}
			
			testEvalDeterministic(function, new Domain[] { rd, rd2, rd2 } , testCases);
			
			for (int i = 0; i < nCases; ++i)
			{
				Object[] testCase = testCases[i] = new Object[2 + size];
				testCase[0] = output;
				testCase[1] = v1.clone();
				for (int j = 0; j < size; ++j)
				{
					testCase[2 + j] = v2[j];
				}
			}
			
			testEvalDeterministic(function, new Domain[] { rd, rd2, rd }, testCases);

			for (int i = 0; i < nCases; ++i)
			{
				Object[] testCase = testCases[i] = new Object[2 + size];
				testCase[0] = output;
				for (int j = 0; j < size; ++j)
				{
					testCase[1 + j] = v1[j];
				}
				testCase[1 + size] = v2.clone();
			}
			
			Domain[] domains = new Domain[2 + size];
			Arrays.fill(domains, rd);
			domains[1 + size] = rd2;
			
			testEvalDeterministic(function, domains, testCases);
			
			for (int i = 0; i < nCases; ++i)
			{
				Object[] testCase = testCases[i] = new Object[1 + 2 * size];
				testCase[0] = output;
				for (int j = 0; j < size; ++j)
				{
					testCase[1 + j] = v1[j];
					testCase[1 + size + j] = v2[j];
				}
			}

			testEvalDeterministic(function, rd, testCases);
		}
		
	}
}
