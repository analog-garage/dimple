/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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
 * Deterministic not-equals function. This is a deterministic directed factor.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (Logical output = values are not all equal)
 * 2...) Arbitrary length list of values (double or integer)
 * 
 */
public class NotEquals extends FactorFunction
{
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	int index = 0;
    	final boolean indicator = arguments[index++].getBoolean();
    	
    	if (arguments.length <= 2)										// One value, must be equal
        	return !indicator ? 0 : Double.POSITIVE_INFINITY;
    	
    	boolean allEqual = true;
    	final double firstVal = arguments[index++].getDouble();
    	for (; index < arguments.length; index++)
    		if (arguments[index].getDouble() != firstVal)
    			allEqual = false;
    	
    	return (indicator != allEqual) ? 0 : Double.POSITIVE_INFINITY;
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
    	if (arguments.length <= 2)							// One value, must be equal
    	{
        	arguments[0].setBoolean(false);					// Replace the output value
        	return;
    	}
    		
    	int index = 1;
    	boolean allEqual = true;
    	final double firstVal = arguments[index++].getDouble();
    	for (; index < arguments.length; index++)
    		if (arguments[index].getDouble() != firstVal)
    			allEqual = false;
    	
    	arguments[0].setBoolean(!allEqual);		// Replace the output value
    }
}
