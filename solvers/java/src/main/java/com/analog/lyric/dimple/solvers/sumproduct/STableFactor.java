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

import java.util.ArrayList;
import java.util.Arrays;

import com.analog.lyric.cs.Sort;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.dimple.solvers.core.kbest.IKBestFactor;
import com.analog.lyric.dimple.solvers.core.kbest.KBestFactorEngine;
import com.analog.lyric.dimple.solvers.core.kbest.KBestFactorTableEngine;



public class STableFactor extends STableFactorBase implements IKBestFactor
{	
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
	protected double [][] _inPortMsgs;
	protected double [][] _outMsgArray;
	protected double [][] _savedOutMsgArray;
	protected double [][][] _outPortDerivativeMsgs;
	protected double [] _dampingParams;
	protected boolean _initCalled = true;
	protected TableFactorEngine _tableFactorEngine;
	protected KBestFactorEngine _kbestFactorEngine;
	protected int _k;
	protected boolean _kIsSmallerThanDomain = false;
	protected boolean _updateDerivative = false;
	protected boolean _dampingInUse = false;
	

	public STableFactor(Factor factor)  
	{
		super(factor);
		_dampingParams = new double[_factor.getPorts().size()];
		_tableFactorEngine = new TableFactorEngine(this);
			
		//TODO: should I recheck for factor table every once in a while?
		if (factor.getFactorFunction().factorTableExists(getFactor().getDomains()))
		{
			_kbestFactorEngine = new KBestFactorTableEngine(this);
		}
		else
		{
			_kbestFactorEngine = new KBestFactorEngine(this);
		}
		
		setK(Integer.MAX_VALUE);

	}
	
	public void setDamping(int index, double val)
	{
		_dampingParams[index] = val;
		
		if (val != 0)
			_dampingInUse = true;
	}
	
	public double getDamping(int index)
	{
		return _dampingParams[index];
	}
	
