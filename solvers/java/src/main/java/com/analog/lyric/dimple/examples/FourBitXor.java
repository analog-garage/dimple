package com.analog.lyric.dimple.examples;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.Bit;
import com.analog.lyric.dimple.model.FactorGraph;

public class FourBitXor 
{

	public static void main(String[] args) 
	{
		
		FactorGraph xorGraph = new FactorGraph();
		Bit [] b = new Bit[4];
		for (int i = 0; i < b.length; i++)
			b[i] = new Bit();
		Bit c = new Bit();
		
		ThreeBitXor xd = new ThreeBitXor();
		
		xorGraph.addFactor(xd,b[0],b[1],c); 
		xorGraph.addFactor(xd,b[2],b[3],c);
		
		double [] inputs = new double [] {.8, .8, .8, .5};
		for (int i = 0; i < inputs.length; i++)
			b[i].setInput(inputs[i]);
		
		xorGraph.solve();
		
		for (int i = 0; i < b.length; i++)
			System.out.println(b[i].getP1());
		for (int i = 0; i < b.length; i++)
			System.out.println(b[i].getValue());

	}

}
