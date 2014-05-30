/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

/**
 * @since 0.06
 * @author Jake
 * 
 * Poisson distribution corresponding to p(k|lambda), where k is the observed number of counts
 * and lambda is the rate parameter.
 * 
 * The conjugate prior for lambda is the Gamma distribution
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * 
 * The variables in the argument are as follows:
 * 
 * 1) lambda: the rate parameter
 * 2) k: the observed number of counts
 * 
 * The rate parameter may optionally be specified as constants in the constructor.
 * In this case, it is not included in the list of arguments.
 */
public class Poisson extends FactorFunction
{
	protected double _lambda;
	protected double _logLambda;
	protected boolean _lambdaParameterConstant = false;
	private int _firstDirectedToIndex = 1;
	
	
	public Poisson() {super();}		// For variable lambda
	public Poisson(double lambda)	// For fixed lambda
	{
		this();
		if (lambda <= 0) throw new DimpleException("lambda must be greater than zero.");
		_lambda = lambda;
		_logLambda = Math.log(lambda);
		_lambdaParameterConstant=true;
		_firstDirectedToIndex=0;	
	}
	
	
	//Evaluating the energy
	@Override
	public double evalEnergy(Object... arguments)
	{
		int index = 0;
		
		// First argument of the factor: lambda
		if (!_lambdaParameterConstant)
		{
			_lambda = FactorFunctionUtilities.toDouble(arguments[index++]);
			if (_lambda < 0)
				return Double.POSITIVE_INFINITY;
			_logLambda = Math.log(_lambda);
		}
		
		// Second argument of the factor: k
		int k = FactorFunctionUtilities.toInteger(arguments[index++]);
		double negativeLogFactorialK = -org.apache.commons.math3.special.Gamma.logGamma(k + 1);

		if (_lambda > 0)
			return -(-_lambda + k*_logLambda + negativeLogFactorialK);
		else if (_lambda==0 && k!=0)
			return Double.POSITIVE_INFINITY;
		else if (_lambda==0 && k==0)
			return 0;
		return Double.POSITIVE_INFINITY;
	}
	
	
	@Override
	public final boolean isDirected() {return true;}
	@Override
	public final int[] getDirectedToIndices(int numEdges)
	{
		return FactorFunctionUtilities.getListOfIndices(_firstDirectedToIndex, numEdges-1);
	}
	
	
	// Factor-specific methods
	public final boolean hasConstantLambdaParameter()
	{
		return _lambdaParameterConstant;
	}
	public final double getLambda()
	{
		return _lambda;
	}
}
