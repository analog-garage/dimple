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

package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Domain;

public class EqualDelta extends FactorFunction 
{
	public EqualDelta()
	{
		super("EqualDelta");
	}
    public double eval(Object ... input)
    {
    	if (input.length == 0)
    		return 1;
    	
    	Object firstVal = input[0];
    	
    	for (int i = 1; i < input.length; i++)
    		if (input[i] != firstVal)
    			return 0;
    	
    	return 1;
    }
    
    
    public FactorTable getFactorTable(Domain [] domainList)
    {
    	boolean allsame = true;
    	DiscreteDomain first = null;
    	DiscreteDomain [] discreteDomains = new DiscreteDomain[domainList.length];
    	
    	for (int i = 0; i < domainList.length; i++)
    	{
    		if (i == 0)
    		{
    			if (!domainList[0].isDiscrete())
    				throw new DimpleException("must be discrete");
    			
    			first = (DiscreteDomain)domainList[0];
    		}
    		else
    		{
    			if (!first.equals(domainList[i]))
    			{
    				allsame = false;
    				break;
    			}
    		}
    		
    		discreteDomains[i] = (DiscreteDomain)domainList[i];
    		
    	}
    	
    	if (!allsame)
    		return super.getFactorTable(domainList);
    	
    	
    	int [][] indices = new int[first.getElements().length][];
    	double [] probs = new double[indices.length];
    	
    	for (int i = 0; i < indices.length; i++)
    	{
    		indices[i] = new int[domainList.length];
    		
    		for (int j = 0; j < indices[i].length; j++)
    		{
    			indices[i][j] = i;
    			probs[i] = 1;
    		}
    	}
    	
    	return new FactorTable(indices,probs, discreteDomains);
    }

}
