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

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

/**
 * Functional interface for solver factor creation.
 * <p>
 * Because this interface only contains one method it can be be satisfied by a Java 8
 * lambda expression, e.g.:
 * <blockquote><pre>
 * ISolverFactorCreator<SFactor,SGraph> creator = (factor,sgraph) => new MyCustomFactor(factor);
 * </pre></blockquote>
 * <p>
 * @param <SFactor> is the returned custom factor base class
 * @param <SGraph> is the base class of the solver graph for the created custom factor.
 * @since 0.08
 * @author Christopher Barber
 * @see CustomFactors
 */
public interface ISolverFactorCreator<SFactor extends ISolverFactor, SGraph extends ISolverFactorGraph>
{
	/**
	 * Create a new custom factor.
	 * <p>
	 * @param factor is the model factor instance for which the solver factor is to be generated.
	 * @param sgraph is the solver graph to which solver factor will be added.
	 * @throws SolverFactorCreationException if factor cannot be created typically because required constraints
	 * have not been met.
	 * @since 0.08
	 */
	SFactor create(Factor factor, SGraph sgraph);
}