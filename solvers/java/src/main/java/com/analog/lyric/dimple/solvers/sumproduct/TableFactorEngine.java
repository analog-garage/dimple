package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;

/*
 * Provides the update and updateEdge logic for sumproduct
 */
public class TableFactorEngine 
{
	STableFactor _tableFactor;
	Factor _factor;
	
	public TableFactorEngine(STableFactor tableFactor)
	{
		_tableFactor = tableFactor;
		_factor = _tableFactor.getFactor();
	}
	
	public void updateEdge(int outPortNum) 
	{
		
		ArrayList<Port> ports = _factor.getPorts();
	    int[][] table = _tableFactor.getFactorTable().getIndices();
	    double[] values = _tableFactor.getFactorTable().getWeights();
	    int tableLength = table.length;
	    int numPorts = ports.size();
	    
        double[] outputMsgs = _tableFactor._outMsgArray[outPortNum];
        
    	double damping = _tableFactor._dampingParams[outPortNum];
    	double [] saved = _tableFactor._savedOutMsgArray[outPortNum];
    	
    	if (damping != 0)
    	{
    		for (int i = 0; i < outputMsgs.length; i++)
    			saved[i] = outputMsgs[i];
    	}
        
    	int outputMsgLength = outputMsgs.length;
        for (int i = 0; i < outputMsgLength; i++) outputMsgs[i] = 0;
                
        
        for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
        {
        	double prob = values[tableIndex];
        	int[] tableRow = table[tableIndex];
    		int outputIndex = tableRow[outPortNum];
        	
        	for (int inPortNum = 0; inPortNum < numPorts; inPortNum++)
        		if (inPortNum != outPortNum)
        			prob *= _tableFactor._inPortMsgs[inPortNum][tableRow[inPortNum]];
        	outputMsgs[outputIndex] += prob;
        }
        
    	double sum = 0; 

    	
    	for (int i = 0; i < outputMsgLength; i++) sum += outputMsgs[i];
    	

		if (sum == 0)
		{
			throw new DimpleException("UpdateEdge failed in SumProduct Solver.  All probabilities were zero when calculating message for port " 
					+ outPortNum + " on factor " + _factor.getLabel());
		}

    	
    	for (int i = 0; i < outputMsgLength; i++) 
    		{
    		outputMsgs[i] /= sum;
    		

    		}

    	
    	if (damping != 0)
    		for (int i = 0; i < outputMsgLength; i++)
    			outputMsgs[i] = (1-damping)*outputMsgs[i] + damping*saved[i];
    	
	}
	

	
	
	public void update() 
	{				

		
		ArrayList<Port> ports = _factor.getPorts();
	    int[][] table = _tableFactor.getFactorTable().getIndices();
	    double[] values = _tableFactor.getFactorTable().getWeights();
	    int tableLength = table.length;
	    int numPorts = ports.size();
	    
	    
	    for (int outPortNum = 0; outPortNum < numPorts; outPortNum++)
	    {
	    	double[] outputMsgs = _tableFactor._outMsgArray[outPortNum];
	    		    	
	    	double damping = _tableFactor._dampingParams[outPortNum];	    	
	    	double [] saved = _tableFactor._savedOutMsgArray[outPortNum];
	    	
	    	if (damping != 0)
	    	{
	    		for (int i = 0; i < outputMsgs.length; i++)
	    			saved[i] = outputMsgs[i];
	    	}
	    	
	    	int outputMsgLength = outputMsgs.length;
	    	for (int i = 0; i < outputMsgLength; i++) outputMsgs[i] = 0;

	    	for (int tableIndex = 0; tableIndex < tableLength; tableIndex++)
	    	{
	    		double prob = values[tableIndex];
	    		int[] tableRow = table[tableIndex];
	    		int outputIndex = tableRow[outPortNum];

	    		for (int inPortNum = 0; inPortNum < numPorts; inPortNum++)
	    			if (inPortNum != outPortNum)
	    			{	    				
	    				prob *= _tableFactor._inPortMsgs[inPortNum][tableRow[inPortNum]];
	    			}
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
	    	{
	    		
	    		outputMsgs[i] /= sum;
	    		
	    	}
	    	
	    	if (damping != 0)
	    		for (int i = 0; i < outputMsgLength; i++)
	    			outputMsgs[i] = (1-damping)*outputMsgs[i] + damping*saved[i];
	    	
	    }
	}
}
