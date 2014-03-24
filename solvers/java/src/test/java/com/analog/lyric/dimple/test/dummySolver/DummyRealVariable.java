/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.dummySolver;

import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.solvers.core.SRealVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class DummyRealVariable extends SRealVariableBase
{

	public DummyRealVariable(Real var)
	{
		super(var);
	}

	@Override
	public void setInputOrFixedValue(Object input, Object fixedValue, boolean hasFixedValue)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object getBelief()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] createMessages(ISolverFactor factor)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object resetInputMessage(Object message)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateEdge(int outPortNum)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void resetEdgeMessages(int portNum)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object getInputMsg(int portIndex)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		// TODO Auto-generated method stub

	}

}
