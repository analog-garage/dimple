package com.analog.lyric.dimple.FactorFunctions.core;


public interface INewFactorTable extends INewFactorTableBase
{
	
	public static enum Representation
	{
		BOTH,
		ENERGY,
		WEIGHT;
	}
	
	public void computeEnergies();
	public void computeWeights();
	
	boolean densify();
	
	public Representation getRepresentation();

	boolean isDense();
	
	public void setEnergy(double energy, int ... indices);
	public void setEnergy(int i, double energy);
	public void setEnergy(int[] indices, double energy);

	public void setRepresentation(Representation representation);
	
	public void setWeight(double weight, int ... indices);
	public void setWeight(int i, double weight);
	public void setWeight(int[] indices, double weight);
	
	boolean sparsify();
}
