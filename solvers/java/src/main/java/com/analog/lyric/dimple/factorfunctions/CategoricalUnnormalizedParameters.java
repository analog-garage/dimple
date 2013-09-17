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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.DimpleException;


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
 */
public class CategoricalUnnormalizedParameters extends FactorFunction
{
	private int _dimension;
	private double[] _alpha;
	private int _firstDirectedToIndex;

	public CategoricalUnnormalizedParameters(int dimension)
	{
		super();
		_dimension = dimension;
		_firstDirectedToIndex = dimension;
		_alpha = new double[dimension];
	}
	
    @Override
	public double evalEnergy(Object... arguments)
    {
    	if (arguments.length <= _dimension)
    		throw new DimpleException("Insufficient number of arguments.");
    	
    	int index = 0;
    	for (int i = 0; i < _dimension; i++)
    	{
    		double a = FactorFunctionUtilities.toDouble(arguments[index++]);	// First _dimension arguments are vector of Alpha parameters
    		if (a < 0) return Double.POSITIVE_INFINITY;
    		_alpha[i] = a;
    	}
    	
    	// Get the normalization value
    	double normalizationValue = 0;
    	for (int i = 0; i < _dimension; i++)
    		normalizationValue += _alpha[i];
    	
    	int length = arguments.length;
    	int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		int x = FactorFunctionUtilities.toInteger(arguments[index]);		// Remaining arguments are Categorical variables
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
    public final int getDimension()
    {
    	return _dimension;
    }
}
