package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.RealJointDomain;

public class PRealJointDomain extends PDomain 
{
	public PRealJointDomain(RealJointDomain domain)
	{
		super(domain);
	}

	public PRealJointDomain(Object [] domains) 
	{		
		super(new RealJointDomain(unwrapRealDomains(domains)));
		// TODO Auto-generated constructor stub
	}
	
	private static RealDomain [] unwrapRealDomains(Object [] domains)
	{
		RealDomain [] rdomains = new RealDomain[domains.length];
		for (int i = 0; i < rdomains.length; i++)
			rdomains[i] = (RealDomain) ((PRealDomain)domains[i]).getModelerObject();
		return rdomains;
		
	}

	public int getNumVars()
	{
		return ((RealJointDomain)getModelerObject()).getNumVars();
	}
}
