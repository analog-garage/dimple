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
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Node;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public abstract class SNode implements ISolverNode 
{
	private Node _model;
	
	public SNode(Node n)
	{
		_model = n;
	}
	
    public INode getModelObject()
    {
    	return _model;
    }

    @Override
    public Object getInputMsg(int portIndex)
    {
    	throw new DimpleException("Not supported for: " + this);
    }
    
    @Override
    public Object getOutputMsg(int portIndex)
    {
    	throw new DimpleException("Not supported for: " + this);
    }
    
    @Override
    public void setInputMsg(int portIndex,Object obj)
    {
    	throw new DimpleException("Not supported for: " + this);
    }
    
    @Override
    public void setOutputMsg(int portIndex,Object obj)
    {
    	throw new DimpleException("Not supported for: " + this);
    }
    
    
	public void moveMessages(ISolverNode other, boolean moveSiblingMessages)
	{
		if (getModelObject().getSiblings().size() != other.getModelObject().getSiblings().size())
			throw new DimpleException("cannot move messages on nodes with different numbers of ports");			
		
		for (int i = 0; i < getModelObject().getSiblings().size(); i++)
		{
			moveMessages(other, i);
			
			INode thisVariable = getModelObject().getSiblings().get(i);
			INode otherVariable = other.getModelObject().getSiblings().get(i);
			thisVariable.getSolver().moveMessages(otherVariable.getSolver(), getModelObject().getSiblingPortIndex(i));
		}
	}
	
//	@Override
//	public void initialize() 
//	{
//		invalidateCache();
//	}
//	
//	@Override
//	public void invalidateCache()
//	{
//		_cacheIsValid = false;
//	}
//	protected void ensureCacheUpdated()
//	{
//		if (!_cacheIsValid)
//		{
//			updateMessageCache();
//			_cacheIsValid = true;
//		}
//	}
//	protected void updateMessageCache()
//	{
//		
//	}
	
	
//	@Override
//	public void connectSibling(INode node)  
//	{
//		invalidateCache();
//	}

}
