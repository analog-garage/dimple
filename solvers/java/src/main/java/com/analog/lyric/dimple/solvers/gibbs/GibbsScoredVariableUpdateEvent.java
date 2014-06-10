/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

import java.io.PrintStream;

import com.analog.lyric.dimple.model.values.Value;

/**
 * Gibbs variable update event that includes sample scores.
 * <p>
 * @since 0.06
 * @author Christopher Barber
 */
public class GibbsScoredVariableUpdateEvent extends GibbsVariableUpdateEvent
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private final double _oldSampleScore;
	private final double _newSampleScore;

	/*--------------
	 * Construction
	 */
	
	GibbsScoredVariableUpdateEvent(
		ISolverVariableGibbs source,
		Value oldValue, double oldSampleScore,
		Value newValue, double newSampleScore,
		int rejectCount)
	{
		super(source, oldValue, newValue, rejectCount);
		_oldSampleScore = oldSampleScore;
		_newSampleScore = newSampleScore;
	}
	
	/*---------------------
	 * DimpleEvent methods
	 */
	
	@Override
	protected void printDetails(PrintStream out, int verbosity)
	{
		super.printDetails(out, verbosity);
		out.format(" score %+g", getScoreDifference());
		if (verbosity > 1)
		{
			out.format(" (%g - %g)", getOldSampleScore(), getNewSampleScore());
		}
	}
	
	/*----------------------------------------
	 * GibbsScoredVariableUpdateEvent methods
	 */
	
	/**
	 * Computes the effect on the total graph score produced by this sample value change.
	 * <p>
	 * Returns {@link #getNewSampleScore()} minus {@link #getOldSampleScore()}.
	 * 
	 * @since 0.06
	 */
	public double getScoreDifference()
	{
		return _newSampleScore - _oldSampleScore;
	}
	
	/**
	 * The value of {@link ISolverVariableGibbs#getCurrentSampleValue()} computed for
	 * the old sample value.
	 * 
	 * @since 0.06
	 */
	public double getOldSampleScore()
	{
		return _oldSampleScore;
	}
	
	/**
	 * The value of {@link ISolverVariableGibbs#getCurrentSampleValue()} computed for
	 * the new sample value.
	 * 
	 * @since 0.06
	 */
	public double getNewSampleScore()
	{
		return _newSampleScore;
	}
}
