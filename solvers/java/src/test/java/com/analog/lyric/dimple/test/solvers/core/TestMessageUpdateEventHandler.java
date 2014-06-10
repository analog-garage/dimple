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

package com.analog.lyric.dimple.test.solvers.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.analog.lyric.dimple.events.DimpleEventHandler;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.SolverEvent;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.solvers.core.FactorToVariableMessageEvent;
import com.analog.lyric.dimple.solvers.core.IMessageUpdateEvent;
import com.analog.lyric.dimple.solvers.core.VariableToFactorMessageEvent;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.Nullable;

/**
 * {@link DimpleEventHandler} for testing {@link IMessageUpdateEvent}.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestMessageUpdateEventHandler extends DimpleEventHandler<SolverEvent>
{
	public boolean printEvents = false;
	
	public final List<IMessageUpdateEvent> observedEvents = new ArrayList<IMessageUpdateEvent>();
	
	public static TestMessageUpdateEventHandler setUpListener(ISolverFactorGraph solver)
	{
		FactorGraph model = solver.getModelObject();
		DimpleEventListener listener = new DimpleEventListener();
		model.setEventListener(listener);
		TestMessageUpdateEventHandler handler = new TestMessageUpdateEventHandler();
		listener.register(handler, VariableToFactorMessageEvent.class, false, model);
		listener.register(handler, FactorToVariableMessageEvent.class, false, model);
		
		for (VariableBase var : model.getVariables())
		{
			ISolverVariable svar = solver.getSolverVariable(var);
			assertTrue(listener.isListeningFor(VariableToFactorMessageEvent.class, svar));
			assertSame(listener, svar.getEventListener());
		}
		
		for (Factor factor : model.getFactors())
		{
			ISolverFactor sfactor = solver.getSolverFactor(factor);
			assertTrue(listener.isListeningFor(FactorToVariableMessageEvent.class, sfactor));
			assertSame(listener, sfactor.getEventListener());
		}
		solver.initialize();
		
		return handler;
	}
	
	@Override
	public void handleEvent(SolverEvent event)
	{
		assertEquals(event.getModelObject(), event.getSolverObject().getModelEventSource());
		
		assertTrue(event instanceof IMessageUpdateEvent);
		IMessageUpdateEvent messageEvent = (IMessageUpdateEvent)event;
		IParameterizedMessage oldMsg = messageEvent.getOldMessage();
		IParameterizedMessage newMsg = messageEvent.getNewMessage();
		observedEvents.add(messageEvent);
		
		assertNotNull(newMsg);
		assertNotSame(oldMsg, newMsg);
		
		if (oldMsg == null)
		{
			assertEquals(Double.POSITIVE_INFINITY, messageEvent.computeKLDivergence(), 0.0);
		}

		ISolverFactor factor = messageEvent.getFactor();
		assertNotNull(factor);
		
		ISolverVariable variable = messageEvent.getVariable();
		assertNotNull(variable);
		
		if (messageEvent.isToFactor())
		{
			assertTrue(event instanceof VariableToFactorMessageEvent);
			assertSame(variable, event.getSolverObject());
			assertSame(factor, variable.getSibling(messageEvent.getEdge()));
		}
		else
		{
			assertTrue(event instanceof FactorToVariableMessageEvent);
			assertSame(factor, event.getSolverObject());
			assertSame(variable, factor.getSibling(messageEvent.getEdge()));
		}
		
		if (printEvents)
		{
			printEvent(messageEvent);
		}
	}

	/**
	 * Asserts that the contents of {@link #observedEvents} corresponds to the given
	 * {@code schedule}.
	 * <p>
	 * This only looks at entries of type {@link EdgeScheduleEntry} and {@link NodeScheduleEntry}.
	 * <p>
	 * @param schedule
	 * @param solver is the root solver graph for use in mapping model to solver nodes.
	 * @since 0.06
	 */
	public void assertEventsFromSchedule(ISchedule schedule, ISolverFactorGraph solver)
	{
		Iterator<IMessageUpdateEvent> eventIter = observedEvents.iterator();
		Iterator<IScheduleEntry> scheduleIter = schedule.iterator();
		
		while (scheduleIter.hasNext())
		{
			IScheduleEntry scheduleEntry = scheduleIter.next();
			if (scheduleEntry instanceof EdgeScheduleEntry)
			{
				EdgeScheduleEntry edgeEntry = (EdgeScheduleEntry)scheduleEntry;
				INode edgeNode = edgeEntry.getNode();
				int edge = edgeEntry.getPortNum();
				
				if (edgeNode instanceof VariableBase)
				{
					VariableBase edgeVar = (VariableBase)edgeNode;
					ISolverVariable svar = solver.getSolverVariable(edgeVar);
					assertEdgeMessageEvent(svar, edge, eventIter);
				}
				else if (edgeNode instanceof Factor)
				{
					Factor edgeFactor = (Factor)edgeNode;
					ISolverFactor sfactor = solver.getSolverFactor(edgeFactor);
					assertEdgeMessageEvent(sfactor, edge, eventIter);
				}
			}
			else if (scheduleEntry instanceof NodeScheduleEntry)
			{
				NodeScheduleEntry nodeEntry = (NodeScheduleEntry)scheduleEntry;
				INode node = nodeEntry.getNode();
				
				if (node instanceof VariableBase)
				{
					VariableBase edgeVar = (VariableBase)node;
					ISolverVariable svar = solver.getSolverVariable(edgeVar);
					for (int edge = 0, n = edgeVar.getSiblingCount(); edge < n; ++edge)
					{
						assertEdgeMessageEvent(svar, edge, eventIter);
					}
				}
				else if (node instanceof Factor)
				{
					Factor edgeFactor = (Factor)node;
					ISolverFactor sfactor = solver.getSolverFactor(edgeFactor);
					for (int edge = 0, n = edgeFactor.getSiblingCount(); edge < n; ++edge)
					{
						assertEdgeMessageEvent(sfactor, edge, eventIter);
					}
				}
			}
		}
		
		assertFalse(eventIter.hasNext());
	}
	
	public void testNodeSchedule(ISolverFactorGraph solver)
	{
		FactorGraph model = solver.getModelObject();

		// Create a fixed schedule to exerise full update messages
		FixedSchedule schedule = new FixedSchedule();
		for (INode node : model.getNodes())
		{
			schedule.add(node);
		}
		model.setSchedule(schedule);
		
		solver.iterate();
		assertEventsFromSchedule(model.getSchedule(), solver);
		observedEvents.clear();
	}
	
	public void testEdgeSchedule(ISolverFactorGraph solver)
	{
		FactorGraph model = solver.getModelObject();
		
		// Create a fixed edge schedule to exercise edge messages
		FixedSchedule schedule = new FixedSchedule();
		for (INode node : model.getNodes())
		{
			for (int i = node.getSiblingCount(); --i>=0;)
			{
				schedule.add(node, i);
			}
		}
		
		model.setSchedule(schedule);
		solver.iterate();
		
		assertEventsFromSchedule(model.getSchedule(), solver);
		observedEvents.clear();
	}
	
	
	private void assertEdgeMessageEvent(@Nullable ISolverVariable svar, int edge,
		Iterator<IMessageUpdateEvent> eventIter)
	{
		if (DimpleEventListener.sourceHasListenerFor(Objects.requireNonNull(svar), VariableToFactorMessageEvent.class))
		{
			assertTrue(eventIter.hasNext());
			IMessageUpdateEvent event = eventIter.next();
			assertTrue(event.isToFactor());
			assertTrue(event instanceof VariableToFactorMessageEvent);
			assertSame(svar, event.getVariable());
			assertSame(edge, event.getEdge());
		}
	}
	
	private void assertEdgeMessageEvent(@Nullable ISolverFactor sfactor, int edge,
		Iterator<IMessageUpdateEvent> eventIter)
	{
		if (DimpleEventListener.sourceHasListenerFor(Objects.requireNonNull(sfactor), FactorToVariableMessageEvent.class))
		{
			assertTrue(eventIter.hasNext());
			IMessageUpdateEvent event = eventIter.next();
			assertFalse(event.isToFactor());
			assertTrue(event instanceof FactorToVariableMessageEvent);
			assertSame(sfactor, event.getFactor());
			assertSame(edge, event.getEdge());
		}
	}
	
	void printEvent(IMessageUpdateEvent event)
	{
		ISolverNode source, target;
		
		if (event.isToFactor())
		{
			source = event.getVariable();
			target = event.getFactor();
		}
		else
		{
			source = event.getFactor();
			target = event.getFactor();
		}
		
		System.out.format("%s: %s to %s: KL = %g",
			event.getClass().getSimpleName(),
			source.getModelObject().getName(),
			target.getModelObject().getName(),
			event.computeKLDivergence()
			);
		System.out.println("");
	}
}