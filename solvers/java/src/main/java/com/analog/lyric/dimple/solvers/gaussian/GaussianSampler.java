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

import java.util.Random;

import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.hybridSampledBP.HybridSampledBPSampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class GaussianSampler extends HybridSampledBPSampler 
{

	public GaussianSampler(Port p, Random random) 
	{
		super(p, random);
	}

	private double [] _msg;
	

	@Override
	public Object generateSample() 
	{
		return _random.nextGaussian()*_msg[1]+_msg[0];
	}

	@Override
	public void initialize() 
	{
		SVariable var = (SVariable)_p.node.getSiblings().get(_p.index).getSolver();
	}

	@Override
	public void createMessage() 
	{
		ISolverVariable var = (ISolverVariable)_p.node.getSiblings().get(_p.index).getSolver();
		_msg = (double[])var.createDefaultMessage();
	}


	@Override
	public Object getInputMsg() 
	{
		return _msg;
	}
	
}
