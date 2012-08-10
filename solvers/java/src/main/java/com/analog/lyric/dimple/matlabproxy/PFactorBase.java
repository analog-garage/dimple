package com.analog.lyric.dimple.matlabproxy;

/*
 * Both Factors and FactorGraphs inherit from this.
 */
public abstract class PFactorBase implements IPNode 
{
	@Override
	public boolean isFactor() 
	{
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean isVariable() 
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isGraph() 
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	abstract public boolean isDiscrete();


}
