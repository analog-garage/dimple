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

package com.analog.lyric.dimple.test.events;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.events.DimpleEvent;
import com.analog.lyric.dimple.events.DimpleEventBlocker;
import com.analog.lyric.dimple.events.DimpleEventHandler;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.DimpleEventListener.IHandlerEntry;
import com.analog.lyric.dimple.events.DimpleEventListener.IHandlersForSource;
import com.analog.lyric.dimple.events.IDimpleEventHandler;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.events.ISolverEventSource;
import com.analog.lyric.dimple.events.ModelEvent;
import com.analog.lyric.dimple.events.SolverEvent;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;
import com.analog.lyric.dimple.solvers.gibbs.SFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestDimpleEventListener
{

	static class TestModelEvent extends ModelEvent
	{
		private static final long serialVersionUID = 1L;

		TestModelEvent(IModelEventSource source)
		{
			super(source);
		}
	}
	
	static class TestVariableEvent extends TestModelEvent
	{
		private static final long serialVersionUID = 1L;

		TestVariableEvent(VariableBase source)
		{
			super(source);
		}
	}
	
	static class TestSolverEvent extends SolverEvent
	{
		private static final long serialVersionUID = 1L;

		TestSolverEvent(ISolverEventSource source)
		{
			super(source);
		}

		@Override
		public IModelEventSource getModelObject()
		{
			return getSource().getModelEventSource();
		}
	}
	
	static class TestSolverFactorEvent extends TestSolverEvent
	{
		private static final long serialVersionUID = 1L;

		TestSolverFactorEvent(ISolverFactor source)
		{
			super(source);
		}
	}
	
	static class TestEventHandler<Event extends DimpleEvent> extends DimpleEventHandler<Event>
	{
		final boolean _consume;
		
		final static List<TestEventHandler<?>> handledBy = new ArrayList<TestEventHandler<?>>();
		
		static <T extends DimpleEvent> TestEventHandler<T> create(boolean consume)
		{
			return new TestEventHandler<T>(consume);
		}
		
		TestEventHandler(boolean consume)
		{
			_consume = consume;
		}
		
		@Override
		public void handleEvent(Event event)
		{
			handledBy.add(this);
			if (_consume)
			{
				event.consumed(true);
			}
		}
	}
	
	@Test
	public void testListener()
	{
		// Set up model/solver for test
		FactorGraph model = new FactorGraph();
		model.setName("model");
		
		VariableBase v1 = new Real();
		model.addVariables(v1);
		v1.setName("v1");
		
		FactorGraph template = new FactorGraph();
		VariableBase b1 = new Real();
		template.addBoundaryVariables(b1);
		template.addFactor(new Normal(0.0, 1.0), b1).setName("normal");
		
		FactorGraph subgraph = model.addFactor(template, v1);
		Factor f1 = subgraph.getFactorByName("normal");
		assertNotNull(f1);
		
		SFactorGraph sgraph = model.setSolverFactory(new GibbsSolver());
		ISolverVariableGibbs sv1 = sgraph.getSolverVariable(v1);
		assertNotNull(sv1);
		
		ISolverFactor sf1 = sgraph.getSolverFactor(f1);
		assertSame(f1, sf1.getModelEventSource());
		assertSame(f1, sf1.getModelObject());
		
		ISolverFactorGraph ssubgraph = sf1.getParentGraph();
		assertNotSame(ssubgraph, sgraph);
		assertSame(sgraph, ssubgraph.getParentGraph());
		
		// Some events
		TestModelEvent modelEvent = new TestModelEvent(model);
		assertSame(model, modelEvent.getSource());
		assertSame(model, modelEvent.getModelObject());
		assertFalse(modelEvent.consumed());
		
		// Test empty listener
		DimpleEventListener listener = new DimpleEventListener();
		assertInvariants(listener);
		assertTrue(Iterables.isEmpty(listener.allHandlers()));
		assertFalse(listener.isListeningFor(TestModelEvent.class, model));
		assertFalse(listener.isListeningFor(ModelEvent.class, model));
		assertFalse(DimpleEventListener.sourceHasListenerFor(model, ModelEvent.class));
		assertTrue(listener.getHandlersFor(TestModelEvent.class, model).isEmpty());
		
		assertHandledBy(listener, modelEvent);
		assertFalse(modelEvent.consumed());
	
		// Test eventSources() order
		ReleasableIterator<IDimpleEventSource> sources1 = listener.eventSources(sv1);
		ReleasableIterator<IDimpleEventSource> sources2 = listener.eventSources(sv1);
		assertNotSame(sources1, sources2);
		assertEventSources(sources1, sv1, v1, sgraph, model);
		assertEventSources(sources2, sv1, v1, sgraph, model);
		expectThrow(UnsupportedOperationException.class, null, sources1, Iterator.class, "remove");
		sources1.release();
		sources2.release();
		sources1 = listener.eventSources(v1);
		assertSame(sources1, sources2);
		assertEventSources(sources1, v1, model);
		sources1.release();
		sources1 = listener.eventSources(sf1);
		assertEventSources(sources1, sf1, f1, ssubgraph, subgraph, sgraph, model);
		sources1.release();
		sources1 = listener.eventSources(f1);
		assertEventSources(sources1, f1, subgraph, model);
		sources1.release();
			
		// Single handler
		TestEventHandler<TestModelEvent> handler1 = TestEventHandler.create(false);
		listener.register(handler1, TestModelEvent.class, false, model);
		listener.register(handler1, TestModelEvent.class, false, model); // no effect
		assertInvariants(listener);
		assertTrue(listener.isListeningFor(TestModelEvent.class, model));
		
		List<IHandlerEntry> entries = listener.getHandlersFor(TestModelEvent.class, model);
		assertEquals(1, entries.size());

		IHandlerEntry entry = entries.get(0);
		assertSame(model, entry.eventSource());
		assertNotEquals(entry, "foo");
		assertEquals(TestModelEvent.class, entry.eventClass());
		assertFalse(entry.handleSubclasses());
		assertSame(handler1, entry.eventHandler());
		
		assertHandledBy(listener, modelEvent, handler1);
		
		expectThrow(UnsupportedOperationException.class, null,
			listener.allHandlers().iterator(), Iterator.class, "remove");
		
		// sourceHasListenerFor
		assertFalse(DimpleEventListener.sourceHasListenerFor(model, TestModelEvent.class));
		model.setEventListener(listener);
		assertTrue(DimpleEventListener.sourceHasListenerFor(model, TestModelEvent.class));
		assertFalse(DimpleEventListener.sourceHasListenerFor(model, TestSolverEvent.class));
		
		// Add a catch-all handler
		TestEventHandler<DimpleEvent> handleAll = TestEventHandler.create(false);
		listener.register(handleAll, DimpleEvent.class, true, model);
		assertInvariants(listener);
		
		assertHandledBy(listener, new TestModelEvent(model), handler1, handleAll);
		assertHandledBy(listener, new TestModelEvent(f1), handler1, handleAll);
		assertHandledBy(listener, new TestVariableEvent(v1), handleAll);
		assertHandledBy(listener, new TestSolverEvent(sf1), handleAll);
		
		// Block events from percolating past the solver subgraph
		listener.block(DimpleEvent.class, true, ssubgraph);
		assertInvariants(listener);
		assertHandledBy(listener, new TestSolverEvent(sf1));
		
		TestEventHandler<TestSolverFactorEvent> handleSolverFactorEvent = TestEventHandler.create(false);
		listener.register(handleSolverFactorEvent, TestSolverFactorEvent.class, false, sf1);
		assertInvariants(listener);
		assertHandledBy(listener, new TestSolverFactorEvent(sf1), handleSolverFactorEvent);
		assertHandledBy(listener, new TestSolverEvent(sf1));
		
		assertEquals(3, Iterables.size(listener.allHandlers()));

		assertFalse(listener.unblock(DimpleEvent.class, sf1));
		assertFalse(listener.unblock(TestModelEvent.class, ssubgraph));
		assertTrue(listener.unblock(DimpleEvent.class, ssubgraph));
		assertInvariants(listener);
		
		assertHandledBy(listener, new TestSolverFactorEvent(sf1), handleSolverFactorEvent, handleAll);
		assertHandledBy(listener, new TestSolverEvent(sf1), handleAll);
		
		assertFalse(listener.unregisterSource(v1));
		assertTrue(listener.unregisterSource(model));
		assertHandledBy(listener, new TestSolverFactorEvent(sf1), handleSolverFactorEvent);
		
		listener.register(handleAll, DimpleEvent.class, true, model);
		listener.register(handleAll, SolverEvent.class, true, ssubgraph);
		assertHandledBy(listener, new TestSolverFactorEvent(sf1), handleSolverFactorEvent, handleAll, handleAll);
		
		listener.unregisterAll();
		assertInvariants(listener);
		assertTrue(Iterables.isEmpty(listener.allHandlers()));
		assertFalse(listener.unregister(handleAll, DimpleEvent.class, model));
		
		listener.register(handleAll, TestSolverEvent.class, true, model);
		listener.register(handleAll, TestSolverFactorEvent.class, true, model);
		listener.register(handleAll, TestVariableEvent.class, true, model);
		listener.register(handleAll, TestModelEvent.class,  true, model);
		// These will override the previous entries since they differ only in their subclass setting:
		listener.register(handleAll, TestVariableEvent.class, false, model);
		listener.register(handleAll, TestModelEvent.class,  false, model);
		listener.register(handleAll, TestSolverEvent.class, false, model);
		listener.register(handleAll, TestSolverFactorEvent.class, false, model);
		assertInvariants(listener);
		
		for (IHandlersForSource allHandlers : listener.allHandlers())
		{
			for (IHandlerEntry e : allHandlers.handlerEntries())
			{
				assertFalse(e.handleSubclasses());
			}
		}
		
		assertHandledBy(listener, new TestSolverEvent(sf1), handleAll);
		assertHandledBy(listener, new TestSolverFactorEvent(sf1), handleAll);
		assertHandledBy(listener, new TestModelEvent(v1), handleAll);
		assertHandledBy(listener, new TestVariableEvent(v1), handleAll);
		
		assertTrue(listener.unregister(handleAll, TestSolverEvent.class, model));
		assertHandledBy(listener, new TestSolverEvent(sf1));
		assertHandledBy(listener, new TestSolverFactorEvent(sf1), handleAll);
		
		listener.register(handleSolverFactorEvent, TestSolverFactorEvent.class, false, model);
		assertHandledBy(listener, new TestSolverFactorEvent(sf1), handleAll, handleSolverFactorEvent);
		
		listener.unregister(handleSolverFactorEvent, TestSolverFactorEvent.class, model);
		assertHandledBy(listener, new TestSolverFactorEvent(sf1), handleAll);
		
		//
		// Test defaultListener
		//
		
		DimpleEventListener defaultListener = DimpleEventListener.getDefault();
		assertTrue(defaultListener.isEmpty());
		assertInvariants(defaultListener);
		assertSame(defaultListener, DimpleEventListener.getDefault());
		assertSame(defaultListener, DimpleEventListener.setDefault(listener));
		assertSame(listener, DimpleEventListener.setDefault(null));
		assertNull(DimpleEventListener.setDefault(null));
		DimpleEventListener defaultListener2 = DimpleEventListener.getDefault();
		assertInvariants(defaultListener2);
		assertNotSame(defaultListener, defaultListener2);
	}

	private void assertEventSources(Iterator<IDimpleEventSource> iter, IDimpleEventSource ... expected)
	{
		List<IDimpleEventSource> actual = new ArrayList<IDimpleEventSource>();
		Iterators.addAll(actual, iter);
		assertFalse(iter.hasNext());
		assertNull(iter.next());
		assertArrayEquals(actual.toArray(), expected);
	}
	
	private void assertHandledBy(DimpleEventListener listener, DimpleEvent event, TestEventHandler<?> ... handlers)
	{
		event.consumed(false);
		TestEventHandler.handledBy.clear();
		
		listener.raiseEvent(event);
		
		assertArrayEquals(handlers, TestEventHandler.handledBy.toArray());
		
		final int nHandlers = handlers.length;
		
		final boolean expectedListening = !(nHandlers == 0 || nHandlers == 1 && handlers[nHandlers - 1].isBlocker());
		assertEquals(expectedListening, listener.isListeningFor(event.getClass(), event.getSource()));
	}
	
	private void assertInvariants(DimpleEventListener listener)
	{
		boolean hasHandlers = false;
		for (IHandlersForSource handlers : listener.allHandlers())
		{
			hasHandlers = true;
			IDimpleEventSource source = handlers.eventSource();
			assertNotNull(source);
			
			List<IHandlerEntry> entries = handlers.handlerEntries();
			assertFalse(entries.isEmpty());
			
			for (int i = entries.size(); --i >= 0; )
			{
				final IHandlerEntry entry  = entries.get(i);
				final Class<? extends DimpleEvent> eventClass = entry.eventClass();
				final IDimpleEventHandler<?> handler = entry.eventHandler();
				
				assertSame(source, entry.eventSource());
				assertTrue(DimpleEvent.class.isAssignableFrom(eventClass));
				assertEquals(handler.isBlocker(), handler == DimpleEventBlocker.INSTANCE);
				
				// subclasses must come before superclasses in the list.
				for (int j = i; --j >= 0; )
				{
					final IHandlerEntry prevEntry = entries.get(j);
					assertNotEquals(prevEntry, entry);
					assertNotEquals(prevEntry.hashCode(), entry.hashCode());
					final Class<? extends DimpleEvent> prevEventClass = prevEntry.eventClass();
					assertFalse(prevEventClass.isAssignableFrom(eventClass));
				}
				
				List<IHandlerEntry> entries2 = listener.getHandlersFor(eventClass, source);
				assertFalse(entries2.isEmpty());
			
				// Either the entry is found in the list of handlers for the class and source or the
				// handler list ends in a blocker.
				assertTrue(entries2.indexOf(entry) >= 0 || entries2.get(entries2.size() -1).eventHandler().isBlocker());
			}
		}
		
		assertEquals(!hasHandlers, listener.isEmpty());
	}
}
