package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.NodeId;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.RealJointDomain;

public class PRealJointVariable extends PVariableBase
{

	public PRealJointVariable(RealJoint rj)
	{
		_variable = rj;
	}

	public PRealJointVariable(PRealJointVariable vOther)
	{
		_variable = vOther._variable;
	}
	
	
	public PRealJointVariable(String name, PRealJointDomain domain) 
	{
		_variable = new RealJoint(NodeId.getNext(),(RealJointDomain)domain.getModelerObject(),name);
	}
	
	@Override
	public boolean isDiscrete() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReal() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	
	public void setInput(Object input) 
	{
    	if (getModelerObject().getParentGraph() != null && getModelerObject().getParentGraph().isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");


		_variable.setInputObject(input);
	}
	
	public Object getBelief() 		// Leaves open the format of the beliefs for a particular solver
	{
		return _variable.getBeliefObject();
	}
		
	public boolean isJoint()
	{
		return true;
	}

}
