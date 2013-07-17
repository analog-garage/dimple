package com.analog.lyric.dimple.examples;

import java.util.Arrays;

import com.analog.lyric.dimple.FactorFunctions.Normal;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.Bit;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.schedulers.SequentialScheduler;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;

public class Tmp {

	public static class Equals extends FactorFunction
	{
		@Override
		public double eval(Object ... args)
		{
			int a = (Integer)args[0];
			int b = (Integer)args[1];

			return a == b ? 1 : 0;
		}
	}


	public static class NotEquals extends FactorFunction
	{
		@Override
		public double eval(Object ... args)
		{
			int a = (Integer)args[0];
			int b = (Integer)args[1];

			return a == b ? 0 : 1;
		}
	}

	public static class XorYequalsZ extends FactorFunction
	{
		@Override
		public double eval(Object ... args)
		{
			int x = (Integer)args[0];
			int y = (Integer)args[1];
			int z = (Integer)args[2];

			return (x == 1 || y == 1) == (z == 1) ? 1 : 0;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		//OK, first we create a simple Factor Graph with a single xor connecting two  
		//variables.
		Equals eq = new Equals();
		FactorGraph fg = new FactorGraph(); 
		Bit b1 = new Bit();
		Bit b2 = new Bit();
		Factor f = fg.addFactor(eq,b1,b2);
		//We can go ahead and set some inputs
		b1.setInput(0.8);
		b1.setInput(0.7);

		//we can examine some edges
		
		System.out.println(f.getPorts().get(0).getInputMsg());
		System.out.println(f.getPorts().get(0).getOutputMsg());

		//we can even set some edge messages
		f.getPorts().get(0).setInputMsgValues(new double [] {0.6,0.4});

		//we can update a node
		b1.update();
		b2.update();

		//or a specific edge		
		b1.updateEdge(f);
		 
		//but updating via portNum is quicker
		b1.updateEdge(0);

		//of course, if we don't know the portNum, we can get it
		int portNum = b1.getPortNum(f);
		b1.updateEdge(portNum);

		//We can do the same kind of stuff with factors
		f.updateEdge(b1);
		f.updateEdge(f.getPortNum(b2));

		//Let's look at some messages again
		System.out.println(b1.getPorts().get(0).getInputMsg());
		System.out.println(b2.getPorts().get(0).getInputMsg());

		//and some beliefs
		System.out.println(b1.getBelief());

	}

}
