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
	public double getSomethingEnergy()  
	{
		throw new DimpleException("getEnergy not yet supported for GaussianAdd");
	}


}
