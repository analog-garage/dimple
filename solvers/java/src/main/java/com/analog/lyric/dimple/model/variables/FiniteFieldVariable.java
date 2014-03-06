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

package com.analog.lyric.dimple.model.variables;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.TypedDiscreteDomain;



public class FiniteFieldVariable extends Discrete
{
	public FiniteFieldVariable(int poly)
	{
		super(_getDomainFromPoly(poly), "FiniteFieldVariable");
		double [] dpoly = _convert2poly(poly);
		this.setProperty("primitivePolynomial", dpoly);
	}
	
	private static TypedDiscreteDomain<Integer> _getDomainFromPoly(int poly)
	{
		double [] dpoly = _convert2poly(poly);
		int max = (int)Math.pow(2,dpoly.length-1)-1;		
		return DiscreteDomain.range(0, max);
	}
	
	private static double [] _convert2poly(int poly)
	{
		
		double log2 = Math.log(poly)/Math.log(2);
		int polySize = (int)Math.floor(log2) + 1;
		
		double [] retval = new double[polySize];
		
		for (int i = 0; i < retval.length; i++)
		{
			if (((1 << i) & poly) != 0)
				retval[retval.length-i-1] = 1;
		}
		
		return retval;
	}
}
