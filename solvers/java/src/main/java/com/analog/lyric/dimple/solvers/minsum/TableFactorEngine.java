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

import com.analog.lyric.dimple.model.factors.Factor;

/*
 * Provides the update and updateEdge logic for minsum
 */
public class TableFactorEngine
{
	MinSumTableFactor _tableFactor;
	Factor _factor;

	public TableFactorEngine(MinSumTableFactor tableFactor)
	{
		_tableFactor = tableFactor;
		_factor = _tableFactor.getFactor();
	}
	
	public void updateEdge(int outPortNum)
	{
	    int[][] table = _tableFactor.getFactorTable().getIndicesSparseUnsafe();
	    double[] values = _tableFactor.getFactorTable().getEnergiesSparseUnsafe();
	    int tableLength = table.length;
	    final int numPorts = _factor.getSiblingCount();


        double[] outputMsgs = _tableFactor.getOutPortMsgs()[outPortNum];
        int outputMsgLength = outputMsgs.length;
        
        if (_tableFactor._dampingInUse)
        {
        	double damping = _tableFactor._dampingParams[outPortNum];
        	if (damping != 0)
        	{
        		double[] saved = _tableFactor._savedOutMsgArray[outPortNum];
        		for (int i = 0; i < outputMsgs.length; i++)
        			saved[i] = outputMsgs[i];
        	}
        }

        
        for (int i = 0; i < outputMsgLength; i++)
        	outputMsgs[i] = Double.POSITIVE_INFINITY;

        double [][] inPortMsgs = _tableFactor.getInPortMsgs();
        
	    // Run through each row of the function table
        for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
        {
        	double L = values[tableIndex];
        	int[] tableRow = table[tableIndex];
        	int outputIndex = tableRow[outPortNum];

        	for (int inPortNum = 0; inPortNum < numPorts; inPortNum++)
        		if (inPortNum != outPortNum)
        			L += inPortMsgs[inPortNum][tableRow[inPortNum]];
        	
        	if (L < outputMsgs[outputIndex])
        		outputMsgs[outputIndex] = L;				// Use the minimum value
        }

	    // Normalize the outputs
        double minPotential = Double.POSITIVE_INFINITY;
        
        for (int i = 0; i < outputMsgLength; i++)
        {
        	double msg = outputMsgs[i];
        	if (msg < minPotential)
        		minPotential = msg;
        }
        
        // Damping
        if (_tableFactor._dampingInUse)
        {
        	double damping = _tableFactor._dampingParams[outPortNum];
        	if (damping != 0)
        	{
        		double[] saved = _tableFactor._savedOutMsgArray[outPortNum];
        		for (int i = 0; i < outputMsgLength; i++)
        			outputMsgs[i] = (1-damping)*outputMsgs[i] + damping*saved[i];
        	}
        }
        
		// Normalize min value
        for (int i = 0; i < outputMsgLength; i++)
        	outputMsgs[i] -= minPotential;
	}
	
	
	public void update()
	{
	    int[][] table = _tableFactor.getFactorTable().getIndicesSparseUnsafe();
	    double[] values = _tableFactor.getFactorTable().getEnergiesSparseUnsafe();
	    int tableLength = table.length;
	    int numPorts = _factor.getSiblingCount();
	    double [][] outPortMsgs = _tableFactor.getOutPortMsgs();

	    for (int port = 0; port < numPorts; port++)
	    {
	    	double[] outputMsgs = outPortMsgs[port];
	    	int outputMsgLength = outputMsgs.length;
	    	
	    	if (_tableFactor._dampingInUse)
	    	{
	    		double damping = _tableFactor._dampingParams[port];
	    		if (damping != 0)
	    		{
	    			double[] saved = _tableFactor._savedOutMsgArray[port];
	    			for (int i = 0; i < outputMsgs.length; i++)
	    				saved[i] = outputMsgs[i];
	    		}
	    	}

	    	for (int i = 0; i < outputMsgLength; i++)
	    		outputMsgs[i] = Double.POSITIVE_INFINITY;
	    }
	    
	    double [][] inPortMsgs = _tableFactor.getInPortMsgs();

	    
	    // Run through each row of the function table
	    for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
	    {
	    	int[] tableRow = table[tableIndex];
	    	
	    	// Sum up the function value plus the messages on all ports
	    	double L = values[tableIndex];
	    	for (int port = 0; port < numPorts; port++)
	    		L += inPortMsgs[port][tableRow[port]];

			// Run through each output port
	    	for (int outPortNum = 0; outPortNum < numPorts; outPortNum++)
	    	{
	    		double[] outputMsgs = outPortMsgs[outPortNum];
	    		int outputIndex = tableRow[outPortNum];											// Index for the output value
	    		double LThisPort = L - inPortMsgs[outPortNum][tableRow[outPortNum]];			// Subtract out the message from this output port
	    		if (LThisPort < outputMsgs[outputIndex])
	    			outputMsgs[outputIndex] = LThisPort;	// Use the minimum value
	    	}
	    }
	   
	    // Damping
	    if (_tableFactor._dampingInUse)
	    {
	    	for (int port = 0; port < numPorts; port++)
	    	{
	    		double damping = _tableFactor._dampingParams[port];
	    		if (damping != 0)
	    		{
	    			double[] saved = _tableFactor._savedOutMsgArray[port];
	    			double[] outputMsgs = outPortMsgs[port];

	    			int outputMsgLength = outputMsgs.length;
	    			for (int i = 0; i < outputMsgLength; i++)
	    				outputMsgs[i] = (1-damping)*outputMsgs[i] + damping*saved[i];
	    		}
	    	}
	    }
    	
	    
    	
	    // Normalize the outputs
	    for (int port = 0; port < numPorts; port++)
	    {
    		double[] outputMsgs = outPortMsgs[port];
    		int outputMsgLength = outputMsgs.length;
	    	double minPotential = Double.POSITIVE_INFINITY;
	    	for (int i = 0; i < outputMsgLength; i++)
	    	{
	    		double msg = outputMsgs[i];
	    		if (msg < minPotential)
	    			minPotential = msg;
	    	}
	    	for (int i = 0; i < outputMsgLength; i++)
	    		outputMsgs[i] -= minPotential;			// Normalize min value
	    }
	}
}
