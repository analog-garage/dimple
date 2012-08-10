package com.analog.lyric.dimple.solvers.gaussian;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorBase;

public class MultivariateGaussianConstMult extends SFactorBase 
{
	private double [][] _constant;
	
	public MultivariateGaussianConstMult(Factor factor)  
	{
		super(factor);
		
		//Make sure this is of the form a = b*c where either b or c is a constant.
		if (factor.getPorts().size() != 2)
			throw new DimpleException("factor must be of form a = b*c where b is a constant matrix");
		
		//TODO: alternatively, one of the ports could be a discrete variable with a single domain
		//      element
		
		FactorFunctionWithConstants ff = (FactorFunctionWithConstants)factor.getFactorFunction();
		if (ff.getConstants().length != 1)
			throw new DimpleException("expected one constant");
		
		double [][] constant; 
		if (ff.getConstants()[0] instanceof double [])
		{
			constant = new double[][] {(double[])ff.getConstants()[0]};
		}
		else
		{
			constant = (double[][])ff.getConstants()[0];
		}
		
		if (ff.getConstantIndices()[0] != 1)
			throw new DimpleException("Expect matrix to be second arg");
		
		
		VariableBase a = (VariableBase)factor.getPorts().get(0).getConnectedNode();
		VariableBase b = (VariableBase)factor.getPorts().get(1).getConnectedNode();
		
		if (a.getDomain().isDiscrete() || b.getDomain().isDiscrete())
			throw new DimpleException("Variables must be reals");
		
		_constant = constant;
		
	}

	@Override
	public void updateEdge(int outPortNum)  
	{
		multiGaBPMatrixMult matMult = new multiGaBPMatrixMult(_constant);
		
		char direction;
		if (outPortNum == 0)
			direction = 'F';
		else
			direction = 'R';
		
		MultivariateMsg outMsg = (MultivariateMsg)_factor.getPorts().get(outPortNum).getOutputMsg();
		MultivariateMsg inMsg = (MultivariateMsg)_factor.getPorts().get(1-outPortNum).getInputMsg();
		
		matMult.ComputeMsg(inMsg, outMsg, direction);
	}

}
