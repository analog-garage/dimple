package com.analog.lyric.dimple.examples;

import com.analog.lyric.dimple.model.Bit;
import com.analog.lyric.dimple.model.FactorGraph;

public class MyGraphCreator {

	public static class Return
	{
		FactorGraph graph;
		Bit a;
		Bit b;
		Bit c;
	}
	
	public static Return createGraph()
	{
		Return r = new Return();
		
		r.graph = new FactorGraph();
		r.a = new Bit();
		r.b = new Bit();
		r.c = new Bit();
		r.graph.addFactor(new ThreeBitXor(),r.a,r.b,r.c);
		return r;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		Return r = createGraph();
		
		r.a.setInput(0.8);
		r.b.setInput(0.9);
		r.graph.solve();
		System.out.println(r.c.getP1());

	}

}
