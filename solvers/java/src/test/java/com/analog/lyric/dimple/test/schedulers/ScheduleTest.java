/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

/**
 * 
 */
package com.analog.lyric.dimple.test.schedulers;

import static java.util.Objects.*;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.XorDelta;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolverGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.util.test.Helpers;

/**
 * @author schweitz
 *
 */
public class ScheduleTest extends DimpleTestBase
{
	@Test
	public void test1()
	{
		int factors = 10;
		String tag = "crazy";
		int iterations = 1;

		FactorGraph fg = new FactorGraph();
		MinSumSolverGraph solver = requireNonNull(fg.setSolverFactory(new MinSumSolver()));
		fg.setName(tag);
		solver.setNumIterations(iterations);

		Discrete[] variables = new Discrete[factors + 1];
		for(int variable = 0; variable < variables.length; ++variable)
		{
			variables[variable] = new Discrete(0.0, 1.0);
		}
		XorDelta xorFF = new XorDelta();
		for(int factor = 0; factor < factors; ++factor)
		{
			fg.addFactor(xorFF, variables[factor], variables[factor + 1]);
		}
		//fg.setNamesByStructure();

		VariableList variableList = fg.getVariables();
		double[][] trivialRandomCodeword =
			com.analog.lyric.util.test.Helpers
				.trivialRandomCodeword(variableList.size());
		for(int variable = 0; variable < variableList.size(); ++variable)
		{
			((Discrete)variableList.getByIndex(variable)).setInput(trivialRandomCodeword[variable]);
		}

		fg.solve();
		double[][] beliefsA = Helpers.beliefs(fg, true);

		FixedSchedule fs = new FixedSchedule(fg);
		for(Variable vb : fg.getVariables())
		{
			fs.add(new NodeScheduleEntry(vb));
		}
		for(Factor f : fg.getNonGraphFactors())
		{
			fs.add(new NodeScheduleEntry(f));
		}
		ISchedule oldSchedule = solver.getSchedule();
		solver.setSchedule(fs);

		fg.solve();
		double[][] beliefsB = Helpers.beliefs(fg, true);

		Helpers.assertBeliefsDifferent(beliefsA, beliefsB);

		solver.setSchedule(oldSchedule);
		fg.solve();
		double[][] beliefsC = Helpers.beliefs(fg, true);
		Helpers.compareBeliefs(beliefsA, beliefsC);

		fg = Helpers.MakeSimpleChainGraph(tag, fg.getFactorGraphFactory(), factors, true);
		solver = (MinSumSolverGraph)requireNonNull(fg.getSolver());
		solver.setNumIterations(iterations);
		fg.solve();

		beliefsA = Helpers.beliefs(fg, true);

		fs = new FixedSchedule(fg);
		for(Variable vb : fg.getVariables())
		{
			fs.add(new NodeScheduleEntry(vb));
		}
		for(Factor f : fg.getNonGraphFactors())
		{
			fs.add(new NodeScheduleEntry(f));
		}
		oldSchedule = solver.getSchedule();
		solver.setSchedule(fs);

		fg.solve();
		beliefsB = Helpers.beliefs(fg, true);

		Helpers.assertBeliefsDifferent(beliefsA, beliefsB);

		solver.setSchedule(oldSchedule);
		fg.solve();

		beliefsC = Helpers.beliefs(fg, true);
		Helpers.compareBeliefs(beliefsA, beliefsC);
	}

}
