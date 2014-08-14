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

import org.junit.Test;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.SFactorGraph;

/**
 * Tests setting of {@link GibbsOptions}
 * @since 0.07
 * @author Christopher Barber
 */
public class TestGibbsOptions
{
	@SuppressWarnings("deprecation")
	@Test
	public void test()
	{
		// Test default values
		assertEquals(1, GibbsOptions.numSamples.defaultIntValue());
		assertEquals(0, GibbsOptions.numRandomRestarts.defaultIntValue());
		assertEquals(1, GibbsOptions.scansPerSample.defaultIntValue());
		
		// Build test graph
		FactorGraph fg = new FactorGraph();
		Bit b1 = new Bit();
		Bit b2 = new Bit();
		fg.addVariables(b1, b2);
		int nVars = fg.getVariableCount();
		
		// Test default initialization
		SFactorGraph sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		
		assertEquals(GibbsOptions.numSamples.defaultIntValue(), sfg.getNumSamples());
		assertEquals(GibbsOptions.numRandomRestarts.defaultIntValue(), sfg.getNumRestarts());
		
		sfg.initialize();
		assertEquals(GibbsOptions.numSamples.defaultIntValue(), sfg.getNumSamples());
		assertEquals(GibbsOptions.numRandomRestarts.defaultIntValue(), sfg.getNumRestarts());
		assertEquals(nVars, sfg.getUpdatesPerSample()); // because scansPerSample is 1
		
		// Test initialization from options
		fg.setSolverFactory(null);
		sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		fg.setOption(GibbsOptions.numSamples, 3);
		fg.setOption(GibbsOptions.numRandomRestarts, 2);
		fg.setOption(GibbsOptions.scansPerSample, 2); // will override updatesPerSample
		
		// These do not take effect until after initialization
		assertEquals(GibbsOptions.numSamples.defaultIntValue(), sfg.getNumSamples());
		assertEquals(GibbsOptions.numRandomRestarts.defaultIntValue(), sfg.getNumRestarts());
		
		sfg.initialize();
		assertEquals(3, sfg.getNumSamples());
		assertEquals(2, sfg.getNumRestarts());
		assertEquals(2 * nVars, sfg.getUpdatesPerSample()); // because scansPerSample is 2
	
		// Test set methods
		sfg.setNumSamples(4);
		assertEquals(4, sfg.getNumSamples());
		assertEquals((Integer)4, sfg.getLocalOption(GibbsOptions.numSamples));
		sfg.setNumRestarts(5);
		assertEquals(5, sfg.getNumRestarts());
		assertEquals((Integer)5, sfg.getLocalOption(GibbsOptions.numRandomRestarts));
		sfg.setUpdatesPerSample(6);
		assertEquals(new Integer(-1), sfg.getLocalOption(GibbsOptions.scansPerSample));
		sfg.setScansPerSample(3);
		assertEquals(3 * nVars, sfg.getUpdatesPerSample());
		assertEquals((Integer)3, sfg.getLocalOption(GibbsOptions.scansPerSample));
	}
}
