/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.analog.lyric.dimple.FactorFunctions.SimpleNormal;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;


public class RealVariableTest
{
	protected static boolean debugPrint = false;
	
	@Test
	public void test1() 
	{
		if (debugPrint) System.out.println("== test1 ==");

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		
		Real a = new Real();
		Real b = new Real(new RealDomain(-1,1));
		Real c = new Real(new SimpleNormal(0,1));
		Real d = new Real(new RealDomain(-1,1), new SimpleNormal(0,1));
		Real e = new Real(new RealDomain(0,Double.POSITIVE_INFINITY));

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

		if (debugPrint) System.out.println("c.Input(0): " + ((FactorFunction)c.getInput()).eval(new Object[]{0d}));
		assertTrue(nearlyEquals(((FactorFunction)c.getInput()).eval(new Object[]{0d}), 1.0));
		
		if (debugPrint) System.out.println("d.Input(1): " + ((FactorFunction)d.getInput()).eval(new Object[]{1d}));
		assertTrue(nearlyEquals(((FactorFunction)d.getInput()).eval(new Object[]{1d}), Math.exp(-1)));

		assertTrue(a.getInputObject() == null);
		a.setInputObject(new SimpleNormal(0,1));
		if (debugPrint) System.out.println("a.Input(0): " + ((FactorFunction)a.getInput()).eval(new Object[]{0d}));
		assertTrue(nearlyEquals(((FactorFunction)a.getInput()).eval(new Object[]{0d}), 1.0));
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
