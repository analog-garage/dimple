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

import java.util.Arrays;
import java.util.Objects;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.Selection;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.STableFactorDoubleArray;
import com.analog.lyric.dimple.solvers.core.kbest.IKBestFactor;
import com.analog.lyric.dimple.solvers.core.kbest.KBestFactorEngine;
import com.analog.lyric.dimple.solvers.core.kbest.KBestFactorTableEngine;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import org.eclipse.jdt.annotation.Nullable;


public class STableFactor extends STableFactorDoubleArray implements IKBestFactor
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
	protected double [][] _savedOutMsgArray = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
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
	
	public STableFactor(Factor factor)
	{
		super(factor);
		
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
	    updateK(getOptionOrDefault(SumProductOptions.maxMessageSize));
		
		if (isOptimizedUpdateEnabled() && _factor.getSiblingCount() > 1)
		{
			_tableFactorEngine = new TableFactorEngineOptimized(this);
		}
		else
		{
			_tableFactorEngine = new TableFactorEngine(this);
		}
	}
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort)
	{
		super.moveMessages(other,portNum,otherPort);
		STableFactor sother = (STableFactor)other;
	    if (_dampingInUse)
	    	_savedOutMsgArray[portNum] = sother._savedOutMsgArray[otherPort];
	    
	}
	
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
			for (int i = 0; i < _inputMsgs.length ;i++)
				updateDerivative(i);
		
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
	
	@Override
	public void createMessages()
	{
		super.createMessages();
	}

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
		return new DiscreteWeightMessage(_outputMsgs[edge]);
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
		double[] params  = SumProductOptions.nodeSpecificDamping.getOrDefault(this).toPrimitiveArray();
		if (params.length == 0 && val != 0.0)
		{
			params = new double[getSiblingCount()];
		}
		if (params.length != 0)
		{
			params[index] = val;
		}
		
		SumProductOptions.nodeSpecificDamping.set(this, params);
		configureDampingFromOptions();
	}
	
	public double getDamping(int index)
	{
		return _dampingParams.length > 0 ? _dampingParams[index] : 0.0;
	}
	
	/**
	 * Enables use of the optimized update algorithm on this factor, if its degree is greater than
	 * 1. The optimized update algorithm is employed only when all of the factor's edges are updated
	 * together with an update call. If the schedule instead uses update_edge, the algorithm is not
	 * used.
	 * 
	 * @since 0.06
	 */
	public void enableOptimizedUpdate()
	{
		setOption(SumProductOptions.enableOptimizedUpdate, true);
	}

	/**
	 * Disables use of the optimized update algorithm on this factor.
	 * 
	 * @see #enableOptimizedUpdate()
	 * @since 0.06
	 */
	public void disableOptimizedUpdate()
	{
		setOption(SumProductOptions.enableOptimizedUpdate, false);
	}

	/**
	 * Reverts to the default setting for enabling of the optimized update algorithm, eliminating
	 * the effect of previous calls to {@link #enableOptimizedUpdate()} or
	 * {@link #disableOptimizedUpdate()}.
	 * 
	 * @since 0.06
	 */
	public void useDefaultOptimizedUpdateEnable()
	{
		unsetOption(SumProductOptions.enableOptimizedUpdate);
	}

	/**
	 * Indicates if the optimized update algorithm is enabled for this factor. If
	 * {@link #enableOptimizedUpdate()} or {@link #disableOptimizedUpdate()} has been called,
	 * returns accordingly. If neither has been called, or if their effect has been reset by
	 * {@link #useDefaultOptimizedUpdateEnable()}, returns the default setting. Only the sum-product
	 * solver supports the tree update algorithm; if the root graph is for a different solver,
	 * always returns false.
	 * 
	 * @return True if the tree update algorithm is enabled on this factor.
	 * @since 0.06
	 */
	public boolean isOptimizedUpdateEnabled()
	{
		ISolverFactorGraph rootGraph = getRootGraph();
		if (rootGraph instanceof SFactorGraph)
		{
			SFactorGraph sfg = (SFactorGraph) rootGraph;
			if (sfg.isOptimizedUpdateSupported())
			{
				return getOptionOrDefault(SumProductOptions.enableOptimizedUpdate);
			}
		}
		return false;
	}

	/**
	 * Indicates if the optimized update algorithm enable has been explicitly set.
	 * 
	 * @return True if using an explicit setting. False if using the default.
	 * @since 0.06
	 */
	protected boolean isOptimizedUpdateExplicitlySet()
	{
		return getOption(SumProductOptions.enableOptimizedUpdate) != null;
	}
	
	public int getK()
	{
		return _k;
	}
	
	public void setK(int k)
	{
		setOption(SumProductOptions.maxMessageSize, k);
		updateK(k);
	}

	private void updateK(int k)
	{
		if (k != _k)
		{
			_k = k;
			_kbestFactorEngine.setK(k);
			_kIsSmallerThanDomain = false;
			for (int i = 0; i < _inputMsgs.length; i++)
			{
				if (_inputMsgs[i] != null && _k < _inputMsgs[i].length)
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
				retval[i] *= _inputMsgs[j][indices[j]];
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
					+ " on factor " +_factor.getLabel());

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
		SFactorGraph sfg = (SFactorGraph)getRootGraph();
		
		boolean isFactorOfInterest = sfg.getCurrentFactorTable() == getFactor().getFactorTable();
		
		double [] weights = _factor.getFactorTable().getWeightsSparseUnsafe();
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
		double [] weights = _factor.getFactorTable().getWeightsSparseUnsafe();
		int [][] indices = _factor.getFactorTable().getIndicesSparseUnsafe();
		
		//calculate product of messages and phi
		double prod = weights[index];
		for (int i = 0; i < _inputMsgs.length; i++)
			prod *= _inputMsgs[i][indices[index][i]];

		double sum = 0;
		
		//if index == weightIndex, add in this term
		if (index == weightIndex && isFactorOfInterest)
		{
			sum = prod / weights[index];
		}
		
		//for each variable
		for (int i = 0; i < _inputMsgs.length; i++)
		{
			SDiscreteVariable var = (SDiscreteVariable)getFactor().getConnectedNodesFlat().getByIndex(i).getSolver();
			
			//divide out contribution
			sum += prod / _inputMsgs[i][indices[index][i]] * var.getMessageDerivative(weightIndex,getFactor())[indices[index][i]];
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
		final double[][][] msgs = _outPortDerivativeMsgs = new double[weights][_inputMsgs.length][];
		for (int i = 0; i < weights; i++)
			for (int j = 0; j < _inputMsgs.length; j++)
				msgs[i][j] = new double[_inputMsgs[j].length];
	}
	
	public double [] getMessageDerivative(int wn, VariableBase var)
	{
		int index = getFactor().getPortNum(var);
		return Objects.requireNonNull(_outPortDerivativeMsgs)[wn][index];
	}
	
	public double calculateMessageForDomainValueAndTableIndex(int domainValue, int outPortNum, int tableIndex)
	{
		IFactorTable ft = getFactor().getFactorTable();
		int [][] indices = ft.getIndicesSparseUnsafe();
		double [] weights = ft.getWeightsSparseUnsafe();

		if (indices[tableIndex][outPortNum] == domainValue)
		{
			double prod = weights[tableIndex];
			for (int j = 0; j < _inputMsgs.length; j++)
			{
				if (outPortNum != j)
				{
					prod *= _inputMsgs[j][indices[tableIndex][j]];
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
		IFactorTable ft = getFactor().getFactorTable();
		double sum = 0;
		int [][] indices = ft.getIndicesSparseUnsafe();
		double [] weights = ft.getWeightsSparseUnsafe();
		
		for (int i = 0; i < indices.length; i++)
		{
			if (indices[i][outPortNum] == domainValue)
			{
				double prod = calculateMessageForDomainValueAndTableIndex(domainValue,outPortNum,i);
				
				if (factorUsesTable && (wn == i))
				{
					sum += prod/weights[i];
				}
				
				for (int j = 0; j < _inputMsgs.length; j++)
				{
					
					if (j != outPortNum)
					{
						SDiscreteVariable sv = (SDiscreteVariable)getFactor().getConnectedNodesFlat().getByIndex(j).getSolver();
						@SuppressWarnings("null")
						double [] dvar = sv.getMessageDerivative(wn,getFactor());
								
						sum += (prod / _inputMsgs[j][indices[i][j]]) * dvar[indices[i][j]];
					}
				}
								
			}
		}
		
		return sum;
	}
	
	public double calculatedg(int outPortNum, int wn, boolean factorUsesTable)
	{
		double sum = 0;
		for (int i = 0; i < _inputMsgs[outPortNum].length; i++)
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
		for (int i = 0; i < _inputMsgs[outPortNum].length; i++)
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
		int D = _inputMsgs[outPortNum].length;
		
		for (int d = 0; d < D; d++)
		{
			updateDerivativeForWeightAndDomain(outPortNum,wn,d,factorUsesTable);
		}
	}
	
	public void updateDerivative(int outPortNum)
	{
		SFactorGraph sfg = (SFactorGraph)getRootGraph();
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
		boolean isFactorOfInterest = ((SFactorGraph)getRootGraph()).getCurrentFactorTable() == getFactor().getFactorTable();
				
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

	@Override
	public double[][] getInPortMsgs()
	{
		return _inputMsgs;
	}

	@Override
	public double[][] getOutPortMsgs()
	{
		return _outputMsgs;
	}

	/*------------------
	 * Internal methods
	 */
	
    protected void configureDampingFromOptions()
    {
     	final int size = getSiblingCount();
    	
    	_dampingParams =
    		getReplicatedNonZeroListFromOptions(SumProductOptions.nodeSpecificDamping, SumProductOptions.damping,
    			size, _dampingParams);
 
    	if (_dampingParams.length > 0 && _dampingParams.length != size)
    	{
			DimpleEnvironment.logWarning("%s has wrong number of parameters for %s\n",
				SumProductOptions.nodeSpecificDamping, this);
    		_dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
    	}
    	
    	_dampingInUse = _dampingParams.length > 0;
    	
    	configureSavedMessages(size);
    }
    
    protected void configureSavedMessages(int size)
    {
    	if (!_dampingInUse)
    	{
    		_savedOutMsgArray = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    	}
    	else if (_savedOutMsgArray.length != size)
    	{
    		_savedOutMsgArray = new double[size][];
    		for (int i = 0; i < size; i++)
    	    {
    			_savedOutMsgArray[i] = new double[_inputMsgs[i].length];
    	    }
    	}
    }


}
