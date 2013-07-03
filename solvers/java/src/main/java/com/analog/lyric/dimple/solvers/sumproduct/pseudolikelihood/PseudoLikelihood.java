package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
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
	private int [][] _data;
	private HashMap<VariableBase,Integer> _var2index = new HashMap<VariableBase, Integer>();
	private VariableBase [] _vars;
	
	/*
	 * Used to convert input data from domain values to indices into the domain lists.
	 */
	public int [][] convertObjects2Indices(Object [][] data)
	{
		int [][] retval = new int[data.length][data[0].length];
		for (int i = 0; i < retval.length; i++)
		{
			for (int j = 0; j < retval[i].length; j++)
			{
				retval[i][j] = _vars[j].asDiscreteVariable().getDiscreteDomain().getIndex(data[i][j]);
			}
		}
		
		return retval;
	}

	
	
//	public PseudoLikelihood(FactorGraph fg, FactorTable[] tables,
//			VariableBase [] vars, Object [][] data, double scaleFactor) 
//	{
//		this(fg,tables,vars,convertObjects2Indices(vars,data),scaleFactor);
//	}
	
	// int [][] dataIndices, double scaleFactor
	
	public PseudoLikelihood(FactorGraph fg, FactorTable[] tables, 
			VariableBase [] vars) 
	{
		super(fg, tables, new Random());
	
		
		_vars = vars;
		
		
		FactorList fl = fg.getFactorsFlat();
		
		//create a mapping from the input variabes to the index into the input variable array
		for (int i = 0; i < vars.length; i++)
			_var2index.put(vars[i], i);
		
		//Factor infos will be used to store joint imperical distributions over the factor
		for (Factor f : fl)
			_factor2factorInfo.put(f,new FactorInfo(f,_var2index));

		//Retrieve all the variables that are connected to factors in the graph.
		HashSet<VariableBase> varsConnectedToFactors = new HashSet<VariableBase>();
		for (Factor f : fl)
			varsConnectedToFactors.addAll(f.getVariables());
		
		//for each variable, create a variable info
		//This will be used to store a joint imperical distribution over all of the variable's neighbors.
		//Additionally it will be used to calculate the probability of a setting of a variable given
		//the emperical distribution and the current factor weights.
		for (VariableBase v : varsConnectedToFactors)
			_var2varInfo.put(v,VariableInfo.createVariableInfo(v, _var2index));

		//Go through all data and add the samples to the variable and factor infos.
		//This will be used to build the histograms for the empirical distributions.

		

	}
	
	
	public void setData(Object [][] data)
	{
		setData(convertObjects2Indices(data));
	}
	
	public void setData(int [][] data)
	{
		_data = data;

		for (FactorInfo fi : _factor2factorInfo.values())
			fi.reset();
		for (VariableInfo vi : _var2varInfo.values())
			vi.reset();
		
		for (int i = 0; i < data.length; i++)
		{
			for (FactorInfo fi : _factor2factorInfo.values())
				fi.addSample(data[i]);
			for (VariableInfo vi : _var2varInfo.values())
				vi.addSample(data[i]);
		}

	}
	
	public void setScaleFactor(double scale)
	{
		_scaleFactor = scale;
	}
	
	public void learn(int numSteps,Object [][] data, double scaleFactor)
	{
		setForceKeep(true);
		setData(data);
		setScaleFactor(scaleFactor);
		super.run(0,numSteps);
	}
	
	public void learn(int numSteps,int [][] data, double scaleFactor)
	{
		setForceKeep(true);
		setData(data);
		setScaleFactor(scaleFactor);
		super.run(0,numSteps);
	}

	public void run(int numRestarts,int numSteps)
	{
		throw new DimpleException("Not supported");
	}
	
	
	public double calculateNumericalGradient(FactorTable table, int weight, double delta)
	{
		if (_data == null)
			throw new DimpleException("Must set data first");
			
		double y1 = calculatePseudoLikelihood();
		double oldval = table.getWeights()[weight];
		double newval = oldval * Math.exp(delta);
		table.getWeights()[weight] = newval;
		double y2 = calculatePseudoLikelihood();
		table.getWeights()[weight] = oldval;		
		return (y2-y1)/delta;
	}

	public double calculatePseudoLikelihood()
	{
		//1/M sum(m) sum(i) sum(j in neighbors(i)) tehta(xj,xi)
		//- 1/M sum(m) sum(i) log Z(neighbors(i))
		FactorGraph fg = getFactorGraph();
		VariableList vl = fg.getVariablesFlat();
		
		double total = 0;
		
		//1/M sum(m) sum(i) sum(j in neighbors(i)) tehta(xj,xi)
		for (int m = 0; m < _data.length; m++)
		{
			for (VariableBase v : vl)
			{
				for (Factor f : v.getFactorsFlat())
				{
					VariableList fvs = f.getVariables();
					int [] indices = new int[fvs.size()];
					for (int i = 0; i < indices.length; i++)
					{
						indices[i] = _data[m][_var2index.get(fvs.getByIndex(i))];
					}
					
					double tmp = Math.log(f.getFactorTable().getWeights()[f.getFactorTable().getWeightIndexFromTableIndices(indices)]);
					total += tmp;
				}
			}
		}
		
		total /= _data.length;
		
		//- 1/M sum(m) sum(i) log Z(neighbors(i))
		double total2 = 0;
		for (int m = 0; m < _data.length; m++)
		{
			for (VariableBase v : vl)
			{
				double sum = 0;
				
				for (int d = 0; d < ((Discrete)v).getDomain().size(); d++)
				{
					double product = 1;
					for (Factor f : v.getFactorsFlat())
					{
						VariableList fvs = f.getVariables();
						int [] indices = new int[fvs.size()];
						for (int i = 0; i < indices.length; i++)
						{
							VariableBase fv = fvs.getByIndex(i);
							if (fv == v)
								indices[i] = d;
							else
								indices[i] = _data[m][_var2index.get(fv)];
						}
						
						product *=  f.getFactorTable().getWeights()[f.getFactorTable().getWeightIndexFromTableIndices(indices)];
					}
					sum += product;
				}
				
				total2 += Math.log(sum);
			}
		}
		
		total -= total2 / _data.length;
		
		return total;
	}
	
	public double [][] calculateGradient()
	{
		if (_data == null)
			throw new DimpleException("Must set data first");
		
		//Calculate the gradient
		FactorTable [] tables = getTables();
		HashMap<FactorTable,ArrayList<Factor>> table2factors = getTable2Factors();
		double [][] gradients = new double[tables.length][];
		
		for (VariableInfo vi : _var2varInfo.values())
			vi.invalidateDistributions();
		
		//for each unique factor table
		for (int i = 0; i < tables.length; i++)
		{
			double [] weights = tables[i].getWeights();
			//TODO: avoid this new?
			gradients[i] = new double[weights.length];
			int [][] indices = tables[i].getIndices();
			int degree = indices[0].length;
			
			//for each factor
			ArrayList<Factor> factors = table2factors.get(tables[i]);
			if (factors != null)
			{
				for (int k = 0; k < factors.size(); k++)
				{
					Factor f = factors.get(k);
					VariableList vl = f.getVariables();
					FactorInfo fi = _factor2factorInfo.get(f);	
	
					//for each weight
					for (int j = 0; j < weights.length; j++)
					{
						//add degree * pd(indices)
						double impericalFactorD = fi.getDistribution().get(indices[j]);
						gradients[i][j] += degree*impericalFactorD;
					}
	
					//for each variable
					for (VariableBase v : vl)
					{
						VariableInfo vi = _var2varInfo.get(v);
						//for each element of the variables domain
						for (int d = 0; d < v.asDiscreteVariable().getDiscreteDomain().size(); d++)
						{
							Set<LinkedList<Integer>> samples = vi.getUniqueSamples();
							
							//for each unique sample
							for (LinkedList<Integer> sample : samples)
							{
								//calculate pneighbors
								double prob = vi.getProb(d,sample);
								
								//find weight index from variable domain and unique sample
								int index = vi.getFactorTableIndex(f, d, sample);
								
								//subtract prob
								gradients[i][index] -= prob;
								
							}
							
						}
						
						
					}
				}
			}
		
		}

		return gradients;
	}
	
	
	public void applyGradient(double [][] gradient)
	{
		//System.out.println("applying gradient " + Arrays.toString(gradient[0]));
		FactorTable [] tables = getTables();
		for (int i = 0; i < tables.length; i++)
		{
			double [] ws = tables[i].getWeights();
			double normalizer = 0;
			for (int j = 0; j < ws.length; j++)
			{
				double tmp = Math.log(ws[j]);
				tmp = tmp + gradient[i][j]*_scaleFactor;
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