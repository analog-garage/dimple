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
 * associated with a variable or fixed parameter vector. The variables are
 * ordered as follows in the argument list:
 * <p>
 * <ul>
 * <li> Parameters (non-negative RealJoint variable).
 * <li> ...) An arbitrary number of RealJoint variables, each one a Dirichlet distributed random variable.
 * </ul>
 * The parameters may optionally be specified as constants in the constructor.
 * In this case, the parameters are not included in the list of arguments.
 */
public class Dirichlet extends UnaryFactorFunction implements IParametricFactorFunction
{
	private static final long serialVersionUID = 1L;

	protected DirichletParameters _parameters;
	private boolean _parametersConstant;
	private int _firstDirectedToIndex;
	
	/*--------------
	 * Construction
	 */
	
	protected Dirichlet(DirichletParameters parameters, int index)
	{
		super((String)null);
		_parameters = parameters;
		_parametersConstant = index == 0;
		_firstDirectedToIndex = index;
	}
	
	public Dirichlet()		// Variable parameters
	{
		this(new DirichletParameters(), 1);
	}
	
	public Dirichlet(DirichletParameters parameters)
	{
		this(parameters, 0);
	}
	
	public Dirichlet(double[] alpha)	// Constant parameters
	{
		this(new DirichletParameters(alpha.length));
		_parameters.setAlpha(alpha);
	}
	
	/**
	 * Construct from specified parameters
	 * @param parameters the following values are supported:
	 * <ul>
	 * <li>alpha (default 1.0)
	 * <li>beta (default 1.0)
	 * </ul>
	 * @since 0.07
	 */
	public Dirichlet(Map<String,Object> parameters)
	{
		this((double[])require(parameters, "alpha", "alphas"));
	}
	
	protected Dirichlet(Dirichlet other)
	{
		super(other);
		_parameters = other._parameters.clone();
		_firstDirectedToIndex = other._firstDirectedToIndex;
		_parametersConstant = other._parametersConstant;
	}
	
	@Override
	public Dirichlet clone()
	{
		return new Dirichlet(this);
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
		
		if (other instanceof Dirichlet)
		{
			Dirichlet that = (Dirichlet)other;
			return _parametersConstant == that._parametersConstant &&
				_parameters.objectEquals(((Dirichlet) other)._parameters) &&
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
    		final double[] alpha = arguments[index++].getDoubleArray(); // First variable is array of parameter values
    		if (_parameters.getSize() != alpha.length)
    			_parameters = new DirichletParameters(alpha.length);
    		_parameters.setAlpha(alpha);
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
    			return _parameters.getAlphas();
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

    /*-------------------------
    * Factor-specific methods
    */
    
    public final double[] getAlphaMinusOneArray()
    {
    	return _parameters.getAlphaMinusOneArray();
    }
    public final int getDimension()
    {
    	return _parameters.getSize();
    }
}
