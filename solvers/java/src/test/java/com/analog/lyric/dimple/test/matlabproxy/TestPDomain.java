package com.analog.lyric.dimple.test.matlabproxy;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.matlabproxy.PDiscreteDomain;
import com.analog.lyric.dimple.matlabproxy.PDomain;
import com.analog.lyric.dimple.matlabproxy.PHelpers;
import com.analog.lyric.dimple.matlabproxy.PRealDomain;
import com.analog.lyric.dimple.matlabproxy.PRealJointDomain;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.RealJointDomain;

public class TestPDomain
{

	@Test
	public void test()
	{
		DiscreteDomain abc = DiscreteDomain.create('a','b','c');
		PDiscreteDomain pabc = new PDiscreteDomain(abc);
		assertInvariants(pabc);
		assertSame(abc, pabc.getModelerObject());
		
		PDomain pabc2 = PHelpers.wrapDomain(abc);
		assertInvariants(pabc2);
		assertTrue(pabc2.isDiscrete());
		assertSame(abc, pabc2.getModelerObject());
		
		RealDomain r = new RealDomain(1.23, 3.1415);
		PRealDomain pr = new PRealDomain(r);
		assertInvariants(pr);
		assertSame(r, pr.getModelerObject());
		
		RealDomain r2 = RealDomain.unbounded();
		assertInvariants(new PRealDomain(r2));
		
		PDomain pr2 = PHelpers.wrapDomain(r2);
		assertInvariants(pr2);
		assertTrue(pr2.isReal());
		assertSame(r2, pr2.getModelerObject());
		
		RealDomain r3 = new RealDomain(0.0, 1.0);
		Object pr3 = new PRealDomain(r3);
		assertInvariants((PDomain)pr3);
		
		RealJointDomain rj = RealJointDomain.create(r, r2, r3);
		PRealJointDomain prj = new PRealJointDomain(rj);
		assertInvariants(prj);
		assertSame(rj, prj.getModelerObject());
		
		PDomain prj2 = PHelpers.wrapDomain(rj);
		assertInvariants(prj2);
		assertSame(rj, prj2.getModelerObject());
		
		Object[] prs = new Object[] {pr, pr2, pr3};
		PRealJointDomain prj3 = new PRealJointDomain(prs);
		assertInvariants(prj3);
		assertEquals(prs.length, prj3.getNumVars());
		
		for (int i = 0; i < prs.length; ++i)
		{
			RealDomain expected = ((PRealDomain)prs[i]).getModelerObject();
			RealDomain actual = prj3.getModelerObject().getRealDomains()[i];
			assertEquals(expected, actual);
		}
	}

	static public void assertInvariants(PDomain pdomain)
	{
		TestPObject.assertInvariants(pdomain);
		Domain mdomain = pdomain.getModelerObject();
		assertNotNull(mdomain);
		assertEquals(pdomain.isDiscrete(), mdomain.isDiscrete());
		assertEquals(pdomain.isJoint(), mdomain.isRealJoint());
		assertEquals(pdomain.isReal(), mdomain.isReal());
		
		if (pdomain.isDiscrete())
		{
			assertDiscreteInvariants((PDiscreteDomain)pdomain);
		}
		if (pdomain.isJoint())
		{
			assertRealJointInvariants((PRealJointDomain)pdomain);
		}
		if (pdomain.isReal())
		{
			assertRealInvariants((PRealDomain)pdomain);
		}
	}
	
	static private void assertDiscreteInvariants(PDiscreteDomain pdiscrete)
	{
		DiscreteDomain mdiscrete = pdiscrete.getModelerObject();
		assertArrayEquals(mdiscrete.getElements(), pdiscrete.getElements());
	}
	
	static private void assertRealInvariants(PRealDomain preal)
	{
		RealDomain mreal = preal.getModelerObject();
		assertEquals(mreal.getUpperBound(), preal.getUpperBound(), 0.0);
		assertEquals(mreal.getLowerBound(), preal.getLowerBound(), 0.0);
	}
	
	static private void assertRealJointInvariants(PRealJointDomain pjoint)
	{
		RealJointDomain mjoint = pjoint.getModelerObject();
		assertEquals(mjoint.getNumVars(), pjoint.getNumVars());
	}
}
