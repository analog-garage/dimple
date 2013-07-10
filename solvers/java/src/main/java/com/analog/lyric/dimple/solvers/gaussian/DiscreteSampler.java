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

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.hybridSampledBP.HybridSampledBPSampler;

public class DiscreteSampler extends HybridSampledBPSampler
{

	public DiscreteSampler(Port p, Random random)
	{
		super(p, random);
		// TODO Auto-generated constructor stub
		
		INode n = _p.node.getSiblings().get(_p.index);
		
		if (! (n instanceof Discrete))
			throw new DimpleException("expected Discrete");
		
		_domain = ((Discrete)n).getDomain();
	}

	private double [] _msg;
	private final DiscreteDomain _domain;
	


	@Override
	public Object generateSample()
	{
		//normalize
		double sum = 0;
		for (int i = 0; i < _msg.length; i++)
			sum += _msg[i];
		
		double d = _random.nextDouble();
		
		double cum = 0;
		
		for (int i = 0; i < _msg.length; i++)
		{
			cum += _msg[i]/sum;
			
			if (d < cum)
			{
				return _domain.getElement(i);
			}
		}
		
		
		return _domain.getElement(_domain.size()-1);
	}
	
	@Override
	public void initialize()
	{
		SVariable var = (SVariable)_p.node.getSiblings().get(_p.index).getSolver();
		_msg = (double[])var.resetInputMessage(_msg);
		
	}

	@Override
	public void createMessage(Object msg)
	{
		_msg = (double[])msg;
	}

	@Override
	public Object getInputMsg()
	{
		return _msg;
	}

	@Override
	public void moveMessages(HybridSampledBPSampler other)
	{
		_msg = ((DiscreteSampler)other)._msg;
	}

}
