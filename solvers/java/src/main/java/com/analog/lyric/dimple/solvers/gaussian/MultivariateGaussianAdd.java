package com.analog.lyric.dimple.solvers.gaussian;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorBase;

public class MultivariateGaussianAdd extends SFactorBase
{

	public MultivariateGaussianAdd(Factor factor) 
	{
		super(factor);
	}

	public void updateEdge(int outPortNum)  
	{
		MultivariateMsg outMsg = (MultivariateMsg)_factor.getPorts().get(outPortNum).getOutputMsg();
		
		// TODO Auto-generated method stub
		//double [] vector = _input.getInformationVector();
		//double [][] matrix = _input.getInformationMatrix();
		
		//MultivariateMsg outMsg = (MultivariateMsg)_var.getPorts().get(outPortNum).getOutputMsg();
		
		int size = outMsg.getMeans().length;
		
		double [] vector = new double[size];
		double [][] matrix = new double[size][];
		for (int i = 0; i < matrix.length; i++)
			matrix[i] = new double[size];
		
		for (int i = 0; i < _factor.getPorts().size(); i++ )
		{
			if (i != outPortNum)
			{				
				//_var.getInputObject();
				MultivariateMsg inMsg = (MultivariateMsg)_factor.getPorts().get(i).getInputMsg();
				
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
