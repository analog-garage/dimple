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

import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class SRealJointVariable extends SVariableBase 
{

	private int _numVars;
	private MultivariateGaussianMessage _input;
	private MultivariateGaussianMessage [] _outputMsgs = new MultivariateGaussianMessage[0];
	private MultivariateGaussianMessage [] _inputMsgs = new MultivariateGaussianMessage[0];
	
	public SRealJointVariable(VariableBase var) 
	{
		super(var);
		
		_numVars = ((RealJointDomain)_var.getDomain()).getNumVars();
	}
	
	@Override
	public void setInputOrFixedValue(Object input, Object fixedValue, boolean hasFixedValue)
	{
		if (hasFixedValue)
			_input = createFixedValueMessage((double[])fixedValue);
		else if (input == null)
			_input = (MultivariateGaussianMessage) createDefaultMessage();
		else
			_input = (MultivariateGaussianMessage)input;
	}
	

	@Override
	public Object getBelief()  
	{
		MultivariateGaussianMessage m = new MultivariateGaussianMessage(_input.getMeans(), _input.getCovariance());		
		doUpdate(m,-1);
		return m;
	}
	
	@Override
	public Object getValue()
	{
		MultivariateGaussianMessage m = (MultivariateGaussianMessage)getBelief();
		return m.getMeans();
	}

	@Override
	public void updateEdge(int outPortNum)  
	{
		MultivariateGaussianMessage outMsg = _outputMsgs[outPortNum];
		doUpdate(outMsg,outPortNum);
	}

	private void doUpdate(MultivariateGaussianMessage outMsg,int outPortNum) 
	{
    	// If fixed value, just return the input, which has been set to a zero-variance message
		if (_var.hasFixedValue())
		{
			outMsg.set(_input);
			return;
		}
		
		double [] vector = _input.getInformationVector();		
		double [][] matrix = _input.getInformationMatrix();		
		
		//MultivariateMsg outMsg = (MultivariateMsg)_var.getPorts().get(outPortNum).getOutputMsg();
		
		for (int i = 0; i < _outputMsgs.length; i++ )
		{
			if (i != outPortNum)
			{				
				//_var.getInputObject();
				MultivariateGaussianMessage inMsg = _inputMsgs[i];
				
				double [] inMsgVector = inMsg.getInformationVector();
				
				for (int j = 0; j < vector.length; j++)
				{
					vector[j] += inMsgVector[j];
				}
				
				double [][] inMsgMatrix = inMsg.getInformationMatrix();
				
				for (int j = 0; j < inMsgMatrix.length; j++)
				{
					for (int k = 0; k < inMsgMatrix[j].length; k++)
					{
						matrix[j][k] += inMsgMatrix[j][k];
					}
				}
			}
		}
		
		outMsg.setInformation(vector,matrix);
	}

	@Override
	public Object [] createMessages(ISolverFactor factor) 
	{
		int portNum = getModelObject().getPortNum(factor.getModelObject());
		int arrayLength = Math.max(_inputMsgs.length, portNum+1);
		_inputMsgs = Arrays.copyOf(_inputMsgs, arrayLength);
		_inputMsgs[portNum] = (MultivariateGaussianMessage)createDefaultMessage();
		_outputMsgs = Arrays.copyOf(_outputMsgs,arrayLength);
		_outputMsgs[portNum] = (MultivariateGaussianMessage)createDefaultMessage();
		return new Object [] {_inputMsgs[portNum],_outputMsgs[portNum]};
	}

	public MultivariateGaussianMessage createDefaultMessage() 
	{
		MultivariateGaussianMessage mm = new MultivariateGaussianMessage();
		return (MultivariateGaussianMessage)resetInputMessage(mm);
	}

	@Override
	public Object resetInputMessage(Object message) 
	{
		double [] means = new double[_numVars];
		double [][] covariance = new double[_numVars][];
		
		
		for (int i = 0; i < covariance.length; i++)
		{
			covariance[i] = new double[_numVars];
			covariance[i][i] = Double.POSITIVE_INFINITY;
		}
		((MultivariateGaussianMessage)message).setMeanAndCovariance(means, covariance);
		return message;
	}

	@Override
	public void resetEdgeMessages( int i ) 
	{
		_inputMsgs[i] = (MultivariateGaussianMessage)resetInputMessage(_inputMsgs[i]);
		_outputMsgs[i] = (MultivariateGaussianMessage)resetOutputMessage(_outputMsgs[i]);		
	}
	
	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort) 
	{
		SRealJointVariable s = (SRealJointVariable)other;
	
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
		_inputMsgs[portIndex] = (MultivariateGaussianMessage)obj;
	}


	public MultivariateGaussianMessage createFixedValueMessage(double[] fixedValue)
	{
		double[][] covariance = new double[_numVars][_numVars];
		for (int i = 0; i < _numVars; i++)
			Arrays.fill(covariance[i], 0);
		MultivariateGaussianMessage message = new MultivariateGaussianMessage(fixedValue, covariance);
		return message;
	}

}
