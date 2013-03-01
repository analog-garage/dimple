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
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;


public class Xor extends FactorFunction 
{
	public Xor()
	{
		super("xorDelta");
	}
	
    @Override
    public double evalEnergy(Object ... arguments)
    {
    	boolean total = false;
    	for(int i = 0; i < arguments.length; ++i)
    		total ^= FactorFunctionUtilities.toBoolean(arguments[i]);

    	return !total ? 0 : Double.POSITIVE_INFINITY;
    }
    
    
    @Override
    public final boolean isDirected()	{return true;}
    @Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
    @Override
	public final boolean isDeterministicDirected() {return true;}
    @Override
	public final void evalDeterministicFunction(Object... arguments)
    {
    	boolean total = false;
    	for(int i = 1; i < arguments.length; ++i)
    		total ^= FactorFunctionUtilities.toBoolean(arguments[i]);
    	
    	arguments[0] = total;		// Replace the output value
    }
}
