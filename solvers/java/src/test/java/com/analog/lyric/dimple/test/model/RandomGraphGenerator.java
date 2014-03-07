/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.model;

import java.util.Random;

import cern.colt.list.IntArrayList;

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;

/**
 * Test helper class for generating test graphs.
 * 
 * @since 0.05
 * @author Christopher Barber
 */
public class RandomGraphGenerator
{
	/*-------
	 * State
	 */
	
	private final Random _rand;
	
	/*--------------
	 * Construction
	 */
	
	public RandomGraphGenerator(Random rand)
	{
		_rand = rand;
	}
	
	/*--------------------------
	 * Graph generation methods
	 */
	
	/**
	 * Builds an n x n grid graph with variable domains choosen randomly from {@code domains}.
	 * 
	 * @param n
	 * @param domains
	 */
	public FactorGraph buildGrid(int n, DiscreteDomain ... domains)
	{
		final FactorGraph graph = new FactorGraph();
		
		final Discrete[][] vars = new Discrete[n][n];
		for (int i = 0; i < n; ++i)
		{
			for (int j = 0; j < n; ++j)
			{
				Discrete var = new Discrete(chooseDomain(domains));
				var.setName(String.format("%s%d", intToBase26(i), j));
				vars[i][j] = var;
				
				if (i > 0)
				{
					Discrete prev = vars[i-1][j];
					addClique(graph, prev, var);
				}
				if (j > 0)
				{
					Discrete prev = vars[i][j-1];
					addClique(graph, prev, var);
				}
			}
		}
		
		return graph;
	}
	
	/**
	 * Extended student Bayesian network from Koller's Probabilistic Graphical Models (Figure 9.8)
	 * <pre>
	 *   c[3]
	 *     |
	 *     v
	 *   d[3]   i[3]
	 *      \   /  \
	 *       v v    v
	 *       g[5]  s[10]
	 *       / |     |
	 *      /  v     |
	 *      | l[2]   |
	 *      |     \  |
	 *      |      v v
	 *      |      j[2]
	 *      |     /
	 *       \   /
	 *        v v
	 *        h[2]
	 * </pre>
	 * Numbers in brackets indicate the variable cardinality.
	 */
	public FactorGraph buildStudentNetwork()
	{
		FactorGraph model = new FactorGraph();
		Discrete c = newDiscrete(3, "c");
		Discrete d = newDiscrete(3, "d");
		addClique(model, d, c);
		Discrete i = newDiscrete(3, "i");
		Discrete g = newDiscrete(5, "g");
		Discrete s = newDiscrete(10, "s");
		addClique(model, g, d, i);
		addClique(model, s, i);
		Discrete l = newDiscrete(2, "l");
		addClique(model, l, g);
		Discrete j = newDiscrete(2, "j");
		addClique(model, j, l, s);
		Discrete h = newDiscrete(2, "h");
		addClique(model, h, g, j);
		return model;
	}
	
	public FactorGraph buildTriangle(DiscreteDomain ... domains)
	{
		FactorGraph model = new FactorGraph();
		Discrete a = newDiscrete(chooseDomain(domains), "a");
		Discrete b = newDiscrete(chooseDomain(domains), "b");
		Discrete c = newDiscrete(chooseDomain(domains), "c");
		addClique(model, a, b);
		addClique(model, b, c);
		addClique(model, c, a);
		return model;
	}
	
	/**
	 * Build graph consisting of smallest possible loop consisting of two variables with domains randomly choosen
	 * from {@code domains} and connected by two separate factors.
	 * 
	 * @param domains
	 * @since 0.05
	 */
	public FactorGraph buildTrivialLoop(DiscreteDomain ... domains)
	{
		FactorGraph model = new FactorGraph();
		Discrete a = newDiscrete(chooseDomain(domains), "a");
		Discrete b = newDiscrete(chooseDomain(domains), "b");
		addClique(model, a, b);
		addClique(model, a, b);
		return model;
	}

	/*------------------------------
	 * RandomGraphGenerator methods
	 */
	
	public void addClique(FactorGraph model, Discrete ... variables)
	{
		final Factor factor = model.addFactor(randomTable(variables), variables);
		labelFactor(factor);
	}
	
	public DiscreteDomain chooseDomain(DiscreteDomain ... domains)
	{
		if (domains.length == 1)
		{
			return domains[0];
		}
		
		return domains[_rand.nextInt(domains.length)];
	}
	
	public Discrete newDiscrete(int cardinality, String name)
	{
		return newDiscrete(DiscreteDomain.range(1, cardinality), name);
	}
	
	public Discrete newDiscrete(DiscreteDomain domain, String name)
	{
		Discrete var = new Discrete(domain);
		var.setName(name);
		return var;
	}

	public IFactorTable randomTable(Discrete ... variables)
	{
		DiscreteDomain[] domains = new DiscreteDomain[variables.length];
		for (int i = variables.length; --i>=0;)
		{
			domains[i] = variables[i].getDomain();
		}
		return randomTable(domains);
	}
	
	public IFactorTable randomTable(DiscreteDomain ... domains)
	{
		IFactorTable table = FactorTable.create(domains);
		table.setRepresentation(FactorTableRepresentation.DENSE_ENERGY);
		table.randomizeWeights(_rand);
		return table;
	}
	
	/*-----------------------
	 * Static helper methods
	 */
	
	/**
	 * Give factor a label of the form f(<i>variables</i>) if it doesn't already have a name.
	 */
	public static void labelFactor(Factor factor)
	{
		if (factor.getExplicitName() == null)
		{
			StringBuffer name = new StringBuffer("f(");
			for (int i = 0, end = factor.getSiblingCount(); i<end; ++i)
			{
				if (i > 0)
					name.append(",");
				name.append(factor.getSibling(i).getLabel());
			}
			name.append(")");
			factor.setLabel(name.toString());
		}
	}
	
	public static void labelFactors(FactorGraph graph)
	{
		for (Factor factor : graph.getFactors())
		{
			labelFactor(factor);
		}
	}

	/*-----------------
	 * Private methods
	 */
	
	private static String intToBase26(int i)
	{
		IntArrayList digits = new IntArrayList();
		for (long l = i & 0xFFFFFFFFL; true; l /= 26)
		{
			digits.add((int)(l % 26));
			if (l < 26)
				break;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int j = digits.size(); --j>=0;)
		{
			sb.append((char)('a' + digits.get(j)));
			// FIXME
		}
		return sb.toString();
	}
	
	
}
