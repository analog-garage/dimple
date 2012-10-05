package com.analog.lyric.dimple.solvers.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;

public abstract class ParameterEstimator 
{
	private FactorGraph _fg;
	private FactorTable [] _tables;
	private Random _r;
	
	public ParameterEstimator(FactorGraph fg, FactorTable [] tables, Random r)
	{
		_fg = fg;
		_tables = tables;
		_r = r;
	}
	
	public FactorTable [] getTables()
	{
		return _tables;
	}
	
	FactorTable [] saveFactorTables(FactorTable [] fts)
	{
		FactorTable [] savedFts = new FactorTable[fts.length];
		for (int i = 0; i < fts.length; i++)
			savedFts[i] = fts[i].copy();
		return savedFts;
	}
	
	FactorTable [] unique(FactorTable  [] factorTables)
	{
		HashSet<FactorTable> set = new HashSet<FactorTable>();
		for (int i = 0; i < factorTables.length; i++)
			set.add(factorTables[i]);
		factorTables = new FactorTable[set.size()];
		int i = 0;
		for (FactorTable ft : set)
		{
			factorTables[i] = ft;
			i++;
		}
		return factorTables;
	}
	
	public void run(int numRestarts, int numSteps)
	{
		//make sure the factortable list is unique
		_tables = unique(_tables);
		
		//measure betheFreeEnergy
		_fg.solve();
		double currentBFE = _fg.getBetheFreeEnergy();
		FactorTable [] bestFactorTables = saveFactorTables(_tables);
		
		//for each restart
		for (int i = 0; i < numRestarts; i++)
		{
			//if not first time, pick random weights
			if (i != 0)
				for (int j = 0; j < _tables.length; j++)
					_tables[j].randomizeWeights(_r);

			//for numSteps
			for (int j = 0; j < numSteps; j++)
			{
				runStep(_fg);
			}
			
			_fg.solve();
			double newBetheFreeEnergy = _fg.getBetheFreeEnergy();
			
			//if betheFreeEnergy is better
			//store this is answer
			if (newBetheFreeEnergy < currentBFE)
			{
				currentBFE = newBetheFreeEnergy;
				bestFactorTables = saveFactorTables(_tables);
			}

		}
		
		//Set weights to best answer
		for (int i = 0; i < _tables.length; i++)
		{
			_tables[i].copy(bestFactorTables[i]);
		}
	}
	
	public abstract void runStep(FactorGraph fg);
	
	public static class BaumWelch extends ParameterEstimator
	{
		HashMap<FactorTable,ArrayList<Factor>> _table2factors;
		
		public BaumWelch(FactorGraph fg, FactorTable[] tables, Random r) 
		{
			super(fg, tables, r);
			HashMap<FactorTable,ArrayList<Factor>> table2factors = new HashMap<FactorTable, ArrayList<Factor>>();
			
			for (Factor f  : fg.getFactorsFlat())
			{
				FactorTable ft = f.getFactorTable();
				if (! table2factors.containsKey(ft))
					table2factors.put(ft,new ArrayList<Factor>());
				table2factors.get(ft).add(f);
			}
			
			_table2factors = table2factors;
		}

		@Override
		public void runStep(FactorGraph fg) 
		{
			
			//run BP
			fg.solve();
			
			//Assign new weights
			//For each Factor Table
			for (FactorTable ft : _table2factors.keySet())
			{
				//Calculate the average of the FactorTable beliefs
				ArrayList<Factor> factors = _table2factors.get(ft);
				
				double [] sum = new double[ft.getRows()];
				
				for (Factor f : factors)
				{
					double [] belief = (double[])f.getSolver().getBelief();
					for (int i = 0; i < sum.length; i++)
						sum[i] += belief[i];
				}
				
				//Set the weights to that
				ft.changeWeights(sum);
			}			
		}		
	}
}
