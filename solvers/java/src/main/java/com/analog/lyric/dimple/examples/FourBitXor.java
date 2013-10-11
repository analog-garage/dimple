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

package com.analog.lyric.dimple.examples;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Bit;

public class FourBitXor 
{

	public static void main(String[] args) 
	{
		
		FactorGraph xorGraph = new FactorGraph();
		Bit [] b = new Bit[4];
		for (int i = 0; i < b.length; i++)
			b[i] = new Bit();
		Bit c = new Bit();
		
		ThreeBitXor xd = new ThreeBitXor();
		
		xorGraph.addFactor(xd,b[0],b[1],c); 
		xorGraph.addFactor(xd,b[2],b[3],c);
		
		double [] inputs = new double [] {.8, .8, .8, .5};
		for (int i = 0; i < inputs.length; i++)
			b[i].setInput(inputs[i]);
		
		xorGraph.solve();
		
		for (int i = 0; i < b.length; i++)
			System.out.println(b[i].getP1());
		for (int i = 0; i < b.length; i++)
			System.out.println(b[i].getValue());

	}

}
