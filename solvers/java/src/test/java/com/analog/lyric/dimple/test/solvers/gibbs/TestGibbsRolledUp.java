/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static com.analog.lyric.math.MoreMatrixUtils.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.repeated.BitStream;
import com.analog.lyric.dimple.model.repeated.DoubleArrayDataSink;
import com.analog.lyric.dimple.model.repeated.DoubleArrayDataSource;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestGibbsRolledUp extends DimpleTestBase
{
	/**
	 * Adapted from MATLAB algoRolledUpGraphs/testGibbsTableFactor.m
	 */
	@Test
	public void testGibbsTableFactor()
	{
		final int N = 100;
		final int bufferSize = 1;
		
		// Create graph
		
		Bit xi = name("xi", new Bit());
		Bit xo = name("xo", new Bit());
		FactorGraph sg = new FactorGraph(xi, xo);
		IFactorTable table = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit());
		table.setWeightsDense(new double[] {0,1,1,0});
		sg.addFactor(table, xi, xo);
		
		FactorGraph fg = new FactorGraph();
		BitStream x = new BitStream("x");
		fg.addRepeatedFactorWithBufferSize(sg, bufferSize, x, x.getSlice(1));
		
		// Generate data
		final double[][] input = new double[N][];
		double val = 1.0;//testRand.nextBoolean() ? 1 : 0;
		input[0] = new double[] { val, 1 - val };
		for (int i = 1; i < N; ++i)
		{
			double p = testRand.nextDouble();
			val = p > table.getWeightForIndices((int)input[i-1][0], 0) ? 1 : 0;
			input[i] = new double[] { val, 1 - val};
		}
		
		// Solve using sum-product
		SumProductSolverGraph sfg1 = requireNonNull(fg.setSolverFactory(new SumProductSolver()));
		x.setDataSource(new DoubleArrayDataSource(input));
		DoubleArrayDataSink sink1 = new DoubleArrayDataSink();
		x.setDataSink(sink1);
		sfg1.solve();
		
		// Solve again using Gibbs
		GibbsSolverGraph sfg2 = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		x.setDataSource(new DoubleArrayDataSource(input));
		DoubleArrayDataSink sink2 = new DoubleArrayDataSink();
		x.setDataSink(sink2);
		
		sfg2.solve();
		
		RealMatrix b1 = wrapRealMatrix(sink1.getArray());
		RealMatrix b2 = wrapRealMatrix(sink2.getArray());
		
//		RealMatrixFormat fmt = new RealMatrixFormat("[","]","","", "; ", ",");
//		System.out.println(fmt.format(wrapRealMatrix(input)));
//		System.out.println(fmt.format(b1));
//		System.out.println(fmt.format(b2));

		assertEquals(0.0, b1.subtract(b2).getNorm(), 1e-20);
		
	}
}
