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

package com.analog.lyric.dimple.test.core.proposalKernels;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.collect.ConstructorRegistry;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.solvers.core.proposalKernels.CircularNormalProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.ProposalKernelOptionKey;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.options.LocalOptionHolder;
import com.analog.lyric.options.OptionValidationException;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
@SuppressWarnings("deprecation")
public class TestProposalKernels extends DimpleTestBase
{
	public static final ProposalKernelOptionKey testKey =
		new ProposalKernelOptionKey(TestProposalKernels.class, "testKey", NormalProposalKernel.class);
	
	@Test
	public void testRegistry()
	{
		ConstructorRegistry<IProposalKernel> registry = DimpleEnvironment.active().proposalKernels();
		assertNull(registry.instantiateOrNull("does not exist"));
		assertNull(registry.instantiateOrNull("ProposalKernelRegistry"));
		assertNull(registry.instantiateOrNull("MyProposalKernel"));
		assertNull(registry.getClassOrNull("MyProposalKernel"));
		IProposalKernel kernel = registry.instantiateOrNull("NormalProposalKernel");
		assertTrue(kernel instanceof NormalProposalKernel);
		IProposalKernel kernel2 = registry.instantiateOrNull("NormalProposalKernel");
		assertTrue(kernel2 instanceof NormalProposalKernel);
		assertNotSame(kernel, kernel2);
		
		registry.addPackage(getClass().getPackage().getName());
		kernel = registry.instantiateOrNull("MyProposalKernel");
		assertTrue(kernel instanceof MyProposalKernel);
		assertSame(MyProposalKernel.class, registry.getClassOrNull("MyProposalKernel"));
		expectThrow(RuntimeException.class, registry, "instantiate", "BrokenProposalKernel");
		assertNull(registry.instantiateOrNull("NoConstructorProposalKernel"));
	}
	
	@Test
	public void testOptionKeys()
	{
		assertSame(IProposalKernel.class, testKey.superClass());
		assertSame(NormalProposalKernel.class, testKey.defaultValue());
		assertSame(NormalProposalKernel.class, testKey.convertValue("NormalProposalKernel"));
		assertSame(NormalProposalKernel.class, testKey.convertValue(NormalProposalKernel.class.getName()));
		assertSame(NormalProposalKernel.class, testKey.convertValue(NormalProposalKernel.class));
		expectThrow(OptionValidationException.class, testKey, "validate", NoConstructorProposalKernel.class);
	}
	
	@Test
	public void testNormalProposalKernel()
	{
		NormalProposalKernel kernel = new NormalProposalKernel();
		assertEquals(1.0, kernel.getStandardDeviation(), 0.0);
		kernel.setStandardDeviation(2.0);
		assertEquals(2.0, kernel.getStandardDeviation(), 0.0);
		
		LocalOptionHolder holder = new LocalOptionHolder();
		kernel = new NormalProposalKernel();
		kernel.configureFromOptions(holder);
		assertEquals(NormalProposalKernel.standardDeviation.defaultValue(), kernel.getStandardDeviation(), 0.0);
		holder.setOption(NormalProposalKernel.standardDeviation, 2.3);
		kernel.configureFromOptions(holder);
		assertEquals(2.3, kernel.getStandardDeviation(), 0.0);
	}
	
	@Test
	public void testCircularNormalProposalKernel()
	{
		CircularNormalProposalKernel kernel = new CircularNormalProposalKernel();
		assertEquals(NormalProposalKernel.standardDeviation.defaultValue(), kernel.getStandardDeviation(), 0.0);
		assertEquals(CircularNormalProposalKernel.lowerBound.defaultValue(), kernel.getLowerBound(), 0.0);
		assertEquals(CircularNormalProposalKernel.upperBound.defaultValue(), kernel.getUpperBound(), 0.0);
		
		kernel.setStandardDeviation(2.0);
		assertEquals(2.0, kernel.getStandardDeviation(), 0.0);
		kernel.setCircularBounds(-1, 1);
		assertEquals(-1.0, kernel.getLowerBound(), 0.0);
		assertEquals(1.0, kernel.getUpperBound(), 0.0);

		LocalOptionHolder holder = new LocalOptionHolder();
		kernel = new CircularNormalProposalKernel();
		kernel.configureFromOptions(holder);
		assertEquals(NormalProposalKernel.standardDeviation.defaultValue(), kernel.getStandardDeviation(), 0.0);
		assertEquals(CircularNormalProposalKernel.lowerBound.defaultValue(), kernel.getLowerBound(), 0.0);
		assertEquals(CircularNormalProposalKernel.upperBound.defaultValue(), kernel.getUpperBound(), 0.0);
		
		holder.setOption(NormalProposalKernel.standardDeviation, 2.3);
		holder.setOption(CircularNormalProposalKernel.lowerBound, -4.3);
		holder.setOption(CircularNormalProposalKernel.upperBound, 5.2);
		kernel.configureFromOptions(holder);
		assertEquals(2.3, kernel.getStandardDeviation(), 0.0);
		assertEquals(-4.3, kernel.getLowerBound(), 0.0);
		assertEquals(5.2, kernel.getUpperBound(), 0.0);
	}
}
