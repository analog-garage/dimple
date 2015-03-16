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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableDoubleArray;

/**
 * Solver variable for Discrete variables under Min-Sum solver.
 * 
 * @since 0.07
 */
public class MinSumDiscrete extends SDiscreteVariableDoubleArray
{
	/*-------
	 * State
	 */
	
	protected boolean _dampingInUse = false;
	protected MinSumDiscreteEdge[] _edges;

	/*--------------
	 * Construction
	 */
	
	MinSumDiscrete(Discrete var, MinSumSolverGraph parent)
	{
		super(var, parent);
		_edges = new MinSumDiscreteEdge[var.getSiblingCount()];
	}
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public void initialize()
	{
		super.initialize();

		final int nEdges = _model.getSiblingCount();
		if (nEdges != _edges.length)
		{
			_edges = new MinSumDiscreteEdge[nEdges];
		}
		for (int i = 0; i < nEdges; ++i)
		{
			_edges[i] = getSiblingEdgeState(i);
		}
		
		configureDampingFromOptions();
	}

	/*---------------
	 * SNode methods
	 */
	
	@Override
	protected void doUpdateEdge(int outPortNum)
	{

		double[] priors = _input;
		final int numPorts = _model.getSiblingCount();
		final int numValue = priors.length;

		final MinSumDiscreteEdge edge = _edges[outPortNum];
		final double[] outMsgs = edge.varToFactorMsg.representation();
		
        double[] savedOutMsgArray = ArrayUtil.EMPTY_DOUBLE_ARRAY;

        // Save previous output for damping
		if (_dampingInUse)
		{
        	savedOutMsgArray = DimpleEnvironment.doubleArrayCache.allocateAtLeast(numValue);
			double damping = edge._damping;
			if (damping != 0)
			{
				System.arraycopy(outMsgs, 0, savedOutMsgArray, 0, numValue);
			}
		}

		System.arraycopy(priors, 0, outMsgs, 0, numValue);
		for (int port = 0; port < outPortNum; ++port)
		{
			final double[] energies = _edges[port].factorToVarMsg.representation();
			for (int i = 0; i < numValue; ++i)
			{
				outMsgs[i] += energies[i];
			}
		}
		for (int port = outPortNum + 1; port < numPorts; ++port)
		{
			final double[] energies = _edges[port].factorToVarMsg.representation();
			for (int i = 0; i < numValue; ++i)
			{
				outMsgs[i] += energies[i];
			}
		}

		// Damping
		if (_dampingInUse)
		{
			double damping = edge._damping;
			if (damping != 0)
			{
				final double inverseDamping = 1.0 - damping;
				for (int m = 0; m < numValue; m++)
					outMsgs[m] = outMsgs[m]*inverseDamping + savedOutMsgArray[m]*damping;
			}

			DimpleEnvironment.doubleArrayCache.release(savedOutMsgArray);
		}

		// Normalize the min
		double minPotential = outMsgs[0];
		for (int i = 1; i < numValue; ++i)
		{
			minPotential = Math.min(minPotential, outMsgs[i]);
		}
		if (minPotential != 0.0)
		{
			for (int i = 0; i < numValue; i++)
			{
				outMsgs[i] -= minPotential;
			}
		}
	}




