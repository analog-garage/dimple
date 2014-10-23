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

package com.analog.lyric.dimple.test.jsproxy;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.jsproxy.JSDiscreteDomain;
import com.analog.lyric.dimple.jsproxy.JSDomain;
import com.analog.lyric.dimple.jsproxy.JSFactor;
import com.analog.lyric.dimple.jsproxy.JSFactorFunction;
import com.analog.lyric.dimple.jsproxy.JSFactorGraph;
import com.analog.lyric.dimple.jsproxy.JSNode;
import com.analog.lyric.dimple.jsproxy.JSRealDomain;
import com.analog.lyric.dimple.jsproxy.JSTableFactorFunction;
import com.analog.lyric.dimple.jsproxy.JSVariable;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;

/**
 * Tests for {@link JSFactorGraph} and other {@link JSNode} implementations.
 * @since 0.07
 * @author Christopher Barber
 */
public class TestJSFactorGraph extends JSTestBase
{
	private static final Random _rand = new Random(23);
	@Test
	public void test()
	{
		JSFactorGraph fg = state.createGraph();
		assertEquals(state.applet, fg.getApplet());
		assertEquals(fg, fg.getGraph());
		assertEquals(JSNode.Type.GRAPH, fg.getNodeType());
		assertNodeInvariants(fg);
		assertEquals(state.solvers.get("SumProduct"), fg.getSolver());
		
		fg.setSolver(null);
		assertNull(fg.getSolver());
		fg.setSolver("SumProduct");
		
		JSDiscreteDomain D6 = state.domains.range(1,6);
		JSDiscreteDomain PairD6 = state.domains.range(2,12);
		JSRealDomain R = state.domains.real();
//		JSRealJointDomain R2 = state.domains.realN(2);
		
		assertNull(fg.getVariable("a"));
		assertNull(fg.getVariable(42));
		assertNull(fg.getFactor("frob"));
		assertNull(fg.getFactor(42));
		
		JSVariable a = fg.addVariable(D6, "a");
		assertEquals("a", a.getName());
		assertEquals(a, fg.getVariable("a"));
		assertEquals(a, fg.getVariable(a.getId()));
		assertEquals(D6, a.domain());
		assertNodeInvariants(a);
		assertSiblings(a);
		
		JSVariable b = fg.addVariable(D6,  "b");
		assertEquals(b, fg.getVariable("b"));
		assertFalse(b.hasFixedValue());
		assertNull(b.getFixedValue());
		double[] inputs = new double[6];
		Arrays.fill(inputs, 1/6.0);
		assertArrayEquals(inputs, (double[])b.getBelief(), 1e-10);
		assertArrayEquals(inputs.clone(), (double[])b.getInput(), 1e-10);
		
		JSVariable c = fg.addVariable(PairD6,  "c");
		
		JSFactor abc = fg.addFactor("Sum", c, a, b);
		assertEquals(Sum.class, abc.getFactorFunction().getDelegate().getClass());
		assertFactorInvariants(abc);
		assertSiblings(abc, c, a, b);
		assertSiblings(a, abc);
		assertSiblings(b, abc);
		assertSiblings(c, abc);
		 
		fg.solve();
		assertEquals(7.0, c.getMaxBeliefValue());
		assertNull(c.getAllSamples());
		assertNull(c.getCurrentSample());
		double[] beliefs = (double[])c.getBelief();
		double[] expectedBeliefs = new double[11];
		for (int i = 1; i < 7; ++i)
		{
			expectedBeliefs[i-1] = i/36.0;
			expectedBeliefs[11-i] = i/36.0;
		}
		assertArrayEquals(expectedBeliefs, beliefs, 1e-10);
		
		b.setFixedValue(1.0);
		assertEquals(1.0, b.getFixedValue());
		assertVariableInvariants(b);
		fg.solve();
		Arrays.fill(expectedBeliefs, 0.0);
		for (int i = 0; i < 6; ++i)
		{
			expectedBeliefs[i] = 1/6.0;
		}
		beliefs = (double[])c.getBelief();
		assertArrayEquals(expectedBeliefs, beliefs, 1e-10);
		
		for (int i = 0; i < inputs.length; ++i)
		{
			inputs[i] = i + 1.0;
		}
		b.setInput(inputs);
		assertArrayEquals(inputs, (double[])b.getInput(), 1e-10);
		for (int i = 0; i < inputs.length; ++i)
		{
			inputs[i] /= 21.0;
		}
		assertArrayEquals(inputs, (double[])b.getBelief(), 1e-10);
		assertFalse(b.hasFixedValue());
		fg.solve();
		
		fg.setSolver(new GibbsSolver());
		fg.setOption(GibbsOptions.numSamples, 100);
		assertEquals(100, fg.getOption(GibbsOptions.numSamples));
		fg.setOption(GibbsOptions.saveAllSamples, true);
		assertEquals(true, fg.getOption(GibbsOptions.saveAllSamples));
		fg.solve();
		assertVariableInvariants(c);
		assertEquals(100, Array.getLength(c.getAllSamples()));
		
		fg = state.createGraph();
		JSVariable d = fg.addVariable(D6, "d");
		JSVariable e = fg.addVariable(D6, "e");
		JSFactor de = fg.addTableFactor(new Object[] { d, e });
		assertTrue(de.getFactorFunction().isTableFactor());
		JSTableFactorFunction deTable = (JSTableFactorFunction)de.getFactorFunction();
		deTable.getTable().setRepresentation("DENSE_WEIGHT");
		deTable.getTable().getDelegate().randomizeWeights(_rand);
		fg.setSolver(state.solvers.get("SumProductSolver"));
		assertEquals(state.solvers.get("SumProduct"), fg.getSolver());
		fg.initialize();
		fg.solveOneStep();
		assertFactorInvariants(de);
		assertVariableInvariants(d);
		assertVariableInvariants(e);
		
		
		fg = state.createGraph();
		a = fg.addVariable(R, "a");
		b = fg.addVariable(R, "b");
		c = fg.addVariable(R, "c");
		d = fg.addVariable(R, "d");
		e = fg.addVariable(R ,"e");
		JSFactor fa = fg.addFactor(parameterizedFunction("Normal", "mean", 1.0), a);
		assertFactorInvariants(fa);
		assertSiblings(fa, a);
	}
	
