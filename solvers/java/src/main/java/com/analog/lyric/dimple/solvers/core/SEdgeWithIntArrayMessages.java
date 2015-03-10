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

import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.solvers.interfaces.ISolverEdgeState;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class SEdgeWithIntArrayMessages extends SEdgeWithSymetricMessages<int[]>
{
	public SEdgeWithIntArrayMessages(int[] varToFactorMsg, int[] factorToVarMsg)
	{
		super(varToFactorMsg, factorToVarMsg);
	}
	
	public SEdgeWithIntArrayMessages(int size)
	{
		super(new int[size], new int[size]);
	}
	
	@Override
	public void reset()
	{
		Arrays.fill(varToFactorMsg, 0);
		Arrays.fill(factorToVarMsg, 0);
	}
	
	@Override
	public void setFrom(ISolverEdgeState other)
	{
		setFactorToVarMsg(other.getFactorToVarMsg());
		setVarToFactorMsg(other.getVarToFactorMsg());
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * @param msg must be a {@code int[]}.
	 */
	@Override
	public void setFactorToVarMsg(@Nullable Object msg)
	{
		final int[] array = (int[])msg;
		System.arraycopy(array, 0, factorToVarMsg, 0, factorToVarMsg.length);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @param msg must be a {@code int[]}.
	 */
	@Override
	public void setVarToFactorMsg(@Nullable Object msg)
	{
		final int[] array = (int[])msg;
		System.arraycopy(array, 0, varToFactorMsg, 0, varToFactorMsg.length);
	}

}