	@Override
	protected void doUpdate()
	{

		double[] priors = _input;
		int numPorts = _model.getSiblingCount();
		int numValue = priors.length;

		// Compute the sum of all messages
		final double[] beliefs = priors.clone();

		for (int port = 0; port < numPorts; port++)
		{
			final double[] inMsgs = _edges[port].factorToVarMsg.representation();
			for (int i = 0; i < numValue; i++)
			{
				beliefs[i] += inMsgs[i];
			}
		}
		
		// Now compute output messages for each outgoing edge
        final double[] savedOutMsgArray =
        	_dampingInUse ? DimpleEnvironment.doubleArrayCache.allocateAtLeast(numValue) : ArrayUtil.EMPTY_DOUBLE_ARRAY;
		for (int port = 0; port < numPorts; port++ )
		{
			final MinSumDiscreteEdge edge = _edges[port];
			double[] outMsgs = edge.varToFactorMsg.representation();
			double minPotential = Double.POSITIVE_INFINITY;
			
			// Save previous output for damping
			if (_dampingInUse && edge._damping != 0)
			{
				System.arraycopy(outMsgs, 0, savedOutMsgArray, 0, numValue);
			}

			double[] inPortMsgsThisPort = edge.factorToVarMsg.representation();
			for (int i = 0; i < numValue; i++)
			{
				double out = beliefs[i] - inPortMsgsThisPort[i];
				if (out < minPotential)
					minPotential = out;
				outMsgs[i] = out;
			}

			// Damping
			if (_dampingInUse)
			{
				double damping = edge._damping;
				if (damping != 0)
				{
					final double inverseDamping = 1.0 - damping;
					for (int m = 0; m < numValue; m++)
					{
						outMsgs[m] = outMsgs[m]*inverseDamping + savedOutMsgArray[m]*damping;
					}
				}
			}

			// Normalize the min
			if (minPotential != 0.0)
			{
				for (int i = 0; i < numValue; i++)
					outMsgs[i] -= minPotential;
			}
		}

	    if (savedOutMsgArray.length > 0)
	    {
	    	DimpleEnvironment.doubleArrayCache.release(savedOutMsgArray);
	    }

	}

	/*-------------------------
	 * ISolverVariable methods
	 */
	
	@Override
	public double[] getBelief()
	{

		double[] priors = _input;
		double[] outBelief = new double[priors.length];
		int numValue = priors.length;
		int numPorts = _model.getSiblingCount();


		for (int i = 0; i < numValue; i++)
		{
			double sum = priors[i];
			for (int port = 0; port < numPorts; port++)
			{
				sum += getSiblingEdgeState(port).factorToVarMsg.getEnergy(i);
			}
			outBelief[i] = sum;
		}

		// Convert to probabilities since that's what the interface expects
		return MessageConverter.toProb(outBelief);
	}


	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue)
	{
		if (input == null)
			_input = MessageConverter.initialValue(_model.getDomain().size());
		else
			// Convert from probabilities since that's what the interface provides
			_input = MessageConverter.fromProb((double[])input);
	}
	
	@Override
	public double getScore()
	{
		if (!_model.hasFixedValue())
			return _input[getGuessIndex()];
		else
			return 0;	// If the value is fixed, ignore the guess
	}


	/**
	 * @deprecated Use {@link BPOptions#damping} or {@link BPOptions#nodeSpecificDamping} options instead.
	 */
	@Deprecated
	public void setDamping(int portIndex, double dampingVal)
	{
		double[] params  = BPOptions.nodeSpecificDamping.getOrDefault(this).toPrimitiveArray();
		if (params.length == 0 && dampingVal != 0.0)
		{
			params = new double[getSiblingCount()];
		}
		if (params.length != 0)
		{
			params[portIndex] = dampingVal;
		}
		
		BPOptions.nodeSpecificDamping.set(this, params);
		configureDampingFromOptions();
	}

	public double getDamping(int portIndex)
	{
		if (_dampingInUse)
		{
			return getSiblingEdgeState(portIndex)._damping;
		}
		
		return 0.0;
	}

	/*---------------
	 * SNode methods
	 */
	
	@Override
	protected boolean supportsMessageEvents()
	{
		return true;
	}
	
	/*-----------------
	 * Private methods
	 */
	
    private void configureDampingFromOptions()
    {
     	final int size = getSiblingCount();
    	
    	double[] dampingParams =
    		getReplicatedNonZeroListFromOptions(BPOptions.nodeSpecificDamping, BPOptions.damping, size, null);
 
    	if (dampingParams.length > 0 && dampingParams.length != size)
    	{
			DimpleEnvironment.logWarning("%s has wrong number of parameters for %s\n",
				BPOptions.nodeSpecificDamping, this);
    		dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
    	}
    	
    	_dampingInUse = dampingParams.length > 0;
    	
    	if (_dampingInUse)
    	{
    		for (int i = 0; i < size; ++i)
    		{
    			_edges[i]._damping = dampingParams[i];
    		}
    	}
    }

    @Override
	@SuppressWarnings("null")
	public MinSumDiscreteEdge getSiblingEdgeState(int siblingIndex)
	{
		return (MinSumDiscreteEdge)getSiblingEdgeState_(siblingIndex);
	}
}
