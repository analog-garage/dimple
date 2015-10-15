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

package com.analog.lyric.dimple.test.matlabproxy;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.matlabproxy.ModelFactory;
import com.analog.lyric.dimple.matlabproxy.PCustomFactors;
import com.analog.lyric.dimple.solvers.core.CustomFactors;
import com.analog.lyric.dimple.solvers.core.ISolverFactorCreator;
import com.analog.lyric.dimple.solvers.gibbs.GibbsCustomFactors;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomBeta;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomGamma;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomNormal;
import com.analog.lyric.dimple.solvers.gibbs.customFactors.CustomPoisson;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Unit test for {@link PCustomFactors}
 * @since 0.08
 * @author Christopher Barber
 */
public class TestPCustomFactors extends DimpleTestBase
{
	@Test
	public void test()
	{
		ModelFactory mf = new ModelFactory();
		
		PCustomFactors pcf = mf.createCustomFactors("GibbsOptions.customFactors");
		CustomFactors<ISolverFactor,ISolverFactorGraph> cf = pcf.getDelegate();
		assertSame(GibbsCustomFactors.class, cf.getClass());
		assertTrue(cf.isMutable());
		assertTrue(cf.keySet().isEmpty());
		
		assertEquals(0, cf.get("xxx").size());
		pcf.add(false, "xxx", "CustomNormal");
		List<ISolverFactorCreator<ISolverFactor,ISolverFactorGraph>> cfactors = cf.get("xxx");
		assertEquals(1, cfactors.size());
		assertEquals(CustomNormal.class.getName(), cfactors.get(0).toString());
		
		pcf.add(false, "xxx", "CustomBeta");
		cfactors = cf.get("xxx");
		assertEquals(2, cfactors.size());
		assertEquals(CustomBeta.class.getName(), cfactors.get(1).toString());
		
		pcf.add(true, "xxx", "CustomGamma");
		cfactors = cf.get("xxx");
		assertEquals(3, cfactors.size());
		assertEquals(CustomGamma.class.getName(), cfactors.get(0).toString());
		
		pcf.add(false, new String[][] {
			new String[] { "yyy", "CustomBeta" },
			new String[] { "yyy", "CustomGamma" },
			new String[] { "zzz", "CustomNormal" }
		});
		
		cfactors = cf.get("yyy");
		assertEquals(2, cfactors.size());
		assertEquals(CustomBeta.class.getName(), cfactors.get(0).toString());
		assertEquals(CustomGamma.class.getName(), cfactors.get(1).toString());
		cfactors = cf.get("zzz");
		assertEquals(1, cfactors.size());
		assertEquals(CustomNormal.class.getName(), cfactors.get(0).toString());
		
		pcf.add(true, new String[][] {
			new String[] { "yyy", "CustomNormal" },
			new String[] { "yyy", "CustomPoisson" },
			new String[] { "zzz", "CustomBeta" }
		});
		cfactors = cf.get("yyy");
		assertEquals(4, cfactors.size());
		assertEquals(CustomNormal.class.getName(), cfactors.get(0).toString());
		assertEquals(CustomPoisson.class.getName(), cfactors.get(1).toString());
		assertEquals(CustomBeta.class.getName(), cfactors.get(2).toString());
		assertEquals(CustomGamma.class.getName(), cfactors.get(3).toString());
		cfactors = cf.get("zzz");
		assertEquals(2, cfactors.size());
		assertEquals(CustomBeta.class.getName(), cfactors.get(0).toString());
		assertEquals(CustomNormal.class.getName(), cfactors.get(1).toString());
		
		pcf.addBuiltins();
		cfactors = cf.get(Normal.class.getName());
		assertEquals(1, cfactors.size());
		assertEquals(CustomNormal.class.getName(), cfactors.get(0).toString());
	}
}
