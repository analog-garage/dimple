package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class SVariableBase implements ISolverVariable
{
	protected VariableBase _var;
	
	public SVariableBase(VariableBase var)
	{
		_var = var;
	}
	
	public void connectPort(Port p) 
	{
		
	}

	
	public void initialize() 
	{
		
	}

	public void remove(Factor factor)
	{
		
	}
	
	
	public ISolverFactorGraph getParentGraph()
	{
		ISolverFactorGraph graph = null;
		FactorGraph mgraph = _var.getParentGraph();
		if(mgraph != null)
		{
			graph = mgraph.getSolver();
		}
		return graph;
	}
	public ISolverFactorGraph getRootGraph()
	{
		ISolverFactorGraph graph = null;
		FactorGraph mgraph = _var.getRootGraph();
		if(mgraph != null)
		{
			graph = mgraph.getSolver();
		}
		return graph;
	}
	

	@Override
	public double getEnergy()  
	{
		throw new DimpleException("not supported");
	}


	@Override
	public void setGuess(Object guess)  
	{
		throw new DimpleException("not supported");
	}


	@Override
	public Object getGuess()  
	{
		throw new DimpleException("not supported");
	}


	@Override
	public void update()  
	{
		for (int i = 0; i < _var.getPorts().size(); i++)
		{
			updateEdge(i);
		}
		
	}

	
}
