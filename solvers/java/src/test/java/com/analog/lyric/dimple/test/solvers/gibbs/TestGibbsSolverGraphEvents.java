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
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.events.DimpleEventHandler;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.solvers.gibbs.GibbsBurnInEvent;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSampleStatisticsEvent;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraphEvent;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestGibbsSolverGraphEvents extends DimpleTestBase
{
	@Test
	public void test()
	{
		FactorGraph fg = new FactorGraph();

		Bit a = new Bit();
		Bit b = new Bit();
		Bit c = new Bit();
		fg.addFactor(new Bernoulli(.4), a ,b, c);
		
		GibbsSolverGraph sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		
		fg.setOption(GibbsOptions.numSamples, 4);
		fg.setOption(GibbsOptions.numRandomRestarts, 2);
		fg.setOption(GibbsOptions.burnInScans, 3);

		//
		// Set up listener
		//
		
		DimpleEnvironment env = fg.getEnvironment();
		DimpleEventListener listener = env.createEventListener();
		GibbsEventHandler handler = new GibbsEventHandler();
		
		fg.solve();
		
		assertTrue(handler.events.isEmpty());
		
		listener.register(handler, GibbsBurnInEvent.class, env);
		
		fg.solve();
		
		int expectedSize = GibbsOptions.numRandomRestarts.getOrDefault(sfg) + 1;
		assertEquals(expectedSize, handler.events.size());
		for (int i = 0; i < expectedSize; ++i)
		{
			GibbsSolverGraphEvent event = handler.events.get(i);
			assertSame(event.getSolverObject(), sfg);
			assertSame(event.getSource(), sfg);
			assertSame(event.getModelObject(), fg);
			
			assertTrue(event instanceof GibbsBurnInEvent);
			GibbsBurnInEvent burnInEvent = (GibbsBurnInEvent)event;
			assertEquals(i, burnInEvent.restartCount());
			assertTrue(Double.isNaN(burnInEvent.temperature()));
			
			assertThat(burnInEvent.toString(1), containsString("burn-in restart " + i));
		}
		
		handler.events.clear();
		
		fg.setOption(GibbsOptions.enableAnnealing, true);
		fg.solve();

		assertEquals(expectedSize, handler.events.size());
		for (int i = 0; i < expectedSize; ++i)
		{
			GibbsSolverGraphEvent event = handler.events.get(i);
			GibbsBurnInEvent burnInEvent = (GibbsBurnInEvent)event;
			
			double temperature = burnInEvent.temperature();
			assertEquals(GibbsOptions.initialTemperature.getOrDefault(sfg), temperature, 0.0);
			String tempString = String.format("temperature %f", temperature);
			assertThat(burnInEvent.toString(1), containsString(tempString));
			assertThat(burnInEvent.toString(0), not(containsString(tempString)));
		}
		
		handler.events.clear();
		listener.register(handler, GibbsSolverGraphEvent.class, env);
		listener.block(GibbsBurnInEvent.class, false, sfg);
		
		sfg.setOption(GibbsOptions.saveAllScores, true);
		sfg.setOption(GibbsOptions.numRandomRestarts, 1);
		fg.solve();
		
		expectedSize = 2 * 4;
		assertEquals(expectedSize, handler.events.size());
		double[] scores = requireNonNull(sfg.getAllScores());
		assertEquals(expectedSize, scores.length);
		for (int i = 0; i < expectedSize; ++i)
		{
			GibbsSampleStatisticsEvent event = (GibbsSampleStatisticsEvent)handler.events.get(i);
			assertEquals(scores[i], event.sampleScore(), 0.0);
		}
	}
	
	static class GibbsEventHandler extends DimpleEventHandler<GibbsSolverGraphEvent>
	{
		List<GibbsSolverGraphEvent> events = new ArrayList<>();
		
		@Override
		public void handleEvent(GibbsSolverGraphEvent event)
		{
			events.add(event);
		}
	}
}
