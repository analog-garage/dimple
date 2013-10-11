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
