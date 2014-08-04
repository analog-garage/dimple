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

package com.analog.lyric.dimple.test.solvers.junctiontree;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransform;
import com.analog.lyric.dimple.model.transform.VariableEliminator.VariableCost;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeOptions;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolver;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolverGraphBase;

/**
 * Unit tests for {@link JunctionTreeOptions}.
 * @since 0.07
 * @author Christopher Barber
 */
public class TestJunctionTreeOptions
{
	@Test
	public void test()
	{
		// Test default values
		assertEquals(false, JunctionTreeOptions.useConditioning.defaultValue());
		assertTrue(JunctionTreeOptions.variableEliminatorCostFunctions.defaultValue().isEmpty());
		assertEquals((Integer)JunctionTreeTransform.DEFAULT_MAX_TRANSFORMATION_ATTEMPTS,
			JunctionTreeOptions.maxTransformationAttempts.defaultValue());
		
		FactorGraph fg = new FactorGraph();
		
		// Check initial defaults
		JunctionTreeSolverGraphBase<?> sfg = requireNonNull(fg.setSolverFactory(new JunctionTreeSolver()));
		assertFalse(sfg.useConditioning());
		assertEquals(JunctionTreeTransform.DEFAULT_MAX_TRANSFORMATION_ATTEMPTS, sfg.maxTransformationAttempts());
		assertEquals(0, sfg.variableEliminatorCostFunctions().length);
		
		assertNull(fg.setSolverFactory(null));
		
		// Set initial options on model.
		fg.setOption(JunctionTreeOptions.useConditioning, true);
		fg.setOption(JunctionTreeOptions.maxTransformationAttempts, 3);
		JunctionTreeOptions.variableEliminatorCostFunctions.set(fg, VariableCost.MIN_FILL);
		
		// Test options that are updated on initialize.
		sfg = requireNonNull(fg.setSolverFactory(new JunctionTreeSolver()));
		assertFalse(sfg.useConditioning());
		assertEquals(JunctionTreeTransform.DEFAULT_MAX_TRANSFORMATION_ATTEMPTS, sfg.maxTransformationAttempts());
		assertEquals(0, sfg.variableEliminatorCostFunctions().length);

		sfg.getTransformer().random().setSeed(42);
		sfg.initialize();
		double r = sfg.getTransformer().random().nextDouble();
		assertTrue(sfg.useConditioning());
		assertEquals(3, sfg.maxTransformationAttempts());
		assertArrayEquals(new Object[] { VariableCost.MIN_FILL.function() }, sfg.variableEliminatorCostFunctions());
		
		// Make sure setting random seed through option produces same result
		fg.setOption(JunctionTreeOptions.randomSeed, 42L);
		sfg.initialize();
		assertEquals(r, sfg.getTransformer().random().nextDouble(), 0.0);
		
		// Test using set methods
		sfg.useConditioning(false);
		assertEquals(false, sfg.getLocalOption(JunctionTreeOptions.useConditioning));
		sfg.maxTransformationAttempts(12);
		assertEquals((Integer)12, sfg.getLocalOption(JunctionTreeOptions.maxTransformationAttempts));
		sfg.variableEliminatorCostFunctions(VariableCost.MIN_NEIGHBORS);
		assertArrayEquals(
			new Object[] { VariableCost.MIN_NEIGHBORS.function() },
			requireNonNull(sfg.getOption(JunctionTreeOptions.variableEliminatorCostFunctions)).toArray());
	}
}
