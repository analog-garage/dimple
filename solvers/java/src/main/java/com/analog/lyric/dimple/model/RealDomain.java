package com.analog.lyric.dimple.model;

public class RealDomain extends Domain
{
	protected double _lowerBound = Double.NEGATIVE_INFINITY;
	protected double _upperBound = Double.POSITIVE_INFINITY;
	
	public RealDomain() {}
	public RealDomain(double[] domain)  {this(domain[0], domain[1]);}
	public RealDomain(double lower, double upper) 
	{
		if (lower > upper) throw new DimpleException("Upper bound must be greater than lower bound");
		_lowerBound = lower;
		_upperBound = upper;
	}
	public double getLowerBound() {return _lowerBound;}
	public double getUpperBound() {return _upperBound;}
	@Override
	public boolean isDiscrete() 
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean equals(Object other)
	{
		if (this == other)
			return true;

		if (!(other instanceof RealDomain))
			return false;

		RealDomain rother = (RealDomain)other;
		
		return _lowerBound == rother._lowerBound && _upperBound == rother._upperBound;
		
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(this._lowerBound);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this._upperBound);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	public String toString()
	{
		return "RealDomain: " + _lowerBound + ", " + _upperBound;
	}
	
	public boolean isJoint()
	{
		return false;
	}

}
