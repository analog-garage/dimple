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

package com.analog.lyric.dimple.test.solvers.gibbs;

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.Random;

import org.apache.commons.math3.stat.StatUtils;
import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.options.DimpleOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.MHSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.SliceSampler;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestGibbsConjugateSampling extends DimpleTestBase
{
	@Test
	public void test3()
	{
		// Adapted from MATLAB testConjugateSampling.m/test3
		
		Random rand = new Random(42);
		
		final double priorMean = 3.0;
		final double priorPrecision = 0.01;
		final double dataMean = 10;
		final double dataPrecision = 0.001;
		final int numDatapoints = 100;
		
		double[] data = new double[numDatapoints];
		double dataSum = 0.0;
		for (int i = 0; i < numDatapoints; ++i)
		{
			dataSum += data[i] = dataMean + rand.nextGaussian() * dataPrecision;
		}
		
		final double expectedPrecision = priorPrecision + numDatapoints * dataPrecision;
		final double expectedStd = 1 / Math.sqrt(expectedPrecision);
		final double expectedMean = (priorMean * priorPrecision + dataSum * dataPrecision) / expectedPrecision;
			
		final FactorGraph fg = new FactorGraph();
		Real mean;
		Real[] x;
		
		try (CurrentModel currrent = using(fg))
		{
			mean = real("mean");
			mean.setPrior(new Normal(priorMean, priorPrecision));
			x = fixed("x", data);
			addFactor(new Normal(), mean, dataPrecision, x);
		}

		fg.setOption(DimpleOptions.randomSeed, 1L);
		fg.setOption(GibbsOptions.numSamples, 1000);
		fg.setOption(GibbsOptions.saveAllSamples, true);
		fg.setOption(GibbsOptions.saveAllScores, true);
		
		GibbsSolverGraph sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		
		GibbsReal smean = sfg.getReal(mean);
		assertEquals("NormalSampler", smean.getSamplerName());
		
		fg.solve();
		
		double[] means = smean.getAllSamples();
		
		assertEquals(expectedMean, StatUtils.mean(means), 0.01);
		assertEquals(expectedStd, Math.sqrt(StatUtils.variance(means)), .05);
		
		// Try again with slice sampler
		mean.setOption(GibbsOptions.realSampler, SliceSampler.class);
		fg.setOption(GibbsOptions.numSamples, 2000);
		fg.solve();
	
		double[] means2 = smean.getAllSamples();
		
		assertEquals("SliceSampler", smean.getSamplerName());
		assertEquals("NormalSampler", sfg.getReal(x[0]).getSamplerName());
		assertEquals(expectedMean, StatUtils.mean(means2), 0.1);
		assertEquals(expectedStd, Math.sqrt(StatUtils.variance(means2)), 0.1);
		
		// Try again with MH
		mean.setOption(GibbsOptions.realSampler, MHSampler.class);
		fg.setOption(GibbsOptions.scansPerSample, 10);
		fg.setOption(GibbsOptions.numSamples, 1000);
		fg.solve();
		
		double[] means3 = smean.getAllSamples();
		
		assertEquals("MHSampler", smean.getSamplerName());
		assertEquals("NormalSampler", sfg.getReal(x[0]).getSamplerName());
		assertEquals(expectedMean, StatUtils.mean(means3), 0.25);
		assertEquals(expectedStd, Math.sqrt(StatUtils.variance(means3)), 0.25);
	}
}
