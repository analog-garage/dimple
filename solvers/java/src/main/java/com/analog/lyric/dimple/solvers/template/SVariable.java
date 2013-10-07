package com.analog.lyric.dimple.solvers.template;

import java.util.Arrays;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class SVariable extends SDiscreteVariableBase
{
	private double [] _input;
	private double [][] _inputMessages = new double[0][];
	private double [][] _outputMessages = new double[0][];
	
	public SVariable(VariableBase var) 
	{
		super(var);
	}

	@Override
	public void setInputOrFixedValue(Object input, Object fixedValue,
			boolean hasFixedValue) 
	{
		if (hasFixedValue)
			throw new DimpleException("Not supported");
		
		if (_input == null)
			_input = new double[_var.asDiscreteVariable().getDiscreteDomain().size()];
		else
			_input = (double[])input;
	}

	@Override
	public Object[] createMessages(ISolverFactor factor) 
	{
		int portNum = _var.getPortNum(factor.getModelObject());
		int newArraySize = Math.max(_inputMessages.length,portNum + 1);
		_inputMessages = Arrays.copyOf(_inputMessages,newArraySize);
		_outputMessages = Arrays.copyOf(_outputMessages,newArraySize);
		int domainLength = _var.asDiscreteVariable().getDiscreteDomain().size();
		_inputMessages[portNum] = new double[domainLength];
		_outputMessages[portNum] = new double[domainLength];
		
		return new Object [] {_inputMessages[portNum],_outputMessages[portNum]};
	}

	@Override
	public Object resetInputMessage(Object message) 
	{
		double [] retval = (double[])_input;
		for (int i = 0; i < retval.length; i++)
			retval[i] = 0;
		return retval;
	}

	@Override
	public void updateEdge(int outPortNum) 
	{
		double [] output = _outputMessages[outPortNum];
		
		for (int i = 0; i < output.length; i++)
		{
			output[i] = _input[i];
		}
		
		for (int i = 0; i < _inputMessages.length; i++)
		{
			if (i != outPortNum)
			{
				for (int j = 0; j < output.length; j++)
				{
					output[j] += _inputMessages[i][j];
				}
			}
		}
	}

	@Override
	public void resetEdgeMessages(int portNum) 
	{
		double [] tmp = (double[])_inputMessages[portNum];
		
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = 0;
		
		tmp = (double[])_outputMessages[portNum];

		for (int i = 0; i < tmp.length; i++)
			tmp[i] = 0;
		
	}

	@Override
	public Object getInputMsg(int portIndex) 
	{
		return _inputMessages[portIndex];
	}

	@Override
	public Object getOutputMsg(int portIndex) 
	{
		return _outputMessages[portIndex];
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum) 
	{
		throw new DimpleException("rolled up graphs not supported");
	}

	@Override
	public double[] getBelief() 
	{
		double [] output = new double[_input.length];
		
		for (int i = 0; i < output.length; i++)
		{
			output[i] = _input[i];
		}
		
		for (int i = 0; i < _inputMessages.length; i++)
		{
			for (int j = 0; j < output.length; j++)
			{
				output[j] += _inputMessages[i][j];
			}
		}
		return output;		
	}

}
