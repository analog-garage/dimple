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

import cern.jet.math.Bessel;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.DimpleException;


/**
 * von Mises distribution. The variables in the argument list are ordered as follows:
 * 
 * 1) Mean parameter
 * 2) Precision parameter (inverse variance) (non-negative)
 * 3...) An arbitrary number of real variables, each must be between -PI and PI
 * 
 * Mean and precision parameters may optionally be specified as constants in the constructor.
 * In this case, the mean and precision are not included in the list of arguments.
 * 
 */
public class VonMises extends FactorFunction
{
	protected double _mean;
	protected double _precision;
	protected double _log2piBesseli0Precision;
	protected boolean _parametersConstant = false;
	protected int _firstDirectedToIndex = 2;
	protected static final double PI = Math.PI;
	protected static final double MINUS_PI = -Math.PI;
	protected static final double _log2pi = Math.log(2*Math.PI);

	public VonMises() {super();}
	public VonMises(double mean, double precision)
	{
		this();
		_mean = mean;
		_precision = precision;
		_log2piBesseli0Precision = Math.log(Bessel.i0(_precision)) + _log2pi;
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
    		_log2piBesseli0Precision = Math.log(Bessel.i0(_precision)) + _log2pi;
    		if (_precision < 0) throw new DimpleException("Negative precision value. Domain must be restricted to non-negative values.");
    	}
    	int length = arguments.length;
    	int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		double x = FactorFunctionUtilities.toDouble(arguments[index]);				// Remaining inputs are VonMises variables
    		if (x < MINUS_PI || x > PI)
    			return Double.POSITIVE_INFINITY;
        	sum -= Math.cos(x - _mean);
    	}
    	return sum * _precision + N * _log2piBesseli0Precision;
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
