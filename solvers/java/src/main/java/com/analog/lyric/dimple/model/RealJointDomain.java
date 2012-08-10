package com.analog.lyric.dimple.model;

import java.util.Arrays;

public class RealJointDomain extends Domain 
{
	private RealDomain [] _domains;
	
	public RealJointDomain(int size)
	{
		_domains = new RealDomain[size];
		
		for (int i = 0; i < size; i++)
			_domains[i] = new RealDomain();
	}
	
	public RealJointDomain(RealDomain ... domains)
	{
		_domains = domains.clone();
	}
	
	@Override
	public boolean isDiscrete() 
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public RealDomain [] getRealDomains()
	{
		return _domains;
	}
	
	public int getNumVars()
	{
		return _domains.length;
	}
	
	public boolean isJoint()
	{
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this._domains);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof RealJointDomain))
			return false;

		RealJointDomain other = (RealJointDomain) obj;
		if (!Arrays.equals(this._domains, other._domains))
			return false;
		return true;
	}
}
