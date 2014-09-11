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
import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;

/**
 * Test to ensure that Gibbs solver orders deterministic directed factors correctly.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class TestGibbsDeterministicFactorOrdering
{
	@Test
	public void test()
	{
		// Builds a deterministic tree of the form:
		//
		//              v0
		//             /  \
		//            v1  v2
		//           /  \/  \
		//          v3  v4  v5
		//         /  \/  \/  \
		//        ..............
		//         \\\\\\\/////
		//              vx
		//
		// Where all factors are simple two operand sums with
		// output variables above the input variables. E.g.,
		// v0 = v1 + v2. All of the bottom variables are set
		// deterministically from single vx variable.
		
		final int depth = 5;
		
		final int nVars = depth * (depth + 1) / 2;
		Real[] vars = new Real[nVars];
		for (int i = 0; i < nVars; ++i)
		{
			vars[i] = new Real();
			vars[i].setName("v" + i);
		}
		
		FactorGraph fg = new FactorGraph();
		fg.addVariables(vars);

		// Create factors in random order.
		ArrayList<Integer> outputIndices = new ArrayList<>(nVars - depth);
		for (int i = nVars - depth; --i>=0;)
		{
			outputIndices.add(i);
		}
		Collections.shuffle(outputIndices);
		
		for (int outputIndex : outputIndices)
		{
			Real outputVar = vars[outputIndex];
			// Triangular root gives you the level in the tree:
			int level = (int)((Math.sqrt(8.0 * outputIndex + 1.0) - 1.0) / 2.0);
			int inputIndex1 = outputIndex + level + 1;
			Real inputVar1 = vars[inputIndex1];
			Real inputVar2 = vars[inputIndex1 + 1];
		
			Factor factor =
				fg.addFactor(new TestFunction(outputVar.getName(), new Sum()), outputVar, inputVar1, inputVar2);
			assertTrue(factor.isDirected());
		}
		
		Real startVar = new Real();
		Real[] baseVars = new Real[depth + 1];
		baseVars[0] = startVar;
		for (int i = 0; i < depth; ++i)
		{
			baseVars[i + 1] = vars[nVars - depth + i];
		}
		fg.addFactor(new TestFunction("dup", new Duplicate()), baseVars);
		
		GibbsSolverGraph sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		sfg.initialize();
		
		for (Factor factor : fg.getFactors())
		{
			TestFunction func = (TestFunction)factor.getFactorFunction();
			func._evalCount = 0;
		}

		ISolverVariableGibbs[] svars = new ISolverVariableGibbs[nVars];
		for (int i = 0; i < nVars; ++i)
		{
			svars[i] = sfg.getSolverVariable(vars[i]);
		}
		
		ISolverVariableGibbs startSVar = requireNonNull(sfg.getSolverVariable(startVar));
		startSVar.setCurrentSample(1);
		assertEquals(Math.pow(2.0, depth - 1), svars[0].getCurrentSampleValue().getDouble(), 0.0);
		
		for (Factor factor : fg.getFactors())
		{
			TestFunction func = (TestFunction)factor.getFactorFunction();
			assertEquals(1, func._evalCount);
		}
	}
	
	private class TestFunction extends FactorFunction
	{
//		private final String _name;
		private FactorFunction _delegate;
		int _evalCount = 0;
		
		private TestFunction(String name, FactorFunction delegate)
		{
//			_name = name;
			_delegate = delegate;
		}
		
	    @Override
		public final @Nullable int[] getDirectedToIndices(int numEdges)
	    {
	    	return _delegate.getDirectedToIndices(numEdges);
	    }

	    @Override
		public double evalEnergy(Value[] values)
		{
			return _delegate.evalEnergy(values);
		}
		
		@Override
		public void evalDeterministic(Value[] arguments)
		{
			_delegate.evalDeterministic(arguments);
			++_evalCount;
		}
		
		@Override
		public boolean isDeterministicDirected()
		{
			return _delegate.isDeterministicDirected();
		}
		
		@Override
		public boolean isDirected()
		{
			return _delegate.isDirected();
		}
	}
	
	private class Duplicate extends FactorFunction
	{
	    @Override
		public final @Nullable int[] getDirectedToIndices(int numEdges)
	    {
	    	int[] indices = new int[numEdges - 1];
	    	for (int i = 1; i < numEdges; ++i)
	    	{
	    		indices[i-1] = i;
	    	}
	    	return indices;
	    }

	    @Override
		public double evalEnergy(Value[] values)
		{
			Value value = values[0];
			for (int i = values.length; --i>=1;)
			{
				if (!value.valueEquals(values[i]))
				{
					return Double.POSITIVE_INFINITY;
				}
			}
			
			return 0.0;
		}
		
		@Override
		public void evalDeterministic(Value[] arguments)
		{
			Value value = arguments[0];
			for (int i = arguments.length; --i>=1;)
			{
				arguments[i].setFrom(value);
			}
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
