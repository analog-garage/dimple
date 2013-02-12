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

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;


public class STableFactor extends STableFactorBase implements ISolverFactorGibbs
{	
    protected DiscreteSample[] _inPortMsgs = null;
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
	    	inPortMsgs[port] = _inPortMsgs[port].index;
	    
    	int outputMsgLength = outMessage.length;
		for (int outIndex = 0; outIndex < outputMsgLength; outIndex++)
		{
			inPortMsgs[outPortNum] = outIndex;
			int weightIndex = factorTable.getWeightIndexFromTableIndices(inPortMsgs);
			if (weightIndex >= 0)
				outMessage[outIndex] = factorTableWeights[weightIndex];
			else
				outMessage[outIndex] = Double.POSITIVE_INFINITY;
		}
	}
	
	
	public void update()
	{
    	throw new DimpleException("Method not supported in Gibbs sampling solver.");
	}
	


	public double getPotential()
	{
	    int[] inPortMsgs = new int[_numPorts];
	    for (int port = 0; port < _numPorts; port++)
	    	inPortMsgs[port] = _inPortMsgs[port].index;
	    
	    return getPotential(inPortMsgs);
	}
	public double getPotential(int[] inputs)
	{
		FactorTable factorTable = getFactorTable();
		int weightIndex = factorTable.getWeightIndexFromTableIndices(inputs);
		if (weightIndex >= 0)
			return factorTable.getPotentials()[weightIndex];
		else
			return Double.POSITIVE_INFINITY;
	}
		
	



	@Override
	public void createMessages() 
	{
    	int size = _factor.getSiblings().size();
    	_numPorts= size;
    	
	    _inPortMsgs = new DiscreteSample[_numPorts];
	    _outPortMsgs = new double[_numPorts][];
	    
	    for (int port = 0; port < _numPorts; port++)
	    {
	    	ISolverVariable svar = _factor.getVariables().getByIndex(port).getSolver();
	    	Object [] messages = svar.createMessages(this);
	    	_inPortMsgs[port] = (DiscreteSample)messages[1];
	    	_outPortMsgs[port] = (double[])messages[0];
	    }
	}


	@Override
	public void initializeEdge(int portNum) 
	{
		_inPortMsgs[portNum].index = 0;
	}


	@Override
	public Object getInputMsg(int portIndex) 
	{
		// TODO Auto-generated method stub
		return _inPortMsgs[portIndex];
	}


	@Override
	public Object getOutputMsg(int portIndex) 
	{
		// TODO Auto-generated method stub
		return _outPortMsgs[portIndex];
	}


	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum) 
	{
		STableFactor tf = (STableFactor)other;
		this._inPortMsgs[thisPortNum] = tf._inPortMsgs[otherPortNum];
		this._outPortMsgs[thisPortNum] = tf._outPortMsgs[otherPortNum];
		
	}


	
}
