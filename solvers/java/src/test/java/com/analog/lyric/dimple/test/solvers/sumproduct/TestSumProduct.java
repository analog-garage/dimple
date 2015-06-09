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

package com.analog.lyric.dimple.test.solvers.sumproduct;

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.XorDelta;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestSumProduct extends DimpleTestBase
{
	@Test
	public void testDamping()
	{
		// Adapted from MATLAB testDamping.m
		
		for (boolean before : new boolean[] { true, false })
		{
			FactorGraph fg = new FactorGraph();
			try (CurrentModel model = using(fg))
			{
				double dampingVal = 0.0;
				
				if (before)
				{
					dampingVal = .4;
					fg.setOption(BPOptions.damping, dampingVal);
				}
				
				Bit a = bit("a"), b = bit("b");
				fg.addFactor(new XorDelta(), a, b);
				
				a.setInput(.8);
				
				if (!before)
				{
					dampingVal = .7;
					fg.setOption(BPOptions.damping, dampingVal);
				}
				
				SumProductSolverGraph sfg = fg.setSolverFactory(new SumProductSolver());
				
				fg.solve();
				
				double msg2xor = dampingVal * .5 + (1-dampingVal) * .8;
				double msgFromXor = dampingVal * .5 + (1-dampingVal)*msg2xor;
				
				assertEquals(msgFromXor, b.getBelief()[1], 1e-12);
			}
		}
	}
}
