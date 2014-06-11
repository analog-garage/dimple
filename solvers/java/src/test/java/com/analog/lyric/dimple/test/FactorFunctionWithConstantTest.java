/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;


public class FactorFunctionWithConstantTest
{
	protected static boolean debugPrint = true;
	
	
	// Test getting the list of directed edges from a factor-function with constants
	// This is a bit tricky since the list must exclude constant entries and renumber the remaining ones
	// to match the numbering of the remaining non-constant variable edges
	@Test
	public void test1()
	{
		int numBits = 10;
		Bit[] b = new Bit[numBits];
		for (int i = 0; i < numBits; i++) b[i] = new Bit();
		
		FactorGraph fg = new FactorGraph();
		Factor f1 = fg.addFactor(new FF1(), b);
		Factor f2 = fg.addFactor(new FF1(), b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], b[9]);
		Factor f3 = fg.addFactor(new FF1(), 0, b[1], b[2], b[3], b[4], b[5], 0, b[7], b[8], b[9]);
		Factor f4 = fg.addFactor(new FF1(), b[0], 0, b[2], 0, b[4], 0, 0, 0, b[8], b[9]);
		Factor f5 = fg.addFactor(new FF1(), b[0], b[1], b[2], 0, 0, b[5], b[6], b[7], b[8], b[9]);
		Factor f6 = fg.addFactor(new FF1(), b[0], b[1], b[2], 0, 0, b[5], b[6], b[7], b[8], 0);
		
		fg.initialize();
		
		int[] d1 = f1.getDirectedTo();
		int[] d2 = f2.getDirectedTo();
		int[] d3 = f3.getDirectedTo();
		int[] d4 = f4.getDirectedTo();
		int[] d5 = f5.getDirectedTo();
		int[] d6 = f6.getDirectedTo();
		
		assertTrue(Arrays.equals(d1, new int[]{1, 3, 5, 6, 7}));
		assertTrue(Arrays.equals(d2, new int[]{1, 3, 5, 6, 7}));
		assertTrue(Arrays.equals(d3, new int[]{0, 2, 4, 5}));
		assertTrue(Arrays.equals(d4, new int[]{}));
		assertTrue(Arrays.equals(d5, new int[]{1, 3, 4, 5}));
		assertTrue(Arrays.equals(d6, new int[]{1, 3, 4, 5}));
	}
	
	public static class FF1 extends FactorFunction
	{
		@Override
	    public double evalEnergy(Object ... input)
	    {
	    	return 0;
	    }
	    
	    @Override
		public boolean isDirected() {return true;}
	    
		@Override
		public int[] getDirectedToIndices(int numEdges)
		{
			return new int[]{1, 3, 5, 6, 7};
		}
	}

}
