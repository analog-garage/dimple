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

package com.analog.lyric.dimple.test.model;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.Queue;

import org.junit.Test;

import com.analog.lyric.dimple.events.DimpleEventHandler;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.FactorGraphEvent;
import com.analog.lyric.dimple.events.FactorGraphFactorEvent;
import com.analog.lyric.dimple.events.FactorGraphSubgraphEvent;
import com.analog.lyric.dimple.events.FactorGraphVariableEvent;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.BoundaryVariableAddEvent;
import com.analog.lyric.dimple.model.core.BoundaryVariableRemoveEvent;
import com.analog.lyric.dimple.model.core.FactorAddEvent;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorRemoveEvent;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.SubgraphAddEvent;
import com.analog.lyric.dimple.model.core.SubgraphRemoveEvent;
import com.analog.lyric.dimple.model.core.VariableAddEvent;
import com.analog.lyric.dimple.model.core.VariableRemoveEvent;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.util.test.SerializationTester;

/**
 * Tests for {@link FactorGraphEvent} generation.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestFactorGraphEvent extends DimpleTestBase
{
	@Test
	public void test()
	{
		final FactorGraphEventHandler handler = new FactorGraphEventHandler();
		final DimpleEventListener listener = new DimpleEventListener();
		
		FactorGraph root = new FactorGraph();
		root.setName("root");
		
		// Initially listen for all factor graph events
		root.getEnvironment().setEventListener(listener);
		assertFalse(listener.isListeningFor(VariableAddEvent.class, root));
		assertFalse(listener.isListeningFor(VariableRemoveEvent.class, root));
		assertFalse(listener.isListeningFor(BoundaryVariableAddEvent.class, root));
		assertFalse(listener.isListeningFor(BoundaryVariableRemoveEvent.class, root));
		assertFalse(listener.isListeningFor(FactorAddEvent.class, root));
		assertFalse(listener.isListeningFor(FactorRemoveEvent.class, root));
		assertFalse(listener.isListeningFor(SubgraphAddEvent.class, root));
		assertFalse(listener.isListeningFor(SubgraphRemoveEvent.class, root));
		listener.register(handler, FactorGraphEvent.class, true, root);
		root.notifyListenerChanged();
		assertTrue(listener.isListeningFor(VariableAddEvent.class, root));
		assertTrue(listener.isListeningFor(VariableRemoveEvent.class, root));
		assertTrue(listener.isListeningFor(BoundaryVariableAddEvent.class, root));
		assertTrue(listener.isListeningFor(BoundaryVariableRemoveEvent.class, root));
		assertTrue(listener.isListeningFor(FactorAddEvent.class, root));
		assertTrue(listener.isListeningFor(FactorRemoveEvent.class, root));
		assertTrue(listener.isListeningFor(SubgraphAddEvent.class, root));
		assertTrue(listener.isListeningFor(SubgraphRemoveEvent.class, root));
		
		handler.assertNoEvents();
		
		Real a = new Real();
		a.setName("a");
		Real b = new Real();
		b.setName("b");
		root.addVariables(a, b);
		
		VariableAddEvent varAddEvent = handler.assertEvent(VariableAddEvent.class, root, a);
		assertFalse(varAddEvent.wasAbsorbedFromSubgraph());
		handler.assertEvent(VariableAddEvent.class, root, b);
		handler.assertNoEvents();

		Real c = new Real();
		c.setName("c");
		Factor f1 = root.addFactor(new Normal(0.0,  1.0), a, b, c);
		f1.setName("f1");
		varAddEvent = handler.assertEvent(VariableAddEvent.class, root, c);
		assertFalse(varAddEvent.wasAbsorbedFromSubgraph());
		FactorAddEvent factorAddEvent = handler.assertEvent(FactorAddEvent.class, root, f1);
		assertFalse(factorAddEvent.wasAbsorbedFromSubgraph());
		handler.assertNoEvents();
		
		FactorGraph templateGraph = new FactorGraph();
		templateGraph.setName("template");
		Real tx = new Real();
		tx.setName("x");
		Real ty = new Real();
		ty.setName("y");
		Real tz = new Real();
		tz.setName("z");
		templateGraph.addBoundaryVariables(tz);
		Factor tf = templateGraph.addFactor(new Normal(2.0, 2.0), tx ,ty, tz);
		tf.setName("f2");
		
		FactorGraph subgraph = root.addGraph(templateGraph, a);
		SubgraphAddEvent subgraphAddEvent = handler.assertEvent(SubgraphAddEvent.class, root, subgraph);
		assertFalse(subgraphAddEvent.wasAbsorbedFromSubgraph());
		handler.assertNoEvents();
		
		Variable x = subgraph.getVariableByName("x");
		assertNotNull(x);
		Variable y = subgraph.getVariableByName("y");
		assertNotNull(y);
		Variable z = subgraph.getVariableByName("z");
		assertNull(z); // boundary variable from template is not instantiated in generated graph

		Factor f2 = subgraph.getFactorByName("f2");
		assertNotNull(f2);
		
		root.absorbSubgraph(subgraph);
		varAddEvent = handler.assertEvent(VariableAddEvent.class, root, x);
		assertTrue(varAddEvent.wasAbsorbedFromSubgraph());
		handler.assertEvent(VariableAddEvent.class, root, y);
		factorAddEvent = handler.assertEvent(FactorAddEvent.class, root, f2);
		assertTrue(factorAddEvent.wasAbsorbedFromSubgraph());
		handler.assertEvent(SubgraphRemoveEvent.class, root, subgraph);
		handler.assertNoEvents();
		
		root.remove(f1);
		handler.assertEvent(FactorRemoveEvent.class, root, f1);
		handler.assertNoEvents();
		
		root.remove(b);
		handler.assertEvent(VariableRemoveEvent.class, root, b);
		handler.assertNoEvents();
		
		root.addBoundaryVariables(b);
		handler.assertEvent(BoundaryVariableAddEvent.class, root, b);
		handler.assertNoEvents();
		
		// FIXME: no BoundaryVariableRemoveEvent yet - see BUG 84
		
		//
		// Test individual listener flags
		//
		
		listener.unregisterAll();
		root.notifyListenerChanged();
		assertFalse(listener.isListeningFor(VariableAddEvent.class, root));
		assertFalse(listener.isListeningFor(VariableRemoveEvent.class, root));
		assertFalse(listener.isListeningFor(BoundaryVariableAddEvent.class, root));
		assertFalse(listener.isListeningFor(BoundaryVariableRemoveEvent.class, root));
		assertFalse(listener.isListeningFor(FactorAddEvent.class, root));
		assertFalse(listener.isListeningFor(FactorRemoveEvent.class, root));
		assertFalse(listener.isListeningFor(SubgraphAddEvent.class, root));
		assertFalse(listener.isListeningFor(SubgraphRemoveEvent.class, root));

		root.remove(c);
		root.addVariables(c);
		assertSame(c, root.getVariableByName("c"));
		handler.assertNoEvents();
	
		listener.register(handler, VariableRemoveEvent.class, false, root);
		assertTrue(requireNonNull(root.getEventListener()).isListeningFor(VariableRemoveEvent.class, root));
		root.notifyListenerChanged();
		root.remove(c);
		handler.assertEvent(VariableRemoveEvent.class, root, c);
		handler.assertNoEvents();
		
		listener.unregisterAll();
		listener.register(handler, VariableAddEvent.class, false, root);
		root.notifyListenerChanged();
		root.addVariables(c);
		handler.assertEvent(VariableAddEvent.class, root, c);
		handler.assertNoEvents();
		
		Factor f3 = root.addFactor(new Normal(), a, b, c);
		root.remove(f3);
		handler.assertNoEvents();
		
		listener.unregisterAll();
		listener.register(handler, FactorAddEvent.class, false, root);
		root.notifyListenerChanged();
		f3 = root.addFactor(new Normal(), a, b, c);
		handler.assertEvent(FactorAddEvent.class, root, f3);
		handler.assertNoEvents();
		
		listener.unregisterAll();
		listener.register(handler, FactorRemoveEvent.class, false, root);
		root.notifyListenerChanged();
		root.remove(f3);
		handler.assertEvent(FactorRemoveEvent.class, root, f3);
		handler.assertNoEvents();
	}

	private static class FactorGraphEventHandler extends DimpleEventHandler<FactorGraphEvent>
	{
		Queue<FactorGraphEvent> _queue = new ArrayDeque<FactorGraphEvent>();
		
		@Override
		public void handleEvent(FactorGraphEvent event)
		{
			_queue.add(event);
			
			assertSame(event.getModelObject(), event.getSource());
			assertNotNull(event.getSource());
		}
		
		private <Event extends FactorGraphEvent> Event assertEvent(Class<Event> eventType, FactorGraph source, Node node)
		{
			@SuppressWarnings("unchecked")
			Event event = (Event) _queue.remove();
			assertTrue(eventType.isAssignableFrom(event.getClass()));
			assertSame(source, event.getSource());
			
			if (event instanceof FactorGraphFactorEvent)
			{
				FactorGraphFactorEvent factorEvent = (FactorGraphFactorEvent)event;
				assertSame(node, factorEvent.getFactor());

				FactorGraphFactorEvent factorEvent2 = SerializationTester.clone(factorEvent);
				assertNotSame(factorEvent2, factorEvent);
				assertNull(factorEvent2.getFactor());
				assertEquals(factorEvent.getFactorName(), factorEvent2.getFactorName());
			}
			else if (event instanceof FactorGraphVariableEvent)
			{
				FactorGraphVariableEvent variableEvent = (FactorGraphVariableEvent)event;
				assertSame(node, variableEvent.getVariable());
				
				FactorGraphVariableEvent variableEvent2 = SerializationTester.clone(variableEvent);
				assertNotSame(variableEvent2, variableEvent);
				assertNull(variableEvent2.getVariable());
				assertEquals(variableEvent.getVariableName(), variableEvent2.getVariableName());
			}
			else if (event instanceof FactorGraphSubgraphEvent)
			{
				FactorGraphSubgraphEvent subgraphEvent = (FactorGraphSubgraphEvent)event;
				assertSame(node, subgraphEvent.getSubgraph());

				FactorGraphSubgraphEvent subgraphEvent2 = SerializationTester.clone(subgraphEvent);
				assertNotSame(subgraphEvent2, subgraphEvent);
				assertNull(subgraphEvent2.getSubgraph());
				assertEquals(subgraphEvent.getSubgraphName(), subgraphEvent2.getSubgraphName());
			}
			
			return event;
		}
		
		private void assertNoEvents()
		{
			assertEquals(null, _queue.peek());
		}
	}
}
