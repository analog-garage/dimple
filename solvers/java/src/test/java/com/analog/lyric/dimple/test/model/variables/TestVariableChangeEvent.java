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

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.Queue;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.events.DimpleEventHandler;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableChangeEvent;
import com.analog.lyric.dimple.model.variables.VariableFixedValueChangeEvent;
import com.analog.lyric.dimple.model.variables.VariableInputChangeEvent;
import com.analog.lyric.dimple.model.variables.VariablePriorChangeEvent;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Test for {@link VariableChangeEvent}s on {@link Variable}.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@SuppressWarnings("deprecation")
public class TestVariableChangeEvent extends DimpleTestBase
{
	@Test
	public void test()
	{
		//
		// Set up model
		//
		
		FactorGraph fg = new FactorGraph();
		
		Real r = new Real();
		r.setName("r");
		fg.addVariables(r);
		
		Discrete d = new Discrete(DiscreteDomain.bit());
		d.setName("d");
		fg.addVariables(d);
		
		//
		// Set up listener
		//
		
		PriorChangeHandler priorHandler = new PriorChangeHandler();
		FixedValueChangeHandler fixedHandler = new FixedValueChangeHandler();
		InputChangeHandler inputHandler = new InputChangeHandler();
		
		DimpleEventListener listener = DimpleEnvironment.active().createEventListener();
		listener.register(priorHandler, VariablePriorChangeEvent.class, false, fg);
		listener.register(fixedHandler, VariableFixedValueChangeEvent.class, false, fg);
		listener.register(inputHandler, VariableInputChangeEvent.class, false, fg);
		fg.initialize();
		
		assertTrue(listener.isListeningFor(VariablePriorChangeEvent.class, r));
		assertTrue(listener.isListeningFor(VariableFixedValueChangeEvent.class, r));
		
		final Value r2_3 = Value.createReal(2.3);
		final Value r4 = Value.createReal(4.0);
		r.setPrior(r2_3);
		priorHandler.assertEvent(r, VariablePriorChangeEvent.Type.ADDED, null, r2_3);
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.ADDED, null, 2.3);
		r.setPrior(r4);
		priorHandler.assertEvent(r, VariablePriorChangeEvent.Type.CHANGED, r2_3, r4);
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.CHANGED, 2.3, 4.0);
		r.setPrior(null);
		priorHandler.assertEvent(r, VariablePriorChangeEvent.Type.REMOVED, r4, null);
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.REMOVED, 4.0, null);
		priorHandler.assertNoEvent();
		fixedHandler.assertNoEvent();
		inputHandler.assertNoEvent();
		
		DiscreteMessage d37 = new DiscreteWeightMessage(new double[] { .3, .7 });
		DiscreteMessage d46 = new DiscreteWeightMessage(new double[] { .4, .6 });
		d.setPrior(d37);
		priorHandler.assertEvent(d, VariablePriorChangeEvent.Type.ADDED, null, d37);
		fixedHandler.assertNoEvent();
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.ADDED, null, d37.representation());
		d.setPrior(d46);
		priorHandler.assertEvent(d, VariablePriorChangeEvent.Type.CHANGED, d37, d46);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.CHANGED, d37.representation(), d46.representation());
		d.setPrior(null);
		priorHandler.assertEvent(d, VariablePriorChangeEvent.Type.REMOVED, d46, null);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.REMOVED, d46.representation(), null);
		
		d.setPrior(d37);
		priorHandler.assertEvent(d, VariablePriorChangeEvent.Type.ADDED, null, d37);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.ADDED, null, d37.representation());
		
		final Value d0 = Value.createWithIndex(d.getDomain(), 0);
		d.setPrior(d0);
		priorHandler.assertEvent(d, VariablePriorChangeEvent.Type.CHANGED, d37, d0);
		fixedHandler.assertEvent(d, VariableFixedValueChangeEvent.Type.ADDED, null, 0);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.REMOVED, d37.representation(), null);
		d.setPrior(d37);
		priorHandler.assertEvent(d, VariablePriorChangeEvent.Type.CHANGED, d0, d37);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.ADDED, null, d37.representation());
		fixedHandler.assertEvent(d, VariableFixedValueChangeEvent.Type.REMOVED, 0, null);
		
		Normal normal = new Normal(1.0, 1.0);
		r.setPrior(normal);
		priorHandler.assertEvent(r, VariablePriorChangeEvent.Type.ADDED, null, normal);
		inputHandler.assertEvent(r, VariableInputChangeEvent.Type.ADDED, null, normal);
		fixedHandler.assertNoEvent();
		final Value r3 = Value.createReal(3.0);
		r.setPrior(r3);
		priorHandler.assertEvent(r, VariablePriorChangeEvent.Type.CHANGED, normal, r3);
		inputHandler.assertEvent(r, VariableInputChangeEvent.Type.REMOVED, normal, null);
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.ADDED, null, 3.0);
		r.setInput(normal);
		priorHandler.assertEvent(r, VariablePriorChangeEvent.Type.CHANGED, r3, normal);
		inputHandler.assertEvent(r, VariableInputChangeEvent.Type.ADDED, null, normal);
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.REMOVED, 3.0, null);
		
		priorHandler.assertNoEvent();
		inputHandler.assertNoEvent();
		fixedHandler.assertNoEvent();
		
		listener.block(VariablePriorChangeEvent.class, false, r);
		listener.block(VariableFixedValueChangeEvent.class, false, r);
		r.notifyListenerChanged();
		assertFalse(listener.isListeningFor(VariablePriorChangeEvent.class, r));
		assertFalse(listener.isListeningFor(VariableFixedValueChangeEvent.class, r));

		final Value r1 = Value.createReal(1.0);
		r.setPrior(r1);
		priorHandler.assertNoEvent();
		fixedHandler.assertNoEvent();
		inputHandler.assertEvent(r, VariableInputChangeEvent.Type.REMOVED, normal, null);

		listener.block(VariableInputChangeEvent.class,  false, r);
		r.notifyListenerChanged();
		d.notifyListenerChanged();

		assertTrue(listener.isListeningFor(VariablePriorChangeEvent.class, d));
		assertTrue(listener.isListeningFor(VariableFixedValueChangeEvent.class, d));
		assertTrue(listener.isListeningFor(VariableInputChangeEvent.class, d));
		
		final Value d1 = Value.create(d.getDomain(), 1.0);
		d.setPrior(d1);
		priorHandler.assertEvent(d, VariablePriorChangeEvent.Type.CHANGED, d37, d1);
		fixedHandler.assertEvent(d, VariableFixedValueChangeEvent.Type.ADDED, null, 1);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.REMOVED, d37.representation(), null);
			
		listener.unblock(VariablePriorChangeEvent.class, r);
		listener.unblock(VariableFixedValueChangeEvent.class, r);
		r.notifyListenerChanged();
		
		r.setPrior(normal);
		priorHandler.assertEvent(r, VariablePriorChangeEvent.Type.CHANGED, r1, normal);
		inputHandler.assertNoEvent();
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.REMOVED, 1.0, null);
		
		DimpleEnvironment.active().setEventListener(null);
		
		r.setPrior(r2_3);
		priorHandler.assertNoEvent();
		inputHandler.assertNoEvent();
		fixedHandler.assertNoEvent();
	}
	
	private static class PriorChangeHandler extends DimpleEventHandler<VariablePriorChangeEvent>
	{
		private Queue<VariablePriorChangeEvent> _events = new ArrayDeque<VariablePriorChangeEvent>();
		
		@Override
		public void handleEvent(VariablePriorChangeEvent event)
		{
			Variable var = event.getModelObject();
			assertEquals(var.getPrior(), event.getNewPrior());
			switch (event.getType())
			{
			case ADDED:
				assertNotNull(var.getPrior());
				assertNull(event.getOldPrior());
				break;
			case REMOVED:
				assertNull(var.getPrior());
				assertNotNull(event.getOldPrior());
				break;
			case CHANGED:
				assertNotNull(event.getNewPrior());
				assertNotNull(event.getOldPrior());
				assertNotEquals(event.getNewPrior(), event.getOldPrior());
			}
			_events.add(event);
		}
		
		void assertEvent(Variable var, VariablePriorChangeEvent.Type type,
			@Nullable IDatum oldValue, @Nullable IDatum newValue)
		{
			VariablePriorChangeEvent event = _events.poll();
			assertNotNull(event);
			assertSame(var, event.getModelObject());
			assertEquals(type, event.getType());
			assertEquals(oldValue, event.getOldPrior());
			assertEquals(newValue, event.getNewPrior());
		}
		
		void assertNoEvent()
		{
			assertTrue(_events.isEmpty());
		}
	}

	private static class FixedValueChangeHandler extends DimpleEventHandler<VariableFixedValueChangeEvent>
	{
		private Queue<VariableFixedValueChangeEvent> _events = new ArrayDeque<VariableFixedValueChangeEvent>();
		
		@Override
		public void handleEvent(VariableFixedValueChangeEvent event)
		{
			Variable var = event.getModelObject();
			assertEquals(var.getFixedValueObject(), event.getNewValue());
			switch (event.getType())
			{
			case ADDED:
				assertTrue(var.hasFixedValue());
				assertNull(event.getOldValue());
				break;
			case REMOVED:
				assertFalse(var.hasFixedValue());
				assertNull(event.getNewValue());
				assertNotNull(event.getOldValue());
				break;
			case CHANGED:
				assertTrue(var.hasFixedValue());
				assertNotNull(event.getNewValue());
				assertNotNull(event.getOldValue());
			}
			_events.add(event);
		}
		
		void assertEvent(Variable var, VariableFixedValueChangeEvent.Type type,
			@Nullable Object oldValue, @Nullable Object newValue)
		{
			VariableFixedValueChangeEvent event = _events.poll();
			assertNotNull(event);
			assertSame(var, event.getModelObject());
			assertEquals(type, event.getType());
			assertEquals(oldValue, event.getOldValue());
			assertEquals(newValue, event.getNewValue());
		}
		
		void assertNoEvent()
		{
			assertTrue(_events.isEmpty());
		}
	}
	private static class InputChangeHandler extends DimpleEventHandler<VariableInputChangeEvent>
	{
		private Queue<VariableInputChangeEvent> _events = new ArrayDeque<VariableInputChangeEvent>();
		
		@Override
		public void handleEvent(VariableInputChangeEvent event)
		{
			Variable var = event.getModelObject();
			if (event.getNewInput() != null)
			{
				assertInputsEqual(var.getInputObject(), event.getNewInput());
			}
			switch (event.getType())
			{
			case ADDED:
				assertTrue(!var.hasFixedValue());
				assertNull(event.getOldInput());
				break;
			case REMOVED:
				assertNull(event.getNewInput());
				assertNotNull(event.getOldInput());
				break;
			case CHANGED:
				assertNotNull(event.getNewInput());
				assertNotNull(event.getOldInput());
			}
			_events.add(event);
		}
		
		void assertEvent(Variable var, VariableInputChangeEvent.Type type,
			@Nullable Object oldInput, @Nullable Object newInput)
		{
			VariableInputChangeEvent event = _events.poll();
			assertNotNull(event);
			assertSame(var, event.getModelObject());
			assertEquals(type, event.getType());
			assertInputsEqual(oldInput, event.getOldInput());
			assertInputsEqual(newInput, event.getNewInput());
		}

		void assertNoEvent()
		{
			assertTrue(_events.isEmpty());
		}
	}
	
	static void assertInputsEqual(@Nullable Object input1, @Nullable Object input2)
	{
		if (input1 instanceof double[])
		{
			assertArrayEquals((double[])input1, (double[])input2, 1e-15);
		}
		else
		{
			assertEquals(input1, input2);
		}
	}
	
}
