package com.analog.lyric.dimple.FactorFunctions.core;

import com.analog.lyric.dimple.model.DiscreteDomain;

public interface INewFactorTable
{
	
	public int argumentsToDenseLocation(Object...arguments);
	
	public void computeEnergies();
	public void computeWeights();
	
	public int domainCount();
	
	public DiscreteDomain getDomain(int i);
	public int getDomainSize(int i);
	
	public double getEnergy(int i);
	public double getEnergy(int ... indices);
	
	public double getWeight(int i);
	public double getWeight(int ... indices);
		
	public void setWeight(int i, double weight);
	public void setWeight(double weight, int ... indices);
	public void setWeight(int[] indices, double weight);
	
	public int indicesToDenseLocation(int ... indices);
	public void indicesFromDenseLocation(int location, int[] indices);
	
	public int indicesToLocation(int ... indices);
	public void indicesFromLocation(int location, int[] indices);
	
	public int denseSize();
	public int size();
	
	int locationToDenseLocation(int location);
	int locationFromDenseLocation(int denseLocation);
	
	void makeDense();
	void makeSparse();
	
	boolean isDense();
}
