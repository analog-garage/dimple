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

package com.analog.lyric.dimple.test.matlabproxy;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.matlabproxy.ModelFactory;
import com.analog.lyric.dimple.matlabproxy.PCustomFactors;
import com.analog.lyric.dimple.matlabproxy.PDimpleEventLogger;
import com.analog.lyric.dimple.matlabproxy.PDiscreteDomain;
import com.analog.lyric.dimple.matlabproxy.PDiscreteVariableVector;
import com.analog.lyric.dimple.matlabproxy.PFactorGraphVector;
import com.analog.lyric.dimple.matlabproxy.PFactorTable;
import com.analog.lyric.dimple.matlabproxy.PFiniteFieldDomain;
import com.analog.lyric.dimple.matlabproxy.PFiniteFieldVariableVector;
import com.analog.lyric.dimple.matlabproxy.PHelpers;
import com.analog.lyric.dimple.matlabproxy.PLogger;
import com.analog.lyric.dimple.matlabproxy.PMultiplexerCPD;
import com.analog.lyric.dimple.matlabproxy.PRealDomain;
import com.analog.lyric.dimple.matlabproxy.PRealJointDomain;
import com.analog.lyric.dimple.matlabproxy.PRealJointVariableVector;
import com.analog.lyric.dimple.matlabproxy.PRealVariableVector;
import com.analog.lyric.dimple.matlabproxy.PScheduler;
import com.analog.lyric.dimple.matlabproxy.PTableFactorFunction;
import com.analog.lyric.dimple.matlabproxy.PVariableVector;
import com.analog.lyric.dimple.matlabproxy.repeated.PDoubleArrayDataSink;
import com.analog.lyric.dimple.matlabproxy.repeated.PDoubleArrayDataSource;
import com.analog.lyric.dimple.matlabproxy.repeated.PFactorFunctionDataSource;
import com.analog.lyric.dimple.matlabproxy.repeated.PMultivariateDataSink;
import com.analog.lyric.dimple.matlabproxy.repeated.PMultivariateDataSource;
import com.analog.lyric.dimple.matlabproxy.repeated.PVariableStreamBase;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Model;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.FiniteFieldDomain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.schedulers.CustomScheduler;
import com.analog.lyric.dimple.schedulers.GibbsRandomScanScheduler;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.solvers.core.multithreading.ThreadPool;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsCustomFactors;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Unit tests for {@link ModelFactory}
 * @since 0.08
 * @author Christopher Barber
 */
public class TestModelFactory extends DimpleTestBase
{
	private final ModelFactory mf = new ModelFactory();

	// These are mostly lame tests of the code in ModelFactory itself and does not test
	// the correctness of the returned objects for the most part.

	@Test
	public void createCustomFactors()
	{
		PCustomFactors pcf = mf.createCustomFactors("GibbsOptions.customFactors");
		assertSame(GibbsCustomFactors.class, pcf.getDelegate().getClass());

		expectThrow(DimpleException.class, "Cannot find option 'Bogus.customFactors'",
			mf, "createCustomFactors", "Bogus.customFactors");
		expectThrow(DimpleException.class, "Option 'BPOptions.damping' is not a CustomFactors option",
			mf, "createCustomFactors", BPOptions.damping.qualifiedName());
	}
	
