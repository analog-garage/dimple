/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.Arrays;

import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.exceptions.DimpleException;
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

/**
 * Implements a factor update approach for sum-product that optimally shares partial results. Since
 * it computes all output edges, it only overrides the update method, and does not override the
 * update_edge method.
 * 
 * @since 0.06
 * @author jking
 */
public class TableFactorEngineOptimized extends TableFactorEngine
{
	/**
	 * The update plan for the factor related to this update engine.
	 */
	private final FactorUpdatePlan _updatePlan;

	/**
	 * Dimple creates an instance of this class per factor.
	 * 
	 * @param tableFactor
	 * @since 0.06
	 */
	public TableFactorEngineOptimized(SumProductTableFactor tableFactor, FactorUpdatePlan updatePlan)
	{
		super(tableFactor);
		_updatePlan = updatePlan;
	}

	@Override
	public void update()
	{
		_updatePlan.apply(_tableFactor);
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
			Arrays.fill(g_values, 0.0);
			int c = 0;
			int msg_index = 0;
			int g_index = 0;
			int g_index_limit = _p;
			double input_value = inputMsg[0];
			for (final double value : f_values)
			{
				g_values[g_index] += value * input_value;
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
			Arrays.fill(g_values, 0.0);
			int n = 0;
			for (final double value : f_values)
			{
				final double input_value = inputMsg[_msg_indices[n]];
				g_values[_g_sparse_indices[n]] += value * input_value;
				n += 1;
			}
		}

		@Override
		public TableWrapper getAuxiliaryTable()
		{
			return _g;
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
			if (damping != 0)
			{
					final double[] saved = tableFactor.getSavedOutMsgArray(_outPortNum);
				System.arraycopy(outputMsg, 0, saved, 0, outputMsg.length);
			}

			double sum = 0.0;
			int f_index = 0;
			for (final double prob : _f.getValues().get())
			{
				outputMsg[f_index] = prob;
				sum += prob;
				f_index += 1;
			}

			if (sum == 0)
			{
				throw new DimpleException(
					"Update failed in SumProduct Solver.  All probabilities were zero when calculating message for port "
						+ _outPortNum + " on factor " + tableFactor.getFactor().getLabel());
			}

			final int outputMsgLength = outputMsg.length;
			for (int i = 0; i < outputMsgLength; i++)
			{
				outputMsg[i] /= sum;
			}

			if (damping != 0)
			{
					final double[] saved = tableFactor.getSavedOutMsgArray(_outPortNum);
				for (int i = 0; i < outputMsgLength; i++)
				{
					outputMsg[i] = (1 - damping) * outputMsg[i] + damping * saved[i];
				}
			}
		}
	}

	static final class SparseOutputStep implements IUpdateStep
	{
		private final TableWrapper _f;

		private final int _outPortNum;

		private final IFactorTable _factorTable;

		/**
		 * @param outPortNum
		 * @since 0.06
		 */
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
			if (damping != 0)
			{
				final double[] saved = tableFactor.getSavedOutMsgArray(_outPortNum);
				System.arraycopy(outputMsg, 0, saved, 0, outputMsg.length);
			}

			double sum = 0.0;
			Arrays.fill(outputMsg, 0);
			int sparseIndex = 0;
			for (final double prob : _f.getValues().get())
			{
				final int f_index = _factorTable.sparseIndexToJointIndex(sparseIndex);
				outputMsg[f_index] = prob;
				sum += prob;
				sparseIndex += 1;
			}

			if (sum == 0)
			{
				throw new DimpleException(
					"Update failed in SumProduct Solver.  All probabilities were zero when calculating message for port "
						+ _outPortNum + " on factor " + tableFactor.getFactor().getLabel());
			}

			final int outputMsgLength = outputMsg.length;
			for (int i = 0; i < outputMsgLength; i++)
			{
				outputMsg[i] /= sum;
			}

			if (damping != 0)
			{
				final double[] saved = tableFactor.getSavedOutMsgArray(_outPortNum);
				for (int i = 0; i < outputMsgLength; i++)
				{
					outputMsg[i] = (1 - damping) * outputMsg[i] + damping * saved[i];
				}
			}
		}
	}

	static final class DenseMarginalizationStepEstimator implements IMarginalizationStepEstimator
	{
		private final CostEstimationTableWrapper _f;

		private final CostEstimationTableWrapper _g;

		DenseMarginalizationStepEstimator(final CostEstimationTableWrapper f,
			final int inPortNum,
			final int dimension,
			final CostEstimationTableWrapper g)
		{
			_f = f;
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
			Costs result = new Costs();
			result.put(CostType.DENSE_MARGINALIZATION_SIZE, _f.getSize());
			result.add(_g.estimateCosts());
			return result;
		}
	}

	static final class SparseMarginalizationStepEstimator implements IMarginalizationStepEstimator
	{
		private final CostEstimationTableWrapper _f;

		private final CostEstimationTableWrapper _g;

		private final int _msg_indices_length;

		SparseMarginalizationStepEstimator(final CostEstimationTableWrapper f,
			final int inPortNum,
			final int dimension,
			final CostEstimationTableWrapper g)
		{
			_f = f;
			_g = g;
			_msg_indices_length = (int) _f.getSize();
		}

		@Override
		public CostEstimationTableWrapper getAuxiliaryTable()
		{
			return _g;
		}

		@Override
		public Costs estimateCosts()
		{
			Costs result = new Costs();
			result.put(CostType.SPARSE_MARGINALIZATION_SIZE, _f.getSize());

			// allocate 4 bytes each entry (int arrays) for 1. message indices and 2. g indices
			final double g_size = _g.getSize();
			double allocations = (_msg_indices_length + g_size) * 4 / 1024.0 / 1024.0 / 1024.0;
			result.put(CostType.MEMORY, allocations);

			// add costs from auxiliary table
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
			Costs result = new Costs();
			result.put(CostType.OUTPUT_SIZE, _f.getSize());
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
			Costs result = new Costs();
			result.put(CostType.OUTPUT_SIZE, _f.getSize());
			return result;
		}
	}
}
