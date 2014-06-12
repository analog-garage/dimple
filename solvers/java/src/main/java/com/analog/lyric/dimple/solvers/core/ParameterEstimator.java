/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.util.misc.Misc;

public abstract class ParameterEstimator
{
	private FactorGraph _fg;
	private IFactorTable [] _tables;
	private Random _r;
	private HashMap<IFactorTable,ArrayList<Factor>> _table2factors;
	private boolean _forceKeep;

	public ParameterEstimator(FactorGraph fg, IFactorTable [] tables, Random r)
	{
		_fg = fg;
		_tables = tables;
		_r = r;
		
		HashMap<IFactorTable,ArrayList<Factor>> table2factors = new HashMap<IFactorTable, ArrayList<Factor>>();

		for (Factor f  : fg.getFactorsFlat())
		{
			IFactorTable ft = f.getFactorTable();
			if (! table2factors.containsKey(ft))
				table2factors.put(ft,new ArrayList<Factor>());
			table2factors.get(ft).add(f);
		}

		//Verify directionality is consistent.
		_table2factors = table2factors;

	}
	
	public void setRandom(Random r)
	{
		_r = r;
	}
	
	public HashMap<IFactorTable,ArrayList<Factor>> getTable2Factors()
	{
		return _table2factors;
	}

	public IFactorTable [] getTables()
	{
		return _tables;
	}

	IFactorTable [] saveFactorTables(IFactorTable [] fts)
	{
		IFactorTable [] savedFts = new IFactorTable[fts.length];
		for (int i = 0; i < fts.length; i++)
			savedFts[i] = fts[i].clone();
		return savedFts;
	}

	IFactorTable [] unique(IFactorTable  [] factorTables)
	{
		HashSet<IFactorTable> set = new HashSet<IFactorTable>();
		for (int i = 0; i < factorTables.length; i++)
			set.add(factorTables[i]);
		factorTables = new IFactorTable[set.size()];
		int i = 0;
		for (IFactorTable ft : set)
		{
			factorTables[i] = ft;
			i++;
		}
		return factorTables;
	}
	
	public FactorGraph getFactorGraph()
	{
		return _fg;
	}
	
	public void setForceKeep(boolean val)
	{
		_forceKeep = val;
	}

	public void run(int numRestarts, int numSteps)
	{
		//make sure the factortable list is unique
		_tables = unique(_tables);

		//measure betheFreeEnergy
		_fg.solve();
		double currentBFE = _fg.getBetheFreeEnergy();
		IFactorTable [] bestFactorTables = saveFactorTables(_tables);

		//for each restart
		for (int i = 0; i <= numRestarts; i++)
		{
			//if not first time, pick random weights
			if (i != 0)
				for (int j = 0; j < _tables.length; j++)
				{
					_tables[j].randomizeWeights(_r);
					if (_tables[j].isDirected())
						_tables[j].normalizeConditional();
				}

			//for numSteps
			for (int j = 0; j < numSteps; j++)
			{
				runStep(_fg);
			}

			_fg.solve();
			double newBetheFreeEnergy = _fg.getBetheFreeEnergy();

			//if betheFreeEnergy is better
			//store this is answer
			if (newBetheFreeEnergy < currentBFE || _forceKeep)
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

		public BaumWelch(FactorGraph fg, IFactorTable[] tables, Random r)
		{
			super(fg, tables, r);

			for (IFactorTable table : getTable2Factors().keySet())
			{
				ArrayList<Factor> factors = getTable2Factors().get(table);
				int [] direction = null;
				for (Factor f : factors)
				{
					if (f.getFactorTable() != table)
					{
						Misc.breakpoint();
					}
					int [] tmp = f.getDirectedTo();
					if (tmp == null)
						throw new DimpleException("Baum Welch only works with directed Factors");
					if (direction == null)
						direction = tmp;
					else
					{
						if (tmp.length != direction.length)
							throw new DimpleException("Directions must be the same for all factors sharing a Factor Table");
						for (int i = 0; i < tmp.length; i++)
							if (tmp[i] != direction[i])
								throw new DimpleException("Directions must be the same for all factors sharing a Factor Table");
					}
				}
			}
		}



		@Override
		public void runStep(FactorGraph fg)
		{

			//run BP
			fg.solve();

			//Assign new weights
			//For each Factor Table
			for (IFactorTable ft : getTable2Factors().keySet())
			{
				//Calculate the average of the FactorTable beliefs
				ArrayList<Factor> factors = getTable2Factors().get(ft);

				double [] sum = new double[ft.sparseSize()];

				for (Factor f : factors)
				{
					if (f.getFactorTable() != ft)
					{
						Misc.breakpoint();
					}
					double [] belief = (double[])requireNonNull(f.getSolver()).getBelief();
					for (int i = 0; i < sum.length; i++)
						sum[i] += belief[i];


				}


				//Get first directionality
				Factor firstFactor = factors.get(0);
				int [] directedTo = firstFactor.getDirectedTo();
				int [] directedFrom = firstFactor.getDirectedFrom();

				//Set the weights to that
				ft.replaceWeightsSparse(sum);
				if (directedTo != null && directedFrom != null)
				{
					ft.makeConditional(BitSetUtil.bitsetFromIndices(directedTo.length + directedFrom.length, directedTo));
				}
	}
		}
	}
}
