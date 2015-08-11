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

import java.util.List;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.minsum.MinSumDiscreteEdge;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolverGraph;

@SuppressWarnings("deprecation") // TODO remove when STableFactor removed
public class CustomXor extends com.analog.lyric.dimple.solvers.minsum.STableFactor
{
	private int _constantParity;
	private int _numPorts;


	public CustomXor(Factor factor, MinSumSolverGraph parent)
	{
		super(factor, parent);
	}

	
	@Override
	public void doUpdateEdge(int outPortNum)
	{
		final MinSumDiscreteEdge outEdge = getSiblingEdgeState(outPortNum);
		final double[] outMsg = outEdge.factorToVarMsg.representation();
		final double savedLLR = outMsg[1];		// LLR value is only in the 1 entry
		
		int hardXor = _constantParity;						// Initialize to parity of any constant inputs
		double min = Double.POSITIVE_INFINITY;
		for (int inPortIndex = 0; inPortIndex < _numPorts; inPortIndex++)
		{
			if (inPortIndex != outPortNum)
			{
				double[] inMsg = getSiblingEdgeState(inPortIndex).varToFactorMsg.representation();
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
		
		outMsg[1] = min * hardXor;
		outMsg[0] = 0;
	    
	   
	    // Damping
		if (_dampingInUse)
		{
			double damping = _dampingParams[outPortNum];
			if (damping != 0)
			{
				outMsg[1] = (1-damping)*outMsg[1] + damping*savedLLR;
			}
		}

	}

	
	@Override
	protected void doUpdate()
	{
		final boolean useDamping = _dampingInUse;
		final int numPorts = _numPorts;
		double[] savedLLR =
			useDamping ? DimpleEnvironment.doubleArrayCache.allocateAtLeast(numPorts) : ArrayUtil.EMPTY_DOUBLE_ARRAY;
		
			
	    if (useDamping)
	    {
	    	for (int port = 0; port < numPorts; port++)
	    	{
	    		if (_dampingParams[port] != 0)
	    		{
	    			savedLLR[port] = getSiblingEdgeState(port).factorToVarMsg.getEnergy(1);		// LLR value is only in the 1 entry
	    		}
	    	}
	    }

		int hardXor = _constantParity;					// Initialize to parity of any constant inputs
		double min = Double.POSITIVE_INFINITY;
		double secMin = Double.POSITIVE_INFINITY;
		int minIndex = -1;
		for (int inPortIndex = 0; inPortIndex < _numPorts; inPortIndex++)
		{
			double[] inMsg = getSiblingEdgeState(inPortIndex).varToFactorMsg.representation();
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
			final MinSumDiscreteEdge edge = getSiblingEdgeState(outPortIndex);
			double[] outMsg = edge.factorToVarMsg.representation();
			double[] inMsg = edge.varToFactorMsg.representation();
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
					double[] outputMsgs = getSiblingEdgeState(port).factorToVarMsg.representation();
					outputMsgs[1] = (1-damping)*outputMsgs[1] + damping*savedLLR[port];
				}
			}
			
			DimpleEnvironment.doubleArrayCache.release(savedLLR);
		}

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
		
		_numPorts = _model.getSiblingCount();
		
		// Pre-compute parity associated with any constant edges
		_constantParity = 1;
		final Factor factor = _model;
		if (factor.hasConstants())
		{
			List<Value> constantValues = factor.getConstantValues();
			int constantSum = 0;
			for (Value value : constantValues)
				constantSum += value.getInt();
			_constantParity = ((constantSum & 1) == 0) ? 1 : -1;
		}
	}
}
