package com.analog.lyric.dimple.test;


import org.junit.*;
import static org.junit.Assert.* ;

import com.analog.lyric.dimple.FactorFunctions.NopFactorFunction;
import com.analog.lyric.dimple.FactorFunctions.XorDelta;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;

public class TableFactorTest {

	@BeforeClass
	public static void setUpBeforeClass()  {
	}

	@AfterClass
	public static void tearDownAfterClass()  {
	}

	@Before
	public void setUp()  {
	}

	@After
	public void tearDown()  {
	}
	@Test
	public void test_simple() 
	{
		Discrete[] vs = new Discrete[]
        {
				new Discrete(0.0,1.0),
				new Discrete(0.0,1.0),
				new Discrete(0.0,1.0),
        };

		FactorGraph fg = new FactorGraph(); 
		fg.setSolverFactory(new com.analog.lyric.dimple.test.dummySolver.DummySolver());
		
		Factor fCustom = fg.addFactor(new NopFactorFunction("dummyCustomFactor"), vs[0],vs[1],vs[2]);
		Factor fCustom2 = fg.addFactor(new NopFactorFunction("dummyCustomFactor"), vs[0],vs[1],vs[2]);
		XorDelta xorFF = new XorDelta();
		DiscreteFactor tf = (DiscreteFactor) fg.addFactor(xorFF, vs[0],vs[1],vs[2]);		
		DiscreteFactor tf2 = (DiscreteFactor) fg.addFactor(xorFF, vs[0],vs[1],vs[2]);		

		fCustom.setName("fCustom");
		fCustom2.setName("fCustom2");
		tf.setName("tf");
		tf2.setName("tf2");
		
		Object ofCustom = fg.getObjectByName("fCustom");
		Object ofCustom2 = fg.getObjectByName("fCustom2");
		Object otf = fg.getObjectByName("tf");
		Object otf2 = fg.getObjectByName("tf2");
		
		assertTrue(fCustom.equals(ofCustom));
		assertTrue(!fCustom.equals(fCustom2));
		assertTrue(!fCustom.equals(ofCustom2));
		assertTrue(!fCustom.equals(tf));
		assertTrue(!fCustom.equals(tf2));
		assertTrue(!fCustom.equals(otf));
		assertTrue(!fCustom.equals(otf2));
		
		assertTrue(tf.equals(otf));
		assertTrue(!tf.equals(tf2));
		assertTrue(!tf.equals(otf2));
		
		
		assertTrue(tf.getFactorTable().getIndices() != null);
		assertTrue(tf.getFactorTable().getWeights()!= null);
		assertTrue(tf.getFactorTable().getIndices().length == 4);
		assertTrue(tf.getFactorTable().getIndices()[0].length == 3);
	}
	
	@Test
	public void test_some_accessors() 
	{
		
	}
}
