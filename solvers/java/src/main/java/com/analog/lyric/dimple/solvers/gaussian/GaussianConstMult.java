package com.analog.lyric.dimple.solvers.gaussian;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorBase;



public class GaussianConstMult extends SFactorBase
{

	private double _constant;
	private int _varIndex;
	
	public GaussianConstMult(com.analog.lyric.dimple.model.Factor factor) 
	{
		super(factor);
		
		//Make sure this is of the form a = b*c where either b or c is a constant.
		if (factor.getPorts().size() != 2)
			throw new DimpleException("factor must be of form a = b*c where b or c is a constant");
		
		FactorFunctionWithConstants ff = (FactorFunctionWithConstants)factor.getFactorFunction();
		if (ff.getConstants().length != 1)
			throw new DimpleException("expected one constant");
		double constant = (Double)ff.getConstants()[0];
		
		
		VariableBase a = (VariableBase)factor.getPorts().get(0).getConnectedNode();
		VariableBase b = (VariableBase)factor.getPorts().get(1).getConnectedNode();
		
		if (a.getDomain().isDiscrete() || b.getDomain().isDiscrete())
			throw new DimpleException("Variables must be reals");
		
		_varIndex = 1;
		
		_constant = constant;
		
		if (_constant == 0)
			throw new DimpleException("constant of 0 not supporetd");
		
	}	

	public void updateEdge(int outPortNum) 
	{
		if (outPortNum == 0)
		{
			updateProduct();
		}
		else if (outPortNum == _varIndex)
		{
			updateVariable();
		}
	}


	public void updateProduct()
	{
		double [] outMsg = (double[])_factor.getPorts().get(0).getOutputMsg();
		double [] inMsg = (double[])_factor.getPorts().get(_varIndex).getInputMsg();
		
		//Up = C*Uv
		outMsg[0] = inMsg[0]*_constant;
		
		//SigmaP = C*sigmav
		outMsg[1] = inMsg[1]*_constant;
		
	}

	public void updateVariable()
	{
		double [] outMsg = (double[])_factor.getPorts().get(_varIndex).getOutputMsg();
		double [] inMsg = (double[])_factor.getPorts().get(0).getInputMsg();
		
		//Uv = Up/C
		outMsg[0] = inMsg[0]/_constant;
		
		//Sigmav = sigmaP/c
		outMsg[1] = inMsg[1]/Math.abs(_constant);

	}

}
