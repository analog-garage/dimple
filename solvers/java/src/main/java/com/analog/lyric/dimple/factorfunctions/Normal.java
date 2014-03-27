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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;


/**
 * Factor for an exchangeable set of Normally distributed variables associated
 * with a variable representing the mean parameter and a second parameter
 * representing the precision. The variables are ordered as follows in
 * the argument list:
 * 
 * 1) Mean parameter (real variable)
 * 2) Precision parameter (inverse variance) (real variable; domain must be non-negative)
 * 3...) An arbitrary number of real variables
 * 
 * Mean and precision parameters may optionally be specified as constants in the constructor.
 * In this case, the mean and precision are not included in the list of arguments.
 * 
 */
public class Normal extends FactorFunction
{
	protected double _mean;
	protected double _precision;
	protected double _logSqrtPrecisionOver2Pi;
	protected double _precisionOverTwo;
	protected boolean _parametersConstant = false;
	protected int _firstDirectedToIndex = 2;
	protected static final double _logSqrt2pi = Math.log(2*Math.PI)*0.5;

	public Normal() {super();}
	public Normal(double mean, double precision)
	{
		this();
		_mean = mean;
		_precision = precision;
		_logSqrtPrecisionOver2Pi = Math.log(_precision)*0.5 - _logSqrt2pi;
		_precisionOverTwo = _precision*0.5;
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
    	if (_precision < 0)
    		throw new DimpleException("Negative precision value. This must be a non-negative value.");
	}
	/**
	 * @since 0.05
	 */
	public Normal(NormalParameters parameters)
	{
		this(parameters.getMean(), parameters.getPrecision());
	}

    @Override
	public double evalEnergy(Object ... arguments)
	{
    	int index = 0;
    	if (!_parametersConstant)
    	{
    		_mean = FactorFunctionUtilities.toDouble(arguments[index++]);				// First variable is mean parameter
    		_precision = FactorFunctionUtilities.toDouble(arguments[index++]);			// Second variable is precision (must be non-negative)
    		_logSqrtPrecisionOver2Pi = Math.log(_precision)*0.5 - _logSqrt2pi;
    		_precisionOverTwo = _precision*0.5;
    		if (_precision < 0) return Double.POSITIVE_INFINITY;
    	}
    	int length = arguments.length;
    	int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		double relInput = FactorFunctionUtilities.toDouble(arguments[index]) - _mean;	// Remaining inputs are Normal variables
    		sum += relInput*relInput;
    	}
    	return sum * _precisionOverTwo - N * _logSqrtPrecisionOver2Pi;
	}
    
    @Override
	public double evalEnergy(Value[] values)
	{
    	int index = 0;
    	if (!_parametersConstant)
    	{
    		_mean = values[index++].getDouble();				// First variable is mean parameter
    		_precision = values[index++].getDouble();			// Second variable is precision (must be non-negative)
    		_logSqrtPrecisionOver2Pi = Math.log(_precision)*0.5 - _logSqrt2pi;
    		_precisionOverTwo = _precision*0.5;
    		if (_precision < 0) return Double.POSITIVE_INFINITY;
    	}
    	int length = values.length;
    	int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		double relInput = values[index].getDouble() - _mean;	// Remaining inputs are Normal variables
    		sum += relInput*relInput;
    	}
    	return sum * _precisionOverTwo - N * _logSqrtPrecisionOver2Pi;
	}
    
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices(int numEdges)
	{
    	// All edges except the parameter edges (if present) are directed-to edges
		return FactorFunctionUtilities.getListOfIndices(_firstDirectedToIndex, numEdges-1);
	}
    
    
    // Factor-specific methods
    public final boolean hasConstantParameters()
    {
    	return _parametersConstant;
    }
	/**
	 * @since 0.05
	 */
    public final NormalParameters getParameters()
    {
    	return new NormalParameters(_mean, _precision);
    }
    public final double getMean()
    {
    	return _mean;
    }
    public final double getPrecision()
    {
    	return _precision;
    }
	/**
	 * @since 0.05
	 */
	public final double getVariance()
	{
		return 1/_precision;
	}
	/**
	 * @since 0.05
	 */
	public final double getStandardDeviation()
	{
		return 1/Math.sqrt(_precision);
	}
	/**
	 * @since 0.05
	 */
    public final void setMean(double mean)
    {
    	_mean = mean;
    }
	/**
	 * @since 0.05
	 */
    public final void setPrecision(double precision)
    {
    	_precision = precision;
		_logSqrtPrecisionOver2Pi = Math.log(_precision)*0.5 - _logSqrt2pi;
		_precisionOverTwo = _precision*0.5;
    }
}
