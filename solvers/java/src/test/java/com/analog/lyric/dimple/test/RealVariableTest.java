/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.variables.Real;


public class RealVariableTest extends DimpleTestBase
{
	@SuppressWarnings("null")
	@Test
	public void test1()
	{
		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		
		Real a = new Real();
		Real b = new Real(new RealDomain(-1,1));
		Real c = new Real();
		Real d = new Real(new RealDomain(-1,1));
		Real e = new Real(new RealDomain(0,Double.POSITIVE_INFINITY));
		c.setPrior(new Normal(0,1));
		d.setPrior(new Normal(0,1));

		a.setName("a");
		b.setName("b");
		c.setName("c");
		d.setName("d");
		d.setName("e");

		assertTrue(a.getRealDomain().getLowerBound() == Double.NEGATIVE_INFINITY);
		assertTrue(a.getRealDomain().getUpperBound() == Double.POSITIVE_INFINITY);
		assertTrue(b.getRealDomain().getLowerBound() == -1);
		assertTrue(b.getRealDomain().getUpperBound() == 1);
		assertTrue(c.getRealDomain().getLowerBound() == Double.NEGATIVE_INFINITY);
		assertTrue(c.getRealDomain().getUpperBound() == Double.POSITIVE_INFINITY);
		assertTrue(d.getRealDomain().getLowerBound() == -1);
		assertTrue(d.getRealDomain().getUpperBound() == 1);
		assertTrue(e.getRealDomain().getLowerBound() == 0);
		assertTrue(e.getRealDomain().getUpperBound() == Double.POSITIVE_INFINITY);

		final double oneOverSqrtTwoPi = 1/Math.sqrt(2*Math.PI);
		
		assertTrue(nearlyEquals(((FactorFunction)c.getInput()).eval(new Object[]{0d}), oneOverSqrtTwoPi));
		
		assertTrue(nearlyEquals(((FactorFunction)d.getInput()).eval(new Object[]{1d}), Math.exp(-0.5)*oneOverSqrtTwoPi));

		assertNull(a.getPrior());
		a.setPrior(new Normal(0,1));
		assertTrue(nearlyEquals(((FactorFunction)a.getPrior()).eval(new Object[]{0d}), oneOverSqrtTwoPi));
	}
	
	
	private static double TOLLERANCE = 1e-12;
	private boolean nearlyEquals(double a, double b)
	{
		double diff = a - b;
		if (diff > TOLLERANCE) return false;
		if (diff < -TOLLERANCE) return false;
		return true;
	}

	
	
}
