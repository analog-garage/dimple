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

import com.analog.lyric.dimple.events.SolverEventSource;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.options.IOptionHolder;

public abstract class SNode extends SolverEventSource implements ISolverNode
{
	/*-----------
	 * Constants
	 */
	
	/**
	 * Bits in {@link #_flags} reserved by this class and its superclasses.
	 */
	protected final int RESERVED_FLAGS = 0xFF000000;
	
	/*-------
	 * State
	 */

	private final Node _model;
	
	/*--------------
	 * Construction
	 */
	
	public SNode(Node n)
	{
		_model = n;
	}
	
	/*-----------------------
	 * IOptionHolder methods
	 */
	
	@Override
	public IOptionHolder getOptionParent()
	{
		return getParentGraph();
	}
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public Node getModelObject()
    {
    	return _model;
    }
	
	
	@Override
	public void initialize()
	{
		clearFlags();
		for (int i = 0, end = getModelObject().getSiblingCount(); i < end; i++)
			resetEdgeMessages(i);
	}
	
	@Override
	public void setInputMsg(int portIndex, Object obj) {
		throw new DimpleException("Not supported by " + this);
	}
	@Override
	public void setOutputMsg(int portIndex, Object obj) {
		throw new DimpleException("Not supported by " + this);
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj) {
		throw new DimpleException("Not supported by " + this);
	}
	@Override
	public void setOutputMsgValues(int portIndex, Object obj) {
		throw new DimpleException("Not supported by " + this);
	}

}
