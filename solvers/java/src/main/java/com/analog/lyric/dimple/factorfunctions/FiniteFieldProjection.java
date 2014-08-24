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
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Deterministic finite field (GF(2^n)) projection, which projects one or more bits of a
 * finite field variable value onto individual bit variables. This is a deterministic directed factor.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) FiniteFieldVariable
 * 2) Constant array of bit positions to project
 * 3...) Bit variables
 * 
 * @since 0.05
 */
public class FiniteFieldProjection extends FactorFunction
{
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	int index = 0;
    	
    	final int finiteFieldValue = arguments[index++].getInt();
    	final int[] bitIndices = FactorFunctionUtilities.toIntArray(arguments[index++].getObject());
    	
    	for (int i = 0; index < arguments.length; i++, index++)
    	{
    		final int bitIndex = bitIndices[i];
    		if (((finiteFieldValue >> bitIndex) & 1) != arguments[index].getInt())
    			return Double.POSITIVE_INFINITY;
    	}
    	
    	return 0;	// All projected bits equal
    }
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public int[] getDirectedToIndices(int numEdges)
	{
		return FactorFunctionUtilities.getListOfIndices(2, numEdges-1);
    }
    @Override
	public final boolean isDeterministicDirected() {return true;}
    
    
    @Override
	public final void evalDeterministic(Value[] arguments)
    {
    	int index = 0;
    	
    	final int finiteFieldValue = arguments[index++].getInt();
    	final int[] bitIndices = FactorFunctionUtilities.toIntArray(arguments[index++].getObject());
    	
    	for (int i = 0; index < arguments.length; i++, index++)
    	{
    		final int bitIndex = bitIndices[i];
    		arguments[index].setInt((finiteFieldValue >> bitIndex) & 1);		// Replace output values
    	}
    }
}
