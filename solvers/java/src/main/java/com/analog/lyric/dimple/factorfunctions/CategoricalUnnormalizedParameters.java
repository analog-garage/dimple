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

import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;
import com.analog.lyric.util.misc.Internal;
import com.analog.lyric.util.misc.Matlab;


/**
 * Parameterized categorical distribution, which corresponds to p(x | alpha),
 * where alpha is a vector of *unnormalized* probabilities
 * <p>
 * Representing alpha as described, the conjugate prior for alpha is such that
 * each entry of alpha is independently distributed according to
 * a Gamma distribution, all with a common Beta parameter.
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * <p>
 * The variables in the argument list are ordered as follows:
 * <ul>
 * <li>1..N) Alpha: Vector of unnormalized probabilities
 * <li>N+1...) An arbitrary number of discrete output variable
 * </ul>
 * The parameters may optionally be specified as constants in the constructor.
 * In this case, the parameters are not included in the list of arguments.
 */
public class CategoricalUnnormalizedParameters extends CategoricalBase
{
	private static final long serialVersionUID = 1L;

	/**
	 * @category internal
	 */
	@Internal
	@Matlab
	public CategoricalUnnormalizedParameters(double dimension)
	{
		// HACK - this method exists because otherwise MATLAB would call the double[] constructor
		// with a singleton array!
		this((int)dimension);
	}
	
	public CategoricalUnnormalizedParameters(int dimension)		// Variable parameters
	{
		super(new DiscreteWeightMessage(dimension), dimension);
	}
	
	/**
	 * @deprecated use {@link #CategoricalUnnormalizedParameters(double[])} instead.
	 * @since 0.05
	 */
	@Deprecated
	public CategoricalUnnormalizedParameters(int dimension, double[] alpha)		// Constant parameters
	{
		this(alpha);
		assert(dimension == alpha.length);
	}
	
	/**
	 * Construct with specific constant alpha parameters.
	 * @since 0.08
	 */
	public CategoricalUnnormalizedParameters(double[] alpha)		// Constant parameters
	{
		super(new DiscreteWeightMessage(alpha));
		_parameters.normalize();
	}
	
	protected CategoricalUnnormalizedParameters(CategoricalUnnormalizedParameters other)
	{
		super(other);
	}
		
	@Override
	public CategoricalUnnormalizedParameters clone()
	{
		return new CategoricalUnnormalizedParameters(this);
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
        	final int dimension = _parameters.size();
    		for (int i = 0; i < dimension; i++)
    		{
    			// First _dimension arguments are vector of Alpha parameters, if not constant
    			final double a = arguments[index++].getDouble();
    			if (a < 0) return Double.POSITIVE_INFINITY;
    			
    			_parameters.setWeight(i, a);
    		}
    	}

    	return _parameters.evalNormalizedEnergy(arguments,  index);
	}
}
