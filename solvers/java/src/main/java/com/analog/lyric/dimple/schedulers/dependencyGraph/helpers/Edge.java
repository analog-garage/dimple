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

package com.analog.lyric.dimple.schedulers.dependencyGraph.helpers;

import com.analog.lyric.dimple.model.core.INode;
import org.eclipse.jdt.annotation.Nullable;

/*
 * Utility class used by LastUpdateGraph and StaticDependencyGraphNodes for
 * tracking the last schedule entry to affect an edge.
 */
public class Edge
{
	public INode from;
	public INode to;
	
	public Edge(INode f, INode t)
	{
		from = f;
		to = t;
	}
	
	@Override
	public int hashCode()
	{
		return from.hashCode()+to.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable Object other)
	{
		if (other == this)
		{
			return true;
		}
		
		if (!(other instanceof Edge))
		{
			return false;
		}
		
		Edge otherEdge = (Edge)other;
		return otherEdge.from == from && otherEdge.to == to;
	}
}
