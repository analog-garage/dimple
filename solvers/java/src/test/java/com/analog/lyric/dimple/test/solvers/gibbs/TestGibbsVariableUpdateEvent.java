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

package com.analog.lyric.dimple.test.solvers.gibbs;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.Test;

import com.analog.lyric.dimple.events.DimpleEvent;
import com.analog.lyric.dimple.events.DimpleEventHandler;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Complex;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.schedulers.GibbsSequentialScanScheduler;
import com.analog.lyric.dimple.solvers.gibbs.GibbsScoredVariableUpdateEvent;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsVariableUpdateEvent;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.util.misc.Nullable;

/**
 * Test generation of {@link GibbsVariableUpdateEvent}s.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@SuppressWarnings("deprecation")
public class TestGibbsVariableUpdateEvent extends DimpleTestBase
{
	static class BogoFunction extends FactorFunction
	{
		@Override
		public double evalEnergy(Object... arguments)
		{
			double energy = 0.0;
			
			for (Object arg : arguments)
			{
				energy += 1.0;
				if (arg instanceof Number)
				{
					energy += Math.abs(((Number) arg).doubleValue());
				}
				else if (arg instanceof double[])
				{
					for (double d : (double[])arg)
					{
						energy += Math.abs(d);
					}
				}
				else
				{
					throw new Error("die");
				}
			}
			
			return energy;
		}
	}
	
	static class VariableUpdateHandler extends DimpleEventHandler<GibbsVariableUpdateEvent>
	{
		List<GibbsVariableUpdateEvent> events = new ArrayList<GibbsVariableUpdateEvent>();
		
		@Override
		public void handleEvent(GibbsVariableUpdateEvent event)
		{
			events.add(event);
//			printEvent(event);
//			SFactorGraph sgraph = (SFactorGraph)event.getSource().getRootGraph();
//			System.out.format("total score: %s\n", sgraph.getTotalPotential());
			
			ISolverVariableGibbs variable = event.getSource();
			assertTrue(event.getNewValue().valueEquals(variable.getCurrentSampleValue()));
			if (event instanceof GibbsScoredVariableUpdateEvent)
			{
				GibbsScoredVariableUpdateEvent scoredEvent = (GibbsScoredVariableUpdateEvent)event;
				assertEquals(variable.getCurrentSampleScore(), scoredEvent.getNewSampleScore(), 0.0);
				
				assertEquals(scoredEvent.getNewSampleScore() - scoredEvent.getOldSampleScore(),
					scoredEvent.getScoreDifference(), 1e-15);
			}
		}
		
		@SuppressWarnings("null")
		void printEvent(GibbsVariableUpdateEvent event)
		{
			System.out.format("%s: %s %s => %s",
				event.getClass().getSimpleName(), event.getModelObject().getName(),
				event.getOldValue(), event.getNewValue());
			if (event instanceof GibbsScoredVariableUpdateEvent)
			{
				GibbsScoredVariableUpdateEvent scoredEvent = (GibbsScoredVariableUpdateEvent)event;
				System.out.format(" score %+f", scoredEvent.getScoreDifference());
			}
			System.out.println("");
		}
	}
	
	@SuppressWarnings("unused")
	@Test
	public void test()
	{
		//
		// Set up model/solver
		//
		
		final FactorFunction function = new BogoFunction();
		
		FactorGraph model = new FactorGraph();
		Discrete d1 = new Discrete(DiscreteDomain.range(0, 9));
		d1.setName("d1");
		Real r1 = new Real();
		r1.setName("r1");
		Complex c1 = new Complex();
		c1.setName("c1");
		
		model.addVariables(d1, r1, c1);
		
		Factor fdr = model.addFactor(function, d1, r1);
		Factor frc = model.addFactor(function, r1, c1);
		Factor fcd = model.addFactor(function, c1, d1);
		
		GibbsSolverGraph sgraph = requireNonNull(model.setSolverFactory(new GibbsSolver()));
		ISolverVariableGibbs sd1 = Objects.requireNonNull(sgraph.getSolverVariable(d1));
		ISolverVariableGibbs sr1 = Objects.requireNonNull(sgraph.getSolverVariable(r1));
		ISolverVariableGibbs sc1 = Objects.requireNonNull(sgraph.getSolverVariable(c1));
		
		sgraph.setBurnInScans(0);
		sgraph.setNumSamples(1);
		sgraph.setTemperature(1.0);
		model.setScheduler(new GibbsSequentialScanScheduler());
		
		//
		// Set up listener
		//
		
		DimpleEventListener listener = new DimpleEventListener();
		VariableUpdateHandler handler = new VariableUpdateHandler();
		listener.register(handler, GibbsVariableUpdateEvent.class, false, model);
		assertTrue(listener.isListeningFor(GibbsVariableUpdateEvent.class, model));
		assertTrue(listener.isListeningFor(GibbsVariableUpdateEvent.class, sc1));
		assertFalse(listener.isListeningFor(GibbsScoredVariableUpdateEvent.class, sc1));

		model.setEventListener(listener);
		assertSame(listener, model.getEventListener());
		assertSame(listener, sd1.getEventListener());

		model.solve();
		assertEvents(handler, GibbsVariableUpdateEvent.class, sd1, sr1, sc1);

		model.setEventListener(null);
		model.solve();
		assertEvents(handler, GibbsVariableUpdateEvent.class);

		model.setEventListener(listener);
		listener.block(GibbsVariableUpdateEvent.class, false,  sr1);
		model.solve();
		assertEvents(handler, GibbsVariableUpdateEvent.class, sd1, sc1);

		listener.unblock(GibbsVariableUpdateEvent.class,  sr1);
		model.solve();
		assertEvents(handler, GibbsVariableUpdateEvent.class, sd1, sr1, sc1);

		listener.register(handler, GibbsVariableUpdateEvent.class, true, model);
		model.solve();
		assertEvents(handler, GibbsScoredVariableUpdateEvent.class, sd1, sr1, sc1);
		double prevScore = sgraph.getTotalPotential();
		sgraph.sample();
		double score = sgraph.getTotalPotential();
		double scoreDifference = assertEvents(handler, GibbsScoredVariableUpdateEvent.class, sd1, sr1, sc1);
		assertEquals(score - prevScore, scoreDifference, 1e-14);
		listener.unregisterAll();
		sd1.notifyListenerChanged();
		sr1.notifyListenerChanged();
		sc1.notifyListenerChanged();
		sgraph.sample();
		assertEvents(handler, null);
	}
	
	/**
	 * Asserts that events with given {@code expectedClass} have occurred on the {@code handler} on the
	 * specified {@code sources} in order. This method clears the handler's event list.
	 * 
	 * @return the cumulative {@link GibbsScoredVariableUpdateEvent#getScoreDifference()} if available, otherwise zero.
	 */
	private double assertEvents(VariableUpdateHandler handler, @Nullable Class<? extends DimpleEvent> expectedClass,
		IDimpleEventSource ... sources)
	{
		double scoreDifference = 0.0;
		final int nSources = sources.length;
		assertEquals(nSources, handler.events.size());
		for (int i = 0; i < nSources; ++i)
		{
			GibbsVariableUpdateEvent event = handler.events.get(i);
			assertSame(expectedClass, event.getClass());
			assertSame(sources[i], event.getSource());
			
			final int rejectCount = event.getRejectCount();
			final Domain domain = event.getSource().getDomain();
			final RealJointDomain jointDomain = domain.asRealJoint();
			
			assertTrue(rejectCount >= 0);
			boolean fullyRejected = false;
			if (jointDomain != null)
			{
				final int n = jointDomain.getDimensions();
				assertTrue(rejectCount <= n);
				fullyRejected = rejectCount == n;
			}
			else
			{
				assertTrue(rejectCount <= 1);
				fullyRejected = rejectCount == 1;
			}
			
			if (fullyRejected)
			{
				assertTrue(event.getOldValue().valueEquals(event.getNewValue()));
			}
			
			if (event instanceof GibbsScoredVariableUpdateEvent)
			{
				final double eventScoreDifference = ((GibbsScoredVariableUpdateEvent)event).getScoreDifference();
				scoreDifference += eventScoreDifference;
				if (fullyRejected)
				{
					assertEquals(eventScoreDifference, 0.0, 0.0);
				}
			}
		}
		handler.events.clear();
		
		return scoreDifference;
	}
}
