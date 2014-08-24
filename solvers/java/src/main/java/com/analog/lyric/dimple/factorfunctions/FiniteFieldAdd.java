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

import static java.util.Objects.*;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.FiniteFieldNumber;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Deterministic finite field (GF(2^n)) addition. This is a deterministic directed factor.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (FiniteFieldVariable; Output = input1 + input2)
 * 2) Input1 (FiniteFieldVariable)
 * 3) Input2 (FiniteFieldVariable)
 * 
 * @since 0.05
 */
public class FiniteFieldAdd extends FactorFunction
{
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	final int result = arguments[0].getInt();
    	final int input1 = arguments[1].getInt();
    	final int input2 = arguments[2].getInt();
    	
    	final int computedResult = input1 ^ input2;
    	
    	return (computedResult == result) ? 0 : Double.POSITIVE_INFINITY;
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
    	// Allow one constant input
    	final Value arg1Value = arguments[1];
    	final Value arg2Value = arguments[2];
    	final Object arg1 = requireNonNull(arg1Value.getObject());
    	final Object arg2 = requireNonNull(arg2Value.getObject());
    	final FiniteFieldNumber input1 = (arg1 instanceof FiniteFieldNumber) ? (FiniteFieldNumber)arg1 : ((FiniteFieldNumber)arg2).cloneWithNewValue(arg1Value.getInt());
    	final FiniteFieldNumber input2 = (arg2 instanceof FiniteFieldNumber) ? (FiniteFieldNumber)arg2 : ((FiniteFieldNumber)arg1).cloneWithNewValue(arg2Value.getInt());
    	arguments[0].setFiniteField(input1.cloneWithNewValue(input1.intValue() ^ input2.intValue()));		// Replace the output value
    }
}
