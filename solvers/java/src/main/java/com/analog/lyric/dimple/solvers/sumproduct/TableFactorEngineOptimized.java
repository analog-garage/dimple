/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.math.Utilities;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.primitives.Ints;

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
	 * A table of existing update plans, per solver graph, keyed by factor table. Factors that share
	 * a factor table share an update plan.
	 */
	private static HashMap<SFactorGraph, HashMap<IFactorTable, FactorUpdatePlan>> _updatePlans =
		new HashMap<SFactorGraph, HashMap<IFactorTable, FactorUpdatePlan>>();

	/**
	 * When a solver graph is initialized, we create an update plan for each factor table. This
	 * method clears this map. Note that it does not clear TableFactorEngineOptimized instances' update
	 * plans.
	 * 
	 * @since 0.06
	 */
	public static void clearUpdatePlans(SFactorGraph factorGraph)
	{
		HashMap<IFactorTable, FactorUpdatePlan> table = _updatePlans.get(factorGraph);
		if (table != null)
		{
			table.clear();
		}
	}

	/**
	 * The update plan for the factor related to this update engine.
	 */
	private final FactorUpdatePlan _updatePlan;

	/**
	 * @see #getSparseThreshold
	 */
	private static double _sparse_threshold = 1.0;

	/**
	 * Gets the threshold used for determining whether to use a sparse or dense representation for
	 * an incomplete table. Incomplete tables with a quantity of non-zero entries that is not less
	 * than the product of this threshold and the table's dimensions will use a dense
	 * representation.
	 * 
	 * @since 0.06
	 */
	public static double getSparseThreshold()
	{
		return _sparse_threshold;
	}

	/**
	 * @see #getSparseThreshold
	 * @since 0.06
	 */
	public static void setSparseThreshold(double sparse_threshold)
	{
		_sparse_threshold = sparse_threshold;
	}

	/**
	 * Dimple creates an instance of this class per factor.
	 * 
	 * @param tableFactor
	 * @since 0.06
	 */
	public TableFactorEngineOptimized(STableFactor tableFactor)
	{
		super(tableFactor);
		_updatePlan = lookupOrCreateUpdatePlan(_tableFactor.getFactorTable());
	}

	/**
	 * We store the update plans, keyed by factor table. This method looks up an update plan based
	 * on a factor table, and if not found, creates one for it and adds it to the table.
	 * 
	 * @param factorTable
	 * @since 0.06
	 */
	private FactorUpdatePlan lookupOrCreateUpdatePlan(final IFactorTable factorTable)
	{
		SFactorGraph factorGraph = (SFactorGraph) _tableFactor.getParentGraph();
		HashMap<IFactorTable, FactorUpdatePlan> table = _updatePlans.get(factorGraph);
		if (table == null)
		{
			table = new HashMap<IFactorTable, FactorUpdatePlan>();
			_updatePlans.put(factorGraph, table);
		}
		FactorUpdatePlan updatePlan = table.get(factorTable);
		if (updatePlan == null)
		{
			updatePlan = new FactorUpdatePlan(factorTable);
			table.put(factorTable, updatePlan);
		}
		return updatePlan;
	}

	@Override
	public void update()
	{
		_updatePlan.apply(_tableFactor);
	}

	/**
	 * A plan for updating a factor. Each factor table has a unique plan, but factors that share a
	 * factor table share a plan. A plan consists of a list of steps to perform, given a factor.
	 * 
	 * @since 0.06
	 * @author jking
	 */
	private final static class FactorUpdatePlan
	{
		private final List<UpdateStep> _steps;

		/**
		 * Constructs an update plan for a given factor table. The same plan may be applied to any
		 * factor that uses the same factor table.
		 * 
		 * @since 0.06
		 */
		public FactorUpdatePlan(IFactorTable factorTable)
		{
			final int n = factorTable.getDimensions();
			_steps = new ArrayList<UpdateStep>((int) (n * Utilities.log2((double) n))); // Initial
																						// capacity
																						// is not
																						// exact but
																						// OK.
			final int order = factorTable.getDomainIndexer().size();
			loop(0, 1, new TableWrapper(factorTable), order);
		}

		/**
		 * Applies an update plan to a factor. The factor must use the same factor table as used to
		 * construct the plan.
		 * 
		 * @since 0.06
		 */
		public void apply(STableFactor tableFactor)
		{
			for (final UpdateStep step : _steps)
			{
				step.apply(tableFactor);
			}
		}

		/**
		 * Along with loop2, performs the tree recursion that produces an update plan. Given a
		 * factor function of N variables, the algorithm splits the variables into two groups of
		 * equal size. A new factor function is formed for each of these groups by marginalizing the
		 * variables in the group, pulling in the input from each variable as it is considered. This
		 * process is repeated recursively until single-variable tables remain, at which point the
		 * table's values contain the output message.
		 * 
		 * @since 0.06
		 */
		private void loop(final int p, int step, final TableWrapper f, final int order)
		{
			final int left = p;
			final int right = p + step;
			loop2(left, right, step * 2, f, order);
			if (right < order)
			{
				loop2(right, left, step * 2, f, order);
			}
		}

		/**
		 * Along with loop, performs the tree recursion that produces an update plan.
		 * 
		 * @see #loop
		 * @since 0.06
		 */
		private void loop2(final int x, final int y, final int step, final TableWrapper f, final int order)
		{
			final int offset = x > y ? 1 : 0;
			TableWrapper g = f;
			for (int i = 0; x + i * step < order; i++)
			{
				final MarginalizationStep marginalizeStep = g.createMarginalizationStep(x + i * step, i + offset);
				_steps.add(marginalizeStep);
				g = marginalizeStep.getAuxiliaryTable();
			}
			if (y + step < order)
			{
				loop(y, step, g, order);
			}
			else
			{
				UpdateStep outputStep = g.createOutputStep(y);
				_steps.add(outputStep);
			}
		}
	}

	/**
	 * A step in an update plan.
	 * 
	 * @since 0.06
	 * @author jking
	 */
	private interface UpdateStep
	{
		/**
		 * Applies the update step to a particular factor.
		 * 
		 * @since 0.06
		 */
		void apply(STableFactor tableFactor);
	}

	/**
	 * Represents the computation performed by a marginalization step.
	 * 
	 * @since 0.06
	 * @author jking
	 */
	private interface MarginalizationStep extends UpdateStep
	{
		/**
		 * Gets the table produced by the marginalization step.
		 * 
		 * @since 0.06
		 */
		TableWrapper getAuxiliaryTable();
	}

	/**
	 * @since 0.06
	 * @author jking
	 */
	private static final class DenseMarginalizationStep implements MarginalizationStep
	{
		private final TableWrapper _f;

		private final TableWrapper _g;

		private final int _inPortNum;

		private final int _d;

		private final int _p;

		/**
		 * @param inPortNum
		 * @param d
		 * @param p
		 * @param g
		 * @param g_values
		 * @since 0.06
		 */
		private DenseMarginalizationStep(final TableWrapper f,
			final int inPortNum,
			final int dimension,
			final IFactorTable g_factorTable)
		{
			_f = f;
			JointDomainIndexer f_indexer = f.get_factorTable().getDomainIndexer();
			_p = f_indexer.getStride(dimension);
			_d = f_indexer.getDomainSize(dimension);
			_g = new TableWrapper(g_factorTable, true);
			_inPortNum = inPortNum;
		}

		@Override
		public void apply(STableFactor tableFactor)
		{
			final double[] f_values = _f._values.get();
			final double[] g_values = _g._values.get();
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

	/**
	 * @since 0.06
	 * @author jking
	 */
	private static final class SparseMarginalizationStep implements MarginalizationStep
	{
		private final TableWrapper _f;

		private final int _inPortNum;

		private final TableWrapper _g;

		private final int[] _msg_indices;

		private final int[] _g_sparse_indices;

		/**
		 * @param inPortNum
		 * @param msg_indices
		 * @param g
		 * @param g_sparse_indices
		 * @param g_values
		 * @since 0.06
		 */
		private SparseMarginalizationStep(final TableWrapper f,
			final int inPortNum,
			final int dimension,
			final IFactorTable g_factorTable,
			final Tuple2<int[][], int[]> g_and_msg_indices)
		{
			_f = f;
			_inPortNum = inPortNum;
			_g = new TableWrapper(g_factorTable, true);
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
		public void apply(STableFactor tableFactor)
		{
			final double[] f_values = _f._values.get();
			final double[] g_values = _g._values.get();
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

	private static final class DenseOutputStep implements UpdateStep
	{
		private final int _outPortNum;

		private final TableWrapper _f;

		/**
		 * @param outPortNum
		 * @since 0.06
		 */
		private DenseOutputStep(int outPortNum, final TableWrapper f)
		{
			_outPortNum = outPortNum;
			_f = f;
		}

		@Override
		public void apply(STableFactor tableFactor)
		{
			final double[] outputMsg = tableFactor.getOutPortMsgs()[_outPortNum];

			final double damping = tableFactor._dampingParams[_outPortNum];
			if (tableFactor._dampingInUse)
			{
				if (damping != 0)
				{
					final double[] saved = tableFactor._savedOutMsgArray[_outPortNum];
					System.arraycopy(outputMsg, 0, saved, 0, outputMsg.length);
				}
			}

			double sum = 0.0;
			int f_index = 0;
			for (final double prob : _f._values.get())
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

			if (tableFactor._dampingInUse)
			{
				if (damping != 0)
				{
					final double[] saved = tableFactor._savedOutMsgArray[_outPortNum];
					for (int i = 0; i < outputMsgLength; i++)
					{
						outputMsg[i] = (1 - damping) * outputMsg[i] + damping * saved[i];
					}
				}
			}
		}
	}

	private static final class SparseOutputStep implements UpdateStep
	{
		private final TableWrapper _f;

		private final int _outPortNum;

		private final IFactorTable _factorTable;

		/**
		 * @param outPortNum
		 * @since 0.06
		 */
		private SparseOutputStep(int outPortNum, final TableWrapper f)
		{
			_outPortNum = outPortNum;
			_f = f;
			_factorTable = _f.get_factorTable();
		}

		@Override
		public void apply(STableFactor tableFactor)
		{
			final double[] outputMsg = tableFactor.getOutPortMsgs()[_outPortNum];

			final double damping = tableFactor._dampingParams[_outPortNum];
			if (tableFactor._dampingInUse && damping != 0)
			{
				final double[] saved = tableFactor._savedOutMsgArray[_outPortNum];
				System.arraycopy(outputMsg, 0, saved, 0, outputMsg.length);
			}

			double sum = 0.0;
			Arrays.fill(outputMsg, 0);
			int sparseIndex = 0;
			for (final double prob : _f._values.get())
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

			if (tableFactor._dampingInUse && damping != 0)
			{
				final double[] saved = tableFactor._savedOutMsgArray[_outPortNum];
				for (int i = 0; i < outputMsgLength; i++)
				{
					outputMsg[i] = (1 - damping) * outputMsg[i] + damping * saved[i];
				}
			}
		}
	}

	/**
	 * @since 0.06
	 * @author jking
	 */
	private static final class TableWrapper
	{
		private final IFactorTable _factorTable;

		private final ThreadLocal<double[]> _values;

		private final int _size;

		private final boolean _isSparse;

		public TableWrapper(IFactorTable factorTable, boolean useThreadLocalValues)
		{
			_factorTable = factorTable;
			_isSparse = factorTable.hasSparseRepresentation();
			if (!useThreadLocalValues)
			{
				final double[] values;
				if (_isSparse)
				{
					values = factorTable.getWeightsSparseUnsafe();
				}
				else
				{
					// Because useThreadLocalValues is only false at the root of the tree, with
					// factor tables from the model,
					// and because the sum-product solver sets those tables' representation to
					// sparse, it is most likely
					// impossible to hit this branch. But just in case...
					values = factorTable.getWeightsDenseUnsafe();
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
					_size = factorTable.getWeightsSparseUnsafe().length;
				}
				else
				{
					_size = factorTable.getWeightsDenseUnsafe().length;
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

		public TableWrapper(final IFactorTable factorTable)
		{
			this(factorTable, false);
		}

		public UpdateStep createOutputStep(final int outPortNum)
		{
			if (_isSparse)
			{
				return new SparseOutputStep(outPortNum, this);
			}
			else
			{
				return new DenseOutputStep(outPortNum, this);
			}
		}

		private Tuple2<int[][], int[]> process_f_indices(final int dimension, final IFactorTable g_factorTable)
		{
			final int[][] all_f_indices = get_factorTable().getIndicesSparseUnsafe();
			final int[] _msg_indices = new int[_size];
			final int[][] g_indices = new int[_size][];
			int n = 0;
			for (final int[] f_indices : all_f_indices)
			{
				_msg_indices[n] = f_indices[dimension];
				g_indices[n] = remove_entry(f_indices, dimension);
				n += 1;
			}

			// The next section of this function sets the weight for each of the used g_indices to a
			// non-zero value.
			Comparator<int[]> comparator = new Comparator<int[]>() {
				@Override
				public int compare(int[] o1, int[] o2)
				{
					int o1l = o1.length;
					int o2l = o2.length;
					if (o2l != o1l)
					{
						return Ints.compare(o2l, o1l);
					}
					else
					{
						for (int i = o1l - 1; i >= 0; i--)
						{
							int x = Ints.compare(o1[i], o2[i]);
							if (x != 0)
							{
								return x;
							}
						}
						return 0;
					}
				}
			};
			ImmutableList<int[]> all_g_indices =
				ImmutableSortedSet.copyOf(comparator, Arrays.asList(g_indices)).asList();
			int[][] indices = new int[all_g_indices.size()][];
			all_g_indices.toArray(indices);
			double[] weights = new double[indices.length];
			Arrays.fill(weights, 1.0);
			g_factorTable.setWeightsSparse(indices, weights);
			
			return new Tuple2<int[][], int[]>(g_indices, _msg_indices);
		}

		public MarginalizationStep createMarginalizationStep(final int inPortNum, final int dimension)
		{
			final JointDomainIndexer f_indexer = _factorTable.getDomainIndexer();
			final JointDomainReindexer g_remover = JointDomainReindexer.createRemover(f_indexer, dimension);
			final JointDomainIndexer g_indexer = g_remover.getToDomains();
			IFactorTable g_factorTable = FactorTable.create(g_indexer);
			if (_isSparse)
			{
				Tuple2<int[][], int[]> g_and_msg_indices = process_f_indices(dimension, g_factorTable);
				if (g_indexer.supportsJointIndexing()
					&& useDense(g_factorTable.countNonZeroWeights(), g_indexer.getCardinality()))
				{
					g_factorTable.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
				}
				return new SparseMarginalizationStep(this, inPortNum, dimension, g_factorTable, g_and_msg_indices);
			}
			else
			{
				g_factorTable.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
				return new DenseMarginalizationStep(this, inPortNum, dimension, g_factorTable);
			}
		}

		public IFactorTable get_factorTable()
		{
			return _factorTable;
		}
	}

	/**
	 * Decides if a dense or sparse table should be used, based on how full the table will be and a
	 * configurable threshold.
	 * 
	 * @param cardinality How many non-zero entries the table will hold.
	 * @param size The product of the table's dimensions.
	 * @return true if the table should be implemented with a dense representation.
	 * @since 0.06
	 */
	private static final boolean useDense(int cardinality, int size)
	{
		return cardinality >= size * _sparse_threshold;
	}

	/**
	 * Utility to remove an element from an int array.
	 * 
	 * @param entries
	 * @param index
	 * @return
	 * @since 0.06
	 */
	private static int[] remove_entry(final int[] entries, final int index)
	{
		final int[] result = new int[entries.length - 1];
		System.arraycopy(entries, 0, result, 0, index);
		System.arraycopy(entries, index + 1, result, index, result.length - index);
		return result;
	}
}
