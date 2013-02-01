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

/**
 * 
 */
package com.analog.lyric.dimple.solvers.interfaces;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.INode;

/**
 * @author schweitz
 *
 */
public interface ISolverNode 
{
	public void update() ;
	public void updateEdge(int outPortNum) ;
	public void initialize() ;
	//public void connectSibling(INode p) ;
	public ISolverFactorGraph getParentGraph();
	public ISolverFactorGraph getRootGraph();
	public double getScore() ;
    public double getInternalEnergy() ;
    public double getBetheEntropy() ;
    public INode getModelObject();
    public Object getInputMsg(int portIndex);
    public Object getOutputMsg(int portIndex);
    public void setInputMsg(int portIndex,Object obj);
    public void setOutputMsg(int portIndex,Object obj);
    public void moveMessages(ISolverNode other, boolean moveSiblingMessages);
    public void moveMessages(ISolverNode other, int portNum);
}
