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
 * Log-normal distribution. The variables in the argument list are ordered as follows:
 * 
 * 1) Mean parameter
 * 2) Precision parameter (inverse variance) (non-negative)
 * 3...) An arbitrary number of real variables
 * 
 * Mean and precision parameters may optionally be specified as constants in the constructor.
 * In this case, the mean and precision are not included in the list of arguments.
 * 
 */
public class LogNormal extends FactorFunction
{
	double _mean;
	double _precision;
	double _logPrecisionOverTwo;
	double _precisionOverTwo;
	boolean _parametersConstant = false;
	int _firstDirectedToIndex = 2;

	public LogNormal() {super();}
	public LogNormal(double mean, double precision)
	{
		this();
		_mean = mean;
		_precision = precision;
		_logPrecisionOverTwo = Math.log(_precision)*0.5;
		_precisionOverTwo = _precision*0.5;
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
    	if (_precision < 0) throw new DimpleException("Negative precision value. This must be a non-negative value.");
	}
	
    @Override
	public double evalEnergy(Object... arguments)
    {
    	int index = 0;
    	if (!_parametersConstant)
    	{
    		_mean = FactorFunctionUtilities.toDouble(arguments[index++]);				// First variable is mean parameter
    		_precision = FactorFunctionUtilities.toDouble(arguments[index++]);			// Second variable is precision (must be non-negative)
    		_logPrecisionOverTwo = Math.log(_precision)*0.5;
    		_precisionOverTwo = _precision*0.5;
    		if (_precision < 0) throw new DimpleException("Negative precision value. Domain must be restricted to non-negative values.");
    	}
    	int length = arguments.length;
    	int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		double x = FactorFunctionUtilities.toDouble(arguments[index]);				// Remaining inputs are LogNormal variables
        	if (x <= 0)
        		return Double.POSITIVE_INFINITY;
        	else
        	{
        		double logX = Math.log(x);
        		double relLogX = logX - _mean;
        		sum += logX + relLogX*relLogX*_precisionOverTwo;
        	}
    	}
    	return sum - N * _logPrecisionOverTwo;
	}
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices(int numEdges)
	{
    	// All edges except the parameter edges (if present) are directed-to edges
		return FactorFunctionUtilities.getListOfIndices(_firstDirectedToIndex, numEdges-1);
	}
}
