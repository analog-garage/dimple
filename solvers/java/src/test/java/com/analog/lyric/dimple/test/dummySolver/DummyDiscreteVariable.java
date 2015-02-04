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

package com.analog.lyric.dimple.test.dummySolver;

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;


public class DummyDiscreteVariable extends SDiscreteVariableBase
{
	private @Nullable double [] _input = new double[1];
	protected Discrete _varDiscrete;

	public DummyDiscreteVariable(Discrete var)
	{
		super(var);
		_varDiscrete = var;
		initializeInputs();
	}

	public void initializeInputs()
	{
		_input = (double[])getDefaultMessage(null);
	}
	
	public Variable getVariable()
	{
		return _model;
	}

	public @Nullable Object getDefaultMessage(@Nullable Port port)
	{
		final double[] input = _input;
		if (input != null)
		{
			double[] msg = new double[input.length];
			java.util.Arrays.fill(msg, java.lang.Math.PI);
	
			return msg;
		}
		else
			return null;
	}


	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue)
	{
		if (input == null)
		{
			_input = null;
		}
		else
		{
			double [] vals = (double[])input;
	
			int len = _varDiscrete.getDiscreteDomain().size();
			
			if (vals.length != len)
				throw new DimpleException("length of priors does not match domain");
	
			_input = vals;
		}

	}


	@Override
	protected void doUpdateEdge(int outPortNum)
	{
	}


	@Override
	protected void doUpdate()
	{
	}

	@Override
	public double[] getBelief()
	{
		return requireNonNull(_input);
	}

	@Override
	public @Nullable Object resetInputMessage(Object message)
	{
		return null;
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
	public void setInputMsg(int portIndex, Object obj) {
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
	}

}
