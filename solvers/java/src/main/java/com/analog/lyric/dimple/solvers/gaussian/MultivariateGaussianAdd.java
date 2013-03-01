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

package com.analog.lyric.dimple.solvers.gaussian;

import com.analog.lyric.dimple.model.Factor;

public class MultivariateGaussianAdd extends MultivariateFactorBase
{

	public MultivariateGaussianAdd(Factor factor) 
	{
		super(factor);
	}

	public void updateEdge(int outPortNum)  
	{
		MultivariateMsg outMsg = _outputMsgs[outPortNum];
		
		//double [] vector = _input.getInformationVector();
		//double [][] matrix = _input.getInformationMatrix();
		
		//MultivariateMsg outMsg = (MultivariateMsg)_var.getPorts().get(outPortNum).getOutputMsg();
		
		int size = outMsg.getMeans().length;
		
		double [] vector = new double[size];
		double [][] matrix = new double[size][];
		for (int i = 0; i < matrix.length; i++)
			matrix[i] = new double[size];
		
		for (int i = 0; i < _factor.getSiblings().size(); i++ )
		{
			if (i != outPortNum)
			{				
				//_var.getInputObject();
				MultivariateMsg inMsg = _inputMsgs[i];
				
				double [] inMsgVector = inMsg.getMeans();
				
				for (int j = 0; j < vector.length; j++)
				{
					if (outPortNum != 0 && i != 0)
						vector[j] -= inMsgVector[j];
					else
						vector[j] += inMsgVector[j];
				}
				
				double [][] inMsgMatrix = inMsg.getCovariance();
				
				for (int j = 0; j < inMsgMatrix.length; j++)
				{
					for (int k = 0; k < inMsgMatrix[j].length; k++)
					{
						matrix[j][k] += inMsgMatrix[j][k];
					}
				}
			}
		}
		
		outMsg.setMeanAndCovariance(vector,matrix);	
	}

}
