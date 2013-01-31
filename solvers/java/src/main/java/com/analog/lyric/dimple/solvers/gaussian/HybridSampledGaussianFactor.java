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

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.hybridSampledBP.HybridSampledBPDistributionGenerator;
import com.analog.lyric.dimple.solvers.core.hybridSampledBP.HybridSampledBPFactor;
import com.analog.lyric.dimple.solvers.core.hybridSampledBP.HybridSampledBPSampler;

public class HybridSampledGaussianFactor extends HybridSampledBPFactor
{

	public HybridSampledGaussianFactor(Factor factor, Random random)
	{
		super(factor, random);
	}

	@Override
	public HybridSampledBPSampler generateSampler(Port p) 
	{
		boolean isDiscretePort = ((VariableBase)p.node.getSiblings().get(p.index)).getDomain().isDiscrete();
		
		if (isDiscretePort)
			return new DiscreteSampler(p, _random);
		else
			return new GaussianSampler(p, _random);
		
	}

	@Override
	public HybridSampledBPDistributionGenerator generateDistributionGenerator(Port p) 
	{
		boolean isDiscretePort = ((VariableBase)p.node.getSiblings().get(p.index)).getDomain().isDiscrete();
		
		if (isDiscretePort)
			return new DiscreteDistributionGenerator(p);
		else
			return new GaussianDistributionGenerator(p);
	}



}
