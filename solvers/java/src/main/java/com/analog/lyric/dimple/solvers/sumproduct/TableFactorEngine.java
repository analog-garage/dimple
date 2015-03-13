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

import java.util.Arrays;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.factors.Factor;

/*
 * Provides the update and updateEdge logic for sumproduct
 */
public class TableFactorEngine
{
	final SumProductTableFactor _tableFactor;
	final Factor _factor;
	
	public TableFactorEngine(SumProductTableFactor tableFactor)
	{
		_tableFactor = tableFactor;
		_factor = _tableFactor.getFactor();
	}
		
	public void updateEdge(int outPortNum)
	{
		final SumProductTableFactor tableFactor = _tableFactor;
	    final int[][] table = tableFactor.getFactorTable().getIndicesSparseUnsafe();
	    final double[] values = tableFactor.getFactorTable().getWeightsSparseUnsafe();
	    final int tableLength = table.length;
	    final int numPorts = _factor.getSiblingCount();
	    
        final double[] outputMsgs = tableFactor.getOutPortMsg(outPortNum);
        final double [][] inputMsgs = tableFactor.getInPortMsgs();
        
    	final int outputMsgLength = outputMsgs.length;

    	double damping = 0;
    	
    	if (tableFactor._dampingInUse)
    	{
    		damping = tableFactor._dampingParams[outPortNum];
    	}
    	
    	final boolean useDamping = damping != 0;
    	
    	final double[] saved = useDamping ?
    		DimpleEnvironment.doubleArrayCache.allocateAtLeast(outputMsgLength) : ArrayUtil.EMPTY_DOUBLE_ARRAY;
    	
    	if (useDamping)
        {
    		System.arraycopy(outputMsgs, 0, saved, 0, outputMsgLength);
        }
        
    	double sum = 0.0;
    	Arrays.fill(outputMsgs, 0);
        
        for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
        {
        	double prob = values[tableIndex];
        	final int[] tableRow = table[tableIndex];
    		final int outputIndex = tableRow[outPortNum];
        	
			for (int inPortNum = 0; inPortNum < outPortNum; ++inPortNum)
				prob *= inputMsgs[inPortNum][tableRow[inPortNum]];
			for (int inPortNum = outPortNum + 1; inPortNum < numPorts; inPortNum++)
				prob *= inputMsgs[inPortNum][tableRow[inPortNum]];
        	
        	outputMsgs[outputIndex] += prob;
        	sum += prob;
        }
        
		if (sum == 0)
		{
			throw new DimpleException("UpdateEdge failed in SumProduct Solver.  All probabilities were zero when calculating message for port "
					+ outPortNum + " on factor " + _factor.getLabel());
		}

		for (int i = 0; i < outputMsgLength; i++)
			outputMsgs[i] /= sum;
    	
		if (useDamping)
		{
			final double inverseDamping = 1 - damping;
			for (int i = 0; i < outputMsgLength; i++)
			{
				outputMsgs[i] = inverseDamping*outputMsgs[i] + damping*saved[i];
			}
			
			DimpleEnvironment.doubleArrayCache.release(saved);
		}
    	
	}
	

	
	
	public void update()
	{
		final SumProductTableFactor tableFactor = _tableFactor;
		final IFactorTable table = tableFactor.getFactorTable();
	    final int[][] tableIndices = table.getIndicesSparseUnsafe();
	    final double[] values = table.getWeightsSparseUnsafe();
	    final int tableLength = tableIndices.length;
	    final int numPorts = _factor.getSiblingCount();
	    
	    
	    final double [][] inMsgs = tableFactor.getInPortMsgs();
	    
	    final boolean useDamping = tableFactor._dampingInUse;
	    
	    final double[] saved = useDamping ?
	    	DimpleEnvironment.doubleArrayCache.allocateAtLeast(table.getDomainIndexer().getSumOfDomainSizes()) :
	    		ArrayUtil.EMPTY_DOUBLE_ARRAY;
	    
	    for (int outPortNum = 0, savedOffset = 0; outPortNum < numPorts; outPortNum++)
	    {
	    	final double[] outputMsgs = tableFactor.getOutPortMsg(outPortNum);
	    	final int outputMsgLength = outputMsgs.length;
	    		    	
	    	if (useDamping)
	    	{
	    		final double damping = tableFactor._dampingParams[outPortNum];
	    		if (damping != 0)
	    		{
	    			System.arraycopy(outputMsgs, 0, saved, savedOffset, outputMsgLength);
	    		}
	    	}
	    	
	    	Arrays.fill(outputMsgs, 0);

	    	for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
	    	{
	    		double prob = values[tableIndex];
	    		final int[] tableRow = tableIndices[tableIndex];
	    		int outputIndex = tableRow[outPortNum];

				for (int inPortNum = 0; inPortNum < outPortNum; ++inPortNum)
					prob *= inMsgs[inPortNum][tableRow[inPortNum]];
				for (int inPortNum = outPortNum + 1; inPortNum < numPorts; inPortNum++)
					prob *= inMsgs[inPortNum][tableRow[inPortNum]];
	    		outputMsgs[outputIndex] += prob;
	    	}

	    	double sum = 0;
	    	for (int i = 0; i < outputMsgLength; i++) sum += outputMsgs[i];
    		if (sum == 0)
    		{
    			throw new DimpleException("Update failed in SumProduct Solver.  All probabilities were zero when calculating message for port "
    					+ outPortNum + " on factor " +_factor.getLabel());
    		}

	    	for (int i = 0; i < outputMsgLength; i++)
	    		outputMsgs[i] /= sum;
	    	
	    	if (useDamping)
	    	{
	    		final double damping = tableFactor._dampingParams[outPortNum];
	    		if (damping != 0)
	    		{
	    			for (int i = 0; i < outputMsgLength; i++)
	    				outputMsgs[i] = (1-damping)*outputMsgs[i] + damping*saved[i+savedOffset];
	    		}
	    	}
	    	
	    	savedOffset += outputMsgLength;
	    }
	    
	    if (useDamping)
	    {
	    	DimpleEnvironment.doubleArrayCache.release(saved);
	    }
	}
}