	private void assertNodeInvariants(JSNode<?> node)
	{
		Node delegate = node.getDelegate();
		assertEquals(state.applet, node.getApplet());
		assertEquals(delegate.getId(), node.getId());
		assertEquals(delegate.getName(), node.getName());
		assertEquals(node.isFactor(), node.getNodeType() == JSNode.Type.FACTOR);
		assertEquals(node.isGraph(), node.getNodeType() == JSNode.Type.GRAPH);
		assertEquals(node.isVariable(), node.getNodeType() == JSNode.Type.VARIABLE);
		
		JSFactorGraph graph = node.getGraph();
		assertNotNull(graph);
		
		JSFactorGraph parent = node.getParent();
		if (parent != null)
		{
			assertEquals(parent.getDelegate(), delegate.getParentGraph());
			assertEquals(graph.equals(node), !parent.equals(graph));
			
			if (node.isVariable())
			{
				assertEquals(node, parent.getVariable(node.getId()));
				assertEquals(node, parent.getVariable(node.getName()));
			}
			else if (node.isFactor())
			{
				assertEquals(node, parent.getFactor(node.getId()));
				assertEquals(node, parent.getFactor(node.getName()));
			}
		}
		else
		{
			assertTrue(node.isGraph()); // We don't allow parentless factors and variables in this API
			assertEquals(node, graph);
		}
		
		final int nSiblings = node.getSiblingCount();
		assertTrue(nSiblings >= 0);
		
		if (node.isGraph())
		{
			assertEquals(0, nSiblings);
		}
		
		for (int i = 0; i < nSiblings; ++i)
		{
			JSNode<?> sibling = node.getSibling(i);
			assertNotNull(sibling);
			
			if (node.isVariable())
			{
				assertTrue(sibling.isFactor());
			}
			else if (node.isFactor())
			{
				assertTrue(sibling.isVariable());
			}
			
			boolean found = false;
			for (int j = sibling.getSiblingCount(); --j>=0;)
			{
				if (node.equals(sibling.getSibling(j)))
				{
					found = true;
				}
			}
			assertTrue(found);
		}
	}
		
	private void assertFactorInvariants(JSFactor factor)
	{
		assertNodeInvariants(factor);
		
		int outputIndex = factor.getOutputIndex();
		int[] indices = factor.getOutputIndices();
		assertEquals(factor.isDirected(), indices != null);
		if (indices != null)
		{
			if (indices.length == 1)
			{
				assertEquals(indices[0], outputIndex);
			}
			else
			{
				assertEquals(-1, outputIndex);
			}
		}
		else
		{
			assertEquals(-1, outputIndex);
		}
	}
	
	private void assertVariableInvariants(JSVariable variable)
	{
		assertNodeInvariants(variable);
		
		assertEquals(variable.hasFixedValue(), variable.getFixedValue() != null);
		
		Object curSample = variable.getCurrentSample();
		Object allSamples = variable.getAllSamples();
		
		JSDomain<?> domain = variable.domain();
		
		if (allSamples != null)
		{
			assertNotNull(curSample);
			assertTrue(allSamples.getClass().isArray());
			
			final int nSamples = Array.getLength(allSamples);
			assertEquals(curSample, Array.get(allSamples, nSamples - 1));
			
			for (int i = 0; i < nSamples; ++i)
			{
				Object sample = Array.get(allSamples, i);
				assertTrue(domain.contains(sample));
			}
		}
	}
	
	private void assertSiblings(JSNode<?> node, JSNode<?> ... siblings)
	{
		assertEquals(siblings.length, node.getSiblingCount());
		for (int i = 0; i < siblings.length; ++i)
		{
			assertEquals(siblings[i], node.getSibling(i));
		}
	}
	
	private JSFactorFunction parameterizedFunction(String function, Object ... keyValuePairs)
	{
		Map<String,Object> parameters = new TreeMap<>();
		for (int i = 0; i < keyValuePairs.length; i += 2)
		{
			parameters.put(keyValuePairs[i].toString(), keyValuePairs[i + 1]);
		}
		return state.functions.create(function, parameters);
	}
}
