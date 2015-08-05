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

package com.analog.lyric.dimple.test.model.variables;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.analog.lyric.dimple.data.DataLayer;
import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.factorfunctions.Negate;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.model.core.Edge;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.NodeType;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Unit test for {@link Variable} class.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@SuppressWarnings("deprecation")
public class TestVariable extends DimpleTestBase
{
	@SuppressWarnings("unused")
	@Test
	public void test()
	{
		FactorGraph fg = new FactorGraph();
		assertNull(fg.getDefaultConditioningLayer());
		
		Real r = new Real();
		assertInvariants(r);
		assertEquals("Real", r.getModelerClassName()); // deprecated
		
		assertNull(r.getParentGraph());
		assertNull(r.getPrior());
		assertNull(r.getCondition());
		assertEquals(RealDomain.unbounded(), r.getDomain());
		
		// deterministic input/output flags
		assertFalse(r.isDeterministicInput());
		assertFalse(r.isDeterministicOutput());
		r.setDeterministicInput();
		assertTrue(r.isDeterministicInput());
		assertFalse(r.isDeterministicOutput());
		r.initialize(); // clears flags
		assertFalse(r.isDeterministicInput());
		assertFalse(r.isDeterministicOutput());
		r.setDeterministicOutput();
		assertFalse(r.isDeterministicInput());
		assertTrue(r.isDeterministicOutput());
		r.initialize();
		
		Value prior = Value.createReal(Math.PI);
		r.setPrior(prior);
		assertSame(prior, r.getPrior());
		assertInvariants(r);
		
		r.setPrior(42);
		assertEquals(42.0, requireNonNull(r.getPriorValue()).getDouble(), 0.0);
		expectThrow(ClassCastException.class, r, "setPrior", "barf");
		assertEquals(42.0, requireNonNull(r.getPriorValue()).getDouble(), 0.0);
		
		NormalParameters normal = new NormalParameters(1.0, 2.0);
		r.setPrior(normal);
		assertSame(normal, r.getPrior());
		assertInvariants(r);
		
		r.setPrior(null);
		assertNull(r.getPrior());
		
		r.setFixedValue(1.2);
		assertEquals(1.2, requireNonNull(r.getPriorValue()).getDouble(), 1e-15);
		assertEquals((Double)1.2, (Double)requireNonNull(r.getFixedValueAsObject()), 1e-15);
		r.setFixedValueObject(null);
		assertNull(r.getPrior());
		
		r.setInputObject(normal);
		assertSame(normal, r.getPrior());
		
		// no parent - cannot create a default conditioning layer
		expectThrow(IllegalStateException.class, r, "setCondition", 12);
		r.setCondition(null); // ok to set to null when there is no parent
		assertNull(r.getCondition());
		
		fg.addVariables(r);
		assertSame(fg, r.getParentGraph());
		
		r.setCondition(normal);
		assertSame(normal, r.getCondition());
		assertNotNull(fg.getDefaultConditioningLayer());
		assertInvariants(r);
		
		r.setCondition(null);
		assertNull(r.getCondition());
		r.setCondition(1.2345);
		assertEquals(1.2345, Value.class.cast(r.getCondition()).getDouble(), 1e-15);
		
		fg.setDefaultConditioningLayer(null);
		r.setCondition(null); // ok to set to null when there is no layer - it won't create a layer
		assertNull(r.getCondition());
		assertNull(fg.getDefaultConditioningLayer());
		
		Factor rf = fg.addFactor(new Normal(0.0, 1.0), r);
		fg.initialize(); // needed to update directedness
		assertInvariants(r);
		
		Real notr = new Real();
		Factor notrf = fg.addFactor(new Negate(), notr, r);
		fg.initialize();
		
		assertInvariants(r);
		assertInvariants(notr);
	}
	
	public static void assertInvariants(Variable var)
	{
		assertTrue(var.isVariable());
		assertSame(var, var.asVariable());
		assertEquals(NodeType.VARIABLE, var.getNodeType());
		
		if (var instanceof Discrete)
		{
			assertSame(var, var.asDiscreteVariable());
		}
		else
		{
			expectThrow(ClassCastException.class, var, "asDiscreteVariable");
		}

		IDatum prior = var.getPrior();
		assertSame(prior instanceof IUnaryFactorFunction ? prior : null, var.getPriorFunction());
		assertSame(prior instanceof Value ? prior : null, var.getPriorValue());
		assertEquals(prior instanceof Value, var.hasFixedValue());
		
		IDatum condition = var.getCondition();

		final FactorGraph graph = var.getParentGraph();
		if (graph == null)
		{
			assertNull(condition);
		}
		else
		{
			DataLayer<?> conditioningLayer = graph.getDefaultConditioningLayer();
			if (conditioningLayer == null)
			{
				assertNull(condition);
			}
			else
			{
				assertSame(conditioningLayer.get(var), condition);
			}
		}
		
		ISolverVariable svar = var.getSolver();
		if (svar != null)
		{
			assertSame(var, svar.getModelObject());
			assertSame(svar, var.getSolverIfType(svar.getClass()));
		}
		
		//
		// Siblings
		//
		
		final int nSiblings = var.getSiblingCount();
		final List<Factor> siblings = var.getSiblings();
		final Factor[] factors = var.getFactors();

		assertTrue(nSiblings >= 0);
		assertEquals(nSiblings, siblings.size());
		assertEquals(nSiblings, factors.length);
		boolean deterministicInput = false, deterministicOutput = false;
		for (int i = 0; i < nSiblings; ++i)
		{
			Factor factor = factors[i];
			assertSame(factor, siblings.get(i));
			assertSame(factor, var.getSibling(i));
			
			Edge edge = var.getSiblingEdge(i);
			assertSame(factor, edge.factor());
			assertSame(var, edge.variable());
			assertSame(i, edge.edgeState().getVariableToFactorEdgeNumber());
			
			if (factor.getFactorFunction().isDeterministicDirected())
			{
				switch (edge.direction())
				{
				case UNDIRECTED:
					break;
				case FROM_FACTOR:
					deterministicOutput = true;
					break;
				case TO_FACTOR:
					deterministicInput = true;
					break;
				}
			}
		}
		assertEquals(deterministicInput, var.isDeterministicInput());
		assertEquals(deterministicOutput, var.isDeterministicOutput());
		
		//
		// Deprecated stuff
		//
		
		assertEquals("Variable", var.getClassLabel());
		if (svar == null)
		{
			assertFalse(var.guessWasSet());
			expectThrow(NullPointerException.class, var, "getGuess");
			expectThrow(NullPointerException.class, var, "setGuess", (Object)null);
		}
		assertSame(var.getPriorFunction(), var.getInputObject());
		expectThrow(UnsupportedOperationException.class, var, "setSolver", (ISolverVariable)null);
		if (prior instanceof Value)
		{
			Value priorValue = (Value)prior;
			assertEquals(priorValue.getObject(), var.getFixedValueAsObject());
		}
		else
		{
			assertNull(var.getFixedValueAsObject());
			assertNull(var.getFixedValueObject());
		}
	}
}
