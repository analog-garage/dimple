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

package com.analog.lyric.dimple.solvers.lp;

import com.analog.lyric.dimple.options.SolverOptions;
import com.analog.lyric.options.StringOptionKey;

/**
 * Options for linear programming (LP) based dimple solver.
 * <p>
 * These options are used by the {@link LPSolver}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class LPOptions extends SolverOptions
{
	/**
	 * Name of external LP implementation used to solve the linear program version of the graph.
	 * <p>
	 * The following values are supported:
	 * <dl>
	 * <dt>""</dt>
	 * <dd>Use the default, which is currently "matlab"</dd>
	 * <dt>"matlab"</dt>
	 * <dd>Only works when using Dimple from MATLAB front-end. This solver uses the LP solver configured
	 * in MATLAB, which is described by the {@link #MatlabLPSolver} option.</dd>
	 * <dt>"CPLEX"</dt>
	 * <dt>"GLPK"</dt>
	 * <dt>"Gurobi"</dt>
	 * <dt>"LpSolve"</dt>
	 * <dt>"MiniSat"</dt>
	 * <dt>"Mosek"</dt>
	 * <dt>"SAT4J"</dt>
	 * </dl>
	 * 
	 * The chosen solver must be separately installed and configured for use in Java.
	 * <p>
	 * @see <a href="http://javailp.sourceforge.net/">Java ILP - Java Interface to ILP Solvers</a>
	 */
	public static final StringOptionKey LPSolver =
		new StringOptionKey(LPOptions.class, "LPSolver");
	
	/**
	 * Name of LP solver to use in MATLAB when "matlab" is the {@link LPSolver} value.
	 * <p>
	 * The following values are currently supported:
	 * <dl>
	 * <dt>""</dt>
	 * <dd>Uses the default, which is currently "matlab"</dd>
	 * <dt>"matlab"<dt>
	 * <dd>Uses {@code linprog} function from the Optimization Toolbox</dd>
	 * <dt>"glpk"</dt>
	 * <dt>"glpkIP"</dt>
	 * <dt>"gurobi"</dt>
	 * <dt>"gurobiIP</dt>
	 * </dl>
	 * 
	 * The chosen solver must be separately installed and configured for use in MATLAB.
	 */
	public static final StringOptionKey MatlabLPSolver =
		new StringOptionKey(LPOptions.class, "MatlabLPSolver");
}
