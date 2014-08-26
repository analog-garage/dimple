/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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


/**
 * Parameterized categorical distribution, which corresponds to p(x | alpha),
 * where alpha is a vector of *unnormalized* probabilities
 * 
 * Representing alpha as described, the conjugate prior for alpha is such that
 * each entry of alpha is independently distributed according to
 * a Gamma distribution, all with a common Beta parameter.
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * 
 * The variables in the argument list are ordered as follows:
 * 
 * 1..N) Alpha: Vector of unnormalized probabilities
 * N+1...) An arbitrary number of discrete output variable (MUST be zero-based integer values) 	// TODO: remove this restriction
 * 
 * The parameters may optionally be specified as constants in the constructor.
 * In this case, the parameters are not included in the list of arguments.
 */
public class CategoricalUnnormalizedParameters extends FactorFunction
{
	private int _dimension;
	private double[] _alpha;
	private boolean _parametersConstant;
	private int _firstDirectedToIndex;

	public CategoricalUnnormalizedParameters(int dimension)		// Variable parameters
	{
		super();
		_dimension = dimension;
		_firstDirectedToIndex = dimension;
		_parametersConstant = false;
		_alpha = new double[dimension];
	}
	
	/**
	 * @since 0.05
	 */
	public CategoricalUnnormalizedParameters(int dimension, double[] alpha)		// Constant parameters
	{
		super();
		_dimension = dimension;
		_firstDirectedToIndex = 0;
		_parametersConstant = true;
		_alpha = alpha.clone();
		double sum = 0;
    	for (int i = 0; i < _alpha.length; i++)
    	{
    		if (_alpha[i] < 0) throw new DimpleException("Non-positive alpha parameter. Domain must be restricted to positive values.");
    		sum += _alpha[i];
    	}
    	for (int i = 0; i < _alpha.length; i++)		// Normalize the alpha vector in case they're not already normalized
    		_alpha[i] /= sum;
	}
	
    @Override
	public final double evalEnergy(Value[] arguments)
    {
    	int index = 0;

    	if (!_parametersConstant)
    	{
    		for (int i = 0; i < _dimension; i++)
    		{
    			final double a = arguments[index++].getDouble();	// First _dimension arguments are vector of Alpha parameters, if not constant
    			if (a < 0) return Double.POSITIVE_INFINITY;
    			_alpha[i] = a;
    		}
    	}
    	
    	// Get the normalization value
    	double normalizationValue = 0;
    	for (int i = 0; i < _dimension; i++)
    		normalizationValue += _alpha[i];
    	
    	final int length = arguments.length;
    	final int N = length - index;								// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		final int x = arguments[index].getInt();				// Remaining arguments are Categorical variables
    		sum += -Math.log(_alpha[x]);
    	}
    	return sum + N * Math.log(normalizationValue);
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
    public final double[] getParameters()
    {
    	return _alpha;
    }
    public final int getDimension()
    {
    	return _dimension;
    }
}
