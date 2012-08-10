package com.analog.lyric.dimple.test;

import com.analog.lyric.dimple.model.Bit;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;

import org.junit.* ;
import static org.junit.Assert.* ;
import java.util.Arrays;


public class VariableTest {

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
	public void test_bit() 
	{
		Bit a = new Bit();
		
		assertTrue(a instanceof Discrete);
		DiscreteDomain d = (DiscreteDomain)a.getDomain();
		assertArrayEquals(d.getElements(),new Object [] {0.0,1.0});
	}

	@Test
	public void test_simpleStuff()  
	{
		Double[] domain = new Double[]{0.0, 1.0};
		Discrete v = new Discrete(domain[0], domain[1]);
		Object[] domainGot = v.getDiscreteDomain().getElements();
		assertTrue(Arrays.equals(domain, domainGot));
		
		assertTrue(v.getId() > -1);
		
		double[] input = new double[]{.5, .5}; 
		v.setInput(input);
		assertTrue(Arrays.equals(v.getInput(), input));
		assertTrue(Arrays.equals(v.getBelief(), input));
		
	}	
	
	@Test(expected=Exception.class)
	public void test_BlowUp() 
	{
		//should kaboom
		new Discrete();
		
	}
}
