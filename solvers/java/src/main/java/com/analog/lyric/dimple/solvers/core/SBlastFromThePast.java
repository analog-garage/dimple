/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class SBlastFromThePast extends SFactorBase
{

	Object _msg;
	
	public SBlastFromThePast(Factor factor) {
		super(factor);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createMessages() 
	{
		Object [] messages = getFactor().getVariables().getByIndex(0).getSolver().createMessages(this);
		_msg = messages[0];
	}

	@Override
	public void updateEdge(int outPortNum) 
	{
	}

	@Override
	public void initialize(int portNum) 
	{
		
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum) 
	{
		//TODO: implement this and replace blastFromThePast implementation
	}
	
	@Override
	public Object getInputMsg(int portIndex) 
	{
		throw new DimpleException("Not supported by " + this);
	}

	@Override
	public Object getOutputMsg(int portIndex) 
	{
		return _msg;
	}


}
