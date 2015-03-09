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

package com.analog.lyric.dimple.solvers.core;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.solvers.interfaces.ISolverEdge;

/**
 * A dummy solver edge type.
 * <p>
 * You can use this in place of {@link ISolverEdge} to document that there is no edge state as in
 * {@linkplain com.analog.lyric.dimple.solvers.lp.LPSolverGraph LPSolverGraph}.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public enum NoSolverEdge implements ISolverEdge
{
	INSTANCE;
	
	private NoSolverEdge() {}
	
	@Override
	public @Nullable Object getFactorToVarMsg()
	{
		return null;
	}

	@Override
	public @Nullable Object getVarToFactorMsg()
	{
		return null;
	}
	
	@Override
	public void reset()
	{
	}
	
	@Override
	public void setFrom(ISolverEdge other)
	{
	}
	
	@Override
	public void setFactorToVarMsg(@Nullable Object msg)
	{
		throw new UnsupportedOperationException("setFactorToVarMsg");
	}

	@Override
	public void setVarToFactorMsg(@Nullable Object msg)
	{
		throw new UnsupportedOperationException("setVarToFactorMsg");
	}
}
