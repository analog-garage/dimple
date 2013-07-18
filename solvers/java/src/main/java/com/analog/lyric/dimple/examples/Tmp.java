package com.analog.lyric.dimple.examples;

import java.util.Arrays;

import com.analog.lyric.dimple.FactorFunctions.NopFactorFunction;
import com.analog.lyric.dimple.FactorFunctions.Normal;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.Bit;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Model;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.repeated.BitStream;
import com.analog.lyric.dimple.model.repeated.DiscreteStream;
import com.analog.lyric.dimple.model.repeated.DoubleArrayDataSink;
import com.analog.lyric.dimple.model.repeated.DoubleArrayDataSource;
import com.analog.lyric.dimple.model.repeated.FactorGraphStream;
import com.analog.lyric.dimple.model.repeated.MultivariateDataSource;
import com.analog.lyric.dimple.model.repeated.RealStream;
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
//simple Markov Model with larger buffer size
Equals eq = new Equals();

//Create the data
int N = 10;
double [][] data = new double[N][];
for (int i = 0; i < N; i++)
	data[i] = new double [] {0.4,0.6};

//Create a data source
DoubleArrayDataSource dataSource = new DoubleArrayDataSource(data);

//Create a variable stream.
DiscreteStream vs = new DiscreteStream(0,1);
vs.setDataSource(dataSource);

//Create our nested graph
Bit in = new Bit();
Bit out = new Bit();
FactorGraph ng = new FactorGraph(in,out);
ng.addFactor(eq,in,out);

//Create our main factor graph
FactorGraph fg = new FactorGraph();

//Build the repeated graph
int bufferSize = 2;
FactorGraphStream fgs = fg.addRepeatedFactorWithBufferSize(ng, 
		bufferSize,vs,vs.getSlice(2));


//Initialize our messages
fg.initialize();

while (true)
{
    //Solve the current time step
    fg.solveOneStep();
    
    //Get the belief for the first variable
    double [] belief = ((double[])vs.get(2).getBeliefObject());
    		System.out.println(Arrays.toString(belief));

    if (fg.hasNext())
    	fg.advance();
    else
    	break;
}

//Initialize our messages
fg.initialize();
fg.setNumSteps(2);

while (true)
{
    //Solve the current time step
    fg.continueSolve(); //This method is need to avoid initialization
    
    //Get the belief for the first variable
    //Get the belief for the first variable
    double [] belief = ((double[])vs.get(2).getBeliefObject());
    		System.out.println(Arrays.toString(belief));

    if (fg.hasNext())
    	fg.advance();
    else
    	break;
}

	}

}
