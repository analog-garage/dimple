package com.analog.lyric.dimple.model;

public class RealJoint extends VariableBase 
{
	public RealJoint(int size) 
	{
		this(new RealJointDomain(size));
	}
	
	public RealJoint(RealJointDomain domain) 
	{
		this(NodeId.getNext(), domain,"RealJoint");
	}
	
	public RealJoint(int id, RealJointDomain domain, String modelerClassName) 
	{
		super(id,modelerClassName,domain);
	}

}
