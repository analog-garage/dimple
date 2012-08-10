package com.analog.lyric.dimple.solvers.gaussian;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorBase;


public class GaussianAdd extends SFactorBase
{
	
	public GaussianAdd(com.analog.lyric.dimple.model.Factor factor) 
	{
		super(factor);
		
		for (int i = 0; i < factor.getPorts().size(); i++)
		{
			VariableBase v = (VariableBase)factor.getPorts().get(i).getConnectedNode();
			
			if (v.getDomain().isDiscrete())
				throw new DimpleException("cannot connect discrete variable to the Gaussian add factor");
		}

	}	

	public void updateEdge(int outPortNum) 
	{
		//TODO: express this as different functions if doing input or output
		ArrayList<Port> ports = _factor.getPorts();
		
		double mu = 0;
		double sigmaSquared = 0;
		
		for (int i = 0; i < ports.size(); i++)
		{
			if (i != outPortNum)
			{
				double [] msg = (double[])ports.get(i).getInputMsg();
				if (outPortNum == 0)
				{
					mu += msg[0];
				}
				else
				{
					if (i == 0)
						mu += msg[0];
					else
						mu -= msg[0];
				}
				
				sigmaSquared += msg[1]*msg[1];
			}
		}

		double [] outMsg = (double[])ports.get(outPortNum).getOutputMsg();
		outMsg[0] = mu;
		outMsg[1] = Math.sqrt(sigmaSquared);
		
		//uout = ua + ub + uc
		//ub = uout-ua-uc
		//sigma^2 = othersigma^2 + theothersigma^2 ...
	}

	//MAGIC!!!!
	public void updateOutput()
	{
		
	}
	
	//ALSO MAGIC!!!!
	public void updateInput(int portNum)
	{
		
	}


	@Override
	public void initialize() 
	{
		
	}

	@Override
	public double getEnergy()  
	{
		throw new DimpleException("getEnergy not yet supported for GaussianAdd");
	}


}
