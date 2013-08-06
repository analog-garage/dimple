package com.analog.lyric.dimple.factorfunctions.core;


public interface INewFactorTable extends INewFactorTableBase
{
	
	public static enum Representation
	{
		DETERMINISTIC(0x00),
		
		DENSE_ENERGY(0x01),
		DENSE_WEIGHT(0x02),
		ALL_DENSE(0x03),
		SPARSE_ENERGY(0x04),
		ALL_ENERGY(0x05),
		SPARSE_ENERGY_DENSE_WEIGHT(0x06),
		NOT_SPARSE_WEIGHT(0x07),
		SPARSE_WEIGHT(0x08),
		DENSE_ENERGY_SPARSE_WEIGHT(0x09),
		ALL_WEIGHT(0x0A),
		NOT_SPARSE_ENERGY(0x0B),
		ALL_SPARSE(0x0C),
		NOT_DENSE_WEIGHT(0x0D),
		NOT_DENSE_ENERGY(0x0E),
		ALL(0x0F)
		;

		private final int _mask;
		
		private Representation(int mask)
		{
			_mask = mask;
		}
		
		private static Representation forMask(int mask)
		{
			// NOTE: assumes ordinal() == _mask
			return Representation.values()[mask];
		}
		
		int mask()
		{
			return _mask;
		}

		Representation difference(Representation that)
		{
			return forMask(_mask & ~that._mask);
		}
		
		Representation union(Representation that)
		{
			return forMask(_mask | that._mask);
		}
		
		public boolean hasDenseEnergy()
		{
			return (_mask & DENSE_ENERGY._mask) != 0;
		}
		
		public boolean hasDenseWeight()
		{
			return (_mask & DENSE_WEIGHT._mask) != 0;
		}

		public boolean hasEnergy()
		{
			return (_mask & ALL_ENERGY._mask) != 0;
		}
		
		public boolean hasWeight()
		{
			return (_mask & ALL_WEIGHT._mask) != 0;
		}
		
		public boolean hasSparse()
		{
			return (_mask & ALL_SPARSE._mask) != 0;
		}
		
		public boolean hasSparseEnergy()
		{
			return (_mask & SPARSE_ENERGY._mask) != 0;
		}
		
		public boolean hasSparseWeight()
		{
			return (_mask & SPARSE_WEIGHT._mask) != 0;
		}
		
		public boolean hasDense()
		{
			return (_mask & ALL_DENSE._mask) != 0;
		}
		
		public boolean isDeterministic()
		{
			return _mask == 0;
		}
	}

	public Representation getRepresentation();

	public void setRepresentation(Representation representation);
}
