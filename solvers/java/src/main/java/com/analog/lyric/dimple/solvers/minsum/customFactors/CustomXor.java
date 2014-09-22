/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.minsum.customFactors;

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.NonNull;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

@SuppressWarnings("deprecation") // TODO remove when STableFactor removed
public class CustomXor extends com.analog.lyric.dimple.solvers.minsum.STableFactor
{
	private double[][] _inPortMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    private double[][] _outPortMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    private double [] _savedOutMsgsLLR = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	private int _constantParity;
	private int _numPorts;


	public CustomXor(Factor factor)
	{
		super(factor);
	}

	
	@Override
	public void doUpdateEdge(int outPortNum)
	{
	    if (_dampingInUse)
	    {
	    	if (_dampingParams[outPortNum] != 0)
	    	{
	    		double[] outputMsgs = _outPortMsgs[outPortNum];
	    		_savedOutMsgsLLR[outPortNum] = outputMsgs[1];		// LLR value is only in the 1 entry
	    	}
	    }

		int hardXor = _constantParity;						// Initialize to parity of any constant inputs
		double min = Double.POSITIVE_INFINITY;
		for (int inPortIndex = 0; inPortIndex < _numPorts; inPortIndex++)
		{
			if (inPortIndex != outPortNum)
			{
				double[] inMsg = _inPortMsgs[inPortIndex];
				double in = inMsg[1] - inMsg[0];			// Get the input LLR value
				if (in < 0)
				{
					hardXor = -hardXor;						// XOR of the sign of the input
					in = -in;								// Absolute value of the input
				}
				if (in < min)								// Find the minimum and second minimum
				{
					min = in;
				}
			}
		}
		
		double[] outMsg = _outPortMsgs[outPortNum];
		outMsg[1] = min * hardXor;
		outMsg[0] = 0;
	    
	   
	    // Damping
		if (_dampingInUse)
		{
			double damping = _dampingParams[outPortNum];
			if (damping != 0)
			{
				double[] outputMsgs = _outPortMsgs[outPortNum];
				outputMsgs[1] = (1-damping)*outputMsgs[1] + damping*_savedOutMsgsLLR[outPortNum];
			}
		}

	}

	
	@Override
	protected void doUpdate()
	{
	    if (_dampingInUse)
	    {
	    	for (int port = 0; port < _numPorts; port++)
	    	{
	    		if (_dampingParams[port] != 0)
	    		{
	    			double[] outputMsgs = _outPortMsgs[port];
	    			_savedOutMsgsLLR[port] = outputMsgs[1];		// LLR value is only in the 1 entry
	    		}
	    	}
	    }

		int hardXor = _constantParity;					// Initialize to parity of any constant inputs
		double min = Double.POSITIVE_INFINITY;
		double secMin = Double.POSITIVE_INFINITY;
		int minIndex = -1;
		for (int inPortIndex = 0; inPortIndex < _numPorts; inPortIndex++)
		{
			double[] inMsg = _inPortMsgs[inPortIndex];
			double in = inMsg[1] - inMsg[0];			// Get the input LLR value
			if (in < 0)
			{
				hardXor = -hardXor;						// XOR of the sign of the input
				in = -in;								// Absolute value of the input
			}
			if (in < min)								// Find the minimum and second minimum
			{
				secMin = min;
				min = in;
				minIndex = inPortIndex;
			}
			else if (in < secMin)
				secMin = in;
		}
		
		for (int outPortIndex = 0; outPortIndex < _numPorts; outPortIndex++)
		{
			double[] outMsg = _outPortMsgs[outPortIndex];
			double[] inMsg = _inPortMsgs[outPortIndex];
			double in = inMsg[1] - inMsg[0];				// Get the input LLR value
			double out;
			if (in < 0)
				out = ((outPortIndex == minIndex) ? secMin : min) * (-hardXor);
			else
				out = ((outPortIndex == minIndex) ? secMin : min) * hardXor;
			outMsg[1] = out;
			outMsg[0] = 0;
		}
	    
	   
	    // Damping
		if (_dampingInUse)
		{
			for (int port = 0; port < _numPorts; port++)
			{
				double damping = _dampingParams[port];
				if (damping != 0)
				{
					double[] outputMsgs = _outPortMsgs[port];
					outputMsgs[1] = (1-damping)*outputMsgs[1] + damping*_savedOutMsgsLLR[port];
				}
			}
		}

	}
	
	@Override
	protected void configureSavedMessages(int size)
	{
		_savedOutMsgsLLR = _dampingInUse ? new double[_dampingParams.length] : ArrayUtil.EMPTY_DOUBLE_ARRAY;
	}
	
	@Override
	public void createMessages()
	{
		final Factor factor = _factor;
		int nVars = factor.getSiblingCount();
		
	    _inPortMsgs = new double[nVars][];
	    _outPortMsgs = new double[nVars][];
	    
	    if (_dampingInUse)
	    	_savedOutMsgsLLR = new double[nVars];
	    
		for (int i = 0; i < nVars; i++)
		{
			ISolverVariable sv = requireNonNull(factor.getSibling(i).getSolver());
			Object [] messages = requireNonNull(sv.createMessages(this));
			_outPortMsgs[i] = (double[])messages[0];
			_inPortMsgs[i] = (double[])messages[1];
		}
	}



	@Override
	public void moveMessages(@NonNull ISolverNode other, int portNum, int otherPort)
	{
		CustomXor x = (CustomXor)other;
		_inPortMsgs[portNum] = x._inPortMsgs[otherPort];
		_outPortMsgs[portNum] = x._outPortMsgs[otherPort];
		_savedOutMsgsLLR[portNum] = x._savedOutMsgsLLR[otherPort];
	}

    @Override
	protected boolean createFactorTableOnInit()
    {
    	return false;
    }

	@Override
	public void initialize()
	{
		super.initialize();
		
		_numPorts = _factor.getSiblingCount();
		
		// Pre-compute parity associated with any constant edges
		_constantParity = 1;
		FactorFunction factorFunction = _factor.getFactorFunction();
		if (factorFunction.hasConstants())
		{
			Object[] constantValues = factorFunction.getConstants();
			int constantSum = 0;
			for (int i = 0; i < constantValues.length; i++)
				constantSum += FactorFunctionUtilities.toInteger(constantValues[i]);
			_constantParity = ((constantSum & 1) == 0) ? 1 : -1;
		}
	}

}
