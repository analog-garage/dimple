package com.analog.lyric.dimple.solvers.minsum;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;


public class SFactorGraph extends SFactorGraphBase 
{
	protected double _damping = 0;

	//private HashMap<double[],double[]> _old2newValues = new HashMap<double[],double[]>();

	public SFactorGraph(com.analog.lyric.dimple.model.FactorGraph factorGraph) 
	{
		super(factorGraph);
	}


	/*
	public ISolverTableFactor createTableFactor(com.lyricsemi.dimple.model.TableFactor factor)  
	{
		return new STableFactor(factor);
	}
	*/

	public ISolverVariable createVariable(com.analog.lyric.dimple.model.VariableBase var)  
	{
		if (!var.getDomain().isDiscrete())
			throw new DimpleException("only support discrete variables");
		
		return new SVariable(var);
	}


	@Override
	public void initialize() 
	{
	}

	@Override
	public ISolverFactor createFactor(Factor factor)  
	{
		return new STableFactor(factor);

	}
	
	/*
	 * Set the global solver damping parameter.  We have to go through all factor graphs
	 * and update the damping parameter on all existing table functions in that graph.
	 */
	public void setDamping(double damping) 
	{		
		_damping = damping;
		for (Factor f : _factorGraph.getNonGraphFactors())
		{
			STableFactor tf = (STableFactor)f.getSolver();
			setDampingForTableFunction(tf);
		}
	}
	
	public double getDamping()
	{
		return _damping;
	}

	/*
	 * This method applies the global damping parameter to all of the table function's ports
	 * and all of the variable ports connected to it.  This might cause problems in the future
	 * when we support different damping parameters per edge.
	 */
	protected void setDampingForTableFunction(STableFactor tf)
	{
		
		for (int i = 0; i < tf.getFactor().getPorts().size(); i++)
		{
			tf.setDamping(i,_damping);
			VariableBase var = (VariableBase)tf.getFactor().getPorts().get(i).getConnectedNode();
			for (int j = 0; j < var.getPorts().size(); j++)
			{
				SVariable svar = (SVariable)var.getSolver();
				svar.setDamping(j,_damping);
			}
		}		

	}

}
