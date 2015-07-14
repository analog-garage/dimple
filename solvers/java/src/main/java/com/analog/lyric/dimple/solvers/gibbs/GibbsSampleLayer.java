/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.gibbs;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.ValueDataLayer;

/**
 * Data layer view of Gibbs sample values.
 * @since 0.08
 * @author Christopher Barber
 */
class GibbsSampleLayer extends ValueDataLayer
{
	/*-------
	 * State
	 */
	
	private final GibbsSolverGraph _solverGraph;
	
	/*--------------
	 * Construction
	 */
	
	GibbsSampleLayer(GibbsSolverGraph sgraph)
	{
		super(sgraph.getModel(), GibbsFactorGraphData.constructor(sgraph.getRootSolverGraph()));
		_solverGraph = sgraph;
	}

	
	@Override
	public GibbsSampleLayer clone()
	{
		return new GibbsSampleLayer(_solverGraph);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (obj instanceof GibbsSampleLayer)
		{
			GibbsSampleLayer other = (GibbsSampleLayer)obj;

			// Since this object doesn't directly hold any data, if two the solver graph is
			// the same, the data must be the same. If the solver graphs are different, then
			// the values must be different since each has its own distinct sample layer.
			return _solverGraph.getRootSolverGraph() == other._solverGraph.getRootSolverGraph();
		}
		
		return super.equals(obj);
	}

	/*-------------
	 * Map methods
	 */
	
	@Override
	public void clear()
	{
		throw removalNotSupported();
	}
	
	/*-------------------
	 * DataLayer methods
	 */

	/**
	 * Returns true to indicate that this is a view of sample values held directly in the Gibbs solver variables.
	 * <p>
	 * Because this is a view, cloning this object does not create a distinct copy of the values.
	 */
	@Override
	public boolean isView()
	{
		return true;
	}


	static RuntimeException removalNotSupported()
	{
		return new UnsupportedOperationException("Gibbs sample values may not be removed");
	}

}
