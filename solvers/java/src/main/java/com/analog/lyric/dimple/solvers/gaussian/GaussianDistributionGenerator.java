/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.swedish.SwedishDistributionGenerator;

public class GaussianDistributionGenerator extends SwedishDistributionGenerator 
{

	public GaussianDistributionGenerator(Port p) 
	{
		super(p);
		// TODO Auto-generated constructor stub
	}

	private double [] _msg;
	
	@Override
	public void initialize() 
	{
		// TODO Auto-generated method stub
		_msg = (double[])_p.getOutputMsg();
	}

	@Override
	public void generateDistributionInPlace(ArrayList<Object> input) 
	{
		// TODO Auto-generated method stub
		double mean = 0;
		
		for (int i = 0; i < input.size(); i++)
		{
			double tmp = (Double)input.get(i);
			
			if (Math.abs(tmp) == Double.POSITIVE_INFINITY)
			{
				_msg[0] = 0;
				_msg[1] = Double.POSITIVE_INFINITY;
				
				return;
			}
			
			mean += tmp;
		}
		
		mean = mean/input.size();
		
		double sigmasquared = 0;
		
		for (int i = 0; i < input.size(); i++)
		{
			double tmp = ((Double)input.get(i) - mean);
			sigmasquared += tmp*tmp;
		}
		
		sigmasquared /= (input.size()-1);
		
		_msg[0] = mean;
		_msg[1] = Math.sqrt(sigmasquared);
	}


}
