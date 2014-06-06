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

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.dimple.events.DimpleEvent;
import com.analog.lyric.dimple.events.DimpleEventHandler;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.events.ISolverEventSource;
import com.analog.lyric.dimple.events.SolverEvent;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.SNode;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.ParameterizedMessageBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.sumproduct.SFactorGraph;
import com.analog.lyric.util.misc.NonNullByDefault;
import com.analog.lyric.util.misc.Nullable;

/**
 * Unit test for {@link SNode} base class.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestSNode
{
	private static class TestNode extends SNode
	{
		private @Nullable ISolverFactorGraph _parent = null;
		private boolean _supportsMessageEvents = false;
		final private Set<Integer> _updatedEdges = new HashSet<Integer>();
		final private Map<Integer,TestMessage> _messages = new HashMap<Integer, TestMessage>();
		final private List<DimpleEvent> _createdEvents = new ArrayList<DimpleEvent>();
		
		public TestNode(Node n)
		{
			super(n);
		}

		@Override
		public void resetEdgeMessages(int edge)
		{
			_updatedEdges.remove(edge);
			_messages.clear();
		}

		@Override
		public @Nullable ISolverFactorGraph getParentGraph()
		{
			return _parent;
		}

		@Override
		public @Nullable ISolverFactorGraph getRootGraph()
		{
			return _parent;
		}

		@Override
		public double getScore()
		{
			return 0;
		}

		@Override
		public double getInternalEnergy()
		{
			return 0;
		}

		@Override
		public double getBetheEntropy()
		{
			return 0;
		}

		@Override
		public @Nullable Object getInputMsg(int portIndex)
		{
			return null;
		}

		@Override
		public IParameterizedMessage getOutputMsg(int edge)
		{
			return _messages.get(edge);
		}

		@Override
		public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
		{
		}

		/*---------------
		 * SNode methods
		 */
		
		@Override
		protected void doUpdateEdge(int edge)
		{
			_updatedEdges.add(edge);
			TestMessage msg = _messages.get(edge);
			if (msg == null)
			{
				msg = new TestMessage(0);
				_messages.put(edge, msg);
			}
			else
			{
				++msg._counter;
			}
		}
		
		@Override
		protected @Nullable IParameterizedMessage cloneMessage(int edge)
		{
			if (_supportsMessageEvents)
			{
				IParameterizedMessage msg = _messages.get(edge);
				return msg != null ? msg.clone() : msg;
			}
			else
			{
				return super.cloneMessage(edge);
			}
		}
		
		@Override
		public @Nullable SolverEvent createMessageEvent(int edge,
			@Nullable IParameterizedMessage oldMsg, @Nullable IParameterizedMessage newMsg)
		{
			SolverEvent event = null;
			
			if (_supportsMessageEvents)
			{
				event = new TestEvent(this, oldMsg, newMsg);
			}
			else
			{
				event = super.createMessageEvent(edge, oldMsg, newMsg);
			}
			
			if (event != null)
			{
				_createdEvents.add(event);
			}
			
			return event;
		}
		
		@Override
		protected Class<? extends SolverEvent> messageEventType()
		{
			return _supportsMessageEvents ? TestEvent.class : super.messageEventType();
		}

		@Override
		protected boolean supportsMessageEvents()
		{
			return _supportsMessageEvents || super.supportsMessageEvents();
		}
	}
	
	@NonNullByDefault
	private static class TestMessage extends ParameterizedMessageBase
	{
		private static final long serialVersionUID = 1L;

		private int _counter;
		
		private TestMessage(int counter)
		{
			_counter = counter;
		}
		
		@Override
		public TestMessage clone()
		{
			return new TestMessage(_counter);
		}
		
		@Override
		public void print(PrintStream out, int verbosity)
		{
			out.format("TestMessage(counter=%d)", _counter);
		}

		@Override
		public double computeKLDivergence(IParameterizedMessage that)
		{
			return Math.abs(_counter - ((TestMessage)that)._counter);
		}

		@Override
		public void setNull()
		{
			_counter = 0;
		}
	}
	
	@NonNullByDefault
	private static class TestEvent extends SolverEvent
	{
		private static final long serialVersionUID = 1L;
		private final @Nullable IParameterizedMessage _oldMsg;
		private final @Nullable IParameterizedMessage _newMsg;

		protected TestEvent(ISolverEventSource source, @Nullable IParameterizedMessage oldMsg,
			@Nullable IParameterizedMessage newMsg)
		{
			super(source);
			_oldMsg = oldMsg;
			_newMsg = newMsg;
		}

		@Override
		public @Nullable IModelEventSource getModelObject()
		{
			return getSource().getModelEventSource();
		}

		@Override
		protected void printDetails(PrintStream out, int verbosity)
		{
		}
	}
	
	@NonNullByDefault
	private static class TestHandler extends DimpleEventHandler<TestEvent>
	{
		@Override
		public void handleEvent(TestEvent event)
		{
			TestMessage oldMsg = (TestMessage)event._oldMsg;
			TestMessage newMsg = (TestMessage)event._newMsg;
			if (oldMsg != null)
			{
				assertEquals(oldMsg._counter + 1, newMsg._counter);
			}
			else
			{
				assertNotNull(newMsg);
			}
		}
	}
	
	@Test
	public void test()
	{
		FactorGraph fg = new FactorGraph();
		Discrete d1 = new Discrete(DiscreteDomain.bit());
		Discrete d2 = new Discrete(DiscreteDomain.bit());
		Discrete d3 = new Discrete(DiscreteDomain.bit());
		fg.addVariables(d1, d2, d3);
		
		TestNode n1 = new TestNode(d1);
		assertSame(d1, n1.getModelObject());
		assertEquals(0, n1.getSiblingCount());
		assertEquals(0, n1.getFlagValue(-1));
		assertNull(n1.getParentGraph());
		assertNull(n1.getOptionParent());
		assertFalse(n1.supportsMessageEvents());
		expectThrow(IndexOutOfBoundsException.class, n1, "getSibling", 0);
		expectThrow(DimpleException.class, "Not supported.*", n1, "setInputMsg", 42, null);
		expectThrow(DimpleException.class, "Not supported.*", n1, "setOutputMsg", 42, null);
		expectThrow(DimpleException.class, "Not supported.*", n1, "setInputMsgValues", 42, null);
		expectThrow(DimpleException.class, "Not supported.*", n1, "setOutputMsgValues", 42, null);
		
		ISolverFactorGraph sfg = new SFactorGraph(fg);
		n1._parent = sfg;
		assertSame(sfg, n1.getParentGraph());
		assertSame(sfg, n1.getOptionParent());
		
		Factor f13 = fg.addFactor(new Normal(0.0, 1.0), d1, d3);
		Factor f12 = fg.addFactor(new Normal(0.0, 1.0), d1, d2);
		
		assertEquals(2, n1.getSiblingCount());
		assertSame(f13.getSolver(), n1.getSibling(0));
		assertSame(f12.getSolver(), n1.getSibling(1));
		
		assertTrue(n1._updatedEdges.isEmpty());
		n1.update();
		assertEquals(2, n1._updatedEdges.size());
		assertTrue(n1._updatedEdges.contains(0));
		assertTrue(n1._updatedEdges.contains(1));

		n1.setFlagValue(-1, -1);
		assertEquals(-1, n1.getFlagValue(-1));
		n1.initialize();
		
		assertEquals(0, n1.getFlagValue(-1));
		assertTrue(n1._updatedEdges.isEmpty());
		
		for (int i = 0; i <2; ++i)
		{
			n1.updateEdge(i);
			assertEquals(1, n1._updatedEdges.size());
			assertTrue(n1._updatedEdges.contains(i));
			n1._updatedEdges.clear();
		}
		assertTrue(n1._createdEvents.isEmpty());
		
		//
		// Test message events
		//
		
		DimpleEventListener listener = new DimpleEventListener();
		fg.setEventListener(listener);
		assertSame(listener, fg.getEventListener());
		assertSame(listener, n1.getEventListener());
		assertFalse(listener.isListeningFor(TestEvent.class, n1));
		// merely listening will not trigger event creation
		n1.initialize();
		n1.update();
		assertTrue(n1._createdEvents.isEmpty());
		
		TestHandler handler = new TestHandler();
		listener.register(handler, TestEvent.class, false, fg);
		assertTrue(listener.isListeningFor(TestEvent.class, n1));
		// still no events, because not enabled
		n1.initialize();
		n1.update();
		assertTrue(n1._createdEvents.isEmpty());

		// still no events because initialize not called
		n1._supportsMessageEvents = true;
		n1.update();
		assertTrue(n1._createdEvents.isEmpty());

		n1.initialize();
		n1.update();
		assertEquals(2, n1._createdEvents.size());
		assertSame(n1, n1._createdEvents.get(0).getSource());
		assertSame(n1, n1._createdEvents.get(1).getSource());
		n1._createdEvents.clear();
		
		n1.updateEdge(0);
		assertEquals(1, n1._createdEvents.size());
		assertSame(n1, n1._createdEvents.get(0).getSource());
		n1._createdEvents.clear();
		
		fg.setEventListener(null);
		n1.notifyListenerChanged();
		n1.updateEdge(0);
		n1.updateEdge(1);
		assertTrue(n1._createdEvents.isEmpty());
	}
}
