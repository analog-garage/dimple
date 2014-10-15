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

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
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
public class Normal extends FactorFunction implements IParametricFactorFunction
{
	protected double _mean;
	protected double _precision;
	protected double _logSqrtPrecisionOver2Pi;
	protected double _precisionOverTwo;
	protected boolean _parametersConstant = false;
	protected int _firstDirectedToIndex = 2;
	protected static final double _logSqrt2pi = Math.log(2*Math.PI)*0.5;

	/*--------------
	 * Construction
	 */
	
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
	
	/**
	 * Construct a Normal function with fixed parameters.
	 * <p>
	 * @param parameters specifies the mean and precision. Several different
	 * keywords are supported. To set the mean parameter, this will first look
	 * for a value using the keyword "mean" and then using "mu" and will otherwise
	 * default to a value of zero. To set the precision parameter, this will first
	 * look for a value using the keyword "precision" and then "tau". If not found,
	 * this will next look for "variance" and if found will set the precision to its
	 * reciprocal. If still not found, it will see if the value is specified as
	 * standard deviation under the keywords "std" or "sigma". If no matching keyword
	 * is found the precision will default to one.
	 * @since 0.07
	 */
	public Normal(Map<String,Object> parameters)
	{
		this(new NormalParameters(parameters));
	}

	/*------------------------
	 * FactorFunction methods
	 */
	
    @Override
	public final double evalEnergy(Value[] arguments)
	{
    	int index = 0;
    	if (!_parametersConstant)
    	{
    		_mean = arguments[index++].getDouble();					// First variable is mean parameter
    		_precision = arguments[index++].getDouble();			// Second variable is precision (must be non-negative)
    		_logSqrtPrecisionOver2Pi = Math.log(_precision)*0.5 - _logSqrt2pi;
    		_precisionOverTwo = _precision*0.5;
    		if (_precision < 0) return Double.POSITIVE_INFINITY;
    	}
    	final int length = arguments.length;
    	final int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		final double relInput = arguments[index].getDouble() - _mean;	// Remaining inputs are Normal variables
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
    
    /*-----------------------------------
     * IParametricFactorFunction methods
     */
    
    @Override
    public int copyParametersInto(Map<String, Object> parameters)
    {
    	if (_parametersConstant)
    	{
    		parameters.put("mean", _mean);
    		parameters.put("precision", _precision);
    		return 2;
    	}
    	return 0;
    }

    @Override
    public @Nullable Object getParameter(String parameterName)
    {
    	if (_parametersConstant)
    	{
    		switch (parameterName)
    		{
    		case "mean":
    		case "mu":
    			return _mean;
    		case "precision":
    			return _precision;
    		case "variance":
    			return 1.0 / _precision;
    		case "sigma":
    		case "std":
    			return Math.sqrt(1.0 / _precision);
    		}
    	}
		return null;
    }

    @Override
	public final boolean hasConstantParameters()
    {
    	return _parametersConstant;
    }

    /*-------------------------
     * Factor-specific methods
     */
    
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
