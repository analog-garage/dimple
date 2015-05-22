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


/**
 * Rayleigh distribution.
 * <p>
 * The variables in the argument list are ordered as follows:
 * <ol>
 * <li>Sigma parameter (non-negative)
 * <li>An arbitrary number of real variables
 * </ol>
 * The sigma parameter may optionally be specified as constant in the constructor.
 * In this case, it is not included in the list of arguments.
 */
public class Rayleigh extends UnaryFactorFunction implements IParametricFactorFunction
{
	private static final long serialVersionUID = 1L;
	
	protected double _sigma;
	protected double _inverseSigmaSquared;
	protected double _halfInverseSigmaSquared;
	protected boolean _parametersConstant = false;
	protected int _firstDirectedToIndex = 1;

	/*--------------
	 * Construction
	 */
	
	public Rayleigh() {super((String)null);}
	public Rayleigh(double sigma)
	{
		this();
		_sigma = sigma;
		_inverseSigmaSquared = 1/(_sigma*_sigma);
		_halfInverseSigmaSquared = _inverseSigmaSquared * 0.5;
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
    	if (_sigma < 0) throw new DimpleException("Negative sigma value. This must be a non-negative value.");
	}
	
	/**
	 * Constructs Rayleigh distribution with fixed sigma parameter.
	 * @param parameters If this contains an entry under the key "sigma", that will be used for the
	 * corresponding parameter, otherwise it will default to 1.0.
	 * @since 0.07
	 */
	public Rayleigh(Map<String,Object> parameters)
	{
		this((double)getOrDefault(parameters, "sigma", 1.0));
	}
	
	protected Rayleigh(Rayleigh other)
	{
		super(other);
		_sigma = other._sigma;
		_inverseSigmaSquared = other._inverseSigmaSquared;
		_halfInverseSigmaSquared = other._halfInverseSigmaSquared;
		_parametersConstant = other._parametersConstant;
		_firstDirectedToIndex = other._firstDirectedToIndex;
	}
	
	@Override
	public Rayleigh clone()
	{
		return new Rayleigh(this);
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
		
		if (other instanceof Rayleigh)
		{
			Rayleigh that = (Rayleigh)other;
			return _parametersConstant == that._parametersConstant &&
				_sigma == that._sigma &&
				_firstDirectedToIndex == that._firstDirectedToIndex;		}
		
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
    		_sigma = arguments[index++].getDouble();				// First variable is sigma
    		_inverseSigmaSquared = 1/(_sigma*_sigma);
    		_halfInverseSigmaSquared = _inverseSigmaSquared * 0.5;
    		if (_sigma < 0) return Double.POSITIVE_INFINITY;
    	}
    	final int length = arguments.length;
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		final double x = arguments[index].getDouble();				// Remaining inputs are Rayleigh variables
        	if (x < 0)
        		return Double.POSITIVE_INFINITY;
        	else
        		sum += x*x*_halfInverseSigmaSquared - Math.log(x*_inverseSigmaSquared);
    	}
    	return sum;
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
    		parameters.put("sigma", _sigma);
    		return 1;
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
    		case "sigma":
    			return _sigma;
    		}
    	}
    	return null;
    }
    
    @Override
    public boolean hasConstantParameters()
    {
    	return _parametersConstant;
    }
}
