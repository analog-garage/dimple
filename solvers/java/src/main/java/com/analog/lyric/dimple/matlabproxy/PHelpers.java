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

import com.analog.lyric.collect.Supers;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.matlabproxy.repeated.IPDataSink;
import com.analog.lyric.dimple.matlabproxy.repeated.IPDataSource;
import com.analog.lyric.dimple.matlabproxy.repeated.PDoubleArrayDataSink;
import com.analog.lyric.dimple.matlabproxy.repeated.PDoubleArrayDataSource;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.factors.FactorList;
import com.analog.lyric.dimple.model.repeated.DoubleArrayDataSink;
import com.analog.lyric.dimple.model.repeated.DoubleArrayDataSource;
import com.analog.lyric.dimple.model.repeated.MultivariateDataSink;
import com.analog.lyric.dimple.model.repeated.MultivariateDataSource;
import com.analog.lyric.dimple.model.repeated.VariableStreamBase;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;

// TODO: how many of these functions are intended to be invoked from MATLAB? And how many don't need to be public?

public class PHelpers
{
	
	public static Variable [] convertToVariableArray(Object [] vlVectors)
	{
		ArrayList<Variable> al = new ArrayList<Variable>();
		for (Object o : vlVectors)
		{
			PVariableVector vec = (PVariableVector)o;
			for (Variable vb : vec.getVariableArray())
				al.add(vb);
		}
		
		Variable [] retval = new Variable[al.size()];
		return al.toArray(retval);
	}
	
	public static Node convertToNode(Object obj)
	{
		return convertToNode((PNodeVector)obj);
	}

	public static Node [] convertToNodeArray(Object nodeVector)
	{
		return convertToNodeArray((PNodeVector)nodeVector);
	}
	
	public static Node [] convertToNodeArray(PNodeVector nodeVector)
	{
		Node [] retval = new Node [nodeVector.size()];
		for (int i = 0; i < retval.length; i++)
			retval[i] = nodeVector.getModelerNode(i);
		return retval;
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
			
			retval[i] = domains[i].getModelerObject();
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
		if (nodes.length == 0)
			return new PFactorVector();
		
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
		Variable [] array = new Variable[vars.size()];
		vars.toArray(array);
		return convertToVariableVector(array);
		
	}
	
	@SuppressWarnings("deprecation")
	static public PVariableVector convertToVariableVector(Variable [] variables)
	{
		final int n = variables.length;
		if (n == 0)
			return new PVariableVector();

		// TODO: When VariableBase is removed, change this to Variable.class
		variables = Supers.narrowArrayOf(com.analog.lyric.dimple.model.variables.VariableBase.class, 1, variables);
		
		Class<?> commonVarClass = variables.getClass().getComponentType();
			
		if (Discrete.class.isAssignableFrom(commonVarClass))
		{
			return new PDiscreteVariableVector(variables);
		}
		else if (Real.class.isAssignableFrom(commonVarClass))
		{
			return new PRealVariableVector(variables);
		}
		else if (RealJoint.class.isAssignableFrom(commonVarClass))
		{
			return new PRealJointVariableVector(variables);
		}
		else
		{
			return new PVariableVector(variables);
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
	
	public static PNodeVector [][] extractVectorization(PNodeVector [] nodeVectors, int [][][] indices)
	{
		int numNodeVectorsPerAddFactor = indices.length;
		int numaddFactors = indices[0].length;
		
		PNodeVector [][] retval = new PNodeVector[numaddFactors][];
		
		for (int i = 0; i < indices.length; i++)
			if (indices[i].length != numaddFactors)
				throw new DimpleException("mismatch of variables sizes");
		
		for (int i = 0; i < numaddFactors; i++)
		{
			retval[i] = new PNodeVector[numNodeVectorsPerAddFactor];
				
			for (int j = 0; j < numNodeVectorsPerAddFactor; j++)
			{
				retval[i][j] = nodeVectors[j].getSlice(indices[j][i]);
			}
		}
		
		return retval;
	}
	
	public static int [][][] extractIndicesVectorized(Object [] indices)
	{
		int [][][] retval = new int[indices.length][][];
		for (int i = 0; i < indices.length; i++)
		{
			
			if (indices[i] instanceof Double)
			{
				int index = (int)(double)(Double)indices[i];
				retval[i] = new int[1][1];
				retval[i][0][0] = index;
			}
			else if (indices[i] instanceof double[][])
			{
				double [][] tmp = (double[][])indices[i];
				retval[i] = new int[tmp.length][tmp[0].length];
				for (int j = 0; j < tmp.length; j++)
					for (int k = 0; k < tmp[0].length; k++)
						retval[i][j][k] = (int)tmp[j][k];
			}
			else if (indices[i] instanceof double[])
			{
				double [] tmp = (double[])indices[i];
				retval[i] = new int[tmp.length][];
				for (int j= 0; j < tmp.length; j++)
					retval[i][j] = new int[]{(int)tmp[j]};
			}
			else
			{
				throw new DimpleException("unsupported indices format: " + indices[i]);
			}
		}
		return retval;
	}
	
	// For non-vectorized node, second index dimension are indices themselves, rather than an array for each vector element
	public static int[][][] extractIndicesNonVectorized(Object[] indices)
	{
		int [][][] retval = new int[indices.length][][];
		for (int i = 0; i < indices.length; i++)
		{
			if (indices[i] instanceof Double)
			{
				int index = (int)(double)(Double)indices[i];
				retval[i] = new int[1][1];
				retval[i][0][0] = index;
			}
			else if (indices[i] instanceof double[])
			{
				double[] tmp = (double[])indices[i];
				retval[i] = new int[1][tmp.length];
				for (int k= 0; k < tmp.length; k++)
					retval[i][0][k] = (int)tmp[k];
			}
			else
			{
				throw new DimpleException("unsupported indices format: " + indices[i]);
			}
		}
		return retval;
	}
	
	public static IPDataSource getDataSources(VariableStreamBase [] streams)
	{
		if (streams[0].getDataSource() instanceof DoubleArrayDataSource)
		{
			DoubleArrayDataSource [] dads = new DoubleArrayDataSource[streams.length];
			for (int i = 0; i < dads.length; i++)
					dads[i] = (DoubleArrayDataSource)streams[i].getDataSource();
			return new PDoubleArrayDataSource(dads);
		}
		else if (streams[0].getDataSource() instanceof MultivariateDataSource)
		{
			throw new DimpleException("not currently supported");
			
		}
		else
			throw new DimpleException("not currently supported");
		
		
	}

	public static IPDataSink getDataSinks(VariableStreamBase [] streams)
	{
		if (streams[0].getDataSink() instanceof DoubleArrayDataSink)
		{
			DoubleArrayDataSink [] dads = new DoubleArrayDataSink[streams.length];
			for (int i = 0; i < dads.length; i++)
					dads[i] = (DoubleArrayDataSink)streams[i].getDataSink();
			return new PDoubleArrayDataSink(dads);
		}
		else if (streams[0].getDataSink() instanceof MultivariateDataSink)
		{
			throw new DimpleException("Multivariate not currently supported");
			
		}
		else
			throw new DimpleException("other not currently supported " + streams[0].getDataSource() + " end");
		
		
	}

	
}
