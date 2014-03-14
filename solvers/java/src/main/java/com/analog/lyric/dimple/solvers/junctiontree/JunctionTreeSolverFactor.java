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

package com.analog.lyric.dimple.solvers.junctiontree;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

/**
 * @since 0.05
 * @author Christopher Barber
 *
 */
public class JunctionTreeSolverFactor extends SFactorBase
{
	/*-------
	 * State
	 */
	
	private final JunctionTreeSolverGraphBase<?> _root;
	
//	private ISolverFactor _delegate;
//	private JointDomainReindexer _reindexer;
	
	/*--------------
	 * Construction
	 */
	
	JunctionTreeSolverFactor(Factor modelFactor, JunctionTreeSolverGraphBase<?> root)
	{
		super(modelFactor);
		_root = root;
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public double getBetheEntropy()
	{
		double sum = 0;
		
		final double [] beliefs = getBelief();
		for (double belief : beliefs)
		{
			sum -= belief * Math.log(belief);
		}
		
		return sum;
	}
	
	@Override
	public Object getInputMsg(int portIndex)
	{
		throw unsupported("getInputMsg");
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		throw unsupported("getOutputMsg");
	}

	@Override
	public JunctionTreeSolverGraphBase<?> getRootGraph()
	{
		return _root;
	}
	
	@Override
	public double getScore()
	{
		throw unsupported("getScore");
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		throw unsupported("moveMessages");
	}

	@Override
	public void resetEdgeMessages(int portNum)
	{
		throw unsupported("resetEdgeMessages");
	}

	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		throw unsupported("setInputMsg");
	}

	@Override
	public void setOutputMsg(int portIndex, Object obj)
	{
		throw unsupported("setOutputMsg");
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		throw unsupported("setInputMsgValues");
	}

	@Override
	public void setOutputMsgValues(int portIndex, Object obj)
	{
		throw unsupported("setOutputMsgValues");
	}

	@Override
	public void updateEdge(int outPortNum)
	{
		throw unsupported("updateEdge");
	}

	/*-----------------------
	 * ISolverFactor methods
	 */
	
	@Override
	public void createMessages()
	{
	}

	@Override
	public double[] getBelief()
	{
		return null; // FIXME
	}
	
	@Override
	public int[][] getPossibleBeliefIndices()
	{
		throw unsupported("getPossibleBeliefIndices"); // FIXME
	}
	
	@Override
	public void moveMessages(ISolverNode other)
	{
		throw unsupported("moveMessages");
	}
	
	@Override
	public void setDirectedTo(int[] indices)
	{
		throw unsupported("setDirectedTo");
	}

	/*-----------------
	 * Private methods
	 */
	
//	private ISolverFactor getDelegate()
//	{
//		final ISolverFactor delegate = _delegate;
//		if (delegate != null)
//		{
//			return delegate;
//		}
//		else
//		{
//			return _delegate = _root.getTransformMap().sourceToTargetFactor(getFactor()).getSolver();
//		}
//	}
//
//	private JointDomainReindexer getDelegateReindexer()
//	{
//		return null;
//	}
	
	private RuntimeException unsupported(String method)
	{
		return DimpleException.unsupportedMethod(getClass(), method);
	}

}
