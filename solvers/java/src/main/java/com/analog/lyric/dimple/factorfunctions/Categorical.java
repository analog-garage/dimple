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

import static com.analog.lyric.math.Utilities.*;

import java.util.Map;

import com.analog.lyric.dimple.model.values.Value;


/**
 * Parameterized categorical distribution, which corresponds to p(x | alpha),
 * where alpha is a RealJoint variable vector of normalized probabilities.
 * 
 * Representing alpha as described, the conjugate prior for alpha is
 * a Dirichlet distribution.
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * 
 * The variables in the argument list are ordered as follows:
 * 
 * 1) Alpha: RealJoint variable containing probabilities
 * 2...) An arbitrary number of discrete output variable
 *
 * The parameters may optionally be specified as constants in the constructor.
 * In this case, the parameters are not included in the list of arguments.
 */
public class Categorical extends CategoricalBase
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	public Categorical()		// Variable parameters
	{
		super();
	}
	
	/**
	 * @since 0.05
	 */
	public Categorical(double[] alpha)	// Constant parameters
	{
		super(alpha);
		normalizeAlphas();
	}
	
	/**
	 * Construct from specified parameters
	 * @param parameters the following keys are supported:
	 * <ul>
	 * <li>alpha(s)
	 * </ul>
	 * @since 0.07
	 */
	public Categorical(Map<String,Object> parameters)
	{
		this((double[])require(parameters, "alpha", "alphas"));
	}
	
	protected Categorical(Categorical other)
	{
		super(other);
	}
	
	@Override
	public Categorical clone()
	{
		return new Categorical(this);
	}
	
	/*------------------------
	 * FactorFunction methods
	 */
	
    @Override
	public final double evalEnergy(Value[] arguments)
    {
    	int index = 0;
    	if (!_parametersConstant)
    		_alpha = arguments[index++].getDoubleArray();	// First argument is the parameter vector, if not constant

    	// Remaining arguments are Categorical variables
    	final int length = arguments.length;
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		final int x = arguments[index].getIndexOrInt();
    		sum += weightToEnergy(_alpha[x]);
    	}
    	return sum;
	}
}
