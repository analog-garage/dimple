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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.UnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DirichletParameters;


/**
 * Factor for an exchangeable set of Dirichlet distributed variables
 * associated with a variable or fixed parameter vector. In this version,
 * all of the parameter values are common and specified as a single real
 * value.  The variables are ordered as follows in the argument list:
 * <p>
 * <ol>
 * <li>Parameter (non-negative Real variable).
 * <li> An arbitrary number of RealJoint variables, each one a Dirichlet distributed random variable.
 * </ol>
 * The dimension of the Dirichlet variable must be specified in the constructor.
 * <p>
 * The parameter may optionally be specified as constants in the constructor.
 * In this case, the parameters are not included in the list of arguments.
 * 
 * @since 0.05
 */
public class ExchangeableDirichlet extends UnaryFactorFunction implements IParametricFactorFunction
{
	private static final long serialVersionUID = 1L;

	protected DirichletParameters _parameters;
	private boolean _parametersConstant;
	private int _firstDirectedToIndex;
	
	private ExchangeableDirichlet(DirichletParameters parameters, int index)
	{
		super((String)null);
		_parameters = parameters;
		_parametersConstant = index == 0;
		_firstDirectedToIndex = index;
		if (!parameters.isSymmetric())
		{
			throw new IllegalArgumentException("ExchangeableDirichlet requires symmetric arguments");
		}
	}
	
	public ExchangeableDirichlet(DirichletParameters parameters)
	{
		this(parameters, 0);
	}
	
	public ExchangeableDirichlet(int dimension)		// Variable parameter
	{
		this(new DirichletParameters(dimension), 1);
	}

	public ExchangeableDirichlet(int dimension, double alpha)	// Constant parameter
	{
		this(new DirichletParameters(dimension, alpha - 1));
	}
	
	protected ExchangeableDirichlet(ExchangeableDirichlet other)
	{
		super(other);
		_parameters = _parameters.clone();
		_firstDirectedToIndex = other._firstDirectedToIndex;
		_parametersConstant = other._parametersConstant;
	}
	
	@Override
	public ExchangeableDirichlet clone()
	{
		return new ExchangeableDirichlet(this);
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
		
		if (other instanceof ExchangeableDirichlet)
		{
			ExchangeableDirichlet that = (ExchangeableDirichlet)other;
			return _parametersConstant == that._parametersConstant &&
				_parameters.objectEquals(_parameters) &&
				_firstDirectedToIndex == that._firstDirectedToIndex;
		}
		
		return false;
	}
	
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	int index = 0;
    	if (!_parametersConstant)
    	{
    		double alpha = arguments[index++].getDouble();
    		if (alpha <= 0)
    			return Double.POSITIVE_INFINITY;
    		_parameters.fillAlphaMinusOne(alpha - 1.0);
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
    		parameters.put("alpha", getParameter("alpha"));
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
    		case "alpha":
    		case "alphas":
    			return getAlphaMinusOne() + 1;
    		}
    	}
    	return null;
    }
    
    @Override
    public DirichletParameters getParameterizedMessage()
    {
    	return _parameters;
    }

    @Override
	public final boolean hasConstantParameters()
    {
    	return _parametersConstant;
    }


    /*--------------------------
     * Factor-specific methods
     */
    
    public final double getAlphaMinusOne()
    {
    	return _parameters.getAlphaMinusOne(0);
    }
    public final double[] getAlphaMinusOneArray()	// Get parameters as if they were separate
    {
    	return _parameters.getAlphaMinusOneArray();
    }
    public final int getDimension()
    {
    	return _parameters.getSize();
    }
}
