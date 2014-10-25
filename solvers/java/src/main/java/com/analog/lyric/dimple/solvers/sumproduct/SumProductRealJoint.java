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

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.MultivariateNormal;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SRealJointVariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

/**
 * Solver variable for RealJoint variables under Sum-Product solver.
 * 
 * @since 0.07
 */
public class SumProductRealJoint extends SRealJointVariableBase
{

	private int _numVars;
	private @Nullable MultivariateNormalParameters _input;
	private MultivariateNormalParameters [] _outputMsgs = new MultivariateNormalParameters[0];
	private MultivariateNormalParameters [] _inputMsgs = new MultivariateNormalParameters[0];

	public SumProductRealJoint(Variable var)
	{
		super(var);
		
		_numVars = ((RealJointDomain)_var.getDomain()).getNumVars();
	}
	
	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue, boolean hasFixedValue)
	{
		if (hasFixedValue)
			_input = createFixedValueMessage((double[])Objects.requireNonNull(fixedValue));
		else if (input == null)
			_input = null;
		else
		{
    		if (input instanceof MultivariateNormal)	// Input is a MultivariateNormal factor function with fixed parameters
    		{
    			MultivariateNormal multivariateNormalInput = (MultivariateNormal)input;
    			if (!multivariateNormalInput.hasConstantParameters())
    				throw new DimpleException("MultivariateNormal factor function used as Input must have constant parameters");
    			_input = multivariateNormalInput.getParameters();
    		}
    		else if (input instanceof Normal[])			// Input is array of univariate Normal factor functions with fixed parameters
    		{
    			Normal[] inputArray = (Normal[])input;
    			if (inputArray.length != _numVars)
    				throw new DimpleException("Number of Inputs must equal the variable dimension");
    			double[] mean = new double[_numVars];
    			double[][] covariance = new double[_numVars][_numVars];
    			for (int i = 0; i < _numVars; i++)
    			{
    				mean[i] = inputArray[i].getMean();
    				covariance[i][i] = inputArray[i].getVariance();		// Diagonal covariance matrix
    			}
    			_input = new MultivariateNormalParameters(mean, covariance);
    		}
    		else if (input instanceof MultivariateNormalParameters)		// Input is a MultivariateNormalParameters object
    		{
    			_input = (MultivariateNormalParameters)input;
    		}
    		else
    			throw new DimpleException("Invalid input type");

		}
	}
	

	@Override
	public Object getBelief()
	{
		MultivariateNormalParameters m = new MultivariateNormalParameters();
		doUpdate(m,-1);
		return m;
	}
	
	@Override
	public Object getValue()
	{
		MultivariateNormalParameters m = (MultivariateNormalParameters)getBelief();
		return m.getMean();
	}
	
	@Override
	public double getScore()
	{
		final MultivariateNormalParameters input = _input;
		if (input == null)
			return 0;
		else
			return (new MultivariateNormal(input)).evalEnergy(getGuess());
	}
	

	@Override
	protected void doUpdateEdge(int outPortNum)
	{
		MultivariateNormalParameters outMsg = _outputMsgs[outPortNum];
		doUpdate(outMsg,outPortNum);
	}

	private void doUpdate(MultivariateNormalParameters outMsg, int outPortNum)
	{
		final MultivariateNormalParameters input = _input;
		
    	// If fixed value, just return the input, which has been set to a zero-variance message
		if (_var.hasFixedValue())
		{
			outMsg.set(Objects.requireNonNull(_input));
			return;
		}

		double[] vector;
		double[][] matrix;
		if (input == null)
		{
			vector = new double[_numVars];
			matrix = new double[_numVars][_numVars];
		}
		else
		{
			vector = input.getInformationVector();
			matrix = input.getInformationMatrix();
		}
		
		for (int i = 0; i < _outputMsgs.length; i++ )
		{
			if (i != outPortNum)
			{
				MultivariateNormalParameters inMsg = _inputMsgs[i];
				
				double [] inMsgVector = inMsg.getInformationVector();
				
				for (int j = 0; j < vector.length; j++)
					vector[j] += inMsgVector[j];
				
				double [][] inMsgMatrix = inMsg.getInformationMatrix();
				
				for (int j = 0; j < inMsgMatrix.length; j++)
					for (int k = 0; k < inMsgMatrix[j].length; k++)
						matrix[j][k] += inMsgMatrix[j][k];
			}
		}
		
		outMsg.setInformation(vector, matrix);
	}

	@Override
	public Object [] createMessages(ISolverFactor factor)
	{
		int portNum = getModelObject().getPortNum(Objects.requireNonNull(factor.getModelObject()));
		int arrayLength = Math.max(_inputMsgs.length, portNum+1);
		_inputMsgs = Arrays.copyOf(_inputMsgs, arrayLength);
		_inputMsgs[portNum] = createDefaultMessage();
		_outputMsgs = Arrays.copyOf(_outputMsgs,arrayLength);
		_outputMsgs[portNum] = createDefaultMessage();
		return new Object [] {_inputMsgs[portNum],_outputMsgs[portNum]};
	}

	public MultivariateNormalParameters createDefaultMessage()
	{
		MultivariateNormalParameters mm = new MultivariateNormalParameters();
		return (MultivariateNormalParameters)resetInputMessage(mm);
	}

	@Override
	public Object resetInputMessage(Object message)
	{
		double[] mean = new double[_numVars];
		double[][] covariance = new double[_numVars][];
		
		
		for (int i = 0; i < covariance.length; i++)
		{
			covariance[i] = new double[_numVars];
			covariance[i][i] = Double.POSITIVE_INFINITY;
		}
		((MultivariateNormalParameters)message).setMeanAndCovariance(mean, covariance);
		return message;
	}

	@Override
	public void resetEdgeMessages( int i )
	{
		_inputMsgs[i] = (MultivariateNormalParameters)resetInputMessage(_inputMsgs[i]);
		_outputMsgs[i] = (MultivariateNormalParameters)resetOutputMessage(_outputMsgs[i]);
	}
	
	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort)
	{
		SumProductRealJoint s = (SumProductRealJoint)other;
	
		_inputMsgs[portNum] = s._inputMsgs[otherPort];
		_outputMsgs[portNum] = s._outputMsgs[otherPort];

	}
	
	@Override
	public Object getInputMsg(int portIndex)
	{
		return _inputMsgs[portIndex];
	}

	@Override
	public Object getOutputMsg(int portIndex) {
		return _outputMsgs[portIndex];
	}
	@Override
	public void setInputMsg(int portIndex, Object obj) {
		_inputMsgs[portIndex] = (MultivariateNormalParameters)obj;
	}


	public MultivariateNormalParameters createFixedValueMessage(double[] fixedValue)
	{
		double[][] covariance = new double[_numVars][_numVars];
		for (int i = 0; i < _numVars; i++)
			Arrays.fill(covariance[i], 0);
		MultivariateNormalParameters message = new MultivariateNormalParameters(fixedValue, covariance);
		return message;
	}

	/*-----------------------
	 * SVariableBase methods
	 */
	
	@Override
	protected MultivariateNormalParameters cloneMessage(int edge)
	{
		return _outputMsgs[edge].clone();
	}
	
	@Override
	protected boolean supportsMessageEvents()
	{
		return true;
	}
}
