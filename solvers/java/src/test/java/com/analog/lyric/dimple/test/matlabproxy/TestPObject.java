package com.analog.lyric.dimple.test.matlabproxy;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.matlabproxy.PDomain;
import com.analog.lyric.dimple.matlabproxy.PFactorFunction;
import com.analog.lyric.dimple.matlabproxy.PFactorGraphVector;
import com.analog.lyric.dimple.matlabproxy.PFactorTable;
import com.analog.lyric.dimple.matlabproxy.PFactorVector;
import com.analog.lyric.dimple.matlabproxy.PNodeVector;
import com.analog.lyric.dimple.matlabproxy.PObject;
import com.analog.lyric.dimple.matlabproxy.PVariableVector;

public class TestPObject
{

	@Test
	public void test()
	{
		// Test defaults
		PObject obj = new PObject() {};
		assertInvariants(obj);
		assertNull(obj.getModelerObject());
		assertFalse(obj.isDiscrete());
		assertFalse(obj.isDomain());
		assertFalse(obj.isGraph());
		assertFalse(obj.isFactor());
		assertFalse(obj.isFactorFunction());
		assertFalse(obj.isFactorTable());
		assertFalse(obj.isJoint());
		assertFalse(obj.isReal());
		assertFalse(obj.isVariable());
		assertFalse(obj.isVector());
	}

	public static void assertInvariants(PObject obj)
	{
		assertEquals(obj.isDomain(), obj instanceof PDomain);
		assertEquals(obj.isGraph(), obj instanceof PFactorGraphVector);
		assertEquals(obj.isFactor(), obj instanceof PFactorVector);
		assertEquals(obj.isFactorFunction(), obj instanceof PFactorFunction);
		assertEquals(obj.isFactorTable(), obj instanceof PFactorTable);
		assertEquals(obj.isVariable(), obj instanceof PVariableVector);
		assertEquals(obj.isVector(), obj instanceof PNodeVector);

		int nVariableTypes = 0;
		nVariableTypes += obj.isDiscrete() ? 1 : 0;
		nVariableTypes += obj.isJoint() ? 1 : 0;
		nVariableTypes += obj.isReal() ? 1 : 0;
		assertTrue(nVariableTypes == 0 || nVariableTypes == 1);
	}
}
