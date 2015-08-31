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

package com.analog.lyric.dimple.test.model.core;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.analog.lyric.dimple.data.DataLayer;
import com.analog.lyric.dimple.data.GenericDataLayer;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.options.DimpleOptions;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestFreezing extends DimpleTestBase
{
	@Test
	public void test()
	{
		FactorGraph fg0 = new FactorGraph();
		Real a = new Real(), b = new Real();
		fg0.addVariables(a, b);
		fg0.addFactor(new Normal(), 0.0, 1.0, a, b);
		
		expectThrow(IllegalStateException.class, ".*has a solver graph.*", fg0, "freezeGraphTree");
		fg0.setSolverFactory(null);
		
		fg0.setDefaultConditioningLayer(new GenericDataLayer(fg0));
		expectThrow(IllegalStateException.class, ".*default conditioning layer is not null.*", fg0, "freezeGraphTree");
		fg0.setDefaultConditioningLayer(null);
		
		assertFalse(fg0.frozen());
		fg0.freezeGraphTree();
		expectFrozenErrors(fg0);
		
		FactorGraph fg1 = new FactorGraph();
		fg1.addGraph(fg0);
		assertFalse(fg1.frozen());
		fg1.setSolverFactory(null);
		fg1.freezeGraphTree();
		expectFrozenErrors(fg1);
	}
	
	private void expectFrozenErrors(FactorGraph graph)
	{
		assertTrue(graph.frozen());
		graph.freezeGraphTree(); // no effect
	
		expectFrozenErrors((Node)graph);
		
		expectFrozen(graph, "addBoundaryVariables", new Real());
		expectFrozen(graph, "addConstant", 42);
		expectFrozen(graph, "addConstant", Value.create(42));
		expectFrozen(graph, "addFactor", new Normal());
		expectFrozen(graph, "addFactor", "Normal");
		expectFrozen(graph, "addGraph", new FactorGraph());
		expectFrozen(graph, "addVariables", new Real());
		expectFrozen(graph, "reindexGraphTree");
		expectFrozen(graph, "setDefaultConditioningLayer", (DataLayer<?>)null);
		expectFrozen(graph, "setEventAndOptionParent", (IDimpleEventSource)null);
		expectFrozen(graph, "setScheduler", (IScheduler)null);
		expectFrozen(graph, "setSolver", (ISolverFactorGraph)null);
		expectFrozen(graph, "setSolverFactory", new SumProductSolver());

		// Deprecated
		expectFrozen(graph, "clearNames");
		expectFrozen(graph, "defineVariableGroup", new ArrayList<>());
		expectFrozen(graph, "setEventListener", (DimpleEventListener)null);
		expectFrozen(graph, "setNamesByStructure");
		expectFrozen(graph, "setNamesByStructure", "b", "o", "f", "r", "c");
		expectFrozen(graph, "setSchedule", (ISchedule)null);
		
		for (Variable var : graph.getOwnedVariables())
		{
			expectFrozenErrors(var);
			expectFrozen(graph, "remove", var);
			expectFrozen(graph, "removeVariables", var);
			expectFrozen(graph, "addVariableBlock", var);
			expectFrozen(graph, "join", var);
			expectFrozen(graph, "split", var);
			expectFrozen(graph, "setChildName", var, null);
		}
		for (Factor factor : graph.getOwnedFactors())
		{
			expectFrozenErrors(factor);
			expectFrozen(graph, "join", factor);
			expectFrozen(graph, "join", new Variable[] { new Real() }, factor);
			expectFrozen(graph, "remove", factor);
			expectFrozen(graph, "replaceEdge", factor, 0, new Real());
		}
		for (FactorGraph subgraph : graph.getOwnedGraphs())
		{
			expectFrozenErrors(subgraph);
			expectFrozen(graph, "absorbSubgraph", subgraph);
			expectFrozen(graph, "remove", subgraph);
		}
	}
	
	private void expectFrozenErrors(Node node)
	{
		// INameable
		expectFrozen(node, "setName", "barf");
		expectFrozen(node, "setLabel", "barf");
		
		// IOptionHolder
		expectFrozen(node, "clearLocalOptions");
		expectFrozen(node, "setOption", DimpleOptions.randomSeed, 1234);
		expectFrozen(node, "unsetOption", DimpleOptions.randomSeed);
		
		expectFrozen(node, "clearMarked");
		expectFrozen(node, "clearVisited");
		expectFrozen(node, "setMarked");
		expectFrozen(node, "setVisited");
		
		// FactorGraphChild
		expectFrozen(node, "setLocalId", 0);
	}
	
	@SafeVarargs
	private final <T> void expectFrozen(Object object, String methodName, T ... args)
	{
		expectThrow(IllegalStateException.class, "Changes cannot be made to frozen graph", object, methodName, args);
	}
}
