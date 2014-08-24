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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.FiniteFieldNumber;
import com.analog.lyric.dimple.model.values.FiniteFieldValue;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Deterministic finite field (GF(2^n)) multiplication. This is a deterministic directed factor.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (FiniteFieldVariable; Output = input1 * input2)
 * 2) Input1 (FiniteFieldVariable)
 * 3) Input2 (FiniteFieldVariable)
 * 
 * @since 0.05
 */
public class FiniteFieldMult extends FactorFunction
{
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	// Allow one constant input
    	final FiniteFieldNumber result = requireNonNull((FiniteFieldNumber)arguments[0].getObject());
    	final Value arg1 = arguments[1];
    	final FiniteFieldNumber input1 = (arg1 instanceof FiniteFieldValue) ? arg1.getFiniteField() : result.cloneWithNewValue(arg1.getInt());
    	final Value arg2 = arguments[2];
    	final FiniteFieldNumber input2 = (arg2 instanceof FiniteFieldValue) ? arg2.getFiniteField() : result.cloneWithNewValue(arg2.getInt());
    	
    	if (!result.isCompatible(input1) || !result.isCompatible(input2))
    		throw new DimpleException("Primitive polynomials must match.");
    	
    	final FiniteFieldNumber computedResult = finiteFieldProduct(input1, input2);
    	
    	return (computedResult.isEqual(result)) ? 0 : Double.POSITIVE_INFINITY;
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
    	if (!input1.isCompatible(input2))
    		throw new DimpleException("Primitive polynomials must match.");
    	arguments[0].setFiniteField(finiteFieldProduct(input1, input2));		// Replace the output value
    }
    
    
    private final FiniteFieldNumber finiteFieldProduct(FiniteFieldNumber input1, FiniteFieldNumber input2)
    {
    	int x = input1.intValue();
    	int y = input2.intValue();
    	int n = input1.getN();
    	int prim_poly = input1.getPrimativePolynomial();
    	int z=0;
    	
    	/* Convolve x and y as bit strings mod 2 */
    	for (int i = 0; i < n; i++)
    		if ((1 & (x>>i)) == 1)
    			z ^= (y<<i);
    	
    	/* Take any "extra" bits located at bit n or higher and fold it back down */
    	for (int i = 2*n; i>=n; i--)
    		if ((1 & (z>>i)) == 1)
    			z ^= (prim_poly<<(i-n));
    	
    	return input1.cloneWithNewValue(z);
    }
}
