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

import cern.jet.math.Bessel;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.UnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;


/**
 * von Mises distribution.
 * <p>
 * The variables in the argument list are ordered as follows:
 * <ol>
 * <li>Mean parameter
 * <li>Precision parameter (inverse variance) (non-negative)
 * <li>An arbitrary number of real variables, each must be between -PI and PI
 * </ol>
 * Mean and precision parameters may optionally be specified as constants in the constructor.
 * In this case, the mean and precision are not included in the list of arguments.
 */
public class VonMises extends UnaryFactorFunction implements IParametricFactorFunction
{
	private static final long serialVersionUID = 1L;

	protected double _mean;
	protected double _precision;
	protected double _log2piBesseli0Precision;
	protected boolean _parametersConstant = false;
	protected int _firstDirectedToIndex = 2;
	protected static final double PI = Math.PI;
	protected static final double MINUS_PI = -Math.PI;
	protected static final double _log2pi = Math.log(2*Math.PI);

	/*--------------
	 * Construction
	 */
	
	public VonMises() {super((String)null);}
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
	
	/**
	 * Constructs von Mises distribution with specified mean/precision parameters.
	 * @param parameters is in the same format accepted by {@link Normal#Normal(Map)}.
	 * @since 0.07
	 */
	public VonMises(Map<String,Object> parameters)
	{
		this(new NormalParameters(parameters));
	}
	
	private VonMises(NormalParameters parameters)
	{
		this(parameters.getMean(), parameters.getPrecision());
	}
	
	protected VonMises(VonMises other)
	{
		super(other);
		_mean = other._mean;
		_precision = other._precision;
		_log2piBesseli0Precision = other._log2piBesseli0Precision;
		_parametersConstant = other._parametersConstant;
		_firstDirectedToIndex = other._firstDirectedToIndex;
	}
	
	@Override
	public VonMises clone()
	{
		return new VonMises(this);
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
		
		if (other instanceof VonMises)
		{
			VonMises that = (VonMises)other;
			return _parametersConstant == that._parametersConstant &&
				_mean == that._mean &&
				_precision == that._precision &&
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
    		_mean = arguments[index++].getDouble();					// First variable is mean parameter
    		_precision = arguments[index++].getDouble();			// Second variable is precision (must be non-negative)
    		_log2piBesseli0Precision = Math.log(Bessel.i0(_precision)) + _log2pi;
    		if (_precision < 0) return Double.POSITIVE_INFINITY;
    	}
    	final int length = arguments.length;
    	final int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		final double x = arguments[index].getDouble();			// Remaining inputs are VonMises variables
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

}
