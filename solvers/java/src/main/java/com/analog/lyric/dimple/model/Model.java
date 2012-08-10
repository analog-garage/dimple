package com.analog.lyric.dimple.model;

import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;

public class Model
{
	IFactorGraphFactory _defaultGraphFactory;

	private Model()
	{
		try
		{
			restoreDefaultDefaultGraphFactory();
		}
		catch(Exception e)
		{
			_defaultGraphFactory = null;
		}
	}


	private static class ModelerHolder
	{
		static final Model INSTANCE = new Model();
	}

	public static Model getInstance()
	{
		return ModelerHolder.INSTANCE;
	}
	
	public void restoreDefaultDefaultGraphFactory() 
	{
		setDefaultGraphFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
	}
	
	public IFactorGraphFactory getDefaultGraphFactory()
	{
		return _defaultGraphFactory;
	}

	public void setDefaultGraphFactory(IFactorGraphFactory graphFactory) 
	{
		_defaultGraphFactory = graphFactory;
	}
}