	public void setK(int k)
	{
		updateCache();
		
		_k = k;
		_kbestFactorEngine.setK(k);
		_kIsSmallerThanDomain = false;
		for (int i = 0; i < _inPortMsgs.length; i++)
		{
			if (_k < _inPortMsgs[i].length)
			{
				_kIsSmallerThanDomain = true;
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.analog.lyric.dimple.solvers.core.SFactorBase#getEnergy()
	 * 
	 * Calculates what kind of energy?
	 */
	public double getScore()
	{
		int [] indices = new int[_factor.getPorts().size()];
		
		for (int i = 0; i < indices.length; i++)
		{
			SVariable tmp = (SVariable)((VariableBase)_factor.getPorts().get(i).getConnectedNode()).getSolver();
			indices[i] = tmp.getGuessIndex();
		}
		
		 int[][] table = getFactorTable().getIndices();
	     double[] values = getFactorTable().getWeights();
	     double maxValue = Double.NEGATIVE_INFINITY;
	     double retVal = Double.POSITIVE_INFINITY;

	     
		 for (int i = 0; i < table.length; i++)
		 {
			 boolean match = true;
			 
			 for (int j = 0; j < indices.length; j++)
			 {
				 if (indices[j] != table[i][j])
				 {
					 match = false;
					 break;
				 }
			 }
			 
			 if (match)
			 {
				 retVal = -Math.log(values[i]);
			 }
			 
			 if (values[i] > maxValue)
				 maxValue = values[i];
		 }		
		 
		 if (maxValue > 0)
			 retVal -= -Math.log(maxValue);
		
		return retVal;
		
	}
	
	public void setUpdateDerivative(boolean updateDer)
	{
		_updateDerivative = updateDer;
	}
	
	public void updateEdge(int outPortNum) 
	{
		updateCache();
		if (_kIsSmallerThanDomain)
			_kbestFactorEngine.updateEdge(outPortNum);
		else
			_tableFactorEngine.updateEdge(outPortNum);

		if (_updateDerivative)
			updateDerivative(outPortNum);
	}
	
	
	@Override
	public void update() 
	{		
		updateCache();	
		if (_kIsSmallerThanDomain)
			//TODO: damping
			_kbestFactorEngine.update();
		else
			_tableFactorEngine.update();
		
		if (_updateDerivative)
			for (int i = 0; i < _inPortMsgs.length ;i++)
				updateDerivative(i);
	}
	
	@Override
	public void connectPort(Port p)  
	{
		_initCalled = true;
	}

	private void updateCache()
	{
		if (_initCalled)
		{
			_initCalled = false;
			int numPorts = _factor.getPorts().size();
			
		    _inPortMsgs = new double[numPorts][];
		    
		    for (int port = 0; port < numPorts; port++) 
		    	_inPortMsgs[port] = (double[])_factor.getPorts().get(port).getInputMsg();
		    
		    _outMsgArray = new double[numPorts][];
		    if (_dampingInUse)
		    	_savedOutMsgArray = new double[numPorts][];
		    
		    for (int port = 0; port < numPorts; port++)
		    {
		    	_outMsgArray[port] = (double[])_factor.getPorts().get(port).getOutputMsg();
		    	if (_dampingInUse)
		    		_savedOutMsgArray[port] = new double[_outMsgArray[port].length];
		    }
		}
	}
	
	public void initialize()
	{
		//We update the cache here.  This works only because initialize() is called on the variables
		//first.  Updating the cache saves msg in double arrays.  initialize replaces these double arrays
		//with new double arrays.  If we didn't call updateCache on initialize, our cache would point
		//to stale information.
		_initCalled = true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.analog.lyric.dimple.solvers.core.SFactorBase#getBelief()
	 * 
	 * Calculates a piece of the beta free energy
	 */
	public double [] getBelief() 
	{
		updateCache();
		
		double [] retval = getUnormalizedBelief();
		double sum = 0; 
		for (int i = 0; i < retval.length; i++)
			sum += retval[i];
		for (int i = 0; i < retval.length; i++)
			retval[i] /= sum;
		return retval;
	}
	
	public double [] getUnormalizedBelief()
	{
		updateCache();
		
		int [][] table = getFactorTable().getIndices();
		double [] values = getFactorTable().getWeights();
		double [] retval = new double[table.length];
		
		
		for (int i = 0; i < table.length; i++)
		{
			retval[i] = values[i];
			for (int j = 0; j < table[i].length; j++)
			{
				retval[i] *= _inPortMsgs[j][table[i][j]];
			}
		}		
		
		return retval;
	}
	

	@Override
	public ArrayList<Port> getPorts() 
	{
		return getFactor().getPorts();
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
		return getFactorTable().getWeights()[index];
	}

	@Override
	public int[] findKBestForMsg(double[] msg, int k) 
	{
		return Sort.quickfindLastKindices(msg, k);
	}

	/******************************************************
	 * Energy, Entropy, and derivatives of all that. 
	 ******************************************************/


	public double getInternalEnergy()
	{
		double [] belief = getBelief();
		double sum = 0;
		for (int i = 0; i < belief.length; i++)
		{
			double tmp = - Math.log(getFactorTable().getWeights()[i]);
			if (tmp != 0)
				sum += belief[i] * tmp;
		}
		
		return sum;		
	}
	
	public double getBetheEntropy()
	{
		double sum = 0;
		
		double [] belief = getBelief();
		for (int i = 0; i < belief.length; i++)
		{
			if (belief[i] != 0)
				sum -= belief[i] * Math.log(belief[i]);
		}
		
		return sum;		
	}
	
	public double calculateDerivativeOfInternalEnergyWithRespectToWeight(int weightIndex)
	{
		SFactorGraph sfg = (SFactorGraph)getRootGraph();
		
		boolean isFactorOfInterest = sfg.getCurrentFactorTable() == getFactor().getFactorTable();
		
		double [] weights = _factor.getFactorTable().getWeights();
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
	
	public double calculateDerivativeOfBeliefNumeratorWithRespectToWeight(int weightIndex, int index, boolean isFactorOfInterest)
	{
		double [] weights = _factor.getFactorTable().getWeights();
		int [][] indices = _factor.getFactorTable().getIndices();
		
		//calculate product of messages and phi
		double prod = weights[index];
		for (int i = 0; i < _inPortMsgs.length; i++)
			prod *= _inPortMsgs[i][indices[index][i]];

		double sum = 0;
		
		//if index == weightIndex, add in this term
		if (index == weightIndex && isFactorOfInterest)
		{
			sum = prod / weights[index];
		}
		
		//for each variable
		for (int i = 0; i < _inPortMsgs.length; i++)
		{
			SVariable var = (SVariable)getFactor().getPorts().get(i).getConnectedNode().getSolver();
			
			//divide out contribution
			sum += prod / _inPortMsgs[i][indices[index][i]] * var.getMessageDerivative(weightIndex,getFactor())[indices[index][i]];
		}
		return sum;
	}
	
	public double calculateDerivativeOfBeliefDenomenatorWithRespectToWeight(int weightIndex, int index, boolean isFactorOfInterest)
	{
		double sum = 0;
		for (int i = 0; i < getFactor().getFactorTable().getWeights().length; i++)
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
		_outPortDerivativeMsgs = new double[weights][_inPortMsgs.length][];
		for (int i = 0; i < weights; i++)
			for (int j = 0; j < _inPortMsgs.length; j++)
				_outPortDerivativeMsgs[i][j] = new double[_inPortMsgs[j].length];
	}
	
	public double [] getMessageDerivative(int wn, VariableBase var)
	{
		int index = getFactor().getPortNum(var);		
		return _outPortDerivativeMsgs[wn][index];
	}	
	
	public double calculateMessageForDomainValueAndTableIndex(int domainValue, int outPortNum, int tableIndex)
	{
		FactorTable ft = getFactor().getFactorTable();
		int [][] indices = ft.getIndices();
		double [] weights = ft.getWeights();

		if (indices[tableIndex][outPortNum] == domainValue)
		{
			double prod = weights[tableIndex];
			for (int j = 0; j < _inPortMsgs.length; j++)
			{
				if (outPortNum != j)
				{
					prod *= _inPortMsgs[j][indices[tableIndex][j]];
				}
			}
			
			return prod;
		}
		else
			return 0;
	}
	
	public double calculateMessageForDomainValue(int domainValue, int outPortNum)
	{
		FactorTable ft = getFactor().getFactorTable();
		double sum = 0;
		int [][] indices = ft.getIndices();
		
		for (int i = 0; i < ft.getWeights().length; i++)
			if (indices[i][outPortNum] == domainValue)
				sum += calculateMessageForDomainValueAndTableIndex(domainValue,outPortNum,i);
		
		return sum;
	}
	
	
	public double calculatedf(int outPortNum, int domainValue, int wn, boolean factorUsesTable)
	{
		FactorTable ft = getFactor().getFactorTable();
		double sum = 0;
		int [][] indices = ft.getIndices();
		double [] weights = ft.getWeights();
		
		for (int i = 0; i < indices.length; i++)
		{
			if (indices[i][outPortNum] == domainValue)
			{
				double prod = calculateMessageForDomainValueAndTableIndex(domainValue,outPortNum,i);
				
				if (factorUsesTable && (wn == i))
				{
					sum += prod/weights[i];
				}
				
				for (int j = 0; j < _inPortMsgs.length; j++)
				{
					
					if (j != outPortNum)
					{
						SVariable sv = (SVariable)getFactor().getPorts().get(j).getConnectedNode().getSolver();
						double [] dvar = sv.getMessageDerivative(wn,getFactor());
								
						sum += (prod / _inPortMsgs[j][indices[i][j]]) * dvar[indices[i][j]];
					}
				}
								
			}
		}
		
		return sum;
	}
	
	public double calculatedg(int outPortNum, int wn, boolean factorUsesTable)
	{
		double sum = 0;
		for (int i = 0; i < _inPortMsgs[outPortNum].length; i++)
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
		for (int i = 0; i < _inPortMsgs[outPortNum].length; i++)
			g += calculateMessageForDomainValue(i,outPortNum);
		
		double derivative = 0;
		if (g != 0)
		{
			double df = calculatedf(outPortNum,d,wn,factorUsesTable);
			double dg = calculatedg(outPortNum,wn,factorUsesTable);
		
			
			//derivative = df;
			derivative = (df*g - f*dg) / (g*g);
			
		}
		
		_outPortDerivativeMsgs[wn][outPortNum][d] = derivative;
		
		
	}
	
	public void updateDerivativeForWeight(int outPortNum, int wn,boolean factorUsesTable)
	{
		int D = _inPortMsgs[outPortNum].length;
		
		for (int d = 0; d < D; d++)
		{
			updateDerivativeForWeightAndDomain(outPortNum,wn,d,factorUsesTable);
		}
	}
	
	public void updateDerivative(int outPortNum)
	{
		SFactorGraph sfg = (SFactorGraph)getRootGraph();
		FactorTable ft = sfg.getCurrentFactorTable();
		int numWeights = ft.getWeights().length;
		
		for (int wn = 0; wn < numWeights; wn++)
		{
			updateDerivativeForWeight(outPortNum,wn,ft == getFactor().getFactorTable());
		}
	}
	
	public double calculateDerivativeOfBetheEntropyWithRespectToWeight(int weightIndex)
	{
		
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
	

}
