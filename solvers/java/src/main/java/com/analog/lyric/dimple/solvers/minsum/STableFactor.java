package com.analog.lyric.dimple.solvers.minsum;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;

public class STableFactor extends STableFactorBase
{	
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
    protected double[][] _inPortMsgs = null;
    protected double[][] _outPortMsgs = null;
    protected double [][] _savedOutMsgArray;
    protected double [] _dampingParams;

    /*
     * We also save the values from the combo table.  This is necessary
     * since the minsum algorithm requires a modified list of values
     */
    //double [] _values = null;

    public STableFactor(Factor factor) 
	{
    	super(factor);
    	
    	//_values = values;
		_dampingParams = new double[_factor.getPorts().size()];
		updateCache();
	}

	public Object getInitialMsgValue(Port port)
	{
		int domainLength = ((Discrete)port.getConnectedNode()).getDiscreteDomain().getElements().length;
		double[] retVal = new double[domainLength];
		for (int i = 0; i < domainLength; i++) retVal[i] = 0;
		return retVal;
	}

	public void updateEdge(int outPortNum) 
	{
		ArrayList<Port> ports = _factor.getPorts();
		
	    int[][] table = _factorTable.getIndices();
	    double[] values = _factorTable.getPotentials();
	    int tableLength = table.length;
	    int numPorts = ports.size();


        double[] outputMsgs = _outPortMsgs[outPortNum];
        int outputMsgLength = outputMsgs.length;
        
    	double damping = _dampingParams[outPortNum];
    	double[] saved = _savedOutMsgArray[outPortNum];
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
        			L += _inPortMsgs[inPortNum][tableRow[inPortNum]];
        	
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
	
	
	@Override
	public void update() 
	{
	    int[][] table = _factorTable.getIndices();
	    double[] values = _factorTable.getPotentials();
	    int tableLength = table.length;
	    int numPorts = _factor.getPorts().size();
	    

	    for (int port = 0; port < numPorts; port++)
	    {
	    	double[] outputMsgs = _outPortMsgs[port];
	    	int outputMsgLength = outputMsgs.length;
	    	
	    	if (_dampingParams[port] != 0)
	    	{
		    	double[] saved = _savedOutMsgArray[port];
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
	    		L += _inPortMsgs[port][tableRow[port]];

			// Run through each output port
	    	for (int outPortNum = 0; outPortNum < numPorts; outPortNum++)
	    	{
	    		double[] outputMsgs = _outPortMsgs[outPortNum];
	    		int outputIndex = tableRow[outPortNum];											// Index for the output value
	    		double LThisPort = L - _inPortMsgs[outPortNum][tableRow[outPortNum]];			// Subtract out the message from this output port
	    		if (LThisPort < outputMsgs[outputIndex]) 
	    			outputMsgs[outputIndex] = LThisPort;	// Use the minimum value
	    	}
	    }
	   
	    // Damping
	    for (int port = 0; port < numPorts; port++)
	    {
	    	double damping = _dampingParams[port];	    	
	    	if (damping != 0)
	    	{
	    		double[] outputMsgs = _outPortMsgs[port];
	    		double[] saved = _savedOutMsgArray[port];
	    		int outputMsgLength = outputMsgs.length;
	    		for (int i = 0; i < outputMsgLength; i++)
	    			outputMsgs[i] = (1-damping)*outputMsgs[i] + damping*saved[i];
	    	}
	    }
	    
    	
	    // Normalize the outputs
	    for (int port = 0; port < numPorts; port++)
	    {
    		double[] outputMsgs = _outPortMsgs[port];
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
	
    public void initialize() 
    {
    	super.initialize();
		//We update the cache here.  This works only because initialize() is called on the variables
		//first.  Updating the cache saves msg in double arrays.  initialize replaces these double arrays
		//with new double arrays.  If we didn't call updateCache on initialize, our cache would point
		//to stale information.
    	updateCache();
    }
    
    private void updateCache()
    {
    	int numPorts = _factor.getPorts().size();
	    _inPortMsgs = new double[numPorts][];
	    _outPortMsgs = new double[numPorts][];
	    _savedOutMsgArray = new double[numPorts][];
	    for (int port = 0; port < numPorts; port++)
	    {
	    	_inPortMsgs[port] = (double[])_factor.getPorts().get(port).getInputMsg();
	    	_outPortMsgs[port] = (double[])_factor.getPorts().get(port).getOutputMsg();
	    	_savedOutMsgArray[port] = new double[_outPortMsgs[port].length];
	    }
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
		throw new DimpleException("getEnergy not yet supported for MinSum");
	}
	
	
}

