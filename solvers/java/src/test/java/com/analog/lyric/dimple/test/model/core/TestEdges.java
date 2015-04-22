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

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.junit.Test;

import com.analog.lyric.dimple.model.core.Edge;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * git@github.com:AnalogDevicesLyricLabs/dimple.git
 * @since 0.08
 * @author Christopher Barber
 */
public class TestEdges extends DimpleTestBase
{
	@Test
	public void test()
	{
		FactorGraph template = new FactorGraph("sub");
		try (CurrentModel cur = using(template))
		{
			Real x = boundary(real("x")), y = boundary(real("y"));
			name("z", sum(x,y));
		}
		
		FactorGraph fg = new FactorGraph("fg");
		testGraphEdgeInvariants(fg);
		
		try (CurrentModel cur = using(fg))
		{
			Real a = real("a"), b = real("b");
			name("c", sum(a,b));
			
			testGraphEdgeInvariants(fg);
		
			FactorGraph sub = fg.addGraph(template, a, b);
			testGraphEdgeInvariants(fg);
			testGraphEdgeInvariants(sub);

			sub.remove(sub.getFactors().getByIndex(0));
			testGraphEdgeInvariants(fg);
			testGraphEdgeInvariants(sub);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void testGraphEdgeInvariants(FactorGraph fg)
	{
		Collection<Edge> edges = fg.getGraphEdges();
		Collection<EdgeState> edgeStates = fg.getGraphEdgeState();
		
		assertEquals(edges.size(), edgeStates.size());

		Iterator<Edge> edgeIter = edges.iterator();
		Iterator<EdgeState> edgeStateIter = edgeStates.iterator();
		for (int i = 0, n = edges.size(); i < n; ++i)
		{
			assertTrue(edgeIter.hasNext());
			assertTrue(edgeStateIter.hasNext());
			Edge edge = edgeIter.next();
			EdgeState edgeState = edgeStateIter.next();
			
			assertSame(fg, edge.graph());
			assertSame(edgeState, edge.edgeState());
			assertSame(edge.edgeIndex(), edgeState.edgeIndex(fg));
			assertSame(edge.type(), edgeState.type(fg));
			
			assertTrue(edge.edgeIndex() >= 0);
			assertTrue(edge.edgeIndex() <= fg.getGraphEdgeStateMaxIndex());
			
			assertSame(fg, edge.getParentGraph());
			assertSame(fg.getRootGraph(), edge.getRootGraph());
			
			UUID uuid = edge.getUUID();
			assertEquals(edge.getGlobalId(), Ids.globalIdFromUUID(uuid));
			
			assertEquals(edge.getLocalId(), edge.getId());
			assertEquals(edge, fg.getChildByLocalId(edge.getLocalId()));
			assertEquals(edge, fg.getRootGraph().getChildByGlobalId(edge.getGlobalId()));
			assertEquals(edge, fg.getRootGraph().getChildByGraphTreeId(edge.getGraphTreeId()));
			
			assertSame(edgeState, fg.getGraphEdgeState(edgeState.edgeIndexInParent(fg)));
			assertSame(edgeState, edgeState.getFactorParent(fg).getGraphEdgeState(edgeState.factorEdgeIndex()));
			assertSame(edgeState, edgeState.getVariableParent(fg).getGraphEdgeState(edgeState.variableEdgeIndex()));
			
			Factor factor = edge.factor();
			Variable variable = edge.variable();
			assertSame(factor, edge.getSibling(variable));
			assertSame(variable, edge.getSibling(factor));
			assertEquals(edge, factor.getSiblingEdge(edge.edgeState().getFactorToVariableEdgeNumber()));
			assertEquals(edge, variable.getSiblingEdge(edge.edgeState().getVariableToFactorEdgeNumber()));
			
			assertSame(factor, edgeState.getFactorParent(fg).getFactorByLocalId(edgeState.factorLocalId()));
			assertSame(variable, edgeState.getFactorParent(fg).getVariableByLocalId(edgeState.variableLocalId()));
			
			switch (edge.type())
			{
			case LOCAL:
				assertTrue(edge.isLocal());
				assertSame(fg, edgeState.getFactorParent(fg));
				assertSame(fg, edgeState.getVariableParent(fg));
				break;
			case INNER:
				assertFalse(edge.isLocal());
				assertTrue(fg.isAncestorOf(edgeState.getFactorParent(fg)));
				assertSame(fg, edgeState.getVariableParent(fg));
				break;
			case OUTER:
				assertFalse(edge.isLocal());
				assertTrue(edgeState.getVariableParent(fg).isAncestorOf(fg));
				assertSame(fg, edgeState.getFactorParent(fg));
				break;
			}
		}
		assertFalse(edgeIter.hasNext());
		assertFalse(edgeStateIter.hasNext());
		
	}
}
