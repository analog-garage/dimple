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

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorList;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class SFactorGraph extends SFactorGraphBase
{
	private double _damping = 0;
	private boolean _calculateDerivativeOfMessages = false;
	private FactorTable _currentFactorTable = null;

	public SFactorGraph(com.analog.lyric.dimple.model.FactorGraph factorGraph) 
	{
		super(factorGraph);
		
	}

	public ISolverFactor createCustomFactor(com.analog.lyric.dimple.model.Factor factor)  
	{
		String funcName = factor.getFactorFunction().getName();
		if (funcName.equals("finiteFieldMult"))
		{
			//VariableList variables = factor.getVariables();
			
			if (factor.getFactorFunction() instanceof FactorFunctionWithConstants)
				return new FiniteFieldConstMult(factor);
			else
				return new FiniteFieldMult(factor);
		}
		else if (funcName.equals("finiteFieldAdd"))
		{
			return new FiniteFieldAdd(factor);    		
		}
		else if (funcName.equals("finiteFieldProjection"))
		{
			return new FiniteFieldProjection(factor);
		}
		else
			throw new DimpleException("Not implemented");
	}
	
	/*

	public ISolverTableFactor createTableFactor(TableFactor factor) 
	{
		// TODO Auto-generated method stub
		STableFactor tf = new STableFactor(factor);
		setDampingForTableFunction(tf);
		return tf;
	}
	 */
	public ISolverVariable createVariable(VariableBase var)  
	{
		if (var.getModelerClassName().equals("FiniteFieldVariable"))
			return new FiniteFieldVariable(var);
		else
			return new SVariable(var);
	}

	@Override
	public boolean customFactorExists(String funcName) 
	{
		if (funcName.equals("finiteFieldMult"))
			return true;
		else if (funcName.equals("finiteFieldAdd"))
			return true;
		else if (funcName.equals("finiteFieldProjection"))
			return true;
		else
			return false;	
	}

	@Override
	public void initialize() 
	{

	}

	private static Random _rand = new Random();

	public static Random getRandom()
	{
		return _rand;
	}
	
	public void setSeed(long seed)
	{
		_rand = new Random(seed);
	}
	
	@Override
	public ISolverFactor createFactor(Factor factor)  
	{
		if (customFactorExists(factor.getFactorFunction().getName()))
		{
			return createCustomFactor(factor);
		}
		else
		{
	
			STableFactor tf = new STableFactor(factor);
			setDampingForTableFunction(tf);
			return tf;
		}
	}
	

	/*
	 * Set the global solver damping parameter.  We have to go through all factor graphs
	 * and update the damping parameter on all existing table functions in that graph.
	 */
	public void setDamping(double damping) 
	{		
		_damping = damping;
		for (Factor f : _factorGraph.getNonGraphFactors())
		{
			STableFactor tf = (STableFactor)f.getSolver();
			setDampingForTableFunction(tf);
		}
	}
	
	public double getDamping()
	{
		return _damping;
	}

	/*
	 * This method applies the global damping parameter to all of the table function's ports
	 * and all of the variable ports connected to it.  This might cause problems in the future
	 * when we support different damping parameters per edge.
	 */
	protected void setDampingForTableFunction(STableFactor tf)
	{
		
		for (int i = 0; i < tf.getFactor().getPorts().size(); i++)
		{
			tf.setDamping(i,_damping);
			VariableBase var = (VariableBase)tf.getFactor().getPorts().get(i).getConnectedNode();
			for (int j = 0; j < var.getPorts().size(); j++)
			{
				SVariable svar = (SVariable)var.getSolver();
				svar.setDamping(j,_damping);
			}
		}		

	}
	
	@Override
	public void estimateParameters(FactorTable [] fts, int numRestarts, int numSteps, double stepScaleFactor)
	{
		//Create a mapping from factor tables to Factors.
		HashMap<FactorTable,ArrayList<Factor>> factorTable2Factors = new HashMap<FactorTable, ArrayList<Factor>>();
		FactorList factors = _factorGraph.getFactorsFlat();
		
		for (Factor f : factors)
		{
			FactorTable ft = f.getFactorTable();
			if (! factorTable2Factors.containsKey(ft))
				factorTable2Factors.put(ft, new ArrayList<Factor>());
			factorTable2Factors.get(ft).add(f);
		}
		
		_factorGraph.solve();
		double currentBetheFreeEnergy = _factorGraph.getBetheFreeEnergy();

		//TODO: be careful about whether I really replace this or not.
		//Save current values of factor tables
		FactorTable [] savedFts = saveFactorTables(fts);
		
		//Calculate energy
		for (int i = 0; i < numRestarts; i++)
		{
			//TODO: add ability to specify starting values.
			//Randomize starting values of factor tables
			for (int j = 0; j < fts.length; j++)
				fts[j].randomizeWeights(SFactorGraph.getRandom());
			
			
			//Estimate parameters for given starting value
			double newBetheFreeEnergy = estimateParametersFromStartValue(factorTable2Factors,numSteps,stepScaleFactor);
			_factorGraph.solve();
			newBetheFreeEnergy = _factorGraph.getBetheFreeEnergy();
			
			
			if (newBetheFreeEnergy < currentBetheFreeEnergy)
			{
				currentBetheFreeEnergy = newBetheFreeEnergy;
				savedFts = saveFactorTables(fts);
			}
			//Reestimate parameters using derivative
			//Calculate Energy
			//if  energy is less than min
			//   Save this set of Factor Tables
		}	
		for (int i = 0; i < fts.length; i++)
		{
			fts[i].copy(savedFts[i]);
		}
	}
	
	FactorTable [] saveFactorTables(FactorTable [] fts)
	{
		FactorTable [] savedFts = new FactorTable[fts.length];
		for (int i = 0; i < fts.length; i++)
			savedFts[i] = fts[i].copy();
		return savedFts;
	}

	public double  estimateParametersFromStartValue(HashMap<FactorTable,
			ArrayList<Factor>> factorTable2Factors, 
			int numReEstimations, double epsilon)
	{
		//for each factor table
		for (int j = 0; j < numReEstimations; j++)
		{
			//_factorGraph.solve();
			for (FactorTable ft : factorTable2Factors.keySet())
			{
				double [] weights = ft.getWeights();
			      //for each weight
				for (int i = 0; i < weights.length; i++)
				{
			           //calculate the derivative
					double derivative = calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(ft, 
							factorTable2Factors.get(ft), i);
					
			        //move the weight in that direction scaled by epsilon
					ft.changeWeight(i,weights[i] - weights[i]*derivative*epsilon);
				}
			}
		}
		//_factorGraph.solve();
		
		return _factorGraph.getBetheFreeEnergy();

	}
	
	public double calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(FactorTable ft,
			int weightIndex)
	{
		FactorList factors = _factorGraph.getFactorsFlat();
		ArrayList<Factor> factorArray = new ArrayList<Factor>();
		//TODO: Write commmon code
		for (Factor f : factors)
		{
			factorArray.add(f);
		}
		
		return calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(ft, factorArray, weightIndex);
	}

	
	public double calculateDerivativeOfBetheFreeEnergyWithRespectToWeight(FactorTable ft,
			ArrayList<Factor> factors,int weightIndex)
	{
		//BFE = InternalEnergy - BetheEntropy
		//InternalEnergy = Sum over all factors (Internal Energy of Factor) 
		//                   + Sum over all variables (Internal Energy of Variable)
		//BetheEntropy = Sum over all factors (BetheEntropy(factor)) 
		//                  + sum over all variables (BetheEntropy(variable)
		//So derivative of BFE = Sum over all factors that contain the weight
		//                                              (derivative of Internal Energy of Factor
		//                                              - derivative of BetheEntropy of Factor)
		//                        
		
		_currentFactorTable = ft;
		_calculateDerivativeOfMessages = true;
		
		for (Factor f : _factorGraph.getFactorsFlat())
			((STableFactor)f.getSolver()).initializeDerivativeMessages(ft.getWeights().length);
		for (VariableBase vb : _factorGraph.getVariablesFlat())
			((SVariable)vb.getSolver()).initializeDerivativeMessages(ft.getWeights().length);
		
		_factorGraph.solve();
		double result = 0;
		for (Factor f : _factorGraph.getFactorsFlat())
		{
			STableFactor stf = (STableFactor)f.getSolver();
			result += stf.calculateDerivativeOfInternalEnergyWithRespectToWeight(weightIndex);
			result -= stf.calculateDerivativeOfBetheEntropyWithRespectToWeight(weightIndex);
					
		}
		for (VariableBase v : _factorGraph.getVariablesFlat())
		{
			SVariable sv = (SVariable)v.getSolver();
			result += sv.calculateDerivativeOfInternalEnergyWithRespectToWeight(weightIndex);
			result += sv.calculateDerivativeOfBetheEntropyWithRespectToWeight(weightIndex);
		}
		
		_calculateDerivativeOfMessages = false;
		return result;
	}
	
	public FactorTable getCurrentFactorTable()
	{
		return _currentFactorTable;
	}
	
	public boolean getCalculateDerivateOfMessages()
	{
		return _calculateDerivativeOfMessages;
	}

}
