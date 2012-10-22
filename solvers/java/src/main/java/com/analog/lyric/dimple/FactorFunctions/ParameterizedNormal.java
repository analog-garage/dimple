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
import com.analog.lyric.dimple.model.DimpleException;


/**
 * @author jeffb
 * 
 *         Factor for an exchangeable set of Normally distributed variables
 *         associated with a variable representing the mean parameter and a
 *         second parameter representing the inverse variance. The variables are
 *         ordered as follows in the argument list:
 * 
 *         1) Mean parameter (real variable)
 *         2) Inverse variance parameter (real variable; domain must be non-negative)
 *         3...) An arbitrary number of real variables.
 */
public class ParameterizedNormal extends FactorFunction
{
	public ParameterizedNormal() {super("ParameterizedNormal");}
	
    @Override
	public double evalEnergy(Object ... input)
	{
    	int index = 0;
    	double mean = (Double)input[index++];				// First variable is mean parameter
    	double inverseVariance = (Double)input[index++];	// Second variable is inverse variance (must be non-negative)
    	if (inverseVariance < 0) throw new DimpleException("Negative inverse variance value. Domain must be restricted to non-negative values.");
    	int length = input.length;
    	int N = length - 2;									// Number of Normal variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		double relInput = (Double)input[index] - mean;	// Remaining inputs are Normal variables
    		sum += relInput*relInput*inverseVariance;
    	}

    	return sum/2 - ((double)N/(double)2) * Math.log(inverseVariance);
	}
}
