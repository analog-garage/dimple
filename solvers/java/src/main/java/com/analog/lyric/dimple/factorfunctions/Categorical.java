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

import java.util.Objects;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
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
 * 2...) An arbitrary number of discrete output variable (MUST be zero-based integer values) 	// TODO: remove this restriction
 *
 * The parameters may optionally be specified as constants in the constructor.
 * In this case, the parameters are not included in the list of arguments.
 */
public class Categorical extends FactorFunction
{
	private double[] _alpha;
	private boolean _parametersConstant;
	private int _firstDirectedToIndex;

	public Categorical()		// Variable parameters
	{
		super();
		_alpha = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_parametersConstant = false;
		_firstDirectedToIndex = 1;	// Parameter vector is an array (one RealJoint variable)
	}
	
	/**
	 * @since 0.05
	 */
	public Categorical(double[] alpha)	// Constant parameters
	{
		super();
		_alpha = alpha.clone();
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
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
	public double evalEnergy(Object... arguments)
    {
    	int index = 0;
    	if (!_parametersConstant)
    		_alpha = (double[])arguments[index++];		// First argument is the parameter vector, if not constant

    	final int length = arguments.length;
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		int x = FactorFunctionUtilities.toInteger(arguments[index]);		// Remaining arguments are Categorical variables
    		sum += -Math.log(_alpha[x]);
    	}
    	return sum;
	}
    
    @Override
	public double evalEnergy(Value[] values)
    {
    	int index = 0;
    	if (!_parametersConstant)
    		_alpha = (double[])Objects.requireNonNull(values[index++].getObject());	// First argument is the parameter vector, if not constant

    	final int length = values.length;
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		int x = FactorFunctionUtilities.toInteger(values[index].getObject());		// Remaining arguments are Categorical variables
    		sum += -Math.log(_alpha[x]);
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
    	return _alpha.length;
    }
}
