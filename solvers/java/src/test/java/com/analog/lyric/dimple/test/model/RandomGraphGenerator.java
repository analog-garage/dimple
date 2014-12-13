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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Random;

import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.IntArrayList;

import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.google.common.collect.ObjectArrays;

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
	
	public static enum Direction
	{
		NONE, BACKWARD, FORWARD;
	}
	
	private static final DiscreteDomain[] _defaultDomains = { DiscreteDomain.bit() };

	private final Random _rand;
	
	private DiscreteDomain[] _domains = _defaultDomains;
	private int _maxBranches = 1;
	private int _maxTreeWidth = 1;
	private Direction _direction = Direction.NONE;
	
	/*--------------
	 * Construction
	 */
	
	public RandomGraphGenerator(Random rand)
	{
		_rand = rand;
	}
	
	/*-------------------
	 * Attribute methods
	 */
	
	public Direction direction()
	{
		return _direction;
	}
	
	public RandomGraphGenerator direction(Direction direction)
	{
		_direction = direction;
		return this;
	}
	
	public DiscreteDomain[] domains()
	{
		return _domains;
	}
	
	public RandomGraphGenerator domains(@Nullable DiscreteDomain ... domains)
	{
		_domains = (domains != null && domains.length > 0) ? domains : _defaultDomains;
		return this;
	}
	
	public int maxBranches()
	{
		return _maxBranches;
	}
	
	public RandomGraphGenerator maxBranches(int n)
	{
		_maxBranches = n;
		return this;
	}
	
	public int maxTreeWidth()
	{
		return _maxTreeWidth;
	}
	
	public RandomGraphGenerator maxTreeWidth(int width)
	{
		_maxTreeWidth = width;
		return this;
	}
	
	/*--------------------------
	 * Graph generation methods
	 */
	
	/**
	 * Builds an n x n grid graph with variable domains chosen randomly from {@link #domains()}.
	 * 
	 * @param n
	 */
	public FactorGraph buildGrid(int n)
	{
		return buildGrid(n, n);
	}
	
	/**
	 * Builds a m x n grid graph with variable domains chosen randomly from {@link #domains()}.
	 * 
	 * @param m
	 * @param n
	 */
	public FactorGraph buildGrid(int m, int n)
	{
		final FactorGraph graph = new FactorGraph();
		
		final Discrete[][] vars = new Discrete[m][n];
		for (int i = 0; i < m; ++i)
		{
			for (int j = 0; j < n; ++j)
			{
				Discrete var = newDiscrete(String.format("%s%d", intToBase26(i), j));
				vars[i][j] = var;
				
				if (i > 0)
				{
					Discrete prev = vars[i-1][j];
					addClique(graph, var, prev);
				}
				if (j > 0)
				{
					Discrete prev = vars[i][j-1];
					addClique(graph, var, prev);
				}
			}
		}
		
		return graph;
	}
	
	public FactorGraph buildRandomGraph(int size)
	{
		final FactorGraph graph = new FactorGraph();
		
		final int nRoots = _rand.nextInt(Math.min(size, maxTreeWidth())) + 1;
		final Discrete[] roots = newDiscretes(nRoots, "root", 0);
		graph.addVariables(roots);
		addRandomGraph(graph, size - nRoots, roots);
		
		return graph;
	}
	
	public void addRandomGraph(FactorGraph graph, int size, Discrete ... roots)
	{
		if (size <= 0)
		{
			return;
		}
		
		final int nRoots = roots.length;
		final int nBranches = 1 + _rand.nextInt(Math.min(size, maxBranches()));
		final int sizePerBranch = size / nBranches;
		
		for (int branch = nBranches; --branch>=0;)
		{
			final int branchSize = branch > 0 ? sizePerBranch : sizePerBranch + size % nBranches ;
			
			final int nVars = 1 + _rand.nextInt(Math.min(branchSize, maxTreeWidth()));
			final Discrete[] vars = newDiscretes(nVars);
			final int nCliques = _rand.nextInt(Math.min(nVars, nRoots)) + 1;
		
			if (nCliques == 1)
			{
				// Just make one big clique
				addClique(graph, roots.length, ObjectArrays.concat(roots, vars, Discrete.class));
			}
			else
			{
				// Randomly order roots and new variables
				final ArrayList<Discrete> randomRoots = new ArrayList<Discrete>(Arrays.asList(roots));
				Collections.shuffle(randomRoots, _rand);
				final ArrayList<Discrete> randomVars = new ArrayList<Discrete>(Arrays.asList(vars));
				Collections.shuffle(randomVars, _rand);
			
				for (int n = nCliques + 1; --n>=1;)
				{
					int nRootsChosen = randomRoots.size();
					int nVarsChosen = randomVars.size();
					
					if (n > 1)
					{
						nRootsChosen = Math.max(1, nRootsChosen / n);
						nVarsChosen = Math.max(1, nVarsChosen / n);
					}
					
					final Discrete[] cliqueVars = new Discrete[nRootsChosen + nVarsChosen];
					int i = 0;
					for (int j = 0; j < nRootsChosen; ++j)
					{
						cliqueVars[i++] = randomRoots.remove(randomRoots.size() - 1);
					}
					for (int j = 0; j < nVarsChosen; ++j)
					{
						cliqueVars[i++] = randomVars.remove(randomVars.size() - 1);
					}
					
					addClique(graph, nRootsChosen, cliqueVars);
				}
			}

		}
		
	}
	
	/**
	 * Builds a random graph in the form of a tree with {@code size} nodes with at most {@code maxBranches}
	 * (i.e. every node has at most {@code maxBranches}+1 siblings) with variable domains chosen randomly
	 * from {@code domains}.
	 */
	public FactorGraph buildRandomTree(int size)
	{
		final FactorGraph graph = new FactorGraph();
		
		Discrete root = newDiscrete("root");
		graph.addVariables(root);

		addRandomTree(graph, size - 1, root);
		
		return graph;
	}
	
	/**
	 * Adds a random tree rooted from given {@code root} with {@code size} nodes with at most {@code maxBranches}
	 * (i.e. every node has at most {@code maxBranches}+1 siblings) with variable domains chosen randomly
	 * from {@code domains}.
	 */
	public void addRandomTree(FactorGraph graph, int size, Discrete root)
	{
		if (size <= 0)
		{
			return;
		}
		
		int nChildren = 1 + _rand.nextInt(Math.min(size, maxBranches()));
		int childSize = size / nChildren;
		for (int i = 0; i < nChildren; ++i)
		{
			Discrete child = newDiscrete();
			addClique(graph, child, root);
			addRandomTree(graph, childSize - 1, child);
		}
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
		addDirectedClique(model, d, c);
		Discrete i = newDiscrete(3, "i");
		Discrete g = newDiscrete(5, "g");
		Discrete s = newDiscrete(10, "s");
		addDirectedClique(model, g, d, i);
		addDirectedClique(model, s, i);
		Discrete l = newDiscrete(2, "l");
		addDirectedClique(model, l, g);
		Discrete j = newDiscrete(2, "j");
		addDirectedClique(model, j, l, s);
		Discrete h = newDiscrete(2, "h");
		addDirectedClique(model, h, g, j);
		return model;
	}
	
	public FactorGraph buildTriangle()
	{
		FactorGraph model = new FactorGraph();
		Discrete a = newDiscrete("a");
		Discrete b = newDiscrete("b");
		Discrete c = newDiscrete("c");
		addClique(model, a, b);
		addClique(model, b, c);
		addClique(model, a, c);
		return model;
	}
	
	/**
	 * Build graph consisting of smallest possible loop consisting of two variables with domains randomly choosen
	 * from {@link #domains()} and connected by two separate factors.
	 */
	public FactorGraph buildTrivialLoop()
	{
		FactorGraph model = new FactorGraph();
		Discrete a = newDiscrete("a");
		Discrete b = newDiscrete("b");
		addClique(model, a, b);
		addClique(model, a, b);
		return model;
	}

	/*------------------------------
	 * RandomGraphGenerator methods
	 */
	
	public Factor addClique(FactorGraph model, Discrete ... variables)
	{
		return addClique(model, 1, variables);
	}
	
	public Factor addClique(FactorGraph model, int nOutputs, Discrete ... variables)
	{
		BitSet toSet = new BitSet(variables.length);
		for (int i = 0; i < nOutputs; ++i)
		{
			toSet.set(i);
		}

		final Factor factor = model.addFactor(randomTable(variables), variables);
		labelFactor(factor);
		switch (_direction)
		{
		case NONE:
			break;
			
		case BACKWARD:
			toSet.flip(0, variables.length);
			//$FALL-THROUGH$
			
		case FORWARD:
			if (factor.hasFactorTable())
			{
				IFactorTable table = factor.getFactorTable();

				table.setDirected(toSet);
				table.normalizeConditional();
			}
			factor.setDirectedTo(BitSetUtil.bitsetToIndices(toSet));
			break;
		}
		return factor;
	}
	
	/**
	 * Adds a directed factor with first variable as output (directedTo).
	 */
	public Factor addDirectedClique(FactorGraph model, Discrete ... variables)
	{
		Direction prevDirection = _direction;
		try
		{
			_direction = Direction.FORWARD;
			return addClique(model, variables);
		}
		finally
		{
			_direction = prevDirection;
		}
	}
	
	/**
	 * Chooses a domain randomly from {@link #domains()}. Returns {@link DiscreteDomain#bit()} if
	 * empty.
	 */
	public DiscreteDomain chooseDomain()
	{
		return chooseDomain(domains());
	}
	
	/**
	 * Chooses a domain randomly from {@code domains}. Returns {@link DiscreteDomain#bit()} if
	 * {@code domains} is empty.
	 */
	public DiscreteDomain chooseDomain(DiscreteDomain ... domains)
	{
		final int nDomains = domains.length;
		
		switch (nDomains)
		{
		case 0:
			return DiscreteDomain.bit();
		case 1:
			return domains[0];
		default:
			return domains[_rand.nextInt(nDomains)];
		}
	}
	
	public Discrete newDiscrete()
	{
		return newDiscrete(null);
	}
	
	public Discrete newDiscrete(String name, int counter)
	{
		return newDiscrete(name + counter);
	}
	
	public Discrete newDiscrete(@Nullable String name)
	{
		return newDiscrete(chooseDomain(), name);
	}
	
	public Discrete newDiscrete(int cardinality, String name)
	{
		return newDiscrete(DiscreteDomain.range(1, cardinality), name);
	}
	
	public Discrete newDiscrete(DiscreteDomain domain, @Nullable String name)
	{
		Discrete var = new Discrete(domain);
		if (name == null || name.isEmpty())
		{
			name = "v" + var.getLocalId();
		}
		var.setName(name);
		return var;
	}

	public Discrete[] newDiscretes(int n)
	{
		final Discrete[] discretes = new Discrete[n];
		for (int i = 0; i < n; ++i)
		{
			discretes[i] = newDiscrete();
		}
		return discretes;
	}
	
	public Discrete[] newDiscretes(int n, String namePrefix, int counter)
	{
		final Discrete[] discretes = new Discrete[n];
		for (int i = 0; i < n; ++i)
		{
			discretes[i] = newDiscrete(namePrefix, counter+i);
		}
		return discretes;
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
		}
		return sb.toString();
	}
	
	
}
