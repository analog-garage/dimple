/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.solvers.minsum;

import java.util.Arrays;

import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.solvers.optimizedupdate.CostEstimationTableWrapper;
import com.analog.lyric.dimple.solvers.optimizedupdate.CostType;
import com.analog.lyric.dimple.solvers.optimizedupdate.Costs;
import com.analog.lyric.dimple.solvers.optimizedupdate.FactorUpdatePlan;
import com.analog.lyric.dimple.solvers.optimizedupdate.IMarginalizationStep;
import com.analog.lyric.dimple.solvers.optimizedupdate.IMarginalizationStepEstimator;
import com.analog.lyric.dimple.solvers.optimizedupdate.ISFactorGraphToOptimizedUpdateAdapter;
import com.analog.lyric.dimple.solvers.optimizedupdate.ISTableFactorSupportingOptimizedUpdate;
import com.analog.lyric.dimple.solvers.optimizedupdate.IUpdateStep;
import com.analog.lyric.dimple.solvers.optimizedupdate.IUpdateStepEstimator;
import com.analog.lyric.dimple.solvers.optimizedupdate.TableWrapper;
import com.analog.lyric.util.misc.Internal;

/**
 * Implements a factor update approach for sum-product that optimally shares partial results. Since
 * it computes all output edges, it only overrides the update method, and does not override the
 * update_edge method.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public class TableFactorEngineOptimized extends TableFactorEngine
{
	/**
	 * The update plan for the factor related to this update engine.
	 */
	private final FactorUpdatePlan _updatePlan;

	public TableFactorEngineOptimized(MinSumTableFactor tableFactor, FactorUpdatePlan updatePlan)
	{
		super(tableFactor);
		_updatePlan = updatePlan;
	}

	@Override
	public void update()
	{
		_updatePlan.apply(_tableFactor);
	}

	static int getStride(int[] dimensions, int dimension)
	{
		int result = 1;
		for (int i = 0; i < dimension; i++)
		{
			result *= dimensions[i];
		}
		return result;
	}

	static final class DenseMarginalizationStep implements IMarginalizationStep
	{
		private final TableWrapper _f;

		private final TableWrapper _g;

		private final int _inPortNum;

		private final int _d;

		private final int _p;

		DenseMarginalizationStep(final TableWrapper f,
			final ISFactorGraphToOptimizedUpdateAdapter helper,
			final int inPortNum,
			final int dimension,
			final IFactorTable g_factorTable)
		{
			_f = f;
			JointDomainIndexer f_indexer = f.getFactorTable().getDomainIndexer();
			_p = f_indexer.getStride(dimension);
			_d = f_indexer.getDomainSize(dimension);
			_g = new TableWrapper(g_factorTable, true, helper, f.getSparseThreshold());
			_inPortNum = inPortNum;
		}

		@Override
		public void apply(ISTableFactorSupportingOptimizedUpdate tableFactor)
		{
			final double[] f_values = _f.getValues().get();
			final double[] g_values = _g.getValues().get();
			final double[] inputMsg = tableFactor.getInPortMsgs()[_inPortNum];
			Arrays.fill(g_values, Double.POSITIVE_INFINITY);
			int c = 0;
			int msg_index = 0;
			int g_index = 0;
			int g_index_limit = _p;
			double input_value = inputMsg[0];
			for (final double value : f_values)
			{
				double v = value + input_value;
				if (v < g_values[g_index])
				{
					g_values[g_index] = v;
				}
				if (++g_index == g_index_limit)
				{
					if (++msg_index == _d)
					{
						msg_index = 0;
						c = g_index_limit;
						g_index_limit += _p;
					}
					g_index = c;
					input_value = inputMsg[msg_index];
				}
			}
		}

		@Override
		public TableWrapper getAuxiliaryTable()
		{
			return _g;
		}
	}

	static final class SparseMarginalizationStep implements IMarginalizationStep
	{
		private final TableWrapper _f;

		private final int _inPortNum;

		private final TableWrapper _g;

		private final int[] _msg_indices;

		private final int[] _g_sparse_indices;

		SparseMarginalizationStep(final TableWrapper f,
			final ISFactorGraphToOptimizedUpdateAdapter isFactorGraphToCostOptimizerAdapter,
			final int inPortNum,
			final int dimension,
			final IFactorTable g_factorTable,
			final Tuple2<int[][], int[]> g_and_msg_indices)
		{
			_f = f;
			_inPortNum = inPortNum;
			_g = new TableWrapper(g_factorTable, true, isFactorGraphToCostOptimizerAdapter, f.getSparseThreshold());
			final int[][] g_indices = g_and_msg_indices.first;
			_msg_indices = g_and_msg_indices.second;
			_g_sparse_indices = new int[g_indices.length];
			if (g_factorTable.hasSparseRepresentation())
			{
				for (int i = 0; i < g_indices.length; i++)
				{
					_g_sparse_indices[i] = g_factorTable.sparseIndexFromIndices(g_indices[i]);
				}
			}
			else
			{
				final JointDomainIndexer g_domainIndexer = g_factorTable.getDomainIndexer();
				for (int i = 0; i < g_indices.length; i++)
				{
					_g_sparse_indices[i] = g_domainIndexer.jointIndexFromIndices(g_indices[i]);
				}
			}
		}

		@Override
		public void apply(ISTableFactorSupportingOptimizedUpdate tableFactor)
		{
			final double[] f_values = _f.getValues().get();
			final double[] g_values = _g.getValues().get();
			final double[] inputMsg = tableFactor.getInPortMsgs()[_inPortNum];
			Arrays.fill(g_values, Double.POSITIVE_INFINITY);
			int n = 0;
			for (final double value : f_values)
			{
				final double input_value = inputMsg[_msg_indices[n]];
				final double v = value + input_value;
				final int index = _g_sparse_indices[n];
				if (v < g_values[index])
				{
					g_values[index] = v;
				}
				n += 1;
			}
		}

		@Override
		public TableWrapper getAuxiliaryTable()
		{
			return _g;
		}
	}

	static final class SparseOutputStep implements IUpdateStep
	{
		private final TableWrapper _f;

		private final int _outPortNum;

		private final IFactorTable _factorTable;

		SparseOutputStep(int outPortNum, final TableWrapper f)
		{
			_outPortNum = outPortNum;
			_f = f;
			_factorTable = _f.getFactorTable();
		}

		@Override
		public void apply(ISTableFactorSupportingOptimizedUpdate tableFactor)
		{
			final double[] outputMsg = tableFactor.getOutPortMsgs()[_outPortNum];

			final double damping = tableFactor.getDamping(_outPortNum);
			if (tableFactor.isDampingInUse() && damping != 0)
			{
				final double[] saved = tableFactor.getSavedOutMsgArray(_outPortNum);
				System.arraycopy(outputMsg, 0, saved, 0, outputMsg.length);
			}

			Arrays.fill(outputMsg, Double.POSITIVE_INFINITY);
			int sparseIndex = 0;
			double minPotential = Double.POSITIVE_INFINITY;
			for (final double prob : _f.getValues().get())
			{
				final int f_index = _factorTable.sparseIndexToJointIndex(sparseIndex);
				outputMsg[f_index] = prob;
				if (prob < minPotential)
				{
					minPotential = prob;
				}
				sparseIndex += 1;
			}

			final int outputMsgLength = outputMsg.length;
			if (tableFactor.isDampingInUse() && damping != 0)
			{
				final double[] saved = tableFactor.getSavedOutMsgArray(_outPortNum);
				for (int i = 0; i < outputMsgLength; i++)
				{
					outputMsg[i] = (1 - damping) * outputMsg[i] + damping * saved[i];
				}
			}

			for (int i = 0; i < outputMsgLength; i++)
			{
				outputMsg[i] -= minPotential;
			}
		}
	}

	static final class DenseOutputStep implements IUpdateStep
	{
		private final int _outPortNum;

		private final TableWrapper _f;

		DenseOutputStep(int outPortNum, final TableWrapper f)
		{
			_outPortNum = outPortNum;
			_f = f;
		}

		@Override
		public void apply(ISTableFactorSupportingOptimizedUpdate tableFactor)
		{
			final double[] outputMsg = tableFactor.getOutPortMsgs()[_outPortNum];

			final double damping = tableFactor.getDamping(_outPortNum);
			if (tableFactor.isDampingInUse())
			{
				if (damping != 0)
				{
					final double[] saved = tableFactor.getSavedOutMsgArray(_outPortNum);
					System.arraycopy(outputMsg, 0, saved, 0, outputMsg.length);
				}
			}

			double minPotential = Double.POSITIVE_INFINITY;
			int f_index = 0;
			for (final double prob : _f.getValues().get())
			{
				outputMsg[f_index] = prob;
				if (prob < minPotential)
				{
					minPotential = prob;
				}
				f_index += 1;
			}

			final int outputMsgLength = outputMsg.length;
			if (tableFactor.isDampingInUse())
			{
				if (damping != 0)
				{
					final double[] saved = tableFactor.getSavedOutMsgArray(_outPortNum);
					for (int i = 0; i < outputMsgLength; i++)
					{
						outputMsg[i] = (1 - damping) * outputMsg[i] + damping * saved[i];
					}
				}
			}

			for (int i = 0; i < outputMsgLength; i++)
			{
				outputMsg[i] -= minPotential;
			}
		}
	}

	static final class DenseMarginalizationStepEstimator implements IMarginalizationStepEstimator
	{
		private final CostEstimationTableWrapper _f;

		private final CostEstimationTableWrapper _g;

		private final int _d;

		private final int _p;

		DenseMarginalizationStepEstimator(final CostEstimationTableWrapper f,
			final int inPortNum,
			final int dimension,
			final CostEstimationTableWrapper g)
		{
			_f = f;
			final int[] dimensions = _f.getDimensions();
			_d = dimensions[dimension];
			_p = TableFactorEngineOptimized.getStride(dimensions, dimension);
			_g = g;
		}

		@Override
		public CostEstimationTableWrapper getAuxiliaryTable()
		{
			return _g;
		}

		@Override
		public Costs estimateCosts()
		{
			long accesses = 0;
			final double g_size = _g.getSize();
			final double f_size = _f.getSize();

			// fill g with +infinity
			accesses += g_size;

			// 1. read each f value
			// 2. read a g value
			accesses += 2 * f_size;

			// store a g value if new minimum
			// (always on 1st per message entry, then 50% chance)
			accesses += _d * (1 + (_p - 1) / 2);

			// read an input value each time the message index changes
			accesses += f_size / _p;

			Costs result = new Costs();
			result.put(CostType.ACCESSES, (double) accesses);

			// include auxiliary table costs
			result.add(_g.estimateCosts());
			return result;
		}
	}

	static final class SparseMarginalizationStepEstimator implements IMarginalizationStepEstimator
	{
		private final CostEstimationTableWrapper _f;

		private final CostEstimationTableWrapper _g;

		private final int _d;

		private final int _p;

		SparseMarginalizationStepEstimator(final CostEstimationTableWrapper f,
			final int inPortNum,
			final int dimension,
			final CostEstimationTableWrapper g)
		{
			_f = f;
			_g = g;
			final int[] dimensions = _f.getDimensions();
			_d = dimensions[dimension];
			_p = getStride(dimensions, dimension);
		}

		@Override
		public CostEstimationTableWrapper getAuxiliaryTable()
		{
			return _g;
		}

		@Override
		public Costs estimateCosts()
		{
			long accesses = 0;
			final double g_size = _g.getSize();
			final double f_size = _f.getSize();

			// fill g with +infinity
			accesses += g_size;

			// 1. read each f value
			// 2. read a message index
			// 3. read the input value at the message index
			// 4. read a g index
			// 5. read a g value at the g index
			accesses += f_size * 5;

			// write the g value if new minimum
			// (always on 1st per message entry, then 50% chance)
			accesses += _d * (1 + (_p - 1) / 2);

			Costs result = new Costs();
			result.put(CostType.ACCESSES, (double) accesses);

			// 4 bytes each entry for int arrays for message indices and g indices
			double allocations = (_d + g_size) * 4;
			result.put(CostType.ALLOCATED_BYTES, allocations);

			// include costs for auxiliary table
			result.add(_g.estimateCosts());

			return result;
		}
	}

	static final class DenseOutputStepEstimator implements IUpdateStepEstimator
	{
		private final CostEstimationTableWrapper _f;

		DenseOutputStepEstimator(final CostEstimationTableWrapper f)
		{
			_f = f;
		}

		@Override
		public Costs estimateCosts()
		{
			long accesses = 0;
			final int outputMsg_length = _f.getDimensions()[0];
			final double f_size = _f.getSize();

			// 1. read each f value
			// 2. write it to the output message
			accesses += f_size * 2;

			// read each output value
			// write it less minPotential
			accesses += outputMsg_length * 2;

			Costs result = new Costs();
			result.put(CostType.ACCESSES, (double) accesses);
			return result;
		}
	}

	static final class SparseOutputStepEstimator implements IUpdateStepEstimator
	{
		private final CostEstimationTableWrapper _f;

		SparseOutputStepEstimator(final CostEstimationTableWrapper f)
		{
			_f = f;
		}

		@Override
		public Costs estimateCosts()
		{
			long accesses = 0;
			final int outputMsg_length = _f.getDimensions()[0];
			final double f_size = _f.getSize();

			// 1. read each f value
			// 2. convert f sparse to joint index
			// 3. write output message entry
			accesses += f_size * 3;

			// 1. fill output message with +infinity
			// 2. read output message entry
			// 3. write output message entry less minPotential
			accesses += outputMsg_length * 3;

			Costs result = new Costs();
			result.put(CostType.ACCESSES, (double) accesses);
			return result;
		}
	}
}
