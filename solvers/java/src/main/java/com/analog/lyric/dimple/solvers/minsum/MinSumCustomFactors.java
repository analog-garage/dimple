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

package com.analog.lyric.dimple.solvers.minsum;

import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.CustomFactors;
import com.analog.lyric.dimple.solvers.core.SolverFactorCreationException;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.minsum.customFactors.CustomXor;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class MinSumCustomFactors extends CustomFactors<ISolverFactor, MinSumSolverGraph>
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	public MinSumCustomFactors()
	{
		super(ISolverFactor.class, MinSumSolverGraph.class);
	}
	
	protected MinSumCustomFactors(MinSumCustomFactors other)
	{
		super(other);
	}
	
	@Override
	public CustomFactors<ISolverFactor, MinSumSolverGraph> clone()
	{
		return new MinSumCustomFactors(this);
	}
	
	/*-----------------------
	 * CustomFactors methods
	 */
	
	@Override
	public void addBuiltins()
	{
		add(Xor.class, CustomXor.class);
		
		// Backward compatibility
		add("customXor", CustomXor.class);
	}
	
	@Override
	public ISolverFactor createDefault(Factor factor, MinSumSolverGraph sgraph)
	{
		if (!factor.isDiscrete())
		{
			throw new SolverFactorCreationException("min-sum only supports discrete factors");
		}
		
		@SuppressWarnings("deprecation") // FIXME remove when STableFactor removed
		ISolverFactor sfactor = new STableFactor(factor, sgraph);
		return sfactor;
	}
}
