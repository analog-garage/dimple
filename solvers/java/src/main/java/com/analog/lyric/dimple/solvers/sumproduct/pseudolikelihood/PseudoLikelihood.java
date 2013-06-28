package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FactorList;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.solvers.core.ParameterEstimator;


public class PseudoLikelihood extends ParameterEstimator
{

	private double _scaleFactor;
	private HashMap<Factor,FactorInfo> _factor2factorInfo = new HashMap<Factor, FactorInfo>();
	private HashMap<VariableBase, VariableInfo> _var2varInfo = new HashMap<VariableBase, VariableInfo>();
	
	
	public static int [][] convertObjects2Indices(VariableBase [] vars, Object [][] data)
	{
		int [][] retval = new int[data.length][data[0].length];
		for (int i = 0; i < retval.length; i++)
		{
			for (int j = 0; j < retval[i].length; j++)
			{
				retval[i][j] = ((Discrete)vars[j]).getDiscreteDomain().getIndex(data[i][j]);
			}
		}
		
		return retval;
	}


	
	public PseudoLikelihood(FactorGraph fg, FactorTable[] tables,
			VariableBase [] vars, Object [][] data, double scaleFactor) 
	{
		this(fg,tables,vars,convertObjects2Indices(vars,data),scaleFactor);
	}

	public PseudoLikelihood(FactorGraph fg, FactorTable[] tables, 
			VariableBase [] vars, int [][] dataIndices, double scaleFactor) 
	{
		super(fg, tables, new Random());
		_scaleFactor = scaleFactor;

		FactorList fl = fg.getFactorsFlat();
		
		
		HashMap<VariableBase,Integer> var2index = new HashMap<VariableBase, Integer>();
		for (int i = 0; i < vars.length; i++)
			var2index.put(vars[i], i);
		
		for (Factor f : fl)
			_factor2factorInfo.put(f,new FactorInfo(f,var2index));

		HashSet<VariableBase> varsConnectedToFactors = new HashSet<VariableBase>();
		for (Factor f : fl)
			varsConnectedToFactors.addAll(f.getVariables());
		
		//for each variable
		for (VariableBase v : varsConnectedToFactors)
			_var2varInfo.put(v,VariableInfo.createVariableInfo(v, var2index));
		
		for (int i = 0; i < dataIndices.length; i++)
		{
			for (FactorInfo fi : _factor2factorInfo.values())
				fi.addSample(dataIndices[i]);
			for (VariableInfo vi : _var2varInfo.values())
				vi.addSample(dataIndices[i]);
		}

	}

	double [][] calculateGradient()
	{
		//Calculate the gradient
		FactorTable [] tables = getTables();
		HashMap<FactorTable,ArrayList<Factor>> table2factors = getTable2Factors();
		double [][] gradients = new double[tables.length][];
		
		for (int i = 0; i < tables.length; i++)
		{
			double [] weights = tables[i].getWeights();
			
			gradients[i] = new double[weights.length];
			
			for (int j = 0; j < weights.length; j++) 
			{
				double total = 0;
				ArrayList<Factor> factors = table2factors.get(tables[i]);
				for (int k = 0; k < factors.size(); k++)
				{
					Factor f = factors.get(k);
					VariableList vl = f.getVariables();
					FactorInfo fi = _factor2factorInfo.get(f);	
					int [][] indices = tables[i].getIndices();
					double impericalFactorD = fi.getDistribution().get(indices[j]);
					
					total += impericalFactorD;
					
					for (int m = 0; m < vl.size(); m++)
					{
						VariableBase vb = vl.getByIndex(m);
						int varIndex = indices[j][m]; 							
						VariableInfo vi = _var2varInfo.get(vb);
						Set<LinkedList<Integer>> uniqueSamples = vi.getUniqueSamples(varIndex);
						
						for (LinkedList<Integer> ll : uniqueSamples)
						{
							double prob = vi.getProb(varIndex, ll);
							total -= prob;
						}
						
					}
				}
				gradients[i][j] = total;
			}
		}
		return gradients;
	}
	
	public void applyGradient(double [][] gradient)
	{
		FactorTable [] tables = getTables();
		for (int i = 0; i < tables.length; i++)
		{
			double [] ws = tables[i].getWeights();
			double normalizer = 0;
			for (int j = 0; j < ws.length; j++)
			{
				double tmp = Math.log(ws[j]);
				tmp += gradient[i][j]*_scaleFactor;
				ws[j] = Math.exp(tmp);
				normalizer += ws[j];
			}
			for (int j = 0; j < ws.length; j++)
				ws[j] /= normalizer;
			
			tables[i].changeWeights(ws);
		}
	}
	
	@Override
	public void runStep(FactorGraph fg) 
	{
		double [][] gradient = calculateGradient();
		applyGradient(gradient);
	}
	
}