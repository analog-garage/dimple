package com.analog.lyric.dimple.examples;

import java.util.Arrays;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.FactorGraph;

public class BadNBitXorTutorial 
{

	public static class BadNBitXor extends FactorFunction
	{
		@Override
		public double eval(Object ... args)
		{
			int sum = 0;
			for (int i = 0; i < args.length; i++)
				sum += (Integer)args[i];
			
			return (sum % 2) == 0 ? 1 : 0;
		}
	}
	
	public static void main(String[] args) 
	{
		FactorGraph fourBitXor = new FactorGraph();
		DiscreteDomain domain = new DiscreteDomain(0,1);
		Discrete b1 = new Discrete(domain);
		Discrete b2 = new Discrete(domain);
		Discrete b3 = new Discrete(domain);
		Discrete b4 = new Discrete(domain);
		fourBitXor.addFactor(new BadNBitXor(),b1,b2,b3,b4);
		b1.setInput(0.2,0.8);
		b2.setInput(0.2,0.8);
		b3.setInput(0.2,0.8);
		b4.setInput(0.5,0.5);
		fourBitXor.solve();
		System.out.println(b4.getValue());
		System.out.println(Arrays.toString(b4.getBelief()));
	}

}
