package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.ArrayList;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.solvers.core.SFactorBase;



public class FiniteFieldProjection extends SFactorBase
{

	private Port [] _bitPorts;
	private FiniteFieldVariable _ffVar;
	private Port _ffVarPort;
	private int [] _portIndex2bitIndex;	
	
	public FiniteFieldProjection(Factor factor)  
	{
		super(factor);
		
	
		VariableList variables = factor.getVariables();
		
		ArrayList<Port> ports = _factor.getPorts();
		
		//First variable is the FiniteFieldVariable
		//Other variables should be bits.
		//TODO: check this is valid
		_ffVar = (FiniteFieldVariable)variables.getByIndex(0).getSolver();
		_ffVarPort = ports.get(0);
		_bitPorts = new Port [_ffVar.getNumBits()];
		_portIndex2bitIndex = new int[ports.size()];
		
		for (int i = 0; i < ports.size(); i++)
			_portIndex2bitIndex[i] = -1;
		//_bitIndices = new int[_ffVar.getNumBits()]
		
		if (variables.size() <= 1)
			throw new DimpleException("need to specify at least one bit for projection");
		
				
		//get constant value and make sure it's in range
		FactorFunctionWithConstants ff = (FactorFunctionWithConstants)_factor.getFactorFunction();
		int [] constIndices = ff.getConstantIndices();
		Object [] constants = ff.getConstants();
		if (constIndices.length != 1)
			throw new DimpleException("expected one constant to specify the array of bit positions");

		double [] domain = (double[])constants[0];
		
		if (variables.size() != 1+domain.length)
			throw new DimpleException("expect finite field variable, bit positions, and bits");

		
		for (int i = 1; i < variables.size(); i++)
		{
			//TODO: error check
			int index = (int)domain[i-1];
			
			if (index < 0 || index >= _bitPorts.length)
				throw new DimpleException("index out of range");
			if (_bitPorts[index] != null)
				throw new DimpleException("Tried to set index twice");
			
			//get Variable and make sure it's a bit.
			Discrete bit = (Discrete)variables.getByIndex(i);
			
			Object [] bitDomain = (Object[])bit.getDiscreteDomain().getElements();
			if (bitDomain.length != 2 || (Double)bitDomain[0] != 0 || (Double)bitDomain[1] != 1)
				throw new DimpleException("expected bit");
			
			_bitPorts[index] = ports.get(i);
			_portIndex2bitIndex[i] = index;
		}
	}

	public void updateEdge(int outPortNum) 
	{
		// TODO Auto-generated method stub
		if (outPortNum == 0)
			updateFiniteField();
		else
		{
			if (outPortNum >= 1)
				updateBit(outPortNum);
		}
		
	}
	
	public void updateFiniteField()
	{
		//for every value of the finite field
		//TODO: cast shouldn't be necessary
		double [] outputs = (double[])_ffVarPort.getOutputMsg();
		int numBits = _ffVar.getNumBits();
		Port p;
		double prod;
		double [][] inputMsgs = new double[numBits][];
		for (int i = 0; i < _bitPorts.length; i++)
		{
			if (_bitPorts[i] != null)
				inputMsgs[i] = (double[])_bitPorts[i].getInputMsg();
		}
		
		//Multiply bit probabilities
		double sum = 0;
		//TODO: is there something inside the solver object that knows the domain length?
		for (int i = 0; i < ((Discrete)_ffVar.getVariable()).getDiscreteDomain().getElements().length; i++)
		{
			prod = 1;
			for (int j = 0; j < numBits; j++)
			{
				p = _bitPorts[j];
				
				if (p != null)
				{
					if (((i >> j) & 1) == 1)
					{
						//is one
						//p.getInputMsg();
						prod *= inputMsgs[j][1];
					}
					else
					{
						prod *= inputMsgs[j][0];
					}
				}
				
			}
			outputs[i] = prod;
			sum += prod;
		}
		
		//normalize
		for (int i = 0; i < outputs.length; i++)
			outputs[i] /= sum;

	}
	
	public void updateBit(int portNum)
	{	
		
		//get output msg for bit
		double [] outputs = (double[])_factor.getPorts().get(portNum).getOutputMsg();
		
		//init to 1 for each
		outputs[0] = 0;
		outputs[1] = 0;
				
		int bit = _portIndex2bitIndex[portNum];
		
		//Iterate each value of finite field
		double [] inputs = (double[])_factor.getPorts().get(0).getInputMsg();
		
		for (int i = 0; i < inputs.length; i++)
		{
			//extract value of bit of interest
			if (((i >> bit) & 1) == 1)
			{
				//bit was one
				outputs[1] += inputs[i];
			}
			else
			{
				//bit was zero
				outputs[0] += inputs[i];
			}
			
			
		}
		
		//normalize
		double sum = outputs[0]+outputs[1];
		outputs[0] /= sum;
		outputs[1] /= sum;
		
		
	}


	@Override
	public void initialize() 
	{
		
	}

	@Override
	public double getEnergy()  
	{
		throw new DimpleException("getEnergy not yet supported for FiniteFieldProjection");
	}

}
