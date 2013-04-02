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
import com.analog.lyric.dimple.model.DimpleException;


/**
 * Factor for an exchangeable set of Normally distributed variables associated
 * with a variable representing the mean parameter and a second parameter
 * representing the precision. The variables are ordered as follows in
 * the argument list:
 * 
 * 1) Mean parameter (real variable)
 * 2) Precision parameter (inverse variance) (real variable; domain must be non-negative)
 * 3...) An arbitrary number of real variables.
 * 
 * Mean and precision parameters may optionally be specified as constants in the constructor.
 * In this case, the mean and precision are not included in the list of arguments.
 * 
 */
public class Normal extends FactorFunction
{
	double _mean;
	double _precisions;
	double _logPrecisionOverTwo;
	boolean _meanConstant = false;
	boolean _precisionConstant = false;
	int _firstDirectedToIndex = 2;

	public Normal() {super();}
	public Normal(double mean, double precision)
	{
		this();
		_mean = mean;
		_meanConstant = true;
		_precisions = precision;
		_logPrecisionOverTwo = Math.log(_precisions)*0.5;
		_precisionConstant = true;
		_firstDirectedToIndex = 0;
    	if (_precisions < 0) throw new DimpleException("Negative precision value. This must be a non-negative value.");
	}

    @Override
	public double evalEnergy(Object ... arguments)
	{
    	int index = 0;
    	if (!_meanConstant)
    		_mean = FactorFunctionUtilities.toDouble(arguments[index++]);				// First variable is mean parameter
    	if (!_precisionConstant)
    	{
    		_precisions = FactorFunctionUtilities.toDouble(arguments[index++]);			// Second variable is precision (must be non-negative)
    		if (_precisions < 0) throw new DimpleException("Negative precision value. Domain must be restricted to non-negative values.");
    		_logPrecisionOverTwo = Math.log(_precisions)*0.5;
    	}
    	int length = arguments.length;
    	int N = length - index;			// Number of Normal variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		double relInput = FactorFunctionUtilities.toDouble(arguments[index]) - _mean;	// Remaining inputs are Normal variables
    		sum += relInput*relInput*_precisions;
    	}

    	return sum*0.5 - N * _logPrecisionOverTwo;
	}
    
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices(int numEdges)
	{
    	// All edges except the parameter edges (if present) are directed-to edges
    	if (numEdges <= _firstDirectedToIndex)
    		throw new DimpleException("Insufficient number of edges");
    	int numDirectedToEdges = numEdges - _firstDirectedToIndex;
    	int[] directedToEdges = new int[numDirectedToEdges];
    	for (int i = 0, edgeIndex = _firstDirectedToIndex; i < numDirectedToEdges; i++, edgeIndex++)
    		directedToEdges[i] = edgeIndex;
		return directedToEdges;
	}
}
