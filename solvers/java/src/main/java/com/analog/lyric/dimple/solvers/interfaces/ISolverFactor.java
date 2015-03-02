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

package com.analog.lyric.dimple.solvers.interfaces;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraphEdgeState;
import com.analog.lyric.dimple.model.factors.Factor;


public interface ISolverFactor extends ISolverNode
{
	@Override
	public Factor getModelObject();
	
	public Object getBelief() ;
	@Override
	public double getInternalEnergy();
	@Override
	public double getBetheEntropy();
	
	@Override
	public ISolverVariable getSibling(int edge);
	
	@Override
	public ISolverFactorGraph getParentGraph();
	
	public @Nullable ISolverEdge createEdge(FactorGraphEdgeState edge);
	
	//In order to support repeated graphs, this method must be implemented.
	//SFactorBase implements this by calling move messages for every port on both
	//the factor and the connected variable.
	public void moveMessages(ISolverNode other);
	
	public int[][] getPossibleBeliefIndices() ;
	
	public void setDirectedTo(int [] indices);
}
