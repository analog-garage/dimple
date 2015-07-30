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
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.UnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.util.misc.Matlab;


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
@Matlab(wrapper="NormalParameters")
public class Normal extends UnaryFactorFunction implements IParametricFactorFunction
{
	private static final long serialVersionUID = 1L;

	protected NormalParameters _parameters;
	protected boolean _parametersConstant;
	protected int _firstDirectedToIndex;

	/*--------------
	 * Construction
	 */
	
	private Normal(NormalParameters parameters, boolean constant)
	{
		super((String)null);
		_parameters = parameters;
		_parametersConstant = constant;
		_firstDirectedToIndex = constant? 0 : 2;
	}
	
	public Normal()
	{
		this(new NormalParameters(), false);
	}
	
	public Normal(double mean, double precision)
	{
		this(new NormalParameters(mean, precision));
    	if (precision < 0)
    		throw new DimpleException("Negative precision value. This must be a non-negative value.");
	}
	/**
	 * @since 0.05
	 */
	public Normal(NormalParameters parameters)
	{
		this(parameters, true);
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

	protected Normal(Normal other)
	{
		super(other);
		_parameters = other._parameters.clone();
		_firstDirectedToIndex = other._firstDirectedToIndex;
		_parametersConstant = other._parametersConstant;
	}
	
	@Override
	public Normal clone()
	{
		return new Normal(this);
	}
	
	/*----------------
	 * IDatum methods
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof Normal)
		{
			Normal that = (Normal)other;
			return _parametersConstant == that._parametersConstant &&
				_parameters.objectEquals(that._parameters) &&
				_firstDirectedToIndex == that._firstDirectedToIndex;
		}
		
		return false;
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
    		double mean = arguments[index++].getDouble();					// First variable is mean parameter
    		double precision = arguments[index++].getDouble();			// Second variable is precision (must be non-negative)
    		if (precision < 0) return Double.POSITIVE_INFINITY;
    		_parameters.setMean(mean);
    		_parameters.setPrecision(precision);
    	}
    	
    	return _parameters.evalNormalizedEnergy(arguments, index);
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
    		parameters.put("mean", getMean());
    		parameters.put("precision", getPrecision());
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
    			return getMean();
    		case "precision":
    			return getPrecision();
    		case "variance":
    			return getVariance();
    		case "sigma":
    		case "std":
    			return getStandardDeviation();
    		}
    	}
		return null;
    }

    @Override
    public NormalParameters getParameterizedMessage()
    {
    	return _parameters;
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
    	return _parameters;
    }
    
    @Matlab
    public final double getMean()
    {
    	return _parameters.getMean();
    }
    
    @Matlab
    public final double getPrecision()
    {
    	return _parameters.getPrecision();
    }
    
	/**
	 * @since 0.05
	 */
    @Matlab
	public final double getVariance()
	{
		return _parameters.getVariance();
	}
    
	/**
	 * @since 0.05
	 */
    @Matlab
	public final double getStandardDeviation()
	{
		return _parameters.getStandardDeviation();
	}
	/**
	 * @since 0.05
	 */
    public final void setMean(double mean)
    {
    	_parameters.setMean(mean);
    }
	/**
	 * @since 0.05
	 */
    public final void setPrecision(double precision)
    {
    	_parameters.setPrecision(precision);
    }
}
