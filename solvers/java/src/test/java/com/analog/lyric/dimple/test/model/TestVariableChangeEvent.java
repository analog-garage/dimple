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

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.events.DimpleEventHandler;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableChangeEvent;
import com.analog.lyric.dimple.model.variables.VariableFixedValueChangeEvent;
import com.analog.lyric.dimple.model.variables.VariableInputChangeEvent;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Test for {@link VariableChangeEvent}s on {@link Variable}.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
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
		
		FixedValueChangeHandler fixedHandler = new FixedValueChangeHandler();
		InputChangeHandler inputHandler = new InputChangeHandler();
		
		DimpleEventListener listener = DimpleEnvironment.active().createEventListener();
		listener.register(fixedHandler, VariableFixedValueChangeEvent.class, false, fg);
		listener.register(inputHandler, VariableInputChangeEvent.class, false, fg);
		fg.initialize();
		
		assertTrue(listener.isListeningFor(VariableFixedValueChangeEvent.class, r));
		
		r.setFixedValue(2.3);
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.ADDED, null, 2.3);
		r.setFixedValue(4.0);
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.CHANGED, 2.3, 4.0);
		r.setInput((FactorFunction)null);
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.REMOVED, 4.0, null);
		fixedHandler.assertNoEvent();
		inputHandler.assertNoEvent();
		
		double[] d37 = new double[] { .3, .7 };
		double[] d46 = new double[] { .4, .6 };
		d.setInput(d37);
		fixedHandler.assertNoEvent();
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.ADDED, null, d37);
		d.setInput(d46);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.CHANGED, d37, d46);
		d.setInput(null);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.REMOVED, d46, null);
		
		d.setInput(d37);
		assertInputsEqual(d37, d.getInputObject());
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.ADDED, null, d37);
		d.setFixedValueIndex(0);
		double[] d10 = d.getInput();
		assertArrayEquals(new double[] { 1, 0}, d10, 0.0);
		fixedHandler.assertEvent(d, VariableFixedValueChangeEvent.Type.ADDED, null, 0);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.REMOVED, d37, null);
		d.setInput(d37);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.ADDED, null, d37);
		fixedHandler.assertEvent(d, VariableFixedValueChangeEvent.Type.REMOVED, 0, null);
		
		Normal normal = new Normal(1.0, 1.0);
		r.setInput(normal);
		inputHandler.assertEvent(r, VariableInputChangeEvent.Type.ADDED, null, normal);
		fixedHandler.assertNoEvent();
		r.setFixedValue(3.0);
		inputHandler.assertEvent(r, VariableInputChangeEvent.Type.REMOVED, normal, null);
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.ADDED, null, 3.0);
		r.setInput(normal);
		inputHandler.assertEvent(r, VariableInputChangeEvent.Type.ADDED, null, normal);
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.REMOVED, 3.0, null);
		
		inputHandler.assertNoEvent();
		fixedHandler.assertNoEvent();
		
		listener.block(VariableFixedValueChangeEvent.class, false, r);
		r.notifyListenerChanged();
		assertFalse(listener.isListeningFor(VariableFixedValueChangeEvent.class, r));
		
		r.setFixedValue(1.0);
		fixedHandler.assertNoEvent();
		inputHandler.assertEvent(r, VariableInputChangeEvent.Type.REMOVED, normal, null);

		listener.block(VariableInputChangeEvent.class,  false, r);
		r.notifyListenerChanged();
		d.notifyListenerChanged();

		assertTrue(listener.isListeningFor(VariableFixedValueChangeEvent.class, d));
		assertTrue(listener.isListeningFor(VariableInputChangeEvent.class, d));
		d.setFixedValue(1.0);
		double[] d01 = d.getInput();
		assertArrayEquals(new double[] { 0, 1 }, d01, 0.0);
		fixedHandler.assertEvent(d, VariableFixedValueChangeEvent.Type.ADDED, null, 1);
		inputHandler.assertEvent(d, VariableInputChangeEvent.Type.REMOVED, d37, null);
			
		listener.unblock(VariableFixedValueChangeEvent.class, r);
		r.notifyListenerChanged();
		
		r.setInput(normal);
		inputHandler.assertNoEvent();
		fixedHandler.assertEvent(r, VariableFixedValueChangeEvent.Type.REMOVED, 1.0, null);
		
		DimpleEnvironment.active().setEventListener(null);
		
		r.setFixedValue(2.3);
		inputHandler.assertNoEvent();
		fixedHandler.assertNoEvent();
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
