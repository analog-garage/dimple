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

package com.analog.lyric.dimple.solvers.optimizedupdate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.Comparators;
import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.util.misc.Internal;

/**
 * Represents factor tables internally for the optimized update algorithm. At the root of the
 * marginalization tree, wraps the factor's factor table. Within the tree, represents auxiliary
 * tables with a lighter-weight representation.
 * <p>
 * The values stored in the table may be weights or energies, depending on the solver.
 * Solver-specific code provides the root-level values, and solver-specific code operates on the
 * values to compute the auxiliary table values.
 * <p>
 * Auxiliary tables employ thread-local storage when multithreading is enabled.
 * 
 * @since 0.06
 * @author jking
 */
@Internal
public final class TableWrapper
{
	private final IFactorTable _factorTable;

	private final ThreadLocal<double[]> _values;

	private final int _size;

	private final boolean _isSparse;

	private final ISFactorGraphToOptimizedUpdateAdapter _helper;
	
	private final double _sparseThreshold;

	public TableWrapper(IFactorTable factorTable, boolean useThreadLocalValues, ISFactorGraphToOptimizedUpdateAdapter isFactorGraphToCostOptimizerAdapter, double sparseThreshold)
	{
		_factorTable = factorTable;
		_isSparse = factorTable.hasSparseRepresentation();
		_helper = isFactorGraphToCostOptimizerAdapter;
		_sparseThreshold = sparseThreshold;
		if (!useThreadLocalValues)
		{
			final double[] values;
			if (_isSparse)
			{
				values = _helper.getSparseValues(factorTable);
			}
			else
			{
				/*
				 * Because useThreadLocalValues is only false at the root of the tree, with
				 * factor tables from the model, and because the sum-product solver sets those
				 * tables' representation to sparse, it is most likely impossible to hit this
				 * branch. But just in case...
				 */
				values = _helper.getDenseValues(factorTable);
			}
			_size = values.length;
			_values = new ThreadLocal<double[]>() {
				@Override
				protected double[] initialValue()
				{
					return values;
				}
			};
		}
		else
		{
			if (_isSparse)
			{
				_size = _helper.getSparseValues(factorTable).length;
			}
			else
			{
				_size = _helper.getDenseValues(factorTable).length;
			}
			_values = new ThreadLocal<double[]>() {
				@Override
				protected double[] initialValue()
				{
					return new double[_size];
				}
			};
		}
	}

	public TableWrapper(final IFactorTable factorTable, ISFactorGraphToOptimizedUpdateAdapter helper, double sparseThreshold)
	{
		this(factorTable, false, helper, sparseThreshold);
	}

	public IUpdateStep createOutputStep(final int outPortNum)
	{
		if (_isSparse)
		{
			return _helper.createSparseOutputStep(outPortNum, this);
		}
		else
		{
			return _helper.createDenseOutputStep(outPortNum, this);
		}
	}

	private Tuple2<int[][], int[]> processIndices(final int dimension, final IFactorTable g_factorTable)
	{
		final int[][] all_f_indices = getFactorTable().getIndicesSparseUnsafe();
		final int[] _msg_indices = new int[_size];
		final int[][] g_indices = new int[_size][];
		int n = 0;
		for (final int[] f_indices : all_f_indices)
		{
			_msg_indices[n] = f_indices[dimension];
			g_indices[n] = ArrayUtil.removeIntArrayEntry(f_indices, dimension);
			n += 1;
		}

		// The next section of this function sets the weight for each of the used g_indices to a
		// non-zero value.
		Comparator<int[]> comparator = Comparators.reverseLexicalIntArray();
		Set<int[]> all_g_indices = new TreeSet<>(comparator);
		all_g_indices.addAll(Arrays.asList(g_indices));
		int[][] indices = all_g_indices.toArray(new int[all_g_indices.size()][]);
		double[] weights = new double[indices.length];
		Arrays.fill(weights, 1.0);
		g_factorTable.setWeightsSparse(indices, weights);

		return new Tuple2<int[][], int[]>(g_indices, _msg_indices);
	}

	public IMarginalizationStep createMarginalizationStep(final int inPortNum, final int dimension)
	{
		final JointDomainIndexer f_indexer = _factorTable.getDomainIndexer();
		final JointDomainReindexer g_remover = JointDomainReindexer.createRemover(f_indexer, dimension);
		final JointDomainIndexer g_indexer = g_remover.getToDomains();
		IFactorTable g_factorTable = FactorTable.create(g_indexer);
		if (_isSparse)
		{
			Tuple2<int[][], int[]> g_and_msg_indices = processIndices(dimension, g_factorTable);
			if (g_indexer.supportsJointIndexing()
				&& (g_factorTable.countNonZeroWeights() >= g_indexer.getCardinality() * _sparseThreshold))
			{
				g_factorTable.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
			}
			return _helper
				.createSparseMarginalizationStep(this, inPortNum, dimension, g_factorTable, g_and_msg_indices);
		}
		else
		{
			g_factorTable.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
			return _helper.createDenseMarginalizationStep(this, inPortNum, dimension, g_factorTable);
		}
	}

	public IFactorTable getFactorTable()
	{
		return _factorTable;
	}

	public ThreadLocal<double[]> getValues()
	{
		return _values;
	}

	public double getSparseThreshold()
	{
		return _sparseThreshold;
	}
}