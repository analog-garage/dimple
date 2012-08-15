package com.analog.lyric.dimple.solvers.minsum;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;

public class TableFactorEngine 
{
	STableFactor _tableFactor;
	Factor _factor;

	public TableFactorEngine(STableFactor tableFactor)
	{
		_tableFactor = tableFactor;
		_factor = _tableFactor.getFactor();
		//_factorTable = _tableFactor.getFactorTable();
	}
	
	public void updateEdge(int outPortNum) 
	{
		ArrayList<Port> ports = _factor.getPorts();
		
	    int[][] table = _tableFactor.getFactorTable().getIndices();
	    double[] values = _tableFactor.getFactorTable().getPotentials();
	    int tableLength = table.length;
	    int numPorts = ports.size();


        double[] outputMsgs = _tableFactor._outPortMsgs[outPortNum];
        int outputMsgLength = outputMsgs.length;
        
    	double damping = _tableFactor._dampingParams[outPortNum];
    	double[] saved = _tableFactor._savedOutMsgArray[outPortNum];
    	if (damping != 0)
    	{
    		for (int i = 0; i < outputMsgs.length; i++)
    			saved[i] = outputMsgs[i];
    	}

        
        for (int i = 0; i < outputMsgLength; i++) 
        	outputMsgs[i] = Double.POSITIVE_INFINITY;

	    // Run through each row of the function table
        for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
        {
        	double L = values[tableIndex];
        	int[] tableRow = table[tableIndex];
        	int outputIndex = tableRow[outPortNum];

        	for (int inPortNum = 0; inPortNum < numPorts; inPortNum++)
        		if (inPortNum != outPortNum)
        			L += _tableFactor._inPortMsgs[inPortNum][tableRow[inPortNum]];
        	
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
    	if (damping != 0)
    		for (int i = 0; i < outputMsgLength; i++)
    			outputMsgs[i] = (1-damping)*outputMsgs[i] + damping*saved[i];
        
		// Normalize min value
        for (int i = 0; i < outputMsgLength; i++) 
        	outputMsgs[i] -= minPotential;
	}
	
	
	public void update() 
	{
	    int[][] table = _tableFactor.getFactorTable().getIndices();
	    double[] values = _tableFactor.getFactorTable().getPotentials();
	    int tableLength = table.length;
	    int numPorts = _factor.getPorts().size();
	    

	    for (int port = 0; port < numPorts; port++)
	    {
	    	double[] outputMsgs = _tableFactor._outPortMsgs[port];
	    	int outputMsgLength = outputMsgs.length;
	    	
	    	if (_tableFactor._dampingParams[port] != 0)
	    	{
		    	double[] saved = _tableFactor._savedOutMsgArray[port];
	    		for (int i = 0; i < outputMsgs.length; i++)
	    			saved[i] = outputMsgs[i];
	    	}

	    	for (int i = 0; i < outputMsgLength; i++) 
	    		outputMsgs[i] = Double.POSITIVE_INFINITY;
	    }
	    

	    // Run through each row of the function table
	    for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
	    {
	    	int[] tableRow = table[tableIndex];
	    	
	    	// Sum up the function value plus the messages on all ports
	    	double L = values[tableIndex]; 
	    	for (int port = 0; port < numPorts; port++)
	    		L += _tableFactor._inPortMsgs[port][tableRow[port]];

			// Run through each output port
	    	for (int outPortNum = 0; outPortNum < numPorts; outPortNum++)
	    	{
	    		double[] outputMsgs = _tableFactor._outPortMsgs[outPortNum];
	    		int outputIndex = tableRow[outPortNum];											// Index for the output value
	    		double LThisPort = L - _tableFactor._inPortMsgs[outPortNum][tableRow[outPortNum]];			// Subtract out the message from this output port
	    		if (LThisPort < outputMsgs[outputIndex]) 
	    			outputMsgs[outputIndex] = LThisPort;	// Use the minimum value
	    	}
	    }
	   
	    // Damping
	    for (int port = 0; port < numPorts; port++)
	    {
	    	double damping = _tableFactor._dampingParams[port];	    	
	    	if (damping != 0)
	    	{
	    		double[] outputMsgs = _tableFactor._outPortMsgs[port];
	    		double[] saved = _tableFactor._savedOutMsgArray[port];
	    		int outputMsgLength = outputMsgs.length;
	    		for (int i = 0; i < outputMsgLength; i++)
	    			outputMsgs[i] = (1-damping)*outputMsgs[i] + damping*saved[i];
	    	}
	    }
	    
    	
	    // Normalize the outputs
	    for (int port = 0; port < numPorts; port++)
	    {
    		double[] outputMsgs = _tableFactor._outPortMsgs[port];
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
