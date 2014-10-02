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

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.util.misc.Internal;

/**
 * Similar to the {@link TableWrapper} class, but for the purpose of estimating the cost of
 * employing the optimized update algorithm. Instead of storing a factor table's data, this
 * implementation only stores information about its dimensions and its density.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public final class CostEstimationTableWrapper
{
	/**
	 * The density of the represented factor table. A value between 0.0 and 1.0.
	 */
	private final double _density;

	/**
	 * The dimensions of the represented factor table.
	 */
	private final int[] _dimensions;

	/**
	 * True if the represented factor table is sparse.
	 */
	private final boolean _isSparse;

	/**
	 * Provides solver-specific facilities.
	 */
	private final ISFactorGraphToOptimizedUpdateAdapter _sFactorGraphAdapter;

	/**
	 * The quantity of entries in the represented factor table.
	 */
	private final double _size;

	/**
	 * Density threshold below which a sparse representation should be preferred over a dense
	 * representation. A value between 0.0 and 1.0.
	 */
	private final double _sparseThreshold;

	public CostEstimationTableWrapper(final IFactorTable factorTable,
		ISFactorGraphToOptimizedUpdateAdapter sFactorGraphAdapter,
		final double sparseThreshold)
	{
		this(getFactorTableDimensions(factorTable), factorTable.density(), sFactorGraphAdapter, sparseThreshold);
	}

	public CostEstimationTableWrapper(final int[] dimensions,
		final double density,
		ISFactorGraphToOptimizedUpdateAdapter sFactorGraphAdapter,
		double sparseThreshold)
	{
		_dimensions = dimensions;
		_density = density;
		_sFactorGraphAdapter = sFactorGraphAdapter;
		_sparseThreshold = sparseThreshold;
		_isSparse = _density < _sparseThreshold;
		double cardinality = 1.0;
		for (int i = 0; i < dimensions.length; i++)
		{
			cardinality *= dimensions[i];
		}
		_size = cardinality * density;
	}

	public IMarginalizationStepEstimator createMarginalizationStep(final int inPortNum, final int dimension)
	{
		final int[] g_dimensions = ArrayUtil.removeIntArrayEntry(getDimensions(), dimension);
		final double g_density = 1.0 - Math.pow(1 - _density, getDimensions()[dimension]);
		CostEstimationTableWrapper g =
			new CostEstimationTableWrapper(g_dimensions, g_density, _sFactorGraphAdapter, _sparseThreshold);
		if (_isSparse)
		{
			return _sFactorGraphAdapter.createSparseMarginalizationStepEstimator(this, inPortNum, dimension, g);
		}
		else
		{
			return _sFactorGraphAdapter.createDenseMarginalizationStepEstimator(this, inPortNum, dimension, g);
		}
	}

	public IUpdateStepEstimator createOutputStep(final int outPortNum)
	{
		if (_isSparse)
		{
			return _sFactorGraphAdapter.createSparseOutputStepEstimator(this);
		}
		else
		{
			return _sFactorGraphAdapter.createDenseOutputStepEstimator(this);
		}
	}

	/**
	 * Estimates costs associated with the table. The costs include the memory required to store
	 * the table.
	 * 
	 * @since 0.07
	 */
	public Costs estimateCosts()
	{
		final double allocations;
		final double _tableData = _size * 8;
		if (_isSparse)
		{
			final double sparseToJointIndices = _size * 4;
			final double sparseIndices = _size * _dimensions.length * 4;
			allocations = _tableData + sparseToJointIndices + sparseIndices;
		}
		else
		{
			allocations = _tableData;
		}
		Costs result = new Costs();
		result.put(CostType.MEMORY, allocations / 1024.0 / 1024.0 / 1024.0);
		return result;
	}

	public int[] getDimensions()
	{
		return _dimensions;
	}

	public double getSize()
	{
		return _size;
	}

	/*
	 * A helper function for a constructor that calls another constructor.
	 */
	private static int[] getFactorTableDimensions(final IFactorTable factorTable)
	{
		JointDomainIndexer indexer = factorTable.getDomainIndexer();
		int[] dimensions = new int[factorTable.getDimensions()];
		for (int i = 0; i < dimensions.length; i++)
		{
			dimensions[i] = indexer.getDomainSize(i);
		}
		return dimensions;
	}
}