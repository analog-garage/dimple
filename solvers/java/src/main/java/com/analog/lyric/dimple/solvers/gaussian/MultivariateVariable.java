package com.analog.lyric.dimple.solvers.gaussian;

import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.RealJointDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SVariableBase;

public class MultivariateVariable extends SVariableBase 
{

	private int _numVars;
	private MultivariateMsg _input;
	
	public MultivariateVariable(VariableBase var) 
	{
		super(var);
		
		_numVars = ((RealJointDomain)_var.getDomain()).getNumVars();
		_input = (MultivariateMsg) getDefaultMessage(null);
	}

	@Override
	public void setInput(Object input)  
	{
		_input = (MultivariateMsg)input;
	}

	@Override
	public Object getDefaultMessage(Port port) 
	{
		
		double [] means = new double[_numVars];
		double [][] covariance = new double[_numVars][];
		
		
		for (int i = 0; i < covariance.length; i++)
		{
			covariance[i] = new double[_numVars];
			covariance[i][i] = Double.POSITIVE_INFINITY;
		}

		//multiGaBPMatrixMult.printMatrix(covariance);
		
		MultivariateMsg mm = new MultivariateMsg(means,covariance);
		
		return mm;
	}

	@Override
	public Object getBelief()  
	{
		// TODO Auto-generated method stub
		MultivariateMsg m = new MultivariateMsg(_input.getMeans(), _input.getCovariance());		
		doUpdate(m,-1);
		return m;
	}

	@Override
	public void updateEdge(int outPortNum)  
	{
		MultivariateMsg outMsg = (MultivariateMsg)_var.getPorts().get(outPortNum).getOutputMsg();
		doUpdate(outMsg,outPortNum);
	}

	private void doUpdate(MultivariateMsg outMsg,int outPortNum) 
	{
		// TODO Auto-generated method stub
		
		double [] vector = _input.getInformationVector();		
		double [][] matrix = _input.getInformationMatrix();		
		
		//MultivariateMsg outMsg = (MultivariateMsg)_var.getPorts().get(outPortNum).getOutputMsg();
		
		for (int i = 0; i < _var.getPorts().size(); i++ )
		{
			if (i != outPortNum)
			{				
				//_var.getInputObject();
				MultivariateMsg inMsg = (MultivariateMsg)_var.getPorts().get(i).getInputMsg();
				
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
}
