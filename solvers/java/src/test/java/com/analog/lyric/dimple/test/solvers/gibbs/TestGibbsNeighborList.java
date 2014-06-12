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

package com.analog.lyric.dimple.test.solvers.gibbs;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.factorfunctions.MatrixProduct;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.ISolverNodeGibbs;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;
import com.analog.lyric.dimple.solvers.gibbs.Solver;

public class TestGibbsNeighborList
{
	@SuppressWarnings("null")
	@Test
	public void test()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new Solver());
		
		Real[][] aMatrix = makeRealMatrix("a", 2,4);
		Real[] aVars = flattenRealMatrix(aMatrix);
		
		Real[][] bMatrix = makeRealMatrix("b", 4,3);
		Real[] bVars = flattenRealMatrix(bMatrix);

		Real[][] cMatrix = makeRealMatrix("c", 2,3);
		Real[] cVars = flattenRealMatrix(cMatrix);

		Real[] vars = new Real[aVars.length + bVars.length + cVars.length];
		System.arraycopy(cVars, 0, vars, 0, cVars.length);
		System.arraycopy(aVars, 0, vars, cVars.length, aVars.length);
		System.arraycopy(bVars, 0, vars, cVars.length + aVars.length, bVars.length);
		
		Factor matrixProduct = fg.addFactor(new MatrixProduct(2,4,3), vars);
		
		fg.initialize();

		// The inputs should have empty scoring lists because the output variables
		// aren't attached to any other factors and don't have inputs.
		for (Real var : cVars)
		{
			assertFalse(((ISolverVariableGibbs)var.getSolver()).hasPotential());
			assertScoreNodes(var, matrixProduct);
		}
		for (Real var : aVars)
		{
			assertScoreNodes(var);
		}
		for (Real var : bVars)
		{
			assertScoreNodes(var);
		}
		
		//
		// Now set inputs on the matrix product output variables and verify that the appropriate variables
		// show up as neighbors of the matrix product input variables.
		//
		
		for (Real var : cVars)
		{
			var.setInput(new Normal());
		}
		
		fg.initialize();
		
		for (Real var : cVars)
		{
			assertTrue(((ISolverVariableGibbs)var.getSolver()).hasPotential());
			assertScoreNodes(var, matrixProduct);
		}
		for (int row = 0, rows = aMatrix.length; row < rows; ++row)
		{
			for (int col = 0, cols = aMatrix[0].length; col < cols; ++col)
			{
				assertScoreNodes(aMatrix[row][col], cMatrix[row]);
			}
		}
		for (int col = 0, cols = bMatrix[0].length; col < cols; ++col)
		{
			Real[] expected = new Real[cMatrix.length];
			for (int crow = 0; crow < cMatrix.length; ++crow)
			{
				expected[crow] = cMatrix[crow][col];
			}
			for (int row = 0, rows = bMatrix.length; row < rows; ++row)
			{
				assertScoreNodes(bMatrix[row][col], expected);
			}
		}
		
		//
		// Clear inputs and instead, attach to Normals as variable
		//
		
		Factor[][] cNormalMatrix = new Factor[cMatrix.length][cMatrix[0].length];
		Factor[] cNormals = new Factor[cMatrix.length * cMatrix[0].length];
		for (int col = 0, cols = cMatrix[0].length, i = 0; col < cols; ++col)
		{
			for (int row = 0, rows = cMatrix.length; row < rows; ++row)
			{
				Real cVar = cMatrix[row][col];
				cVar.setInput((FactorFunction)null);
				Factor factor = fg.addFactor(new Normal(), cVar, 1, new Real(RealDomain.nonNegative()));
				cNormalMatrix[row][col] = factor;
				cNormals[i++] = factor;
			}
		}
		
		fg.initialize();
		
		for (int i = 0; i < cVars.length; ++i)
		{
			Real var = cVars[i];
			assertFalse(((ISolverVariableGibbs)var.getSolver()).hasPotential());
			assertScoreNodes(var, matrixProduct, cNormals[i]);
		}
		for (int row = 0, rows = aMatrix.length; row < rows; ++row)
		{
			for (int col = 0, cols = aMatrix[0].length; col < cols; ++col)
			{
				assertScoreNodes(aMatrix[row][col], cNormalMatrix[row]);
			}
		}
		for (int col = 0, cols = bMatrix[0].length; col < cols; ++col)
		{
			Node[] expected = new Node[cMatrix.length];
			for (int crow = 0; crow < cMatrix.length; ++crow)
			{
				expected[crow] = cNormalMatrix[crow][col];
			}
			for (int row = 0, rows = bMatrix.length; row < rows; ++row)
			{
				assertScoreNodes(bMatrix[row][col], expected);
			}
		}
		
		//
		// Make sure that visiting the same factor twice from different edges gets
		// reflected in the neighbors.
		//
		
		Real x = new Real(RealDomain.nonNegative());
		fg.addFactor(new Copy(), x, aMatrix[0][0], bMatrix[1][1]);
		
		fg.initialize();
		
		{
			ArrayList<Node> expectedList = new ArrayList<Node>();
			for (Node node : cNormalMatrix[0])
				expectedList.add(node);
			for (int row = 0, rows = cMatrix.length; row < rows; ++row)
				expectedList.add(cNormalMatrix[row][1]);
			assertScoreNodes(x, expectedList.toArray(new Node[expectedList.size()]));
		}
		
	} // test
	
	/*----------------
	 * Helper methods
	 */
	
	private void assertScoreNodes(VariableBase var, Node ... scoreNodes)
	{
		Set<ISolverNodeGibbs> expectedNodes = new HashSet<ISolverNodeGibbs>(scoreNodes.length);
		for (Node node : scoreNodes)
		{
			expectedNodes.add((ISolverNodeGibbs)node.getSolver());
		}
		
		ISolverVariableGibbs svar = requireNonNull((ISolverVariableGibbs)var.getSolver());
		
		int count = 0;
		ReleasableIterator<ISolverNodeGibbs> iter = svar.getSampleScoreNodes();
		while (iter.hasNext())
		{
			++count;
			ISolverNodeGibbs snode = iter.next();
			assertTrue(expectedNodes.contains(snode));
		}
		iter.release();
		
		assertEquals(expectedNodes.size(), count);
	}
	
	private Real[][] makeRealMatrix(String namePrefix, int rows, int cols)
	{
		Real[][] matrix = new Real[rows][cols];
		for (int row = 0; row < rows; ++row)
			for (int col = 0; col < cols; ++col)
			{
				Real var = new Real(RealDomain.nonNegative());
				var.setName(String.format("%s[%d,%d]", namePrefix, row, col));
				matrix[row][col] = var;
			}
		return matrix;
	}
	
	private Real[] flattenRealMatrix(Real[][] matrix)
	{
		final int rows = matrix.length;
		final int cols = matrix[0].length;
		
		Real[] flattened = new Real[rows * cols];
		
		int cur = 0;
		
		for (int col = 0; col < cols; ++col)
			for (int row = 0; row < rows; ++row)
				flattened[cur++] = matrix[row][col];
		
		return flattened;
	}
	
	/*-----------------
	 * FactorFunctions
	 */
	
	// Replicates first argument to remaining args.
	static class Copy extends FactorFunction
	{
		@Override
		public int[] getDirectedToIndices(int numEdges)
		{
			int[] edges = new int[numEdges - 1];
			for (int i = edges.length; --i>=0;)
				edges[i] = i + 1;
			return edges;
		}
		
		@Override
		public double evalEnergy(Object ... arguments)
		{
			Object value = arguments[0];
			for (int i = arguments.length; --i>=1;)
				if (!value.equals(arguments[i]))
					return Double.POSITIVE_INFINITY;
			
			return 0.0;
		}
		
		@Override
		public void evalDeterministic(Object[] arguments)
		{
			Object value = arguments[0];
			for (int i = arguments.length; --i>=1;)
				arguments[i] = value;
		}
		
		@Override
		public boolean isDeterministicDirected()
		{
			return true;
		}
		
		@Override
		public boolean isDirected()
		{
			return true;
		}
	}
	

}
