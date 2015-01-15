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

import org.eclipse.jdt.annotation.Nullable;

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
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue)
	{
	}

	@Override
	public @Nullable Object getBelief()
	{
		return null;
	}

	@Override
	public @Nullable Object[] createMessages(ISolverFactor factor)
	{
		return null;
	}

	@Override
	public @Nullable Object resetInputMessage(Object message)
	{
		return null;
	}

	@Override
	protected void doUpdateEdge(int outPortNum)
	{
	}

	@Override
	public void resetEdgeMessages(int portNum)
	{
	}

	@Override
	public @Nullable Object getInputMsg(int portIndex)
	{
		return null;
	}

	@Override
	public @Nullable Object getOutputMsg(int portIndex)
	{
		return null;
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
	}

}
