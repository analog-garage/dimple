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
import com.analog.lyric.dimple.test.DimpleTestBase;

public class TestPObject extends DimpleTestBase
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
