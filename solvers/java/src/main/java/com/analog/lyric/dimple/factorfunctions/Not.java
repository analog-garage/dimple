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
 *  Deterministic logical NOT. This is a deterministic directed factor.
 * 
 *  The variables are ordered as follows in the argument list:
 * 
 *  1) Output (Logical NOT of input)
 *  2) Input (inputs may be boolean, double 0.0/1.0 or integer 0/1)
 * 
 */
public class Not extends FactorFunction
{
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	final boolean outValue = arguments[0].getBoolean();
    	final boolean inValue = arguments[1].getBoolean();
    	final boolean notValue = !inValue;
    	
    	return (notValue == outValue) ? 0 : Double.POSITIVE_INFINITY;
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
    	arguments[0].setBoolean(!arguments[1].getBoolean());
    }
}
