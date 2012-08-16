/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.NodeId;

/*
 * This proxy wraps the Discrete class
 */
public class PDiscreteVariable extends PVariableBase
{
	public PDiscreteVariable(Discrete mvar)
	{
		_variable = mvar;
	}
	
	
	public void setInput(double... input) 
	{
    	if (getModelerObject().getParentGraph() != null && getModelerObject().getParentGraph().isSolverRunning()) 
    		throw new DimpleException("No changes allowed while the solver is running.");

		((Discrete)_variable).setInput(input.clone());
	}
	


	public PDiscreteVariable(PDiscreteVariable vOther)
	{
		_variable = vOther._variable;
	}
	
	public PDiscreteVariable(DiscreteDomain domain) 
	{
		if(domain.getElements().length < 1)
		{
			throw new DimpleException(String.format("ERROR Variable domain length %d must be at least 2", domain.getElements().length));
		}

		//This code is REALLY slow. 
		//Commenting out. 
		//We want poly-morphic re-creation of objects, but 
		//we'll need a better way to do it. 
		//String name = this.getClass().getName();
		//String [] result = name.split("\\.");
		//name = result[result.length-1];
		
		_variable = new Discrete(NodeId.getNext(), domain, "Variable");
		//_variable = new Discrete(domain, "Variable");
	}
	
	protected Object clone()
	{
		PDiscreteVariable vCopy = new PDiscreteVariable((Discrete)_variable);
		return vCopy;
	}

	public double[] getInput() 
	{
		return (double[])_variable.getInputObject();
	}


	public double[] getBelief() 
	{
		return (double[])_variable.getBeliefObject();
	}
	

	/*
	public String getDomainString() 
	{
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Domain size:%d\n", getDomain().length));
		for(int i = 0; i < getDomain().length; ++i)
		{
			sb.append(String.format("\tidx:%d  entry[%s]  inp[%s]  blf[%s]\n"
					, i
					, getDomain()[i].toString()
					, ((Double)getInput()[i]).toString()
					, ((Double)getBelief()[i]).toString()));
		}
		return sb.toString();
	}
	*/
	
	@Override
	public boolean isDiscrete() 
	{
		// TODO Auto-generated method stub
		return true;
	}


	@Override
	public boolean isReal() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLabel(String name) 
	{
		_variable.setLabel(name);
	}
	
	public boolean isJoint()
	{
		return false;
	}

}
