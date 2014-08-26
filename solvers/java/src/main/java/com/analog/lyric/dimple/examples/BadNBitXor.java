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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;

public class BadNBitXor
{

	@NonNullByDefault
	public static class BadNBitXorFactor extends FactorFunction
	{
		@Override
		public final double evalEnergy(Value[] args)
		{
			int sum = 0;
			for (int i = 0; i < args.length; i++)
				sum += args[i].getInt();
			
			return (sum % 2) == 0 ? 0 : Double.POSITIVE_INFINITY;
		}
	}
	
	public static void main(String[] args)
	{
		FactorGraph fourBitXor = new FactorGraph();
		DiscreteDomain domain = DiscreteDomain.bit();
		Discrete b1 = new Discrete(domain);
		Discrete b2 = new Discrete(domain);
		Discrete b3 = new Discrete(domain);
		Discrete b4 = new Discrete(domain);
		fourBitXor.addFactor(new BadNBitXorFactor(),b1,b2,b3,b4);
		b1.setInput(0.2,0.8);
		b2.setInput(0.2,0.8);
		b3.setInput(0.2,0.8);
		b4.setInput(0.5,0.5);
		fourBitXor.solve();
		System.out.println(b4.getValue());
		System.out.println(Arrays.toString(b4.getBelief()));
	}

}
