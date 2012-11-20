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

package com.analog.lyric.dimple.solvers.gibbs;

import java.util.ArrayList;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;


public class STableFactor extends STableFactorBase implements ISolverFactorGibbs
{	
    protected int[][] _inPortMsgs = null;
    protected double[][] _outPortMsgs = null;
    protected int _numPorts;

    
	public STableFactor(Factor factor) 
	{
		super(factor);
	}
	

	public void updateEdge(int outPortNum)
	{
		FactorTable factorTable = getFactorTable();
	    double[] factorTableWeights = factorTable.getPotentials();
	    
	    double[] outMessage = _outPortMsgs[outPortNum];
	    int[] inPortMsgs = new int[_numPorts];
	    for (int port = 0; port < _numPorts; port++)
	    	inPortMsgs[port] = _inPortMsgs[port][0];
	    
    	int outputMsgLength = outMessage.length;
		for (int outIndex = 0; outIndex < outputMsgLength; outIndex++)
		{
			inPortMsgs[outPortNum] = outIndex;
			int weightIndex = factorTable.getWeightIndexFromTableIndices(inPortMsgs);
			outMessage[outIndex] = factorTableWeights[weightIndex];
		}
	}
	
	
	public void update()
	{
    	throw new DimpleException("Method not supported in Gibbs sampling solver.");
	}
	

	public Object getDefaultMessage(Port port) 
	{
		// WARNING: This method of initialization doesn't ensure a valid joint
		// value if the joint distribution has any zero-probability values
		return new int[]{0};
	}

	public double Potential() {return getPotential();}
	public double getPotential()
	{
	    int[] inPortMsgs = new int[_numPorts];
	    for (int port = 0; port < _numPorts; port++)
	    	inPortMsgs[port] = _inPortMsgs[port][0];
	    
	    return getPotential(inPortMsgs);
	}
	public double getPotential(int[] inputs)
	{
		FactorTable factorTable = getFactorTable();
		int weightIndex = factorTable.getWeightIndexFromTableIndices(inputs);
		return factorTable.getPotentials()[weightIndex];
	}
	
	
	@Override
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
    	ArrayList<Port> ports = _factor.getPorts();
    	_numPorts= ports.size();
	    _inPortMsgs = new int[_numPorts][];
	    _outPortMsgs = new double[_numPorts][];
	    
	    for (int port = 0; port < _numPorts; port++)
	    {
	    	_inPortMsgs[port] = (int[])ports.get(port).getInputMsg();
	    	_outPortMsgs[port] = (double[])ports.get(port).getOutputMsg();
	    }
    }


	
}
