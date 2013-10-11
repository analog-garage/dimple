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

package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.nestedgraphs.MultiplexerCPD;

public class PMultiplexerCPD extends PFactorGraphVector 
{
	private MultiplexerCPD _multiplexor;
	
	public PMultiplexerCPD(Object [][] domains) 
	{
		super(new MultiplexerCPD(domains,true,true));
				
		_multiplexor = (MultiplexerCPD)getGraph();
	}
	
	public PMultiplexerCPD(Object [] domain, int numZs) 
	{
		super(new MultiplexerCPD(domain,numZs,true,true));
				
		_multiplexor = (MultiplexerCPD)getGraph();
	}

	public PDiscreteVariableVector getY()
	{
		return new PDiscreteVariableVector(_multiplexor.getY());
	}
	
	public PDiscreteVariableVector getA()
	{
		return new PDiscreteVariableVector(_multiplexor.getA());
	}
	
	public PDiscreteVariableVector getZA()
	{
		return new PDiscreteVariableVector(_multiplexor.getZA());
	}
	
	public PDiscreteVariableVector [] getZs()
	{
		Discrete [] zs = _multiplexor.getZs();
		PDiscreteVariableVector [] retval = new PDiscreteVariableVector[zs.length];
		for (int i = 0; i < zs.length; i++)
			retval[i] = new PDiscreteVariableVector(zs[i]);
		
		return retval;
	}
	
}
