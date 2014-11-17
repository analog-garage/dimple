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

/**
 * Event triggered after updating samples on all variables in Gibbs solver.
 * <p>
 * This event holds statistics relevant to the last round of sampling.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class GibbsSampleStatisticsEvent extends GibbsSolverGraphEvent
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	final double _sampleScore;
	final boolean _wasBest;
	final double _oldTemperature;
	final double _newTemperature;
	
	/*--------------
	 * Construction
	 */
	
	GibbsSampleStatisticsEvent(
		GibbsSolverGraph source,
		double sampleScore,
		boolean wasBest,
		double oldTemperature,
		double newTemperature)
	{
		super(source);
		_sampleScore = sampleScore;
		_wasBest = wasBest;
		_oldTemperature = oldTemperature;
		_newTemperature = newTemperature;
	}

	/*---------------------
	 * DimpleEvent methods
	 */
	
	@Override
	protected void printDetails(PrintStream out, int verbosity)
	{
		out.format("total sample score %g", _sampleScore);
		if (_wasBest)
		{
			out.print(" (best)");
		}
		if (verbosity > 0 && _oldTemperature == _oldTemperature)
		{
			out.format("\ntemperature %g => %g", _oldTemperature, _newTemperature);
		}
	}

	/*------------------------------------
	 * GibbsSampleStatisticsEvent methods
	 */
	
	/**
	 * If annealing is enabled for the graph, this is the new temperature value.
	 * <p>
	 * If annealing is not enabled, this will be NaN.
	 * 
	 * @since 0.08
	 * @see #oldTemperature()
	 * @see GibbsOptions#enableAnnealing
	 */
	public double newTemperature()
	{
		return _newTemperature;
	}
	
	/**
	 * If annealing is enabled for the graph, this is the previous temperature value.
	 * <p>
	 * If annealing is not enabled, this will be NaN.
	 * 
	 * @since 0.08
	 * @see #newTemperature()
	 * @see GibbsOptions#enableAnnealing
	 */
	public double oldTemperature()
	{
		return _oldTemperature;
	}
	
	/**
	 * The value of {@link GibbsSolverGraph#getSampleScore()} for the current sample values.
	 * @since 0.08
	 * @see #wasBestSampleScore()
	 */
	public double sampleScore()
	{
		return _sampleScore;
	}
	
	/**
	 * True if sample had the best score so far since the graph was initialized.
	 * @since 0.08
	 * @see #sampleScore()
	 */
	public boolean wasBestSampleScore()
	{
		return _wasBest;
	}
}
