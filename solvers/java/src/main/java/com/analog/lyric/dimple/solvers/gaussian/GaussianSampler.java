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

import java.util.Random;

import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.swedish.SwedishSampler;

public class GaussianSampler extends SwedishSampler 
{

	public GaussianSampler(Port p, Random random) 
	{
		super(p, random);
		// TODO Auto-generated constructor stub
	}

	private double [] _msg;
	
	@Override
	public void initialize() 
	{
		_msg = (double[])_p.getInputMsg();
	}

	@Override
	public Object generateSample() 
	{
		return _random.nextGaussian()*_msg[1]+_msg[0];
	}
	
}
