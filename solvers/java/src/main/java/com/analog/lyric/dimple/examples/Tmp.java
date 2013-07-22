package com.analog.lyric.dimple.examples;

import java.util.Arrays;

import com.analog.lyric.dimple.FactorFunctions.NopFactorFunction;
import com.analog.lyric.dimple.FactorFunctions.Normal;
import com.analog.lyric.dimple.FactorFunctions.XorDelta;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.FactorFunctions.core.HybridSampledBPFactorFunction;
import com.analog.lyric.dimple.model.Bit;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FiniteFieldVariable;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Model;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.RealJoint;
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
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.solvers.gaussian.MultivariateMsg;
import com.analog.lyric.dimple.solvers.sumproduct.SFactorGraph;
import com.analog.lyric.dimple.solvers.sumproduct.STableFactor;
import com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood.PseudoLikelihood;
import com.analog.lyric.util.misc.MapList;

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

	
	public class GaussianAddFactorFunction extends HybridSampledBPFactorFunction
	{

	    public GaussianAddFactorFunction() 
	    {
	        super("GaussianAdd");
	    }

	    @Override
	    public double acceptanceRatio(int portIndex, Object... inputs) 
	    {
	        return 1;
	    }

	    @Override
	    public Object generateSample(int portIndex, Object... inputs) 
	    {
	        if (portIndex == 0)
	        {
		    double sum = 0;
		    for (int i = 0; i < inputs.length; i++)
	            {
	                sum += (Double)inputs[i];
	            }
	            return sum;
	         }
	         else
	         {
	             double sum = (Double)inputs[0];
	             for (int i = 1; i < inputs.length; i++)
	             {
	                 sum -= (Double)inputs[i];
	             }
	             return sum;
	         }
	    }
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		BitStream bs = new BitStream();
		DoubleArrayDataSource dads = null;
		MultivariateDataSource mds = new MultivariateDataSource();
		DoubleArrayDataSink dads = new DoubleArrayDataSink();
		
	}

}
