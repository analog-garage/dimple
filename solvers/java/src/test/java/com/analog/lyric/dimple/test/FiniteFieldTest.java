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

import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Model;
import com.analog.lyric.dimple.model.variables.FiniteFieldVariable;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;


public class FiniteFieldTest extends DimpleTestBase
{

	static @Nullable IFactorGraphFactory<?> _oldSolver;
	
	@BeforeClass
	public static void setUpBeforeClass()  {
		_oldSolver = Model.getInstance().getDefaultGraphFactory();
	}

	@AfterClass
	public static void tearDownAfterClass()  {
		Model.getInstance().setDefaultGraphFactory(_oldSolver);
	}

	@Before
	public void setUp()  {
	}

	@After
	public void tearDown()  {
	}
		
	@SuppressWarnings("null")
	@Test
	public void test_doubleXor()
	{
		int primPoly = 19;
		int k = 4;
		
		double [] priors = new double[(int)Math.pow(2,k)];
		double sum = 0;
		
		for (int i = 0; i < priors.length; i++)
		{
			priors[i] = 1.0/100.0;
			if (i > 0)
				sum += priors[i];
		}
		
		priors[0] =  1-sum;

		Model m = Model.getInstance();
		m.setDefaultGraphFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());

		FactorGraph fg = new FactorGraph();

		FiniteFieldVariable [] ffx = new FiniteFieldVariable [3];
		for (int i = 0; i < ffx.length; i++)
		{
			ffx[i] = new FiniteFieldVariable(primPoly);
			ffx[i].setPrior(priors);
		}
		
		fg.addFactor("finiteFieldAdd", ffx);
		fg.addFactor("finiteFieldAdd", ffx);
		
		((SumProductSolverGraph)fg.getSolver()).setNumIterations(10);
		fg.solve();
		
		
		
		for (int i = 0; i < ffx.length; i++)
		{
			double [] beliefs = (double[])ffx[i].getBeliefObject();
			for (int j = 0; j < beliefs.length; j++)
			{
				double actual = 0;
				if (j == 0)
					actual = 1;
					
				assertEquals(beliefs[j], actual,1e-10);
			}
		}
		
		//Variable v = new FiniteFieldVariable(19);
	}
	
	
}
