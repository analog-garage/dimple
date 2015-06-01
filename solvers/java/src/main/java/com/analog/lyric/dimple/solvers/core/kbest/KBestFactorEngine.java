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

package com.analog.lyric.dimple.solvers.core.kbest;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.util.misc.IndexCounter;

/*
 * This class provides an implementation for update and updateEdge that can
 * be used by solver factor classes.  It implements a k best algorithm.
 * 
 * pseudocode of algorithm
 * 
 * updateEdge(outPort)
 *	For each input msg
 *		sort by probability and pick the k most likely
 *
 *	initialize outputMsg to zero (or equivalent) for all values
 *
 *	For every single value of the output message (not just the kbset)
 *	
 *		For the n^k combinations of inputs (where n is number of input edges)
 *			prod = calculate factor function (or equivalent for minsum)
 *			prod *= all of the input probabilities for those values
 *	
 *			sum the prod with the current value for the output message at this value (or equivalent for minsum)
 *
 *	Normalize outputmsg (subtract smallest value)
 *
 * There is no optimization for update(all)
 */
public class KBestFactorEngine
{
	private int _k;
	private IKBestFactor _kbestFactor;
	private double [][] _outPortMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
	private double [][] _inPortMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
	
	private void updateCache()
	{
		
		_inPortMsgs = _kbestFactor.getInPortMsgs();
		_outPortMsgs = _kbestFactor.getOutPortMsgs();
			
	}
	
	public KBestFactorEngine(IKBestFactor f)
	{
		_kbestFactor = f;
	}
	
	public void update()
	{
		updateCache();

		for (int i = 0; i < _outPortMsgs.length; i++)
			updateEdgeInternal(i);
	}
	
	public void setK(int k)
	{
		_k = k;
	}
	
	
	/*
	 * Code for updating given no factor table but java factor function
	 */
	public void updateEdge(int outPortNum)
	{
		updateCache();
		updateEdgeInternal(outPortNum);
	}
	
	protected void updateEdgeInternal(int outPortNum)
	{
		
		
		//Initialize the outputMsg to Infinite potentials.
		double [] outputMsg = _outPortMsgs[outPortNum];
		_kbestFactor.initMsg(outputMsg);

		//Cache the input messages.
		//Cache the domains
		Object [][] domains = new Object[_inPortMsgs.length][];
		for (int i = 0; i < _inPortMsgs.length; i++)
			
			domains[i] = ((Discrete)_kbestFactor.getFactor().getConnectedNodeFlat(i)).getDiscreteDomain().getElements();
		
		//We will store the kbest indices in this array
		int [][] domainIndices = new int[_inPortMsgs.length][];
		
		//We will store the truncated domainlengths here.
		int [] domainLengths = new int[_inPortMsgs.length];
		
		//For each port
		for (int i = 0; i < _inPortMsgs.length; i++)
		{
			double [] inPortMsg = _inPortMsgs[i];

			//If this is the output port, we only store one value at a time.
			if (i == outPortNum)
				domainIndices[i] = new int[]{0};
			else
			{
				//Here we check to see that k is actually less than the domain length
				if (_k < inPortMsg.length)
					domainIndices[i] = _kbestFactor.findKBestForMsg(inPortMsg,_k);
				else
				{
					//If it's not, we just map indices one to one.
					domainIndices[i] = new int[inPortMsg.length];
					for (int j = 0; j < domainIndices[i].length; j++)
						domainIndices[i][j] = j;
				}
			}
			
			domainLengths[i] = domainIndices[i].length;
		}
		
		//cache the factor function.
		//FactorFunction ff = _kbestFactor.getFactorFunction();
		

		//Used to iterate all combinations of truncated domains.
		IndexCounter ic = new IndexCounter(domainLengths);

		int [] inputIndices = new int[_inPortMsgs.length];
		
		//We fill out a value for every value for the output message (no truncating to k)
		for (int outputIndex = 0; outputIndex < outputMsg.length; outputIndex++)
		{
			//Here we set the output port's index appropriately
			domainIndices[outPortNum][0] = outputIndex;
		

			//For all elements of cartesian product
			for (int [] indices : ic)
			{
				//initialize the sum
				double sum = _kbestFactor.initAccumulator();
				
				for (int i = 0; i < indices.length; i++)
				{
					//Don't count the output port
					if (i != outPortNum)
						//i == port index, indices[i] == which of the truncated domain indices to retrieve
						//domainIndices[i][indices[i]] == the actual index of the input msg.
						sum = _kbestFactor.accumulate(sum, _inPortMsgs[i][domainIndices[i][indices[i]]]);

					//Here we set the input value for this port
					inputIndices[i] = domainIndices[i][indices[i]];
					//ffInput[i] =  domains[i][domainIndices[i][indices[i]]];
				}
				
				//Evaluate the factor function and add that potential to the sum.
				double result = getFactorFunctionValueForIndices(inputIndices,domains);
				sum = _kbestFactor.accumulate(sum, result);

				outputMsg[outputIndex] = _kbestFactor.combine(outputMsg[outputIndex] , sum);
			}
		}

		_kbestFactor.normalize(outputMsg);
		
	}
	
	protected double getFactorFunctionValueForIndices(int [] inputIndices, Object [][] domains)
	{
		Object [] ffInput = new Object[inputIndices.length];
		for (int i = 0; i < ffInput.length; i++)
			ffInput[i] = domains[i][inputIndices[i]];
		return _kbestFactor.evalFactorFunction(ffInput);
	}

	protected IKBestFactor getIKBestFactor()
	{
		return _kbestFactor;
	}

}
