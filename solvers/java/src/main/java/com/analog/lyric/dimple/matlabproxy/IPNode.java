/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

package com.analog.lyric.dimple.matlabproxy;


import com.analog.lyric.dimple.model.INameable;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;

public interface IPNode extends INameable
{
    public Port[] getPorts();
	public INode getModelerObject();
	
    public void update() ;
	public void updateEdge(int outPortNum) ;
	
	public PFactorGraph getParentGraph() ;
	public PFactorGraph getRootGraph() ;
	public boolean hasParentGraph();
	public boolean isFactor();
	public boolean isVariable();
	public boolean isGraph();
}
