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

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Model;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.gaussian.MultivariateMsg;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;

/*
 * The model factory creates variable vectors and FactorGraphs for MATLAB
 */
public class ModelFactory
{

	public MultivariateMsg createMultivariateMsg(double [] means, double [][] covar)
	{
		return new MultivariateMsg(means, covar);
	}

	public PRealJointVariableVector createRealJointVariableVector(String className, PRealJointDomain domain, int numEls) 
	{
		return new PRealJointVariableVector(className, domain, numEls);
	}

	public PDiscreteVariableVector createDiscreteVariableVector(String className, PDiscreteDomain domain, int numEls) 
	{
		return new PDiscreteVariableVector(className,domain,numEls);
	}
	// For backwards compatibility with MATLAB
	public PDiscreteVariableVector createVariableVector(String className, PDiscreteDomain domain, int numEls) 
	{
		return new PDiscreteVariableVector(className,domain,numEls);
	}

	public PRealJointDomain createRealJointDomain(Object [] realDomains)
	{
		return new PRealJointDomain(realDomains);
	}

	public PDiscreteDomain createDiscreteDomain(Object [] elements)
	{
		return new PDiscreteDomain(new DiscreteDomain(elements));
	}

	public PRealDomain createRealDomain(double lowerBound, double upperBound) 
	{
		return new PRealDomain(new RealDomain(lowerBound,upperBound));
	}

	public PVariableStreamBase createDiscreteStream(PDiscreteDomain domain, double numVars) 
	{
		return new PDiscreteStream(domain,(int)numVars);
	}

	public PVariableStreamBase createRealStream(PRealDomain domain, int numVars) 
	{
		return new PRealStream(domain,numVars);
	}

	public PVariableStreamBase createRealJointStream(PRealJointDomain domain, int numVars) 
	{
		return new PRealJointStream(domain, numVars);
	}

	public PTableFactorFunction createTableFactorFunction(String name, int [][] indices, double [] values, Object [] domains)
	{
		PDiscreteDomain [] dds = new PDiscreteDomain[domains.length];

		for (int i = 0; i < domains.length; i++)
		{
			dds[i] = (PDiscreteDomain)domains[i];
		}
		return new PTableFactorFunction(name,indices,values,dds);
	}

	public PFactorTable createFactorTable(Object table, Object [] domains)
	{
		PDiscreteDomain [] dds = new PDiscreteDomain[domains.length];

		for (int i = 0; i < domains.length; i++)
		{
			dds[i] = (PDiscreteDomain)domains[i];
		}

		return new PFactorTable(table,dds);
	}

	public PFactorTable createFactorTable(int [][] indices, double [] values, Object [] domains)
	{
		PDiscreteDomain [] dds = new PDiscreteDomain[domains.length];

		for (int i = 0; i < domains.length; i++)
		{
			dds[i] = (PDiscreteDomain)domains[i];
		}

		return new PFactorTable(indices,values,dds);
	}

	public PFactorTable createFactorTable(Object [] domains)
	{
		PDiscreteDomain [] dds = new PDiscreteDomain[domains.length];

		for (int i = 0; i < domains.length; i++)
		{
			dds[i] = (PDiscreteDomain)domains[i];
		}


		return new PFactorTable(dds);
	}



	public PRealVariableVector createRealVariableVector(String className, PRealDomain domain, FactorFunction input, int numEls) 
	{
		return new PRealVariableVector(className, domain, input, numEls);
	}


	// Create graph
	public PFactorGraphVector createGraph(Object [] vector) 
	{
		ArrayList<VariableBase> alVars = new ArrayList<VariableBase>();

		for (int i = 0; i < vector.length; i++)
		{
			PVariableVector tmp = (PVariableVector)vector[i];
			VariableBase [] vars = tmp.getVariableArray();
			for (int j = 0; j <vars.length; j++)
				alVars.add(vars[j]);
		}

		VariableBase [] input = new VariableBase[alVars.size()];
		alVars.toArray(input);
		FactorGraph f = new FactorGraph(input);

		return new PFactorGraphVector(f);
	}


	// Set solver

	public void setSolver(IFactorGraphFactory solver) 
	{
		Model.getInstance().setDefaultGraphFactory(solver);
	}

	public PDoubleArrayDataSource getDoubleArrayDataSource(double numVars)
	{
		return new PDoubleArrayDataSource((int)numVars);
	}
	
	public PDoubleArrayDataSink getDoubleArrayDataSink(double numVars)
	{
		return new PDoubleArrayDataSink((int)numVars);
	}

	public PMultivariateDataSource getMultivariateDataSource(double numVars)
	{
		return new PMultivariateDataSource((int)numVars);
	}
	
	public PMultivariateDataSink getMultivariateDataSink(double numVars)
	{
		return new PMultivariateDataSink((int)numVars);
	}

}
