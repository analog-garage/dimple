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
 * Event triggered upon completion of random-restart and burn-in phase of Gibbs solver.
 * <p>
 * Applicable to Gibbs solver graphs.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class GibbsBurnInEvent extends GibbsSolverGraphEvent
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	final int _restartCount;
	final double _temperature;
	
	/*--------------
	 * Construction
	 */
	
	GibbsBurnInEvent(GibbsSolverGraph source, int restartCount, double temperature)
	{
		super(source);
		_restartCount = restartCount;
		_temperature = temperature;
	}

	@Override
	protected void printDetails(PrintStream out, int verbosity)
	{
		out.format("Gibbs burn-in restart %d", _restartCount);
		if (verbosity > 0)
		{
			if (_temperature == _temperature)
			{
				out.format(", temperature %f", _temperature);
			}
		}
	}
	
	/*--------------------------
	 * GibbsBurnInEvent methods
	 */
	
	/**
	 * The restart count.
	 * <p>
	 * When burn-in is invoked manually by calling the {@link GibbsSolverGraph#burnIn()} method, this will
	 * be zero. When invoked from {@link GibbsSolverGraph#solve()} or {@link GibbsSolverGraph#solveOneStep()},
	 * this will be the number of the restart.
	 * <p>
	 * @since 0.08
	 * @see GibbsOptions#numRandomRestarts
	 */
	public int restartCount()
	{
		return _restartCount;
	}
	
	/**
	 * The temperature at the end of the burn-in phase if annealing is enabled.
	 * <p>
	 * If annealing is enabled for the graph this will be the starting temperature, otherwise
	 * it will be NaN.
	 * *
	 * @since 0.08
	 * @see GibbsOptions#enableAnnealing
	 * @see GibbsOptions#initialTemperature
	 */
	public double temperature()
	{
		return _temperature;
	}
}
