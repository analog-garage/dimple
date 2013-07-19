package com.analog.lyric.dimple.FactorFunctions.core;


public interface INewFactorTable extends INewFactorTableBase
{
	
	public static enum Representation
	{
		ENERGY(true, false),
		WEIGHT(false, true),
		BOTH(true, true);
	
		private final boolean _storeEnergies;
		private final boolean _storeWeights;
		
		private Representation(boolean storeEnergies, boolean storeWeights)
		{
			_storeEnergies = storeEnergies;
			_storeWeights = storeWeights;
		}
		
		public boolean storeEnergies()
		{
			return _storeEnergies;
		}
		
		public boolean storeWeights()
		{
			return _storeWeights;
		}
		
	}
	
	public void computeEnergies();
	public void computeWeights();
	
	boolean densify();
	
	public Representation getRepresentation();

	boolean isDense();
	
	public void setEnergyForArguments(double energy, Object ... arguments);
	public void setEnergyForIndices(double energy, int ... indices);
	public void setEnergyForLocation(double energy, int location);
	public void setEnergyForJointIndex(double energy, int jointIndex);

	public void setRepresentation(Representation representation);
	
	public void setWeightForArguments(double weight, Object ... arguments);
	public void setWeightForIndices(double weight, int ... indices);
	public void setWeightForLocation(double weight, int i);
	public void setWeightForJointIndex(double weight, int jointIndex);
	
	boolean sparsify();
}
