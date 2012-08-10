package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;



public class STableFactor extends STableFactorBase
{	
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
	double [][] _inPortMsgs;
	double [][] _outMsgArray;
	double [][] _savedOutMsgArray;
	double [] _dampingParams;
	boolean _initCalled = true;
	

	public STableFactor(Factor factor)  
	{
		super(factor);
		
		
		_dampingParams = new double[_factor.getPorts().size()];
	}
	
	public void setDamping(int index, double val)
	{
		_dampingParams[index] = val;
	}
	
	public double getDamping(int index)
	{
		return _dampingParams[index];
	}
	

	public double getEnergy()
	{
		int [] indices = new int[_factor.getPorts().size()];
		
		for (int i = 0; i < indices.length; i++)
		{
			SVariable tmp = (SVariable)((VariableBase)_factor.getPorts().get(i).getConnectedNode()).getSolver();
			indices[i] = tmp.getGuessIndex();
		}
		
		 int[][] table = _factorTable.getIndices();
	     double[] values = _factorTable.getWeights();
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
	
	public void updateEdge(int outPortNum) 
	{
		updateCache();
		

		
		ArrayList<Port> ports = _factor.getPorts();
	    int[][] table = _factorTable.getIndices();
	    double[] values = _factorTable.getWeights();
	    int tableLength = table.length;
	    int numPorts = ports.size();
	    
        double[] outputMsgs = _outMsgArray[outPortNum];
        
    	double damping = _dampingParams[outPortNum];
    	double [] saved = _savedOutMsgArray[outPortNum];
    	
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
        			prob *= _inPortMsgs[inPortNum][tableRow[inPortNum]];
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
	

	
	
	@Override
	public void update() 
	{		
		updateCache();
		

		
		ArrayList<Port> ports = _factor.getPorts();
	    int[][] table = _factorTable.getIndices();
	    double[] values = _factorTable.getWeights();
	    int tableLength = table.length;
	    int numPorts = ports.size();
	    
	    
	    for (int outPortNum = 0; outPortNum < numPorts; outPortNum++)
	    {
	    	double[] outputMsgs = _outMsgArray[outPortNum];
	    		    	
	    	double damping = _dampingParams[outPortNum];	    	
	    	double [] saved = _savedOutMsgArray[outPortNum];
	    	
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
	    				prob *= _inPortMsgs[inPortNum][tableRow[inPortNum]];
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
	
	@Override
	public void connectPort(Port p)  
	{
		// TODO Auto-generated method stub
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
		    _savedOutMsgArray = new double[numPorts][];
		    for (int port = 0; port < numPorts; port++)
		    {
		    	_outMsgArray[port] = (double[])_factor.getPorts().get(port).getOutputMsg();
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
	
	
	public double [] getBelief() 
	{
		updateCache();
		
		//throw new DimpleException("not supported");
		int [][] table = _factorTable.getIndices();
		double [] values = _factorTable.getWeights();
		double [] retval = new double[table.length];
		
		double sum = 0;
		
		for (int i = 0; i < table.length; i++)
		{
			retval[i] = values[i];
			for (int j = 0; j < table[i].length; j++)
			{
				retval[i] *= _inPortMsgs[j][table[i][j]];
			}
			sum += retval[i];
		}
		
		for (int i = 0; i < retval.length; i++)
		{
			retval[i] /= sum;
		}
		
		return retval;
	}

}
