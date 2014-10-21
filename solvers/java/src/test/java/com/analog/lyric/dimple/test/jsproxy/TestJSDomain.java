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

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;
import netscape.javascript.JSException;

import org.junit.Test;

import com.analog.lyric.dimple.jsproxy.JSDiscreteDomain;
import com.analog.lyric.dimple.jsproxy.JSDomain;
import com.analog.lyric.dimple.jsproxy.JSDomainFactory;
import com.analog.lyric.dimple.jsproxy.JSRealDomain;
import com.analog.lyric.dimple.jsproxy.JSRealJointDomain;
import com.analog.lyric.dimple.model.domains.ObjectDomain;

/**
 * Tests for {@link JSDomain} classes and {@link JSDomainFactory}.
 * @since 0.07
 * @author Christopher Barber
 */
public class TestJSDomain extends JSTestBase
{
	@Test
	public void test()
	{
		JSDomainFactory domains = state.domains;
		
		JSDiscreteDomain bit = domains.bit();
		assertEquals(2, bit.discreteSize());
		assertInvariants(bit);
		
		JSDiscreteDomain bool = domains.bool();
		assertEquals(2, bool.discreteSize());
		assertInvariants(bool);
		
		JSDiscreteDomain letters = domains.discrete(new Object[] { "a", "b", "c", "d" });
		assertEquals(4, letters.discreteSize());
		assertEquals("c", letters.getElement(2));
		assertInvariants(letters);
		
		JSDiscreteDomain digits = domains.range(0, 9);
		assertEquals(10, digits.discreteSize());
		assertEquals(5.0, digits.getElement(5));
		assertInvariants(digits);
		
		JSRealDomain unbounded = domains.real();
		assertEquals(Double.NEGATIVE_INFINITY, unbounded.getLowerBound(), 0.0);
		assertEquals(Double.POSITIVE_INFINITY, unbounded.getUpperBound(), 0.0);
		assertTrue(unbounded.contains(4.2));
		assertInvariants(unbounded);
		
		JSRealDomain unit = domains.real(0.0, 1.0);
		assertEquals(0.0, unit.getLowerBound(), 0.0);
		assertEquals(1.0, unit.getUpperBound(), 0.0);
		assertTrue(unit.contains(0.0));
		assertFalse(unit.contains(1.001));
		assertInvariants(unit);
		
		JSRealJointDomain r2 = domains.realN(2);
		assertEquals(2, r2.dimensions());
		assertEquals(unbounded, r2.getRealDomain(0));
		assertInvariants(r2);
		
		JSRealJointDomain unitCube = domains.realN(new JSRealDomain[] { unit, unit, unit });
		assertEquals(3, unitCube.dimensions());
		assertEquals(unit, unitCube.getRealDomain(1));
		assertInvariants(unitCube);
		
		expectThrow(JSException.class, "Unsupported domain type.*", domains, "wrap", ObjectDomain.instance());
	}
	
	private void assertInvariants(JSDomain<?> domain)
	{
		assertEquals(state.applet, domain.getApplet());
		assertFalse(domain.contains(domain));
		assertTrue(domain.dimensions() > 0);
		if (domain.isDiscrete())
		{
			JSDiscreteDomain discrete = (JSDiscreteDomain)domain;
			assertTrue(domain.discreteSize() > 0);
			for (int i = domain.discreteSize(); --i>=0;)
			{
				Object element = discrete.getElement(i);
				assertEquals(element, discrete.getDelegate().getElement(i));
				assertTrue(discrete.contains(element));
			}
		}
		else
		{
			assertEquals(-1, domain.discreteSize());
		}
		if (domain.isRealJoint())
		{
			JSRealJointDomain realJoint = (JSRealJointDomain)domain;
			assertTrue(realJoint.dimensions() > 1);
			for (int i = realJoint.dimensions(); --i>=0;)
			{
				assertInvariants(realJoint.getRealDomain(i));
			}
		}
		assertEquals(domain.getDomainType() == JSDomain.Type.DISCRETE, domain.isDiscrete());
		assertEquals(domain.getDomainType() == JSDomain.Type.REAL, domain.isReal());
		assertEquals(domain.getDomainType() == JSDomain.Type.REAL_JOINT, domain.isRealJoint());
		assertEquals(domain, state.domains.wrap(domain.getDelegate()));
	}
}
