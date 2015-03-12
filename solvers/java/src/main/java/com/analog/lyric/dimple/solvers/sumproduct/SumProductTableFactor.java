/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.sumproduct;

import static com.analog.lyric.math.Utilities.*;
import static java.util.Objects.*;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.Selection;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraphEdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.core.STableFactorDoubleArray;
import com.analog.lyric.dimple.solvers.core.kbest.IKBestFactor;
import com.analog.lyric.dimple.solvers.core.kbest.KBestFactorEngine;
import com.analog.lyric.dimple.solvers.core.kbest.KBestFactorTableEngine;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.optimizedupdate.FactorTableUpdateSettings;
import com.analog.lyric.dimple.solvers.optimizedupdate.FactorUpdatePlan;
import com.analog.lyric.dimple.solvers.optimizedupdate.ISTableFactorSupportingOptimizedUpdate;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateApproach;
import com.analog.lyric.util.misc.Internal;

/**
 * Solver representation of table factor under Sum-Product solver.
 * 
 * @since 0.07
 */
public class SumProductTableFactor extends STableFactorDoubleArray
	implements IKBestFactor, ISTableFactorSupportingOptimizedUpdate
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
	protected @Nullable double [][][] _outPortDerivativeMsgs;
	protected double [] _dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	protected @Nullable TableFactorEngine _tableFactorEngine;
	protected KBestFactorEngine _kbestFactorEngine;
	protected int _k;
	protected boolean _kIsSmallerThanDomain = false;
	protected boolean _updateDerivative = false;
	protected boolean _dampingInUse = false;
	
	
	/*--------------
	 * Construction
	 */
	
	public SumProductTableFactor(Factor factor, ISolverFactorGraph parent)
	{
		super(factor, parent);

		//TODO: should I recheck for factor table every once in a while?
		if (factor.getFactorFunction().factorTableExists(getFactor()))
		{
			_kbestFactorEngine = new KBestFactorTableEngine(this);
		}
		else
		{
			_kbestFactorEngine = new KBestFactorEngine(this);
		}
	}

	@Override
	public void initialize()
	{
		super.initialize();
		
		configureDampingFromOptions();
		updateK(getOptionOrDefault(BPOptions.maxMessageSize));
	}
	
	@Internal
	public void setupTableFactorEngine()
	{
		FactorUpdatePlan updatePlan = null;
		final FactorTableUpdateSettings factorTableUpdateSettings = getFactorTableUpdateSettings();
		if (factorTableUpdateSettings != null)
		{
			updatePlan = factorTableUpdateSettings.getOptimizedUpdatePlan();
		}
		if (updatePlan != null)
		{
			_tableFactorEngine = new TableFactorEngineOptimized(this, updatePlan);
		}
		else
		{
			_tableFactorEngine = new TableFactorEngine(this);
		}
	}

	@Internal
	@Nullable FactorTableUpdateSettings getFactorTableUpdateSettings()
	{
		ISolverFactorGraph rootGraph = getRootSolverGraph();
		if (rootGraph instanceof SumProductSolverGraph)
		{
			final SumProductSolverGraph sfg = (SumProductSolverGraph) rootGraph;
			return sfg.getFactorTableUpdateSettings(getFactor());
		}
		return null;
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	private TableFactorEngine getTableFactorEngine()
	{
		final TableFactorEngine tableFactorEngine = _tableFactorEngine;
		if (tableFactorEngine != null)
		{
			return tableFactorEngine;
		}
		else
		{
			throw new DimpleException("The solver was not initialized. Use solve() or call initialize() before iterate().");
		}
	}

	@Override
	protected void doUpdate()
	{
		
		if (_kIsSmallerThanDomain)
			//TODO: damping
			_kbestFactorEngine.update();
		else
			getTableFactorEngine().update();
		
		if (_updateDerivative)
		{
			for (int i = 0, n = getSiblingCount(); i < n; i++)
				updateDerivative(i);
		}
		
	}
	
	@Override
	public void doUpdateEdge(int outPortNum)
	{
		
		if (_kIsSmallerThanDomain)
			_kbestFactorEngine.updateEdge(outPortNum);
		else
			getTableFactorEngine().updateEdge(outPortNum);

		if (_updateDerivative)
			updateDerivative(outPortNum);
		
	}
	
	/*-----------------------
	 * ISolverFactor methods
	 */
	
	/*
	 * (non-Javadoc)
	 * @see com.analog.lyric.dimple.solvers.core.SFactorBase#getBelief()
	 * 
	 * Calculates a piece of the beta free energy
	 */
	@Override
	public double [] getBelief()
	{
		double [] retval = getUnormalizedBelief();
		double sum = 0;
		for (int i = 0; i < retval.length; i++)
			sum += retval[i];
		for (int i = 0; i < retval.length; i++)
			retval[i] /= sum;
		return retval;
	}
	
	/*---------------
	 * SNode methods
	 */
	
	@Override
	public DiscreteMessage cloneMessage(int edge)
	{
		return getSiblingEdgeState(edge).factorToVarMsg.clone();
	}
	
	
	/*--------------------------
	 * STableFactorBase methods
	 */
	
	@Override
	protected void setTableRepresentation(IFactorTable table)
	{
		table.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT_WITH_INDICES);
	}
	
	@Override
	public boolean supportsMessageEvents()
	{
		return true;
	}
	
	/*-------------
	 * New methods
	 */
	
	@Deprecated
	public void setDamping(int index, double val)
	{
		double[] params  = BPOptions.nodeSpecificDamping.getOrDefault(this).toPrimitiveArray();
		if (params.length == 0 && val != 0.0)
		{
			params = new double[getSiblingCount()];
		}
		if (params.length != 0)
		{
			params[index] = val;
		}
		
		BPOptions.nodeSpecificDamping.set(this, params);
		configureDampingFromOptions();
	}
	
	@Override
	public double getDamping(int index)
	{
		return _dampingParams.length > 0 ? _dampingParams[index] : 0.0;
	}

	/**
	 * Enables use of the optimized update algorithm on this factor, if its degree is greater than
	 * 1. The optimized update algorithm is employed only when all of the factor's edges are updated
	 * together with an update call. If the schedule instead uses update_edge, the algorithm is not
	 * used.
	 * <p>
	 * This method is deprecated; instead set UpdateOptions.updateApproach.
	 * 
	 * @since 0.06
	 */
	@Deprecated
	public void enableOptimizedUpdate()
	{
		setOption(BPOptions.updateApproach, UpdateApproach.OPTIMIZED);
	}

	/**
	 * Disables use of the optimized update algorithm on this factor.
	 * <p>
	 * This method is deprecated; instead set UpdateOptions.updateApproach.
	 * 
	 * @see #enableOptimizedUpdate()
	 * @since 0.06
	 */
	@Deprecated
	public void disableOptimizedUpdate()
	{
		setOption(BPOptions.updateApproach, UpdateApproach.NORMAL);
	}

	/**
	 * Reverts to the default setting for enabling of the optimized update algorithm, eliminating
	 * the effect of previous calls to {@link #enableOptimizedUpdate()} or
	 * {@link #disableOptimizedUpdate()}.
	 * <p>
	 * This method is deprecated; instead reset UpdateOptions.updateApproach.
	 * 
	 * @since 0.06
	 */
	@Deprecated
	public void useDefaultOptimizedUpdateEnable()
	{
		unsetOption(BPOptions.updateApproach);
	}

	/**
	 * Returns the effective update approach for the factor. If the update approach is set to
	 * automatic, this value is not valid until the graph is initialized. Note that a factor
	 * with only one edge always employs the normal update approach.
	 * 
	 * @since 0.07
	 */
	public UpdateApproach getEffectiveUpdateApproach()
	{
		FactorTableUpdateSettings factorTableUpdateSettings = getFactorTableUpdateSettings();
		if (factorTableUpdateSettings != null && factorTableUpdateSettings.getOptimizedUpdatePlan() != null)
		{
			return UpdateApproach.OPTIMIZED;
		}
		else
		{
			return UpdateApproach.NORMAL;
		}
	}
	
	@Internal
	public @Nullable UpdateApproach getAutomaticUpdateApproach()
	{
		FactorTableUpdateSettings updateSettings = getFactorTableUpdateSettings();
		if (updateSettings != null)
		{
			return updateSettings.getAutomaticUpdateApproach();
		}
		return null;
	}

	public int getK()
	{
		return _k;
	}
	
	public void setK(int k)
	{
		setOption(BPOptions.maxMessageSize, k);
		updateK(k);
	}

	private void updateK(int k)
	{
		if (k != _k)
		{
			_k = k;
			_kbestFactorEngine.setK(k);
			_kIsSmallerThanDomain = false;
			for (Variable var : _model.getSiblings())
			{
				if (k < var.asDiscreteVariable().getDomain().size())
				{
					_kIsSmallerThanDomain = true;
					break;
				}
			}
		}
	}


	public void setUpdateDerivative(boolean updateDer)
	{
		_updateDerivative = updateDer;
	}
	
	
	
	public double [] getUnormalizedBelief()
	{
		final int [][] table = getFactorTable().getIndicesSparseUnsafe();
		final double [] values = getFactorTable().getWeightsSparseUnsafe();
		final int nEntries = values.length;
		final double [] retval = new double[nEntries];
		
		
		for (int i = 0; i < nEntries; i++)
		{
			retval[i] = values[i];
			
			final int[] indices = table[i];
			for (int j = 0; j < indices.length; j++)
			{
				retval[i] *= getSiblingEdgeState(j).varToFactorMsg.getWeight(indices[j]);
			}
		}
		
		return retval;
	}
	

	@Override
	public FactorFunction getFactorFunction()
	{
		return getFactor().getFactorFunction();
	}

	@Override
	public double initAccumulator()
	{
		return 1;
	}

	@Override
	public double accumulate(double oldVal, double newVal)
	{
		return oldVal*newVal;
	}

	@Override
	public double combine(double oldVal, double newVal)
	{
		return oldVal+newVal;
	}

	@Override
	public void normalize(double[] outputMsg)
	{
		double sum = 0;
		for (int i = 0; i < outputMsg.length; i++)
			sum += outputMsg[i];
		
		if (sum == 0)
			throw new DimpleException("Update failed in SumProduct Solver.  All probabilities were zero when calculating message for port "
					+ " on factor " +_model.getLabel());

		for (int i = 0; i < outputMsg.length; i++)

			outputMsg[i] /= sum;
	}

	@Override
	public double evalFactorFunction(Object[] inputs)
	{
		return getFactor().getFactorFunction().eval(inputs);
	}

	@Override
	public void initMsg(double[] msg)
	{
		Arrays.fill(msg, 0);
	}

	@Override
	public double getFactorTableValue(int index)
	{
		return getFactorTable().getWeightsSparseUnsafe()[index];
	}

	@Override
	public int[] findKBestForMsg(double[] msg, int k)
	{
		return Selection.findLastKIndices(msg, k);
	}

	/******************************************************
	 * Energy, Entropy, and derivatives of all that.
	 ******************************************************/


	@Override
	public double getInternalEnergy()
	{
		final double [] beliefs = getBelief();
		final double [] weights = getFactorTable().getWeightsSparseUnsafe();
		
		double sum = 0;
		for (int i = beliefs.length; --i>=0;)
		{
			sum += beliefs[i] * weightToEnergy(weights[i]);
		}
		
		return sum;
	}
	
	@Override
	public double getBetheEntropy()
	{
		double sum = 0;
		
		final double [] beliefs = getBelief();
		for (double belief : beliefs)
		{
			sum -= belief * Math.log(belief);
		}
		
		return sum;
	}
	
	@SuppressWarnings("null")
	public double calculateDerivativeOfInternalEnergyWithRespectToWeight(int weightIndex)
	{
		SumProductSolverGraph sfg = (SumProductSolverGraph)getRootSolverGraph();
		
		boolean isFactorOfInterest = sfg.getCurrentFactorTable() == getFactor().getFactorTable();
		
		double [] weights = _model.getFactorTable().getWeightsSparseUnsafe();
		//TODO: avoid recompute
		double [] beliefs = getBelief();
		
		double sum = 0;
		
		for (int i = 0; i < weights.length; i++)
		{
			//beliefs = getUnormalizedBelief();
	
			//Belief'(weightIndex)*(-log(weight(weightIndex))) + Belief(weightIndex)*(-log(weight(weightIndex)))'
			//(-log(weight(weightIndex)))' = - 1 / weight(weightIndex)
			double mlogweight = -Math.log(weights[i]);
			double belief = beliefs[i];
			double mlogweightderivative = 0;
			if (i == weightIndex && isFactorOfInterest)
				mlogweightderivative = -1.0 / weights[weightIndex];
			double beliefderivative = calculateDerivativeOfBeliefWithRespectToWeight(weightIndex,i,isFactorOfInterest);
			sum += beliefderivative*mlogweight + belief*mlogweightderivative;
			//sum += beliefderivative;
		}
		//return beliefderivative*mlogweight + belief*mlogweightderivative;
	
		return sum;
	}
	
	@SuppressWarnings("null")
	public double calculateDerivativeOfBeliefNumeratorWithRespectToWeight(int weightIndex, int index, boolean isFactorOfInterest)
	{
		final int nEdges = getSiblingCount();
		final IFactorTable table = _model.getFactorTable();
		final double weight = table.getWeightForSparseIndex(index);
		final int [] entryIndices = table.getIndicesSparseUnsafe()[index];
		
		//calculate product of messages and phi
		double prod = weight;
		for (int i = 0; i < nEdges; i++)
		{
			prod *= getSiblingEdgeState(i).varToFactorMsg.getWeight(entryIndices[i]);
		}

		double sum = 0;
		
		//if index == weightIndex, add in this term
		if (index == weightIndex && isFactorOfInterest)
		{
			sum = prod / weight;
		}
		
		final Factor factor = getFactor();
		
		//for each variable
		for (int i = 0; i < nEdges; i++)
		{
			final FactorGraphEdgeState edge = factor.getSiblingEdgeState(i);
			final SumProductDiscrete var = (SumProductDiscrete)getSibling(i);
			
			//divide out contribution
			final int j = entryIndices[i];
			sum += prod / getSiblingEdgeState(i).varToFactorMsg.getWeight(j) *
				var.getMessageDerivative(weightIndex, edge.getVariableToFactorIndex())[j];
		}
		return sum;
	}
	
	public double calculateDerivativeOfBeliefDenomenatorWithRespectToWeight(int weightIndex, int index, boolean isFactorOfInterest)
	{
		double sum = 0;
		for (int i = 0, end = getFactor().getFactorTable().sparseSize(); i < end; i++)
			sum += calculateDerivativeOfBeliefNumeratorWithRespectToWeight(weightIndex,i,isFactorOfInterest);
		return sum;
	}
	
	public double calculateDerivativeOfBeliefWithRespectToWeight(int weightIndex,int index, boolean isFactorOfInterest)
	{
		double [] un = getUnormalizedBelief();
		double f = un[index];
		double fderivative = calculateDerivativeOfBeliefNumeratorWithRespectToWeight(weightIndex, index,isFactorOfInterest);
		double gderivative = calculateDerivativeOfBeliefDenomenatorWithRespectToWeight(weightIndex, index,isFactorOfInterest);
		double g = 0;
		for (int i = 0; i < un.length; i++)
			g += un[i];
		
		double tmp = (fderivative*g-f*gderivative)/(g*g);
		return tmp;
	}
	
	public void initializeDerivativeMessages(int weights)
	{
		final int nEdges = getSiblingCount();
		final double[][][] msgs = _outPortDerivativeMsgs = new double[weights][nEdges][];
		for (int i = 0; i < weights; i++)
		{
			for (int j = 0; j < nEdges; j++)
			{
				msgs[i][j] = new double[getSiblingDimension(j)];
			}
		}
	}
	
	/**
	 * @deprecated instead use {@link #getMessageDerivative(int, int)}
	 */
	@Deprecated
	public double [] getMessageDerivative(int wn, Variable var)
	{
		return getMessageDerivative(wn, _model.getPortNum(var));
	}
	
	public double[] getMessageDerivative(int wn, int edgeNumber)
	{
		return requireNonNull(_outPortDerivativeMsgs)[wn][edgeNumber];
	}
	
	public double calculateMessageForDomainValueAndTableIndex(int domainValue, int outPortNum, int tableIndex)
	{
		IFactorTable ft = getFactor().getFactorTable();
		int [] entryIndices = ft.getIndicesSparseUnsafe()[tableIndex];

		if (entryIndices[outPortNum] == domainValue)
		{
			final double weight = ft.getWeightForSparseIndex(tableIndex);
			double prod = weight;
			for (int j = 0, n = getSiblingCount(); j < n; j++)
			{
				if (outPortNum != j)
				{
					prod *= getSiblingEdgeState(j).varToFactorMsg.getWeight(entryIndices[j]);
				}
			}
			
			return prod;
		}
		else
			return 0;
	}
	
	public double calculateMessageForDomainValue(int domainValue, int outPortNum)
	{
		IFactorTable ft = getFactor().getFactorTable();
		double sum = 0;
		int [][] indices = ft.getIndicesSparseUnsafe();
		
		for (int i = 0, end = ft.sparseSize(); i < end; i++)
			if (indices[i][outPortNum] == domainValue)
				sum += calculateMessageForDomainValueAndTableIndex(domainValue,outPortNum,i);
		
		return sum;
	}
	
	
	public double calculatedf(int outPortNum, int domainValue, int wn, boolean factorUsesTable)
	{
		final Factor factor = _model;
		final IFactorTable ft = factor.getFactorTable();
		double sum = 0;
		final int [][] indices = ft.getIndicesSparseUnsafe();
		final double [] weights = ft.getWeightsSparseUnsafe();
		final int nEdges = getSiblingCount();
		
		for (int si = 0, n = indices.length; si < n; si++)
		{
			final int[] entryIndices = indices[si];
			
			if (entryIndices[outPortNum] == domainValue)
			{
				double prod = calculateMessageForDomainValueAndTableIndex(domainValue,outPortNum,si);
				
				if (factorUsesTable && (wn == si))
				{
					sum += prod/weights[si];
				}
				
				for (int i = 0; i < nEdges; i++)
				{
					
					if (i != outPortNum)
					{
						FactorGraphEdgeState edge = factor.getSiblingEdgeState(i);
						SumProductDiscrete sv = (SumProductDiscrete)getSibling(i);
						double [] dvar = sv.getMessageDerivative(wn, edge.getVariableToFactorIndex());
								
						final int j = entryIndices[i];
						sum += (prod / getSiblingEdgeState(i).varToFactorMsg.getWeight(j)) * dvar[j];
					}
				}
								
			}
		}
		
		return sum;
	}
	
	public double calculatedg(int outPortNum, int wn, boolean factorUsesTable)
	{
		double sum = 0;
		for (int i = 0, n = getSiblingDimension(outPortNum); i < n; i++)
			sum += calculatedf(outPortNum,i,wn,factorUsesTable);
		
		return sum;
				
	}
	
	public void updateDerivativeForWeightAndDomain(int outPortNum, int wn, int d,boolean factorUsesTable)
	{
		//TODO: not re-using computation efficiently.
		
		//calculate f
		double f = calculateMessageForDomainValue(d,outPortNum);
		
		//calculate g
		double g = 0;
		for (int i = 0, n = getSiblingDimension(outPortNum); i < n; i++)
			g += calculateMessageForDomainValue(i,outPortNum);
		
		double derivative = 0;
		if (g != 0)
		{
			double df = calculatedf(outPortNum,d,wn,factorUsesTable);
			double dg = calculatedg(outPortNum,wn,factorUsesTable);
		
			
			//derivative = df;
			derivative = (df*g - f*dg) / (g*g);
			
		}
		
		Objects.requireNonNull(_outPortDerivativeMsgs)[wn][outPortNum][d] = derivative;
		
		
	}
	
	public void updateDerivativeForWeight(int outPortNum, int wn,boolean factorUsesTable)
	{
		int D = getSiblingDimension(outPortNum);
		
		for (int d = 0; d < D; d++)
		{
			updateDerivativeForWeightAndDomain(outPortNum,wn,d,factorUsesTable);
		}
	}
	
	public void updateDerivative(int outPortNum)
	{
		SumProductSolverGraph sfg = (SumProductSolverGraph)getRootSolverGraph();
		@SuppressWarnings("null")
		IFactorTable ft = sfg.getCurrentFactorTable();
		@SuppressWarnings("null")
		int numWeights = ft.sparseSize();
		
		for (int wn = 0; wn < numWeights; wn++)
		{
			updateDerivativeForWeight(outPortNum,wn,ft == getFactor().getFactorTable());
		}
	}
	
	public double calculateDerivativeOfBetheEntropyWithRespectToWeight(int weightIndex)
	{
		
		@SuppressWarnings("null")
		boolean isFactorOfInterest = ((SumProductSolverGraph)getRootSolverGraph()).getCurrentFactorTable() == getFactor().getFactorTable();
				
		//Belief'(weightIndex)*(-log(Belief(weightIndex))) + Belief(weightIndex)*(-log(Belief(weightIndex)))'
		double [] beliefs = getBelief();
		double sum = 0;
		for (int i = 0; i < beliefs.length; i++)
		{
			double beliefderivative = calculateDerivativeOfBeliefWithRespectToWeight(weightIndex,i,isFactorOfInterest);
			double belief = beliefs[i];
			sum += beliefderivative*(-Math.log(belief)) - beliefderivative;
		}
		return sum;
	}

	// FIXME eliminate this method
	@Override
	public double[][] getInPortMsgs()
	{
		final int nSiblings = getSiblingCount();
		final double[][] messages = new double[nSiblings][];
		for (int i = 0; i < nSiblings; ++i)
		{
			messages[i] = getSiblingEdgeState(i).varToFactorMsg.representation();
		}
		return messages;
	}

	// FIXME eliminate this method
	@Override
	public double[][] getOutPortMsgs()
	{
		final int nSiblings = getSiblingCount();
		final double[][] messages = new double[nSiblings][];
		for (int i = 0; i < nSiblings; ++i)
		{
			messages[i] = getSiblingEdgeState(i).factorToVarMsg.representation();
		}
		return messages;
	}

	@Override
	public boolean isDampingInUse()
	{
		return _dampingInUse;
	}

	/*------------------
	 * Internal methods
	 */
	
	protected void configureDampingFromOptions()
	{
		final int size = getSiblingCount();

		_dampingParams =
			getReplicatedNonZeroListFromOptions(BPOptions.nodeSpecificDamping, BPOptions.damping, size,
				_dampingParams);

		if (_dampingParams.length > 0 && _dampingParams.length != size)
		{
			DimpleEnvironment.logWarning("%s has wrong number of parameters for %s\n",
				BPOptions.nodeSpecificDamping, this);
			_dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		}

		_dampingInUse = _dampingParams.length > 0;
	}

	@SuppressWarnings("null")
	@Override
	public SumProductDiscreteEdge getSiblingEdgeState(int siblingIndex)
	{
		return (SumProductDiscreteEdge)getSiblingEdgeState_(siblingIndex);
	}
}
