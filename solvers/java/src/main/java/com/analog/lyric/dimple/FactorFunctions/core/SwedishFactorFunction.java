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

package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.Random;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.swedish.SwedishDistributionGenerator;
import com.analog.lyric.dimple.solvers.core.swedish.SwedishSampler;

public abstract class SwedishFactorFunction extends FactorFunction
{

	protected Random _random;
	
	public SwedishFactorFunction(String name) 
	{
		super(name);
	}
	
	public void attachRandom(Random random)
	{
		_random = random;
	}

	@Override
	public double eval(Object... input) 
	{
		throw new DimpleException("not implemented");
	}

	public abstract double acceptanceRatio(int outPortIndex, Object ... inputs);
	public abstract Object generateSample(int outPortIndex, Object ... inputs);
	public abstract SwedishSampler generateSampler(Port p);
	public abstract SwedishDistributionGenerator generateDistributionGenerator(Port p);
	
	public String runSwedishFish()
	{
		return "mmmmm";
	}
}
