package com.analog.lyric.dimple.solvers.sumproduct;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class SFactorGraph extends SFactorGraphBase
{
	private double _damping = 0;

	public SFactorGraph(com.analog.lyric.dimple.model.FactorGraph factorGraph) 
	{
		super(factorGraph);
		
	}

	public ISolverFactor createCustomFactor(com.analog.lyric.dimple.model.Factor factor)  
	{
		String funcName = factor.getFactorFunction().getName();
		if (funcName.equals("finiteFieldMult"))
		{
			//VariableList variables = factor.getVariables();
			
			if (factor.getFactorFunction() instanceof FactorFunctionWithConstants)
				return new FiniteFieldConstMult(factor);
			else
				return new FiniteFieldMult(factor);
		}
		else if (funcName.equals("finiteFieldAdd"))
		{
			return new FiniteFieldAdd(factor);    		
		}
		else if (funcName.equals("finiteFieldProjection"))
		{
			return new FiniteFieldProjection(factor);
		}
		else
			throw new DimpleException("Not implemented");
	}
	
	/*

	public ISolverTableFactor createTableFactor(TableFactor factor) 
	{
		// TODO Auto-generated method stub
		STableFactor tf = new STableFactor(factor);
		setDampingForTableFunction(tf);
		return tf;
	}
	 */
	public ISolverVariable createVariable(VariableBase var)  
	{
		if (var.getModelerClassName().equals("FiniteFieldVariable"))
			return new FiniteFieldVariable(var);
		else
			return new SVariable(var);
	}

	@Override
	public boolean customFactorExists(String funcName) 
	{
		if (funcName.equals("finiteFieldMult"))
			return true;
		else if (funcName.equals("finiteFieldAdd"))
			return true;
		else if (funcName.equals("finiteFieldProjection"))
			return true;
		else
			return false;	
	}

	@Override
	public void initialize() 
	{

	}

	@Override
	public ISolverFactor createFactor(Factor factor)  
	{
		if (customFactorExists(factor.getFactorFunction().getName()))
		{
			return createCustomFactor(factor);
		}
		else
		{
	
			STableFactor tf = new STableFactor(factor);
			setDampingForTableFunction(tf);
			return tf;
		}
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
