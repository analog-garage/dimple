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
import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorBase;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FactorList;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Node;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.RealJointDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;

public class PHelpers
{
	public static Node convertToNode(Object obj)
	{
		return convertToNode((PNodeVector)obj);
	}
	
	public static Node convertToNode(PNodeVector nodeVector)
	{
		if (nodeVector.size() != 1)
			throw new DimpleException("only works with 1 node currently");
		return nodeVector.getModelerNode(0);
	}
	
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
	
	public static PDomain wrapDomain(Domain d)
	{
		if (d instanceof RealJointDomain)
			return new PRealJointDomain((RealJointDomain)d);
		else if (d instanceof DiscreteDomain)
			return new PDiscreteDomain((DiscreteDomain)d);
		else if (d instanceof RealDomain)
			return new PRealDomain((RealDomain)d);
		else
			return new PDomain(d);
	}
	
	public static PFactorVector convertToFactorVector(Node [] nodes)
	{
		if (nodes[0] instanceof DiscreteFactor)
			return new PDiscreteFactorVector(nodes);
		else if (nodes[0] instanceof FactorGraph)
			return new PFactorGraphVector(nodes);
		else
			return new PFactorVector(nodes);
	}
	
	public static PFactorVector [] convertToFactorVector(FactorList factors)
	{
		return convertFactorListToFactors(factors);
	}

	
	public static PVariableVector convertToVariableVector(VariableList vars) 
	{
		VariableBase [] array = new VariableBase[vars.size()];
		vars.toArray(array);
		return convertToVariableVector(array);
		
	}
	
	static public PVariableVector convertToVariableVector(VariableBase [] variables)
	{
		boolean isDiscrete = false;
		boolean allSame = true;

		isDiscrete = (variables[0] instanceof Discrete);

		for (int i = 1; i < variables.length; i++)
		{
			if (  (variables[i] instanceof Discrete) != isDiscrete)
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

			if (variables[0] instanceof Real)							// Assumes all variables in the array are of the same class
			{
				Real[] ivars = new Real[variables.length];
				for (int i = 0; i < variables.length; i++)
					ivars[i] = (Real)variables[i];
				return new PRealVariableVector(ivars);
			}
			else if (variables[0] instanceof RealJoint)
			{
				RealJoint[] ivars = new RealJoint[variables.length];
				for (int i = 0; i < variables.length; i++)
					ivars[i] = (RealJoint)variables[i];
				return new PRealJointVariableVector(ivars);
			}
			else
			{
				Discrete[] ivars = new Discrete[variables.length];
				for (int i = 0; i < variables.length; i++)
					ivars[i] = (Discrete)variables[i];
				return new PDiscreteVariableVector(ivars);
			}
		}
	}

	public static Factor [] convertObjectArrayToFactors(Object [] objects)
	{
		Factor [] retval = new Factor[objects.length];
		for (int i = 0; i < objects.length; i++)
		{
			Node n = convertToNode(objects[i]);
			retval[i] = (Factor)n; 
		}
		return retval;
	}
	
	

	public static PNodeVector [] convertObjectArrayToNodeVectorArray(Object [] objects)
	{
		PNodeVector [] vars = new PNodeVector[objects.length];
		for (int i = 0; i < objects.length; i++)
			vars[i] = (PNodeVector)objects[i];
		return vars;
	}
	
	
	public static PVariableVector [] convertObjectArrayToVariableVectorArray(Object [] objects)
	{
		PVariableVector [] vars = new PVariableVector[objects.length];
		for (int i = 0; i < objects.length; i++)
			vars[i] = (PVariableVector)objects[i];
		return vars;
	}

	public static PFactorVector [] convertToFactors(FactorBase [] functions) 
	{
		PFactorVector [] factors = new PFactorVector[functions.length];
		for (int i = 0; i < functions.length; i++)
			factors[i] = (PFactorVector)wrapObject(functions[i]);
		return factors;
	}
	
//	public static Factor[] convertToFactors(PFactor [] functions)
//	{
//		Factor[] factors = new Factor[functions.length];
//		for (int i = 0; i < functions.length; i++)
//		{
//			factors[i] = functions[i].getModelerObject();
//		}
//
//		return factors;
//	}	

//	public static PFactorBase [] convertFactorBaseListToFactors(Collection<FactorBase> vbs) 
//	{
//		return convertToFactors(vbs.toArray(new FactorBase[0]));
//	}
	
//	
	public static PFactorVector [] convertFactorListToFactors(Collection<Factor> vbs) 
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
    		if (vars[i] instanceof PVariableVector)
    		{
    			PVariableVector varVec = (PVariableVector)vars[i];
    			
    			for (int j = 0; j < varVec.size(); j++)
    			{
    				alVars.add(varVec.getModelerNode(j));
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
//
//	public static VariableBase[] convertToMVariables(IPNode [] variables)
//	{
//		VariableBase [] vbs = new VariableBase[variables.length];
//		for (int i = 0; i < variables.length; i++)
//		{
//			vbs[i] = (VariableBase) variables[i].getModelerObject();    		
//		}
//		return vbs;
//		
//	}
//	
//
//	public static PVariableBase convertToVariable(VariableBase v)
//	{
//		if (v instanceof Real)
//			return new PRealVariable((Real)v);
//		else if (v instanceof RealJoint)
//			return new PRealJointVariable((RealJoint)v);
//		else
//			return new PDiscreteVariable((Discrete)v);
//	}
//
//	public static PVariableBase[] 		convertToVariables(VariableBase [] vbs)
//	{
//		PVariableBase [] variables = new PVariableBase[vbs.length];
//		for (int i = 0; i < variables.length; i++)
//		{
//			variables[i] = convertToVariable(vbs[i]);
//		}
//		return variables;
//	}
//	public static PVariableBase[] 		convertToVariables(Collection<VariableBase> vbs)
//	{
//		return convertToVariables(vbs.toArray(new VariableBase[0]));
//	}	
//
	public static PNodeVector wrapObject(INode node) 
	{
		if (node instanceof DiscreteFactor)
		{
			return new PDiscreteFactorVector((DiscreteFactor)node);
		}
		else if (node instanceof Factor)
		{
			return new PFactorVector((Factor)node);
		}
		else if (node instanceof Real)
		{
			return new PRealVariableVector((Real)node);
		}
		else if (node instanceof Discrete)
		{
			return new PDiscreteVariableVector((Discrete)node);
		}
		else if (node instanceof FactorGraph)
		{
			return new PFactorGraphVector((FactorGraph)node);
		}
		else
			throw new DimpleException("unrecognized type");

	}
}
