package com.analog.lyric.dimple.test;

import com.analog.lyric.dimple.FactorFunctions.NopFactorFunction;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FiniteFieldVariable;
import com.analog.lyric.dimple.model.Model;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.sumproduct.SFactorGraph;

import org.junit.* ;
import static org.junit.Assert.* ;


public class FiniteFieldTest {

	static IFactorGraphFactory _oldSolver;
	
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
			ffx[i].setInput(priors);
		}
		
		fg.addFactor(new NopFactorFunction("finiteFieldAdd"), ffx);
		fg.addFactor(new NopFactorFunction("finiteFieldAdd"), ffx);
		
		((SFactorGraph)fg.getSolver()).setNumIterations(10);
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
