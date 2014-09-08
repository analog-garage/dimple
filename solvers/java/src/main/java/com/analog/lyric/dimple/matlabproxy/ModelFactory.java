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

import com.analog.lyric.dimple.matlabproxy.repeated.PDiscreteStream;
import com.analog.lyric.dimple.matlabproxy.repeated.PDoubleArrayDataSink;
import com.analog.lyric.dimple.matlabproxy.repeated.PDoubleArrayDataSource;
import com.analog.lyric.dimple.matlabproxy.repeated.PFactorFunctionDataSource;
import com.analog.lyric.dimple.matlabproxy.repeated.PMultivariateDataSink;
import com.analog.lyric.dimple.matlabproxy.repeated.PMultivariateDataSource;
import com.analog.lyric.dimple.matlabproxy.repeated.PRealJointStream;
import com.analog.lyric.dimple.matlabproxy.repeated.PRealStream;
import com.analog.lyric.dimple.matlabproxy.repeated.PVariableStreamBase;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Model;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.multithreading.ThreadPool;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import org.eclipse.jdt.annotation.Nullable;

/*
 * The model factory creates variable vectors and FactorGraphs for MATLAB
 */
public class ModelFactory
{

	public MultivariateNormalParameters createMultivariateNormalParameters(double[] mean, double[][] covariance)
	{
		return new MultivariateNormalParameters(mean, covariance);
	}

	public NormalParameters createNormalParameters(double mean, double precision)
	{
		return new NormalParameters(mean, precision);
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
	
	public PFiniteFieldVariableVector createFiniteFieldVariableVector(PFiniteFieldDomain domain, int numEls)
	{
		return new PFiniteFieldVariableVector(domain, numEls);
	}

	public PRealJointDomain createRealJointDomain(Object [] realDomains)
	{
		return new PRealJointDomain(realDomains);
	}

	public PDiscreteDomain createDiscreteDomain(Object [] elements)
	{
		return new PDiscreteDomain(DiscreteDomain.create(elements));
	}

	public PFiniteFieldDomain createFiniteFieldDomain(int primitivePolynomial)
	{
		return new PFiniteFieldDomain(DiscreteDomain.finiteField(primitivePolynomial));
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



	public PRealVariableVector createRealVariableVector(String className, PRealDomain domain, int numEls)
	{
		return new PRealVariableVector(className, domain, numEls);
	}


	// Create graph
	public PFactorGraphVector createGraph(Object [] vector)
	{
		ArrayList<Variable> alVars = new ArrayList<Variable>();

		for (int i = 0; i < vector.length; i++)
		{
			PVariableVector tmp = (PVariableVector)vector[i];
			Variable [] vars = tmp.getVariableArray();
			for (int j = 0; j <vars.length; j++)
				alVars.add(vars[j]);
		}

		Variable [] input = new Variable[alVars.size()];
		alVars.toArray(input);
		FactorGraph f = new FactorGraph(input);

		return new PFactorGraphVector(f);
	}

	public PDimpleEventLogger createEventLogger()
	{
		return new PDimpleEventLogger();
	}
	
	// Set solver

	public void setSolver(@Nullable IFactorGraphFactory<?> solver)
	{
		Model.getInstance().setDefaultGraphFactory(solver);
	}

	public PFactorFunctionDataSource getFactorFunctionDataSource(double numVars)
	{
		return new PFactorFunctionDataSource((int)numVars);
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

	public PMultiplexerCPD getMultiplexerCPD(Object [] zDomains)
	{
		Object [][] domains = new Object[zDomains.length][];
		
		for (int i = 0; i < zDomains.length; i++)
		{
			domains[i] = (Object[])zDomains[i];
		}
		

		return new PMultiplexerCPD(domains);
	}
	
	public PMultiplexerCPD getMultiplexerCPD(Object [] domain, double numZs)
	{
		return new PMultiplexerCPD(domain,(int)numZs);
	}

	public void setNumThreadsToDefault()
	{
		ThreadPool.setNumThreadsToDefault();
	}

	
	public void setNumThreads(int numThreads)
	{
		ThreadPool.setNumThreads(numThreads);
	}
	public int getNumThreads()
	{
		return ThreadPool.getNumThreads();
	}
	
	public PLogger getLogger()
	{
		return PLogger.INSTANCE;
	}
}
