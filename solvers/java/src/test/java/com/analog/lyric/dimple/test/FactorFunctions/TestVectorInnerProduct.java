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

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.VectorInnerProduct;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.values.Value;

public class TestVectorInnerProduct extends FactorFunctionTester
{

	@Test
	public void test()
	{
		VectorInnerProduct function = new VectorInnerProduct();
		
		// Some simple hand-coded cases
		testEvalDeterministic(function, RealDomain.unbounded(),
			new Value[] { Value.create(23.0), Value.create(2.0), Value.create(3.0), Value.create(4.0), Value.create(5.0) },
			new Value[] { Value.create(1.0), Value.create(7.0), Value.create(3.0), Value.create(-2.0), Value.create(5.0) },
			new Value[] { Value.create(23.5), Value.create(2.5), Value.create(3.5), Value.create(1.0), Value.create(6.0) }
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
			
			Value[][] testCases = new Value[nCases][];
			for (int i = 0; i < nCases; ++i)
			{
				Value[] testCase = testCases[i] = new Value[3];
				testCase[0] = Value.create(output);
				testCase[1] = Value.create(v1.clone());
				testCase[2] = Value.create(v2.clone());
			}
			
			testEvalDeterministic(function, new Domain[] { rd, rd2, rd2 } , testCases);
			
			for (int i = 0; i < nCases; ++i)
			{
				Value[] testCase = testCases[i] = new Value[2 + size];
				testCase[0] = Value.create(output);
				testCase[1] = Value.create(v1.clone());
				for (int j = 0; j < size; ++j)
				{
					testCase[2 + j] = Value.create(v2[j]);
				}
			}
			
			testEvalDeterministic(function, new Domain[] { rd, rd2, rd }, testCases);

			for (int i = 0; i < nCases; ++i)
			{
				Value[] testCase = testCases[i] = new Value[2 + size];
				testCase[0] = Value.create(output);
				for (int j = 0; j < size; ++j)
				{
					testCase[1 + j] = Value.create(v1[j]);
				}
				testCase[1 + size] = Value.create(v2.clone());
			}
			
			Domain[] domains = new Domain[2 + size];
			Arrays.fill(domains, rd);
			domains[1 + size] = rd2;
			
			testEvalDeterministic(function, domains, testCases);
			
			for (int i = 0; i < nCases; ++i)
			{
				Value[] testCase = testCases[i] = new Value[1 + 2 * size];
				testCase[0] = Value.create(output);
				for (int j = 0; j < size; ++j)
				{
					testCase[1 + j] = Value.create(v1[j]);
					testCase[1 + size + j] = Value.create(v2[j]);
				}
			}

			testEvalDeterministic(function, rd, testCases);
		}
	
		// TODO: test evalEnergy for non-deterministic cases
	}
}
