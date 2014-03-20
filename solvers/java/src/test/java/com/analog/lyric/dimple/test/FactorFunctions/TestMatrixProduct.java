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

import com.analog.lyric.dimple.factorfunctions.MatrixProduct;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;

public class TestMatrixProduct extends FactorFunctionTester
{
	private final Random _rand = new Random(123123);
	
	@Test
	public void test()
	{
		final int minSize = 2, maxSize = 4;

		for (int nr = minSize; nr <= maxSize; ++nr)
			for (int nx = minSize; nx <= maxSize; ++nx)
				for (int nc = minSize; nc <= maxSize; ++nc)
					testDeterministic(nr, nx, nc);
	}
	
	private void testDeterministic(final int nr, final int nx, final int nc)
	{
		final int nCases = 5;
		
		final int outLength = nr * nc;
		
		final int in1Row = nr;
		final int in1Col = nx;
		final int in2Row = nx;
		final int in2Col = nc;
		
		final int in1Length = in1Row * in1Col;
		final int in2Length = in2Row * in2Col;
		
		final MatrixProduct function = new MatrixProduct(nr, nx, nc);
		
		final int[] outputIndices = new int[outLength];
		for (int i = 0; i < outLength; ++i)
		{
			outputIndices[i] = i;
		}
		
		final double[][][] in1Matrices = new double[nCases][][];
		final double[][][] in2Matrices = new double[nCases][][];
		final double[][][] outMatrices = new double[nCases][][];
		
		for (int i = 0; i < nCases; ++i)
		{
			double[][] in1Matrix = in1Matrices[i] = new double[in1Row][];
			for (int row = 0; row < in1Row; ++row)
			{
				double[] rowValues = in1Matrix[row] = new double[in1Col];
				for (int col = 0; col < in1Col; ++col)
				{
					rowValues[col] = 1 + (_rand.nextDouble() * 10);
				}
			}
			double[][] in2Matrix = in2Matrices[i] = new double[in2Row][];
			for (int row = 0; row < in2Row; ++row)
			{
				double[] rowValues = in2Matrix[row] = new double[in2Col];
				for (int col = 0; col < in2Col; ++col)
				{
					rowValues[col] = 1 + (_rand.nextDouble() * 10);
				}
			}
			double[][] outMatrix = outMatrices[i] = new double[nr][];
			for (int row = 0; row < nr; ++row)
			{
				outMatrix[row] = new double[nc];
				for (int col = 0; col < nc; ++col)
				{
					double out = 0.0;
					for (int x = 0; x < nx; ++x)
						out += in1Matrix[row][x] * in2Matrix[x][col];
					outMatrix[row][col] = out;
				}
			}
		}
		
		final RealDomain rd = RealDomain.unbounded();
		Domain[] domains = null;
		Object[][] testCases = new Object[nCases][];
		
		//
		// Constant matrix inputs
		//
		
		domains = new Domain[2 + outLength];
		Arrays.fill(domains, rd);
		domains[outLength] = null; // matrix
		domains[outLength + 1] = null;

		for (int i = 0; i < nCases; ++i)
		{
			double[][] in1Matrix = in1Matrices[i];
			double[][] in2Matrix = in2Matrices[i];
			double[][] outMatrix = outMatrices[i];

			Object[] testCase = testCases[i] = new Object[2 + outLength];
			int j = 0;
			for (int col = 0; col < nc; ++col)
				for (int row = 0; row < nr; ++row)
					testCase[j++] = outMatrix[row][col];
			testCase[outLength] = in1Matrix;
			testCase[outLength + 1] = in2Matrix;
		}

		testEvalDeterministic(function, domains, outputIndices, testCases);

		//
		// Second matrix constant, first flattened variables
		//

		domains = new Domain[1 + outLength + in1Length];
		Arrays.fill(domains, rd);
		domains[domains.length - 1] = null; // second input matrix

		for (int i = 0; i < nCases; ++i)
		{
			double[][] in1Matrix = in1Matrices[i];
			double[][] in2Matrix = in2Matrices[i];
			double[][] outMatrix = outMatrices[i];

			Object[] testCase = testCases[i] = new Object[domains.length];
			int j = 0;
			for (int col = 0; col < nc; ++col)
				for (int row = 0; row < nr; ++row)
					testCase[j++] = outMatrix[row][col];
			for (int col = 0; col < in1Col; ++col)
				for (int row = 0; row < in1Row; ++row)
					testCase[j++] = in1Matrix[row][col];
			testCase[j++] = in2Matrix;
		}

		testEvalDeterministic(function, domains, outputIndices, testCases);

		//
		// First matrix constant, second flattened variables
		//

		domains = new Domain[1 + outLength + in2Length];
		Arrays.fill(domains, rd);
		domains[outLength] = null; // first input matrix

		for (int i = 0; i < nCases; ++i)
		{
			double[][] in1Matrix = in1Matrices[i];
			double[][] in2Matrix = in2Matrices[i];
			double[][] outMatrix = outMatrices[i];

			Object[] testCase = testCases[i] = new Object[domains.length];
			int j = 0;
			for (int col = 0; col < nc; ++col)
				for (int row = 0; row < nr; ++row)
					testCase[j++] = outMatrix[row][col];
			testCase[j++] = in1Matrix;
			for (int col = 0; col < in2Col; ++col)
				for (int row = 0; row < in2Row; ++row)
					testCase[j++] = in2Matrix[row][col];
		}

		testEvalDeterministic(function, domains, outputIndices, testCases);
		
		//
		// Flattened matrix variable inputs
		//
		
		domains = new Domain[] { rd };

		for (int i = 0; i < nCases; ++i)
		{
			double[][] in1Matrix = in1Matrices[i];
			double[][] in2Matrix = in2Matrices[i];
			double[][] outMatrix = outMatrices[i];

			Object[] testCase = testCases[i] = new Object[outLength + in1Length + in2Length];
			int j = 0;
			for (int col = 0; col < nc; ++col)
				for (int row = 0; row < nr; ++row)
					testCase[j++] = outMatrix[row][col];
			for (int col = 0; col < in1Col; ++col)
				for (int row = 0; row < in1Row; ++row)
					testCase[j++] = in1Matrix[row][col];
			for (int col = 0; col < in2Col; ++col)
				for (int row = 0; row < in2Row; ++row)
					testCase[j++] = in2Matrix[row][col];
		}

		testEvalDeterministic(function, domains, outputIndices, testCases);
		
		// TODO: test evalEnergy for non-deterministic cases w/ smoothing
	}
}
