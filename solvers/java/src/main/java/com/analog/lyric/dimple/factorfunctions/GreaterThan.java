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

package com.analog.lyric.dimple.factorfunctions;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Deterministic greater-than function. This is a deterministic directed factor.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (Logical output = FirstValue &gt; SecondValue)
 * 2) FirstValue (double or integer)
 * 3) SecondValue (double or integer)
 * 
 */
public class GreaterThan extends FactorFunction
{
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	final boolean indicator = arguments[0].getBoolean();
    	final double firstVal = arguments[1].getDouble();
    	final double secondVal = arguments[2].getDouble();
    	
    	if (indicator == (firstVal > secondVal))
    		return 0;
    	else
    		return Double.POSITIVE_INFINITY;
    }

    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
    @Override
	public final boolean isDeterministicDirected() {return true;}
    @Override
	public final void evalDeterministic(Value[] arguments)
    {
    	final double firstVal = arguments[1].getDouble();
    	final double secondVal = arguments[2].getDouble();
    	
    	arguments[0].setBoolean(firstVal > secondVal);		// Replace the output value
    }
}
