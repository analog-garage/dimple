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

import com.analog.lyric.dimple.model.factors.Factor;
import org.eclipse.jdt.annotation.Nullable;


public interface ISolverFactor extends ISolverNode
{
	@Override
	public @Nullable Factor getModelObject();
	
	public Object getBelief() ;
	@Override
	public double getInternalEnergy();
	@Override
	public double getBetheEntropy();
	
	@Override
	public ISolverVariable getSibling(int edge);
	
	//This method is called on a solver factor when it is first created.
	//This method should create messages to and from variables.
	public abstract void createMessages();
	
	//In order to support repeated graphs, this method must be implemented.
	//SFactorBase implements this by calling move messages for every port on both
	//the factor and the connected variable.
	public void moveMessages(ISolverNode other);
	
	public int[][] getPossibleBeliefIndices() ;
	
	public void setDirectedTo(int [] indices);
}
