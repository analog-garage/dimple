package com.analog.lyric.dimple.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.solvers.gibbs.SDiscreteVariable;
import com.analog.lyric.dimple.solvers.gibbs.SFactorGraph;
import com.analog.lyric.dimple.solvers.gibbs.STableFactor;


public class GibbsTest
{
	protected static boolean debugPrint = false;
	
	@Test
	public void basicTest() 
	{
		int numSamples = 10000;
		int updatesPerSample = 2;
		int burnInUpdates = 2000;

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		SFactorGraph solver = (SFactorGraph)graph.getSolver();
		solver.setNumSamples(numSamples);
		solver.setUpdatesPerSample(updatesPerSample);
		solver.setBurnInUpdates(burnInUpdates);
		
		Discrete a = new Discrete(1,0);
		Discrete b = new Discrete(1,0);
		a.setName("a");
		b.setName("b");
		
		Factor pA = graph.addFactor(new PA(), a);
		Factor pBA = graph.addFactor(new PBA(), b, a);
		
		solver.setSeed(1);					// Make this repeatable
		solver.saveAllSamples();
		graph.solve();

		SDiscreteVariable sa = (SDiscreteVariable)a.getSolver();
		SDiscreteVariable sb = (SDiscreteVariable)b.getSolver();
		STableFactor sA = (STableFactor)pA.getSolver();
		STableFactor sBA = (STableFactor)pBA.getSolver();

		Object[] aSamples = sa.getAllSamples();
		Object[] bSamples = sb.getAllSamples();
		int aSum = 0;
		for (Object s : aSamples) aSum += (Integer)s;
		double aMean = (double)aSum/(double)aSamples.length;
		if (debugPrint) System.out.println("sai: " + aMean);
		int bSum = 0;
		for (Object s : bSamples) bSum += (Integer)s;
		double bMean = (double)bSum/(double)bSamples.length;
		if (debugPrint) System.out.println("sbi: " + bMean);

		if (debugPrint) System.out.println("aBest: " + (Integer)sa.getBestSample());
		if (debugPrint) System.out.println("bBest: " + (Integer)sb.getBestSample());
		
		double totalPotential = 0;
		totalPotential += sA.getPotential(new int[]{sa.getBestSampleIndex()});
		totalPotential += sBA.getPotential(new int[]{sb.getBestSampleIndex(),sa.getBestSampleIndex()});
		if (debugPrint) System.out.println("Min potential: " + totalPotential + " (" + Math.exp(-totalPotential) + ")");

		if (debugPrint) System.out.println("a: " + a.getBelief()[0]);
		if (debugPrint) System.out.println("b: " + b.getBelief()[0]);

		double pa1 = 0.2;
		double pa0 = 1 - pa1;
		double pb1Ia1 = 0.1;
		double pb0Ia1 = 1 - pb1Ia1;
		double pb1Ia0 = 0.75;
		double pb0Ia0 = 1 - pb1Ia0;
		double pa1b1 = pa1*pb1Ia1;
		double ba1b0 = pa1*pb0Ia1;
		double pa0b1 = pa0*pb1Ia0;
		@SuppressWarnings("unused")
		double pa0b0 = pa0*pb0Ia0;
		double pa1m = pa1b1 + ba1b0;
		double pb1m = pa1b1 + pa0b1;
		if (debugPrint) System.out.println("pa1: " + pa1m);
		if (debugPrint) System.out.println("pb1: " + pb1m);
		
		assertTrue((Integer)sa.getBestSample() == 0);
		assertTrue((Integer)sb.getBestSample() == 1);
		assertTrue(nearlyEquals(a.getBelief()[0],aMean));
		assertTrue(nearlyEquals(b.getBelief()[0],bMean));
		assertTrue(nearlyEquals(a.getBelief()[0],0.1994));
		assertTrue(nearlyEquals(b.getBelief()[0],0.6169));
		assertTrue(nearlyEquals(Math.exp(-totalPotential),0.6));
	}
	
	public static class PA extends FactorFunction 
	{
		public PA() {super("PA");}
	    public double eval(Object ... input)
	    {
	    	double value = 0;
	    	int a = (Integer)input[0];
	    	if (a == 1)
	    	    value = 0.2;
	    	else
	    	    value = 0.8;
	    	return value;
	    }
	}
	
	public static class PBA extends FactorFunction 
	{
		public PBA() {super("PBA");}
	    public double eval(Object ... input)
	    {
	    	double value = 0;
	    	int b = (Integer)input[0];
	    	int a = (Integer)input[1];
	    	if ((b == 1) && (a == 1))
	    		value = 0.1;
	    	else if ((b == 0) && (a == 1))
	    	    value = 0.9;
	    	else if ((b == 1) && (a == 0))
	    	    value = 0.75;
	    	else
	    		value = 0.25;
	    	return value;
	    }
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
