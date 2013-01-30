package com.analog.lyric.dimple.solvers.minsum;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.dimple.solvers.sumproduct.SVariable;

public class CustomXor extends SFactorBase
{
    protected double[][] _inPortMsgs = null;
    protected double[][] _outPortMsgs = null;
    protected double [] _savedOutMsgsLLR;
    protected double [] _dampingParams;
	private boolean _dampingInUse = false;
	private int _numPorts;


	public CustomXor(Factor factor)
	{
		super(factor);
	}

	
	@Override
	public void updateEdge(int outPortNum)
	{
	    if (_dampingInUse)
	    {
	    	if (_dampingParams[outPortNum] != 0)
	    	{
	    		double[] outputMsgs = _outPortMsgs[outPortNum];
	    		_savedOutMsgsLLR[outPortNum] = outputMsgs[1];		// LLR value is only in the 1 entry
	    	}
	    }

		int hardXor = 1;
		double min = Double.POSITIVE_INFINITY;
		for (int inPortIndex = 0; inPortIndex < _numPorts; inPortIndex++)
		{
			if (inPortIndex != outPortNum)
			{
				double[] inMsg = _inPortMsgs[inPortIndex];
				double in = inMsg[1] - inMsg[0];			// Get the input LLR value
				if (in < 0)
				{
					hardXor = -hardXor;						// XOR of the sign of the input
					in = -in;								// Absolute value of the input
				}
				if (in < min)								// Find the minimum and second minimum
				{
					min = in;
				}
			}
		}
		
		double[] outMsg = _outPortMsgs[outPortNum];
		outMsg[1] = min * hardXor;
		outMsg[0] = 0;
	    
	   
	    // Damping
		if (_dampingInUse)
		{
			double damping = _dampingParams[outPortNum];	    	
			if (damping != 0)
			{
				double[] outputMsgs = _outPortMsgs[outPortNum];
				outputMsgs[1] = (1-damping)*outputMsgs[1] + damping*_savedOutMsgsLLR[outPortNum];
			}
		}

	}

	
	@Override
	public void update()
	{
	    if (_dampingInUse)
	    {
	    	for (int port = 0; port < _numPorts; port++)
	    	{
	    		if (_dampingParams[port] != 0)
	    		{
	    			double[] outputMsgs = _outPortMsgs[port];
	    			_savedOutMsgsLLR[port] = outputMsgs[1];		// LLR value is only in the 1 entry
	    		}
	    	}
	    }

		int hardXor = 1;
		double min = Double.POSITIVE_INFINITY;
		double secMin = Double.POSITIVE_INFINITY;
		int minIndex = -1;
		for (int inPortIndex = 0; inPortIndex < _numPorts; inPortIndex++)
		{
			double[] inMsg = _inPortMsgs[inPortIndex];
			double in = inMsg[1] - inMsg[0];			// Get the input LLR value
			if (in < 0)
			{
				hardXor = -hardXor;						// XOR of the sign of the input
				in = -in;								// Absolute value of the input
			}
			if (in < min)								// Find the minimum and second minimum
			{
				secMin = min;
				min = in;
				minIndex = inPortIndex;
			}
			else if (in < secMin)
				secMin = in;
		}
		
		for (int outPortIndex = 0; outPortIndex < _numPorts; outPortIndex++)
		{
			double[] outMsg = _outPortMsgs[outPortIndex];
			double[] inMsg = _inPortMsgs[outPortIndex];
			double in = inMsg[1] - inMsg[0];				// Get the input LLR value
			double out;
			if (in < 0)
				out = ((outPortIndex == minIndex) ? secMin : min) * (-hardXor);
			else
				out = ((outPortIndex == minIndex) ? secMin : min) * hardXor;
			outMsg[1] = out;
			outMsg[0] = 0;
		}
	    
	   
	    // Damping
		if (_dampingInUse)
		{
			for (int port = 0; port < _numPorts; port++)
			{
				double damping = _dampingParams[port];	    	
				if (damping != 0)
				{
					double[] outputMsgs = _outPortMsgs[port];
					outputMsgs[1] = (1-damping)*outputMsgs[1] + damping*_savedOutMsgsLLR[port];
				}
			}
		}

	}
	
	
    
 
	
	public void setDamping(int index, double val)
	{
		_dampingParams[index] = val;
		
		if (val != 0)
			_dampingInUse = true;
	}
	
	public double getDamping(int index)
	{
		return _dampingParams[index];
	}


	@Override
	public void initialize() 
	{
		for (int i = 0; i < _inPortMsgs.length; i++)
		{
			SVariable sv = (SVariable)_factor.getSiblings().get(i).getSolver();
			_inPortMsgs[i] = (double[])sv.resetMessage(_inPortMsgs[i]);
		}

	}


	@Override
	protected void createMessages() 
	{
		int numPorts = _factor.getSiblings().size();
		
	    _inPortMsgs = new double[numPorts][];
	    
	    for (int port = 0; port < numPorts; port++) 
	    	_inPortMsgs[port] = (double[])((ISolverVariable)(_factor.getSiblings().get(port).getSolver())).createDefaultMessage();
	    
	    _outPortMsgs = new double[numPorts][];
	    if (_dampingInUse)
	    	_savedOutMsgsLLR = new double[numPorts];
	    
	}


	@Override
	protected void connectToVariables() 
	{
		// TODO Auto-generated method stub
		//messages were created in constructor
		int index = 0;
		for (VariableBase vb : _factor.getVariables())
		{
			ISolverVariable sv = vb.getSolver();
			_outPortMsgs[index] = (double[]) sv.createMessages(this, _inPortMsgs[index]);
			index++;
		}	
	}

//	   protected void updateMessageCache()
//	    {
//	    	_numPorts = _factor.getPorts().size();
//		    _inPortMsgs = new double[_numPorts][];
//		    _outPortMsgs = new double[_numPorts][];
//		    if (_dampingInUse)
//		    	_savedOutMsgsLLR = new double[_numPorts];
//		    
//		    for (int port = 0; port < _numPorts; port++)
//		    {
//		    	_inPortMsgs[port] = (double[])_factor.getPorts().get(port).getInputMsg();
//		    	_outPortMsgs[port] = (double[])_factor.getPorts().get(port).getOutputMsg();
//		    }
//	    }
//
//		



}
