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

import com.analog.lyric.dimple.factorfunctions.MatrixVectorProduct;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;

public class TestMatrixVectorProduct extends FactorFunctionTester
{

	private final Random _rand = new Random(123123);
	
	@Test
	public void test()
	{
		for (int inLength = 2; inLength < 4; ++inLength)
		{
			for (int outLength = 2; outLength < 4; ++outLength)
			{
				testDeterministic(inLength, outLength);
			}
		}
	}
	
	private void testDeterministic(int inLength, int outLength)
	{
		final int nCases = 5;
		final MatrixVectorProduct function = new MatrixVectorProduct(inLength, outLength);
		
		final RealDomain rd = RealDomain.unbounded();
		final RealJointDomain rd2 = RealJointDomain.create(2);
		
		final int[] outputIndices = new int[outLength];
		for (int i = 0; i < outLength; ++i)
		{
			outputIndices[i] = i;
		}
		
		final double[][][] matrices = new double[nCases][][];
		final double[][] inVectors = new double[nCases][];
		final double[][] outVectors = new double[nCases][];
		
		for (int i = 0; i < nCases; ++i)
		{
			double[][] matrix = matrices[i] = new double[outLength][];
			for (int row = 0; row < outLength; ++row)
			{
				double[] rowValues = matrix[row] = new double[inLength];
				for (int col = 0; col < inLength; ++col)
				{
					rowValues[col] = 1 + (_rand.nextDouble() * 10);
				}
			}
	
			double[] inVector = inVectors[i] = new double[inLength];
			for (int col = 0; col < inLength; ++col)
			{
				inVector[col] = 1 + (_rand.nextDouble() * 10);
			}
		
			double[] outVector = outVectors[i] = new double[outLength];
			for (int row = 0; row < outLength; ++row)
			{
				for (int col = 0; col < inLength; ++col)
				{
					outVector[row] += matrix[row][col] * inVector[col];
				}
			}
		}
		
		//
		// Constant matrix and input vector
		//
		
		Domain[] domains = new Domain[2 + outLength];
		Arrays.fill(domains, rd);
		domains[outLength] = null; // matrix
		domains[outLength + 1] = rd2;
		
		Object[][] testCases = new Object[nCases][];
		for (int i = 0; i < nCases; ++i)
		{
			double[] inVector = inVectors[i];
			double[][] matrix = matrices[i];
			double[] outVector = outVectors[i];
			
			Object[] testCase = testCases[i] = new Object[2 + outLength];
			for (int j = 0; j < outLength; ++j)
			{
				testCase[j] = outVector[j];
			}
			testCase[outLength] = matrix;
			testCase[outLength + 1] = inVector.clone();
		}
		
		testEvalDeterministic(function, domains, outputIndices, testCases);
		
		//
		// Constant matrix, flattened variable vector
		//
		
		domains = new Domain[1 + outLength + inLength];
		Arrays.fill(domains, rd);
		domains[outLength] = null; // matrix
		
		for (int i = 0; i < nCases; ++i)
		{
			double[] inVector = inVectors[i];
			double[][] matrix = matrices[i];
			double[] outVector = outVectors[i];
			
			Object[] testCase = testCases[i] = new Object[domains.length];
			for (int j = 0; j < outLength; ++j)
			{
				testCase[j] = outVector[j];
			}
			testCase[outLength] = matrix;
			for (int j = 0; j < inLength; ++j)
			{
				testCase[outLength + 1 + j] = inVector[j];
			}
		}
		
		testEvalDeterministic(function, domains, outputIndices, testCases);
		
		//
		// Constant input vector, flattened variable matrix
		//
		
		domains = new Domain[1 + outLength + inLength * outLength];
		Arrays.fill(domains, rd);
		domains[domains.length - 1] = rd2;

		for (int i = 0; i < nCases; ++i)
		{
			double[] inVector = inVectors[i];
			double[][] matrix = matrices[i];
			double[] outVector = outVectors[i];

			Object[] testCase = testCases[i] = new Object[domains.length];
			for (int j = 0; j < outLength; ++j)
			{
				testCase[j] = outVector[j];
			}
			int j = outLength;
			for (int col = 0; col < inLength; ++col)
			{
				for (int row = 0; row < outLength; ++row)
				{
					testCase[j++] = matrix[row][col];
				}
			}
			testCase[domains.length - 1] = inVector;
		}

		testEvalDeterministic(function, domains, outputIndices, testCases);
		
		//
		// Flattened variable matrix and input vector
		//
		
		domains = new Domain[outLength + inLength + inLength * outLength];
		Arrays.fill(domains, rd);
		int inVectorOffset = domains.length - inLength;

		for (int i = 0; i < nCases; ++i)
		{
			double[] inVector = inVectors[i];
			double[][] matrix = matrices[i];
			double[] outVector = outVectors[i];

			Object[] testCase = testCases[i] = new Object[domains.length];
			for (int j = 0; j < outLength; ++j)
			{
				testCase[j] = outVector[j];
			}
			int j = outLength;
			for (int col = 0; col < inLength; ++col)
			{
				for (int row = 0; row < outLength; ++row)
				{
					testCase[j++] = matrix[row][col];
				}
			}
			for (j = 0; j < inLength; ++j)
			{
				testCase[inVectorOffset + j] = inVector[j];
			}
		}

		testEvalDeterministic(function, domains, outputIndices, testCases);
		
		// TODO: test evalEnergy for non-deterministic cases w/ smoothing
	}

}
