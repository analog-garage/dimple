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
import java.util.HashMap;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.hybridSampledBP.HybridSampledBPDistributionGenerator;

public class DiscreteDistributionGenerator extends HybridSampledBPDistributionGenerator
{

	public DiscreteDistributionGenerator(Port p) 
	{
		super(p);
		// TODO Auto-generated constructor stub
	}

	private double [] _msg;
	private HashMap<Object,Integer> _domain2index;
	
	@Override
	public void initialize()  
	{
		// TODO Auto-generated method stub
		INode n = _p.getConnectedNode();
		
		if (!(n instanceof Discrete))
			throw new DimpleException("expected Discrete");
		
		Discrete d = (Discrete)n;
		Object [] domain = d.getDiscreteDomain().getElements();
		
		_domain2index = new HashMap<Object, Integer>();
		
		for (int i = 0; i < domain.length; i++)
		{
			_domain2index.put(domain[i],i);
		}
		
		_msg = (double[])_p.getOutputMsg();
	}

	@Override
	public void generateDistributionInPlace(ArrayList<Object> input) 
	{
	
		for (int i = 0; i < _msg.length; i++)
		{
			_msg[i] = 0;
		}
		
		for (int i = 0; i < input.size(); i++)
		{
			_msg[_domain2index.get(input)] = _msg[_domain2index.get(input)]+1;
		}
		
		//normalize
		for (int i = 0; i < _msg.length; i++ )
			_msg[i] /= input.size();
		
	}

}
