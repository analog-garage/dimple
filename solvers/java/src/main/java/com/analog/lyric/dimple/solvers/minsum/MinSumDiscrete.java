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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableDoubleArray;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

/**
 * Solver variable for Discrete variables under Min-Sum solver.
 * 
 * @since 0.07
 */
public class MinSumDiscrete extends SDiscreteVariableDoubleArray
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
	protected double[][] _savedOutMsgArray = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
	protected double[] _dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	protected boolean _dampingInUse = false;

	
	public MinSumDiscrete(Variable var)
	{
		super(var);
	}
	
	@Override
	public void initialize()
	{
		super.initialize();

		configureDampingFromOptions();
	}

	@Override
	protected void doUpdateEdge(int outPortNum)
	{

		double[] priors = _input;
		int numPorts = _var.getSiblingCount();
		int numValue = priors.length;

		// Compute the sum of all messages
		double minPotential = Double.POSITIVE_INFINITY;
		double[] outMsgs = _outputMessages[outPortNum];

		// Save previous output for damping
		if (_dampingInUse)
		{
			double damping = _dampingParams[outPortNum];
			if (damping != 0)
			{
				double[] saved = _savedOutMsgArray[outPortNum];
				for (int i = 0; i < outMsgs.length; i++)
					saved[i] = outMsgs[i];
			}
		}

		for (int i = 0; i < numValue; i++)
		{
			double out = priors[i];
			for (int port = 0; port < numPorts; port++)
				if (port != outPortNum) out += _inputMessages[port][i];
			outMsgs[i] = out;

			if (out < minPotential)
				minPotential = out;
		}

		// Damping
		if (_dampingInUse)
		{
			double damping = _dampingParams[outPortNum];
			if (damping != 0)
			{
				double[] saved = _savedOutMsgArray[outPortNum];
				for (int m = 0; m < numValue; m++)
					outMsgs[m] = outMsgs[m]*(1-damping) + saved[m]*damping;
			}
		}

		// Normalize the min
		for (int i = 0; i < numValue; i++)
			outMsgs[i] -= minPotential;
	}




	@Override
	protected void doUpdate()
	{

		double[] priors = _input;
		int numPorts = _var.getSiblingCount();
		int numValue = priors.length;

		// Compute the sum of all messages
		double[] beliefs = new double[numValue];

		for (int i = 0; i < numValue; i++)
		{
			double sum = priors[i];
			for (int port = 0; port < numPorts; port++)
				sum += _inputMessages[port][i];
			beliefs[i] = sum;
		}


		// Now compute output messages for each outgoing edge
		for (int port = 0; port < numPorts; port++ )
		{
			double[] outMsgs = _outputMessages[port];
			double minPotential = Double.POSITIVE_INFINITY;
			
			// Save previous output for damping
			if (_dampingInUse)
			{
				double damping = _dampingParams[port];
				if (damping != 0)
				{
					double[] saved = _savedOutMsgArray[port];
					for (int i = 0; i < outMsgs.length; i++)
						saved[i] = outMsgs[i];
				}
			}

			double[] inPortMsgsThisPort = _inputMessages[port];
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
				double damping = _dampingParams[port];
				if (damping != 0)
				{
					double[] saved = _savedOutMsgArray[port];
					for (int m = 0; m < numValue; m++)
						outMsgs[m] = outMsgs[m]*(1-damping) + saved[m]*damping;
				}
			}

			// Normalize the min
			for (int i = 0; i < numValue; i++)
				outMsgs[i] -= minPotential;
		}


	}

	@Override
	public double[] getBelief()
	{

		double[] priors = _input;
		double[] outBelief = new double[priors.length];
		int numValue = priors.length;
		int numPorts = _var.getSiblingCount();


		for (int i = 0; i < numValue; i++)
		{
			double sum = priors[i];
			for (int port = 0; port < numPorts; port++) sum += _inputMessages[port][i];
			outBelief[i] = sum;
		}

		// Convert to probabilities since that's what the interface expects
		return MessageConverter.toProb(outBelief);
	}


	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue, boolean hasFixedValue)
	{
		if (input == null)
			_input = MessageConverter.initialValue(((DiscreteDomain)_var.getDomain()).size());
		else
			// Convert from probabilities since that's what the interface provides
			_input = MessageConverter.fromProb((double[])input);
	}
	
	
	@Override
	public double getScore()
	{
		if (!_var.hasFixedValue())
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
		if (portIndex >= _dampingParams.length)
			return 0;
		else
			return _dampingParams[portIndex];
	}


	@Override
	public Object [] createMessages(ISolverFactor factor)
	{
		Object [] retval = super.createMessages(factor);
		
		return retval;
	}

	@Override
	public @NonNull double[] resetInputMessage(Object message)
	{
		final double[] result = (double[])message;
		Arrays.fill(result, 0);
		return result;
	}

	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort)
	{
		super.moveMessages(other, portNum, otherPort);
		MinSumDiscrete sother = (MinSumDiscrete)other;
		
		if (_dampingInUse)
		{
			_savedOutMsgArray[portNum] = sother._savedOutMsgArray[otherPort];

			_dampingParams[portNum] = sother._dampingParams[otherPort];
		}
	}

	/*---------------
	 * SNode methods
	 */
	
	@Override
	protected DiscreteEnergyMessage cloneMessage(int edge)
	{
		return new DiscreteEnergyMessage(_outputMessages[edge]);
	}
	
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
    	
    	_dampingParams =
    		getReplicatedNonZeroListFromOptions(BPOptions.nodeSpecificDamping, BPOptions.damping,
    			size, _dampingParams);
 
    	if (_dampingParams.length > 0 && _dampingParams.length != size)
    	{
			DimpleEnvironment.logWarning("%s has wrong number of parameters for %s\n",
				BPOptions.nodeSpecificDamping, this);
    		_dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
    	}
    	
    	_dampingInUse = _dampingParams.length > 0;
    	
    	if (!_dampingInUse)
    	{
    		_savedOutMsgArray = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    	}
    	else if (_savedOutMsgArray.length != size)
    	{
    		_savedOutMsgArray = new double[size][];
    		for (int i = 0; i < size; i++)
    	    {
    			_savedOutMsgArray[i] = new double[_inputMessages[i].length];
    	    }
    	}
    }
}
