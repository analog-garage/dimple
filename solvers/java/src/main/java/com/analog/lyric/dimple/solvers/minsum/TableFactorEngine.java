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

package com.analog.lyric.dimple.solvers.minsum;

import java.util.Arrays;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;

/*
 * Provides the update and updateEdge logic for minsum
 */
public class TableFactorEngine
{
	final MinSumTableFactor _tableFactor;
	final Factor _factor;

	public TableFactorEngine(MinSumTableFactor tableFactor)
	{
		_tableFactor = tableFactor;
		_factor = _tableFactor.getFactor();
	}
	
	public void updateEdge(int outPortNum)
	{
	    final int[][] table = _tableFactor.getFactorTable().getIndicesSparseUnsafe();
	    final double[] values = _tableFactor.getFactorTable().getEnergiesSparseUnsafe();
	    final int tableLength = table.length;
	    final int numPorts = _factor.getSiblingCount();


        final double[] outputMsgs = _tableFactor.getOutPortMsg(outPortNum);
        final int outputMsgLength = outputMsgs.length;
		double[] saved = ArrayUtil.EMPTY_DOUBLE_ARRAY;
        
        if (_tableFactor._dampingInUse)
        {
        	double damping = _tableFactor._dampingParams[outPortNum];
        	if (damping != 0)
        	{
				saved = DimpleEnvironment.doubleArrayCache.allocateAtLeast(outputMsgLength);
				System.arraycopy(outputMsgs, 0, saved, 0, outputMsgLength);
        	}
        }

        Arrays.fill(outputMsgs, Double.POSITIVE_INFINITY);

        final double [][] inPortMsgs = _tableFactor.getInPortMsgs();
        
	    // Run through each row of the function table
        for (int tableIndex = tableLength; --tableIndex>=0;)
        {
        	double L = values[tableIndex];
        	final int[] tableRow = table[tableIndex];
        	final int outputIndex = tableRow[outPortNum];

        	int inPortNum = numPorts;
        	while (--inPortNum > outPortNum)
        		L += inPortMsgs[inPortNum][tableRow[inPortNum]];
        	while (--inPortNum >= 0)
        		L += inPortMsgs[inPortNum][tableRow[inPortNum]];
        	
        	if (L < outputMsgs[outputIndex])
        		outputMsgs[outputIndex] = L;				// Use the minimum value
        }

        // Damping
        if (_tableFactor._dampingInUse)
        {
        	double damping = _tableFactor._dampingParams[outPortNum];
        	if (damping != 0)
        	{
        		final double inverseDamping = 1.0 - damping;
        		for (int i = outputMsgLength; --i>=0;)
        		{
        			outputMsgs[i] = inverseDamping*outputMsgs[i] + damping*saved[i];
        		}
        	}
        }
        
		if (saved.length > 0)
		{
			DimpleEnvironment.doubleArrayCache.release(saved);
		}

	    // Normalize the outputs
        double minPotential = outputMsgs[0];
        for (int i = outputMsgLength; --i>=0;)
        {
        	minPotential = Math.min(minPotential, outputMsgs[i]);
        }
        
		// Normalize min value
		if (minPotential != 0.0)
		{
			for (int i = outputMsgLength; --i>=0;)
			{
				outputMsgs[i] -= minPotential;
			}
		}
	}
	
	
	public void update()
	{
		final IFactorTable table = _tableFactor.getFactorTable();
		final JointDomainIndexer indexer = table.getDomainIndexer();
	    final int[][] tableIndices = table.getIndicesSparseUnsafe();
	    final double[] values = table.getEnergiesSparseUnsafe();
	    final int tableLength = tableIndices.length;
	    final int numPorts = _factor.getSiblingCount();
	    double [][] outPortMsgs = _tableFactor.getOutPortMsgs();

	    final boolean useDamping = _tableFactor._dampingInUse;
	    
	    double[] saved = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	    	
	    if (useDamping)
	    {
	    	saved = DimpleEnvironment.doubleArrayCache.allocateAtLeast(indexer.getSumOfDomainSizes());
	    	for (int port = 0, savedOffset = 0; port < numPorts; port++)
	    	{
	    		final double[] outputMsgs = outPortMsgs[port];
	    		final int outputMsgLength = outputMsgs.length;

	    		if (useDamping)
	    		{
	    			double damping = _tableFactor._dampingParams[port];
	    			if (damping != 0)
	    			{
	    				System.arraycopy(outputMsgs, 0, saved, savedOffset, outputMsgLength);
	    			}
	    		}

	    		Arrays.fill(outputMsgs, Double.POSITIVE_INFINITY);
	    		savedOffset += outputMsgLength;
	    	}
	    }
	    else
	    {
	    	for (double[] outMsg : outPortMsgs)
	    	{
	    		Arrays.fill(outMsg, Double.POSITIVE_INFINITY);
	    	}
	    }
	    
	    final double [][] inPortMsgs = _tableFactor.getInPortMsgs();

	    
	    // Run through each row of the function table
	    for (int tableIndex = tableLength; --tableIndex>=0;)
	    {
	    	final int[] tableRow = tableIndices[tableIndex];
	    	
	    	// Sum up the function value plus the messages on all ports
	    	double L = values[tableIndex];
	    	for (int port = numPorts; --port>=0;)
	    		L += inPortMsgs[port][tableRow[port]];

			// Run through each output port
	    	for (int outPortNum = numPorts; --outPortNum>=0;)
	    	{
	    		final double[] outputMsgs = outPortMsgs[outPortNum];
	    		final int outputIndex = tableRow[outPortNum];											// Index for the output value
	    		final double LThisPort = L - inPortMsgs[outPortNum][outputIndex];			// Subtract out the message from this output port
	    		outputMsgs[outputIndex] = Math.min(outputMsgs[outputIndex], LThisPort);
	    	}
	    }
	   
	    // Damping
	    if (useDamping)
	    {
	    	for (int port = 0, savedOffset = 0; port < numPorts; port++)
	    	{
		    	final double[] outputMsgs = outPortMsgs[port];
		    	final int outputMsgLength = outputMsgs.length;
	    		final double damping = _tableFactor._dampingParams[port];

	    		if (damping != 0)
	    		{
	    			final double inverseDamping = 1.0 - damping;
	    			for (int i = outputMsgLength; --i>=0;)
	    			{
	    				outputMsgs[i] = inverseDamping*outputMsgs[i] + damping*saved[i+savedOffset];
	    			}
	    		}
	    		
	    		savedOffset += outputMsgLength;
	    	}
	    	
	    	DimpleEnvironment.doubleArrayCache.release(saved);
	    }
    	
	    
    	
	    // Normalize the outputs
	    for (int port = numPorts; --port>=0;)
	    {
    		double[] outputMsgs = outPortMsgs[port];
    		int outputMsgLength = outputMsgs.length;
	    	double minPotential = Double.POSITIVE_INFINITY;
	    	for (int i = outputMsgLength; --i>=0;)
	    	{
	    		minPotential = Math.min(minPotential, outputMsgs[i]);
	    	}
	    	if (minPotential != 0.0)
	    	{
	    		for (int i = outputMsgLength; --i>=0;)
	    			outputMsgs[i] -= minPotential;			// Normalize min value
	    	}
	    }
	}
}
