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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.UnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.util.misc.Matlab;


/**
 * Factor for an exchangeable set of multivariate Normally distributed variables associated
 * with a vector representing the mean parameter and a matrix (in the form of an array of vectors)
 * representing the precision, or alternatively a vector representing the information vector and
 * a matrix representing the information matrix.
 * <p>
 * TODO: THE CURRENT IMPLEMENTATION OF THIS FACTOR FUNCTION SUPPORTS ONLY CONSTANT PARAMETRES,
 * SPECIFIED IN THE CONSTRUCTOR.  VARIABLE PARAMETER SUPPORT SHOULD BE ADDED.
 * <p>
 * The variables are ordered as follows in the argument list:
 * 
 * 1...) An arbitrary number of RealJoint variables
 * 
 * @since 0.05
 */
@Matlab(wrapper="MultivariateNormalParameters")
public class MultivariateNormal extends UnaryFactorFunction
{
	private static final long serialVersionUID = 1L;

	private MultivariateNormalParameters _parameters;
	private boolean _parametersConstant = true; // TODO: support variable parameters
	private int _firstDirectedToIndex;
	protected static final double _logSqrt2pi = Math.log(2*Math.PI)*0.5;


	// Constructors
//	public MultivariateNormal() {super();}	// TODO: Implement variable parameters case
	public MultivariateNormal(double[] mean, double[][] covariance)
	{
		this(new MultivariateNormalParameters(mean, covariance));
	}
	public MultivariateNormal(double[] vector, double[][] matrix, boolean informationForm)
	{
		this(new MultivariateNormalParameters(vector, matrix, informationForm));
	}

	public MultivariateNormal(MultivariateNormalParameters parameters)
	{
		super((String)null);
		_parameters = parameters;
		initializeConstantParameters(parameters);
	}
	
	protected MultivariateNormal(MultivariateNormal other)
	{
		super(other);
		_parameters = other._parameters.clone();
		_parametersConstant = other._parametersConstant;
		_firstDirectedToIndex = other._firstDirectedToIndex;
	}
	
	@Override
	public MultivariateNormal clone()
	{
		return new MultivariateNormal(this);
	}
	
	// Common initialization method used when parameters are constant
	private void initializeConstantParameters(MultivariateNormalParameters parameters)
	{
		_parameters = parameters;
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
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
		
		if (other instanceof MultivariateNormal)
		{
			MultivariateNormal that = (MultivariateNormal)other;
			return _parametersConstant == that._parametersConstant &&
				_firstDirectedToIndex == that._firstDirectedToIndex &&
				_parameters.objectEquals(that._parameters);
		}
		
		return false;
	}
	

    @Override
	public final double evalEnergy(Value[] arguments)
	{
    	final MultivariateNormalParameters params = _parameters;
    	final int length = arguments.length;
    	int index = 0;
    	final int N = length - index;			// Number of non-parameter variables

    	double sum = 0;
    	
    	for (; index < length; index++)
    	{
    		sum += params.evalEnergy(arguments[index]);
    	}

    	return sum - N * params.getNormalizationEnergy();
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
    public final MultivariateNormalParameters getParameters()
    {
    	return _parameters.clone();
    }
    public final void setParameters(MultivariateNormalParameters parameters)
    {
    	initializeConstantParameters(parameters);
    }
}
