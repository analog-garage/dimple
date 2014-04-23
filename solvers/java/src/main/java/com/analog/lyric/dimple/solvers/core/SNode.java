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

import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.options.AbstractOptionHolder;
import com.analog.lyric.options.IOptionHolder;

public abstract class SNode extends AbstractOptionHolder implements ISolverNode
{
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
	
	/*----------------------------
	 * IDimpleEventSource methods
	 */
	
    @Override
	public FactorGraph getContainingGraph()
	{
    	return _model.getContainingGraph();
	}

    @Override
    public IDimpleEventListener getEventListener()
    {
    	return getContainingGraph().getEventListener();
    }
    
    @Override
    public ISolverFactorGraph getEventParent()
    {
    	return getParentGraph();
    }
    
	@Override
	public String getEventSourceName()
	{
		// FIXME - determine what this should be
		return toString();
	}

    @Override
    public IModelEventSource getModelEventSource()
    {
    	return getModelObject();
    }
    
	/*----------------------------
	 * ISolverEventSource methods
	 */
	
    @Override
	public ISolverFactorGraph getContainingSolverGraph()
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
