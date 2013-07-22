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
	
	public void setRepresentation(Representation representation);
	
	boolean sparsify();
}
