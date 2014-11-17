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

package com.analog.lyric.dimple.test.solvers.core;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableEntry;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTableIterator;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.options.SolverOptions;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Unit test for STableFactorBase
 */
public class TestSTableFactorBase extends DimpleTestBase
{
	@Test
	public void test()
	{
		FactorGraph fg = new FactorGraph();
	
		final int nBits = 40;
		Discrete[] bits = new Discrete[nBits];
		for (int i = 0; i < nBits; ++i)
		{
			bits[i] = new Bit();
			bits[i].setName("bit" + i);
		}
		fg.addVariables(bits);
		Discrete big = new Discrete(DiscreteDomain.range(0, Short.MAX_VALUE));
		
		fg.setOption(SolverOptions.maxAutomaticFactorTableSize, Integer.MAX_VALUE);
		
		Factor f2 = fg.addFactor(new Function(), bits[0], bits[1]);
		Factor f8 = fg.addFactor(new Function(), bits[0], bits[1], bits[2], bits[3], bits[4], bits[5], bits[6], bits[7]);
		Factor flarge = fg.addFactor(new Function(), bits);
		
		Discrete[] vars = new Discrete[5];
		vars[0] = big;
		System.arraycopy(bits, 0, vars, 1, 4);
		Factor sum = fg.addFactor(new Sum(), vars);
		assertTrue(sum.isDirected());
		assertTrue(sum.getFactorFunction().isDeterministicDirected());
		
		GibbsSolverGraph sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		
		STableFactorBase sf2 = requireNonNull((STableFactorBase)sfg.getSolverFactor(f2));
		STableFactorBase sf8 = requireNonNull((STableFactorBase)sfg.getSolverFactor(f8));
		STableFactorBase sflarge = requireNonNull((STableFactorBase)sfg.getSolverFactor(flarge));
		STableFactorBase ssum = requireNonNull((STableFactorBase)sfg.getSolverFactor(sum));
	
		assertNull(sf2.getFactorTableIfComputed());
		assertInvariants(sf2);
		assertNull(sf8.getFactorTableIfComputed());
		assertInvariants(sf8);
		assertNull(sflarge.getFactorTableIfComputed());
		assertInvariants(sflarge);
		assertNull(ssum.getFactorTableIfComputed());
		assertInvariants(ssum);
		
		sfg.initialize();

		assertNotNull(sf2.getFactorTableIfComputed());
		assertInvariants(sf2);
		assertNotNull(sf8.getFactorTableIfComputed());
		assertInvariants(sf8);
		assertNull(sflarge.getFactorTableIfComputed());
		assertInvariants(sflarge);
		assertNotNull(ssum.getFactorTableIfComputed());
		assertInvariants(ssum);
		
		sf2.clearFactorTable();
		assertNull(sf2.getFactorTableIfComputed());
		sf8.clearFactorTable();
		assertNull(sf8.getFactorTableIfComputed());
		
		fg.setOption(SolverOptions.maxAutomaticFactorTableSize, 0);
		
		sfg.initialize();
		
		assertNull(sf2.getFactorTableIfComputed());
		assertNull(sf8.getFactorTableIfComputed());
		assertNull(sflarge.getFactorTableIfComputed());
		
		fg.setOption(SolverOptions.maxAutomaticFactorTableSize, 100);

		sfg.initialize();

		assertNotNull(sf2.getFactorTableIfComputed());
		assertNull(sf8.getFactorTableIfComputed());
		assertNull(sflarge.getFactorTableIfComputed());
	}
	
	private void assertInvariants(STableFactorBase sfactor)
	{
		final FactorFunction function = sfactor.getFactor().getFactorFunction();
		final IFactorTable table = sfactor.getFactorTableIfComputed();
		if (table != null)
		{
			assertSame(table, sfactor.getFactorTable());
			assertSame(table, sfactor.getFactorTableIfComputed());
			
			assertTrue(Arrays.deepEquals(table.getIndicesSparseUnsafe(), sfactor.getPossibleBeliefIndices()));
			
			IFactorTableIterator iter = table.fullIterator();
			for (FactorTableEntry entry; (entry = iter.next()) != null;)
			{
				assertEquals(entry.energy(), function.evalEnergy(entry.values()), 0.0);
			}
		}
	}
	
	private static class Function extends FactorFunction
	{
		@Override
		public double evalEnergy(Value[] values)
		{
			long energy = 0L;
			for (Value value : values)
			{
				energy <<= 1;
				energy |= (value.getInt() & 1);
			}
			return energy;
		}
	}
}