	@Test
	public void createDomains()
	{
		PDiscreteDomain pdiscrete = mf.createDiscreteDomain(new Object[] { 0.0, 1.0, 2.0 });
		assertSame(DiscreteDomain.create(0.0,1.0,2.0), pdiscrete.getDelegate());
		
		PRealDomain punit = mf.createRealDomain(0.0, 1.0);
		assertEquals(0.0, punit.getLowerBound(), 0.0);
		assertEquals(1.0, punit.getUpperBound(), 1.0);
		assertSame(RealDomain.create(0.0, 1.0), punit.getDelegate());
		
		PRealDomain preal = mf.createRealDomain(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		assertSame(RealDomain.unbounded(), preal.getDelegate());
		
		PRealJointDomain prj = mf.createRealJointDomain(new Object[] { punit, preal });
		assertSame(RealJointDomain.create(RealDomain.create(0.0,1.0), RealDomain.unbounded()), prj.getDelegate());
		
		PFiniteFieldDomain pff = mf.createFiniteFieldDomain(0x2f);
		assertSame(0x2f, pff.getPrimitivePolynomial());
	}
	
	@Test
	public void createEventLogger()
	{
		PDimpleEventLogger plogger = mf.createEventLogger();
		assertEquals(0, plogger.verbosity());
		assertEquals("stderr", plogger.filename());
		plogger.close();
	}
	
	@Test
	public void createFactorTable()
	{
		PDiscreteDomain pdd2 = mf.createDiscreteDomain(new Object[] {1, 2});
		PDiscreteDomain pdd3 = mf.createDiscreteDomain(new Object[] { 1,2,3});
		
		PDiscreteDomain[] domains = new PDiscreteDomain[] { pdd2, pdd3 };
		PFactorTable pft = mf.createFactorTable(domains);
		assertArrayEquals(domains, pft.getDomains());
		IFactorTable ft = pft.getDelegate();
		assertEquals(2, ft.getDimensions());
		assertEquals(pdd2.getDelegate(), ft.getDomainIndexer().get(0));
		assertEquals(pdd3.getDelegate(), ft.getDomainIndexer().get(1));
		assertEquals(0, ft.countNonZeroWeights());
		
		int[][] indices = new int[][] {
			new int[] { 0, 0 },
			new int[] { 1, 1 },
			new int[] { 0, 2 },
			new int[] { 1, 2 }
		};
		double[] values = new double[] {1,2,3,4};
		
		pft = mf.createFactorTable(indices, values, domains);
		for (int i = values.length; --i>=0;)
		{
			assertEquals(values[i], pft.get(indices[i]), 0.0);
		}
		
		PTableFactorFunction ptff = mf.createTableFactorFunction("ff", indices, values, domains);
		assertEquals("ff", ptff.getDelegate().getName());
		pft = ptff.getFactorTable();
		for (int i = values.length; --i>=0;)
		{
			assertEquals(values[i], pft.get(indices[i]), 0.0);
		}
		
		double[][][] rawValues = new double[][][] {
			new double[][] {
				new double[] { 1, 5 },
				new double[] { 3, 0 },
			},
			new double [][] {
				new double[] { 2, 6 },
				new double[] { 4, 8 },
			}
		};
		
		pft = mf.createFactorTable(rawValues, new Object[] { pdd2, pdd2, pdd2});
		assertArrayEquals(new double[] { 1,2,3,4,5,6,8 }, pft.getWeights(), 0.0);
	}
	
	@Test
	public void createGraph()
	{
		PFactorGraphVector pfg = mf.createGraph(new Object[0]);
		assertEquals(1, pfg.getDelegate().length);
		FactorGraph fg = pfg.getGraph();
		assertSame(fg, pfg.getDelegate()[0]);
		assertEquals(0, fg.getBoundaryVariableCount());
		
		PRealDomain prd = mf.createRealDomain(0.0, 1.0);
		PDiscreteDomain pdd = mf.createDiscreteDomain(new Object[]{0.0,1.0});
		PVariableVector vv1 = mf.createRealVariableVector(prd, 3);
		PVariableVector vv2 = mf.createDiscreteVariableVector(pdd, 2);
		PFactorGraphVector pfg2 = mf.createGraph(new Object[] { vv1, vv2 });
		FactorGraph fg2 = pfg2.getGraph();
		assertEquals(5, fg2.getBoundaryVariableCount());
		assertSame(prd.getDelegate(), fg2.getBoundaryVariable(2).getDomain());
		assertSame(pdd.getDelegate(), fg2.getBoundaryVariable(3).getDomain());
	}
	
	@Test
	public void createParameterizedMessages()
	{
		// DiscreteMessage
		DiscreteMessage discrete = mf.createDiscreteMessage("energy", 2, null);
		assertFalse(discrete.storesWeights());
		assertEquals(2, discrete.size());
		assertArrayEquals(new double[2], discrete.getEnergies(), 0.0);
		discrete = mf.createDiscreteMessage("weight", 3, new double[] { 1,2,3 });
		assertTrue(discrete.storesWeights());
		assertArrayEquals(new double[] {1,2,3}, discrete.getWeights(), 0.0);
		
		// NormalParameters
		NormalParameters normal = mf.createNormalParameters(1.0, 2.0);
		assertEquals(1.0, normal.getMean(), 0.0);
		assertEquals(2.0, normal.getPrecision(), 0.0);
		
		// MultivariateNormalParameters
		final double[] means = new double[] {1.0, 2.0};
		final double[][] covariance = new double[][] {
			new double[] {1.0, .5},
			new double[] {.5, 2.0}
		};
		MultivariateNormalParameters multi = mf.createMultivariateNormalParameters(means, covariance);
		assertArrayEquals(means, multi.getMeans(), 0.0);
		assertArrayEquals(covariance[0], multi.getCovariance()[0], 0.0);
		assertArrayEquals(covariance[1], multi.getCovariance()[1], 0.0);
	}
	
	@Test
	public void createScheduler()
	{
		PScheduler scheduler = mf.createScheduler("GibbsRandomScanScheduler");
		assertSame(GibbsRandomScanScheduler.class, scheduler.getDelegate().getClass());
		
		expectThrow(RuntimeException.class, mf, "createScheduler", "NoSuchScheduler");
		
		PFactorGraphVector pfg = mf.createGraph(new Object[0]);
		
		PRealDomain prd = mf.createRealDomain(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		PVariableVector pvars = mf.createRealVariableVector(prd, 5);
		Variable[] vars = pvars.getVariableArray();
		pfg.getGraph().addVariables(vars);
		
		scheduler = mf.createCustomScheduler(pfg, GibbsOptions.scheduler, new Object[] {
			PHelpers.wrapObject(vars[0]),
			PHelpers.wrapObject(vars[3])
		});
		assertTrue(scheduler.getDelegate() instanceof CustomScheduler);
		ISchedule schedule = scheduler.getDelegate().createSchedule(pfg.getGraph());
		assertTrue(schedule instanceof FixedSchedule);
		FixedSchedule fixed = (FixedSchedule)schedule;
		assertEquals(2, fixed.size());
		IScheduleEntry entry = fixed.get(0);
		assertEquals(IScheduleEntry.Type.NODE, entry.type());
		assertEquals(vars[0], ((NodeScheduleEntry)entry).getNode());
		assertEquals(vars[3], ((NodeScheduleEntry)fixed.get(1)).getNode());
		
		scheduler = mf.createCustomScheduler(pfg, "GibbsOptions.scheduler", new Object[] {
			PHelpers.wrapObject(vars[1])
		});
		schedule = scheduler.getDelegate().createSchedule(pfg.getGraph());
		fixed = (FixedSchedule)schedule;
		assertEquals(1, fixed.size());
		assertEquals(vars[1], ((NodeScheduleEntry)fixed.get(0)).getNode());
		
		scheduler = mf.createCustomScheduler(pfg, null, new Object[] {
			PHelpers.wrapObject(vars[2])
		});
		schedule = scheduler.getDelegate().createSchedule(pfg.getGraph());
		fixed = (FixedSchedule)schedule;
		assertEquals(1, fixed.size());
		assertEquals(vars[2], ((NodeScheduleEntry)fixed.get(0)).getNode());

		scheduler = mf.createCustomScheduler(pfg, "", new Object[] {
			PHelpers.wrapObject(vars[2])
		});
		schedule = scheduler.getDelegate().createSchedule(pfg.getGraph());
		fixed = (FixedSchedule)schedule;
		assertEquals(1, fixed.size());
		assertEquals(vars[2], ((NodeScheduleEntry)fixed.get(0)).getNode());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void createVariableVectors()
	{
		final RealDomain rd = RealDomain.create(0.0,1.0);
		final PRealDomain prd = mf.createRealDomain(0.0,  1.0);
		assertSame(rd, prd.getDelegate());
		PRealVariableVector prv = mf.createRealVariableVector(prd, 2);
		assertEquals(2, prv.size());
		assertSame(rd, prv.getVariable(0).getDomain());
		assertSame(rd, prv.getVariable(1).getDomain());
		prv = mf.createRealVariableVector("ignored", prd, 3);
		assertEquals(3, prv.size());
		assertSame(rd, prv.getVariable(0).getDomain());
		
		DiscreteDomain dd = DiscreteDomain.bit();
		PDiscreteDomain pdd = mf.createDiscreteDomain(new Object[] {0,1});
		assertSame(dd, pdd.getDelegate());
		PDiscreteVariableVector pdv = mf.createDiscreteVariableVector(pdd, 3);
		assertEquals(3, pdv.size());
		assertSame(dd, pdv.getVariable(0).getDomain());
		assertSame(dd, pdv.getVariable(2).getDomain());
		pdv = mf.createDiscreteVariableVector("gag", pdd, 2);
		assertEquals(2, pdv.size());
		assertSame(dd, pdv.getVariable(0).getDomain());
		pdv = mf.createVariableVector("xxx", pdd, 4);
		assertEquals(4, pdv.size());
		assertSame(dd, pdv.getVariable(3).getDomain());
		
		RealJointDomain rjd = RealJointDomain.create(rd, 2);
		assertSame(rjd, RealJointDomain.create(rd,rd));
		PRealJointDomain prdj = mf.createRealJointDomain(new Object[] { prd, prd } );
		assertSame(rjd, prdj.getDelegate());
		PRealJointVariableVector prjv = mf.createRealJointVariableVector(prdj, 2);
		assertEquals(2, prjv.size());
		assertSame(rjd, prjv.getVariable(0).getDomain());
		assertSame(rjd, prjv.getVariable(1).getDomain());
		prjv = mf.createRealJointVariableVector("bogus", prdj, 3);
		assertEquals(3, prjv.size());
		assertSame(rjd, prjv.getVariable(2).getDomain());
		
		FiniteFieldDomain ffd = FiniteFieldDomain.create(0x2f);
		PFiniteFieldDomain pffd = mf.createFiniteFieldDomain(0x2f);
		assertSame(ffd, pffd.getDelegate());
		PFiniteFieldVariableVector pffv = mf.createFiniteFieldVariableVector(pffd, 2);
		assertEquals(2, pffv.size());
		assertSame(ffd, pffv.getVariable(0).getDomain());
	}
	
	@Test
	public void getLogger()
	{
		PLogger logger = mf.getLogger();
		assertSame(logger, mf.getLogger());
	}
	
	@Test
	public void getMultiplexerCPD()
	{
		PDiscreteDomain pdd2 = mf.createDiscreteDomain(new Object[] {1,2});
		
		PMultiplexerCPD pmcpd = mf.getMultiplexerCPD(new Object[]{1,2}, 3);
		assertEquals(1, pmcpd.size());
		assertSame(pdd2.getDelegate(), pmcpd.getY().getDomain().getDelegate());
		assertEquals(3, pmcpd.getZs().length);
		
		pmcpd = mf.getMultiplexerCPD(new Object[][] { new Object[] {1,2}, new Object [] {3,4}});
		assertEquals(1, pmcpd.size());
		assertEquals(2, pmcpd.getZs().length);
	}
	
	@Test
	public void setSolver()
	{
		try
		{
			mf.setSolver(null);
			assertNull(Model.getInstance().getDefaultGraphFactory());

			IFactorGraphFactory<?> solverFactory = new GibbsSolver();
			mf.setSolver(solverFactory);
			assertSame(solverFactory, Model.getInstance().getDefaultGraphFactory());
		}
		finally
		{
			// This shouldn't be necessary, but this state is currently shared by other tests
			Model.getInstance().restoreDefaultDefaultGraphFactory();
		}
	}
	
	@Test
	public void streamOperations()
	{
		PDiscreteDomain pdd = mf.createDiscreteDomain(new Object[] { 0.0,1.0 });
		PVariableStreamBase pds = mf.createDiscreteStream(pdd, 2);
		assertEquals(2, pds.getModelerObjects().length);
		assertSame(pdd.getDelegate(), pds.getModelerObjects()[0].getDomain());

		PRealDomain prd = mf.createRealDomain(0.0,1.0);
		PVariableStreamBase prs = mf.createRealStream(prd, 2);
		assertEquals(2, prs.getModelerObjects().length);
		assertSame(prd.getDelegate(), prs.getModelerObjects()[0].getDomain());

		PRealJointDomain prjd = mf.createRealJointDomain(new Object[] { prd, prd });
		PVariableStreamBase prjs = mf.createRealJointStream(prjd, 2);
		assertEquals(2, prjs.getModelerObjects().length);
		assertSame(prjd.getDelegate(), prjs.getModelerObjects()[0].getDomain());

		PFactorFunctionDataSource pffds = mf.getFactorFunctionDataSource(3);
		assertEquals(3, pffds.getModelObjects().length);
		
		PDoubleArrayDataSource pdads = mf.getDoubleArrayDataSource(2);
		assertEquals(2, pdads.getModelObjects().length);
		
		PDoubleArrayDataSink pdadsink = mf.getDoubleArrayDataSink(3);
		assertEquals(3, pdadsink.getModelObjects().length);
		
		PMultivariateDataSource pmds = mf.getMultivariateDataSource(4);
		assertEquals(4, pmds.getModelObjects().length);
		
		PMultivariateDataSink pmdsink = mf.getMultivariateDataSink(2);
		assertEquals(2, pmdsink.getModelObjects().length);
	}
	
	@Test
	public void threadOperations()
	{
		// Thread operations
		ThreadPool.setNumThreadsToDefault();
		int nthreads = mf.getNumThreads();
		mf.setNumThreads(42);
		assertEquals(42, mf.getNumThreads());
		assertEquals(42, ThreadPool.getNumThreads());
		mf.setNumThreadsToDefault();
		assertEquals(nthreads, mf.getNumThreads());
	}
}
