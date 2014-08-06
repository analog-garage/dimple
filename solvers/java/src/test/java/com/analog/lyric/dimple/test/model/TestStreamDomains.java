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

package com.analog.lyric.dimple.test.model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.model.domains.ComplexDomain;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.repeated.BitStream;
import com.analog.lyric.dimple.model.repeated.ComplexStream;
import com.analog.lyric.dimple.model.repeated.DiscreteStream;
import com.analog.lyric.dimple.model.repeated.RealJointStream;
import com.analog.lyric.dimple.model.repeated.RealStream;

public class TestStreamDomains
{
	
	@SuppressWarnings("null")
	@Test
	public void test()
	{
		BitStream b = new BitStream();
		assertArrayEquals(new Object[] {0, 1}, b.getDomain().asDiscrete().getElements());
		
		Object[] dList = new Object[] {0, 1, 2, 4};
		DiscreteDomain dDomain = DiscreteDomain.create(dList);
		DiscreteStream d = new DiscreteStream(dList);
		DiscreteStream dd = new DiscreteStream(dDomain);
		assertArrayEquals(dList, d.getDomain().asDiscrete().getElements());
		assertArrayEquals(dList, dd.getDomain().asDiscrete().getElements());
		
		double L = -7;
		double U = 12.2;
		RealDomain rInfDomain = RealDomain.create();
		RealDomain rDomain = RealDomain.create(L, U);
		RealStream r = new RealStream();
		RealStream ri = new RealStream(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		RealStream rid = new RealStream(rInfDomain);
		RealStream rr = new RealStream(L, U);
		RealStream rrd = new RealStream(rDomain);
		assertEquals(r.getDomain().getLowerBound(), Double.NEGATIVE_INFINITY, 0);
		assertEquals(r.getDomain().getUpperBound(), Double.POSITIVE_INFINITY, 0);
		assertEquals(ri.getDomain().getLowerBound(), Double.NEGATIVE_INFINITY, 0);
		assertEquals(ri.getDomain().getUpperBound(), Double.POSITIVE_INFINITY, 0);
		assertEquals(rid.getDomain().getLowerBound(), Double.NEGATIVE_INFINITY, 0);
		assertEquals(rid.getDomain().getUpperBound(), Double.POSITIVE_INFINITY, 0);
		assertEquals(rr.getDomain().getLowerBound(), L, 0);
		assertEquals(rr.getDomain().getUpperBound(), U, 0);
		assertEquals(rrd.getDomain().getLowerBound(), L, 0);
		assertEquals(rrd.getDomain().getUpperBound(), U, 0);
		
		double L2 = 7.2;
		double U2 = 27;
		RealDomain rDomain2 = RealDomain.create(L2, U2);
		ComplexDomain cDomain = ComplexDomain.create(rDomain, rDomain2);
		ComplexStream c = new ComplexStream();
		ComplexStream cd = new ComplexStream(cDomain);
		assertEquals(c.getDomain().getDimensions(), 2);
		assertEquals(cd.getDomain().getDimensions(), 2);
		assertEquals(c.getDomain().getRealDomain(0).getLowerBound(), Double.NEGATIVE_INFINITY, 0);
		assertEquals(c.getDomain().getRealDomain(0).getUpperBound(), Double.POSITIVE_INFINITY, 0);
		assertEquals(c.getDomain().getRealDomain(1).getLowerBound(), Double.NEGATIVE_INFINITY, 0);
		assertEquals(c.getDomain().getRealDomain(1).getUpperBound(), Double.POSITIVE_INFINITY, 0);
		assertEquals(cd.getDomain().getRealDomain(0).getLowerBound(), L, 0);
		assertEquals(cd.getDomain().getRealDomain(0).getUpperBound(), U, 0);
		assertEquals(cd.getDomain().getRealDomain(1).getLowerBound(), L2, 0);
		assertEquals(cd.getDomain().getRealDomain(1).getUpperBound(), U2, 0);
		
		double L3 = Double.NEGATIVE_INFINITY;
		double U3 = 0;
		RealDomain rDomain3 = RealDomain.create(L3, U3);
		RealJointDomain jDomain = RealJointDomain.create(rDomain, rDomain2, rDomain3);
		RealJointStream j = new RealJointStream(3);
		RealJointStream jd = new RealJointStream(jDomain);
		assertEquals(j.getDomain().getDimensions(), 3);
		assertEquals(jd.getDomain().getDimensions(), 3);
		assertEquals(j.getDomain().getRealDomain(0).getLowerBound(), Double.NEGATIVE_INFINITY, 0);
		assertEquals(j.getDomain().getRealDomain(0).getUpperBound(), Double.POSITIVE_INFINITY, 0);
		assertEquals(j.getDomain().getRealDomain(1).getLowerBound(), Double.NEGATIVE_INFINITY, 0);
		assertEquals(j.getDomain().getRealDomain(1).getUpperBound(), Double.POSITIVE_INFINITY, 0);
		assertEquals(j.getDomain().getRealDomain(2).getLowerBound(), Double.NEGATIVE_INFINITY, 0);
		assertEquals(j.getDomain().getRealDomain(2).getUpperBound(), Double.POSITIVE_INFINITY, 0);
		assertEquals(jd.getDomain().getRealDomain(0).getLowerBound(), L, 0);
		assertEquals(jd.getDomain().getRealDomain(0).getUpperBound(), U, 0);
		assertEquals(jd.getDomain().getRealDomain(1).getLowerBound(), L2, 0);
		assertEquals(jd.getDomain().getRealDomain(1).getUpperBound(), U2, 0);
		assertEquals(jd.getDomain().getRealDomain(2).getLowerBound(), L3, 0);
		assertEquals(jd.getDomain().getRealDomain(2).getUpperBound(), U3, 0);
	}
	

}
