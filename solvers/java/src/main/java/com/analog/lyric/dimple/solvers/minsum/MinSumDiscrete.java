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
import com.analog.lyric.collect.DoubleArrayCache;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableDoubleArray;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;

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
	
	protected @Nullable double[] _dampingParams = null;
	protected double[][] _inMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
	protected double[][] _outMsgs = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;

	/*--------------
	 * Construction
	 */
	
	MinSumDiscrete(Discrete var, MinSumSolverGraph parent)
	{
		super(var, parent);
	}
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public void initialize()
	{
		super.initialize();

		final int nEdges = _model.getSiblingCount();
		if (nEdges != _inMsgs.length)
		{
			_inMsgs = new double[nEdges][];
			_outMsgs = new double[nEdges][];
		}
		for (int i = 0; i < nEdges; ++i)
		{
			MinSumDiscreteEdge edge = getSiblingEdgeState(i);
			_inMsgs[i] = edge.factorToVarMsg.representation();
			_outMsgs[i] = edge.varToFactorMsg.representation();
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

		final double[] outMsgs = _outMsgs[outPortNum];
		final double[] dampingParams = _dampingParams;
		final double damping = dampingParams != null ? dampingParams[outPortNum] : 0.0;
		
		if (damping != 0.0)
		{
	        // Save previous output for damping
			final double[] savedOutMsgArray = DimpleEnvironment.doubleArrayCache.allocateAtLeast(numValue);
			System.arraycopy(outMsgs, 0, savedOutMsgArray, 0, numValue);

			System.arraycopy(priors, 0, outMsgs, 0, numValue);
			int port = numPorts;
			while (--port > outPortNum)
			{
				final double[] energies = _inMsgs[port];
				for (int i = numValue; --i>=0;)
				{
					outMsgs[i] += energies[i];
				}
			}
			while(--port >= 0)
			{
				final double[] energies = _inMsgs[port];
				for (int i = numValue; --i>=0;)
				{
					outMsgs[i] += energies[i];
				}
			}

			// Apply damping
			final double inverseDamping = 1.0 - damping;
			for (int m = numValue; --m>=0;)
			{
				outMsgs[m] = outMsgs[m]*inverseDamping + savedOutMsgArray[m]*damping;
			}
			
			// Release temp array
			DimpleEnvironment.doubleArrayCache.release(savedOutMsgArray);
		}
		else
		{
			System.arraycopy(priors, 0, outMsgs, 0, numValue);
			int port = numPorts;
			while (--port > outPortNum)
			{
				final double[] energies = _inMsgs[port];
				for (int i = numValue; --i>=0;)
				{
					outMsgs[i] += energies[i];
				}
			}
			while(--port >= 0)
			{
				final double[] energies = _inMsgs[port];
				for (int i = numValue; --i>=0;)
				{
					outMsgs[i] += energies[i];
				}
			}
		}

		// Normalize the min
		double minPotential = outMsgs[0];
		for (int i = 1; i < numValue; ++i)
		{
			minPotential = Math.min(minPotential, outMsgs[i]);
		}
		if (minPotential != 0.0)
		{
			for (int i = numValue; --i>=0;)
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
		final DoubleArrayCache cache = DimpleEnvironment.doubleArrayCache;
		final double[] beliefs = cache.allocateAtLeast(numValue);
		System.arraycopy(priors, 0, beliefs, 0, numValue);

		for (int port = numPorts; --port>=0;)
		{
			final double[] inMsgs = _inMsgs[port];
			for (int i = numValue; --i>=0;)
			{
				beliefs[i] += inMsgs[i];
			}
		}
		
		final double[] dampingParams = _dampingParams;
		
		if (dampingParams != null)
		{
	        final double[] savedOutMsgArray = cache.allocateAtLeast(numValue);
	        
			for (int port = numPorts; --port>=0; )
			{
				final double[] outMsgs = _outMsgs[port];
				double minPotential = Double.POSITIVE_INFINITY;

				final double damping = dampingParams[port];
				if (damping != 0.0)
				{
					System.arraycopy(outMsgs, 0, savedOutMsgArray, 0, numValue);
				}

				final double[] inPortMsgsThisPort = _inMsgs[port];
				for (int i = numValue; --i>=0;)
				{
					double out = beliefs[i] - inPortMsgsThisPort[i];
					minPotential = Math.min(minPotential, out);
					outMsgs[i] = out;
				}

				// Damping
				if (damping != 0)
				{
					final double inverseDamping = 1.0 - damping;
					for (int m = numValue; --m>=0;)
					{
						outMsgs[m] = outMsgs[m]*inverseDamping + savedOutMsgArray[m]*damping;
					}
				}

				// Normalize the min
				if (minPotential != 0.0)
				{
					for (int i = numValue; --i>=0;)
						outMsgs[i] -= minPotential;
				}
			}
	        
	        
	        // Release temp array
	    	cache.release(savedOutMsgArray);
		}
		else
		{
			for (int port = numPorts; --port>=0; )
			{
				final double[] outMsgs = _outMsgs[port];
				double minPotential = Double.POSITIVE_INFINITY;
				
				final double[] inPortMsgsThisPort = _inMsgs[port];
				for (int i = numValue; --i>=0;)
				{
					double out = beliefs[i] - inPortMsgsThisPort[i];
					minPotential = Math.min(minPotential, out);
					outMsgs[i] = out;
				}

				// Normalize the min
				if (minPotential != 0.0)
				{
					for (int i = numValue; --i>=0;)
						outMsgs[i] -= minPotential;
				}
			}
		}
		
		cache.release(beliefs);
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
			if (input instanceof DiscreteMessage)
				_input = ((DiscreteMessage)input).getEnergies();
			else
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
	public void setDamping(int siblingNumber, double dampingVal)
	{
		double[] params  = BPOptions.nodeSpecificDamping.getOrDefault(this).toPrimitiveArray();
		if (params.length == 0 && dampingVal != 0.0)
		{
			params = new double[getSiblingCount()];
		}
		if (params.length != 0)
		{
			params[siblingNumber] = dampingVal;
		}
		
		BPOptions.nodeSpecificDamping.set(this, params);
		configureDampingFromOptions();
	}

	public double getDamping(int siblingNumber)
	{
		final double[] dampingParams = _dampingParams;
		return dampingParams != null ? dampingParams[siblingNumber] : 0.0;
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
    	
    	double[] dampingParams = _dampingParams =
    		getReplicatedNonZeroListFromOptions(BPOptions.nodeSpecificDamping, BPOptions.damping, size, _dampingParams);
 
    	if (dampingParams.length > 0 && dampingParams.length != size)
    	{
			DimpleEnvironment.logWarning("%s has wrong number of parameters for %s\n",
				BPOptions.nodeSpecificDamping, this);
			_dampingParams = null;
    	}
    	
    	if (dampingParams.length == 0)
    	{
    		_dampingParams = null;
    	}
    }

    @Override
	@SuppressWarnings("null")
	public MinSumDiscreteEdge getSiblingEdgeState(int siblingIndex)
	{
		return (MinSumDiscreteEdge)getSiblingEdgeState_(siblingIndex);
	}
}
