/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

import java.util.ArrayList;
import java.util.Collection;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorBase;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.VariableBase;

public class PHelpers
{
	public static DiscreteDomain [] convertDomains(PDiscreteDomain [] domains)
	{
		DiscreteDomain [] retval = new DiscreteDomain[domains.length];
		
		
		for (int i = 0; i < domains.length; i++)
		{
			if (!domains[i].getModelerObject().isDiscrete())
				throw new RuntimeException("ack");
			
			retval[i] = (DiscreteDomain)domains[i].getModelerObject();
		}
	
		
		return retval;
	}
	
	static public PVariableVector convertToVariableVector(PVariableBase[] variables)
	{
		boolean isDiscrete = false;
		boolean allSame = true;

		isDiscrete = variables[0].isDiscrete();

		for (int i = 1; i < variables.length; i++)
		{
			if (variables[i].isDiscrete() != isDiscrete)
			{
				allSame = false;
				break;
			}
		}

		if (!allSame)
		{
			PVariableVector varVec = new PVariableVector(variables);
			
			return varVec;
		}
		else
		{

			if (variables[0] instanceof PRealVariable)							// Assumes all variables in the array are of the same class
			{
				PRealVariable[] ivars = new PRealVariable[variables.length];
				for (int i = 0; i < variables.length; i++)
					ivars[i] = new PRealVariable((PRealVariable)variables[i]);
				return new PRealVariableVector(ivars);
			}
			else if (variables[0] instanceof PRealJointVariable)
			{
				PRealJointVariable[] ivars = new PRealJointVariable[variables.length];
				for (int i = 0; i < variables.length; i++)
					ivars[i] = new PRealJointVariable((PRealJointVariable)variables[i]);
				return new PRealJointVariableVector(ivars);
			}
			else
			{
				PDiscreteVariable[] ivars = new PDiscreteVariable[variables.length];
				for (int i = 0; i < variables.length; i++)
					ivars[i] = new PDiscreteVariable((PDiscreteVariable)variables[i]);
				return new PDiscreteVariableVector(ivars);
			}
		}
	}

	public static PFactor [] convertObjectArrayToFactors(Object [] objects)
	{
		PFactor [] retval = new PFactor[objects.length];
		for (int i = 0; i < objects.length; i++)
		{
			retval[i] = (PFactor)objects[i];
		}
		return retval;
	}

	public static PVariableVector [] convertObjectArrayToVariableVectorArray(Object [] objects)
	{
		PVariableVector [] vars = new PVariableVector[objects.length];
		for (int i = 0; i < objects.length; i++)
			vars[i] = (PVariableVector)objects[i];
		return vars;
	}

	public static PFactorBase [] convertToFactors(FactorBase [] functions) 
	{
		PFactorBase [] factors = new PFactorBase[functions.length];
		for (int i = 0; i < functions.length; i++)
		{
			if(functions[i] instanceof DiscreteFactor)
			{
				factors[i] = new PDiscreteFactor((DiscreteFactor)functions[i]);    		    			    			
			}
			else if (functions[i] instanceof FactorGraph)
			{
				factors[i] = new PFactorGraph((FactorGraph)functions[i]);
			}
			else
			{
				factors[i] = new PFactor((Factor)functions[i]);    		    			
			}
		}
		return factors;
	}
	public static Factor[] convertToFactors(PFactor [] functions)
	{
		Factor[] factors = new Factor[functions.length];
		for (int i = 0; i < functions.length; i++)
		{
			factors[i] = functions[i].getModelerObject();
		}

		return factors;
	}	
	public static PFactorBase [] convertFactorBaseListToFactors(Collection<FactorBase> vbs) 
	{
		return convertToFactors(vbs.toArray(new FactorBase[0]));
	}	
	
	public static PFactorBase [] convertFactorListToFactors(Collection<Factor> vbs) 
	{
		return convertToFactors(vbs.toArray(new FactorBase[0]));
	}	

	@SuppressWarnings("unchecked")
	public static Object [] convertToMVariablesAndConstants(Object [] vars)
	{
		@SuppressWarnings("rawtypes")
		ArrayList alVars = new ArrayList();
    	
    	for (int i = 0; i < vars.length; i++)
    	{
    		if (vars[i] instanceof PVariableBase)
    		{
    			alVars.add(((PVariableBase)vars[i]).getModelerObject());
    		}
    		else if (vars[i] instanceof PVariableVector)
    		{
    			PVariableVector varVec = (PVariableVector)vars[i];
    			
    			for (int j = 0; j < varVec.size(); j++)
    			{
    				alVars.add(varVec.getVariable(j).getModelerObject());
    			}
    		}
    		else
    		{
    			alVars.add(vars[i]);
    		}
    	}
    	
    	
		Object [] newvars = new Object[alVars.size()];
		
		for (int i = 0; i < newvars.length; i++)
		{
			newvars[i] = alVars.get(i);
		}
		
		return newvars;

	}
	
	public static VariableBase[] 		convertToMVariables(PVariableBase [] variables)
	{
		VariableBase [] vbs = new VariableBase[variables.length];
		for (int i = 0; i < variables.length; i++)
		{
			vbs[i] = (VariableBase) variables[i].getModelerObject();    		
		}
		return vbs;
	}

	public static PVariableBase convertToVariable(VariableBase v)
	{
		if (v instanceof Real)
			return new PRealVariable((Real)v);
		else if (v instanceof RealJoint)
			return new PRealJointVariable((RealJoint)v);
		else
			return new PDiscreteVariable((Discrete)v);
	}

	public static PVariableBase[] 		convertToVariables(VariableBase [] vbs)
	{
		PVariableBase [] variables = new PVariableBase[vbs.length];
		for (int i = 0; i < variables.length; i++)
		{
			variables[i] = convertToVariable(vbs[i]);
		}
		return variables;
	}
	public static PVariableBase[] 		convertToVariables(Collection<VariableBase> vbs)
	{
		return convertToVariables(vbs.toArray(new VariableBase[0]));
	}	

	public static Object wrapObject(INode node) 
	{
		if (node instanceof DiscreteFactor)
		{
			return new PDiscreteFactor((DiscreteFactor)node);
		}
		else if (node instanceof Factor)
		{
			return new PFactor((Factor)node);
		}
		else if (node instanceof Real)
		{
			return new PRealVariableVector(new PVariableBase[]{ new PRealVariable((Real)node)});
		}
		else if (node instanceof Discrete)
		{
			return new PDiscreteVariableVector(new PDiscreteVariable[]{new PDiscreteVariable((Discrete)node)});
		}
		else if (node instanceof FactorGraph)
		{
			return new PFactorGraph((FactorGraph)node);
		}
		else
			throw new DimpleException("unrecognized type");

	}
}
