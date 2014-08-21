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
 * Binomial distribution, which corresponds to p(x | N, p),
 * where x is a count of the number of ones, N is the total count (ones and zeros),
 * and p is the probability parameter.
 * 
 * The conjugate prior for p is a Beta distribution.
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * 
 * The variables in the argument list are ordered as follows:
 * 
 * 1) N: Parameter indicating the total count (ones and zeros)
 * 2) p: Probability parameter
 * 3) x: Count of ones
 * 
 * N parameter may optionally be specified as constants in the constructor.
 * In this case, N is not included in the list of arguments.
 */
public class Binomial extends FactorFunction
{
	protected int _N;
	protected double _negativeLogFactorialN;
	protected boolean _NParameterConstant = false;
	private int _firstDirectedToIndex = 2;
	
	
	public Binomial() {super();}		// Variable N
	public Binomial(int N)				// Fixed N
	{
		this();
		_N = N;
    	if (_N < 0) throw new DimpleException("N must be a non-negative value.");
		_negativeLogFactorialN = -org.apache.commons.math3.special.Gamma.logGamma(_N + 1);
		_NParameterConstant = true;
		_firstDirectedToIndex = 1;
	}

	
    @Override
	public final double evalEnergy(Value[] arguments)
    {
    	int index = 0;
    	if (!_NParameterConstant)
    	{
    		_N = arguments[index++].getInt();					// First argument is N parameter
    		if (_N < 0) return Double.POSITIVE_INFINITY;
    		_negativeLogFactorialN = -org.apache.commons.math3.special.Gamma.logGamma(_N + 1);
    	}
    	
    	final double p = arguments[index++].getDouble();		// Next argument is the probability parameter
		if (p < 0 || p > 1) return Double.POSITIVE_INFINITY;

		final int numOnes = arguments[index++].getInt();		// Next argument is the one-count
		if (numOnes < 0 || numOnes > _N) return Double.POSITIVE_INFINITY;
		int numZeros = _N - numOnes;
		

    	if (p == 0)
    		if (numOnes > 0)
    			return Double.POSITIVE_INFINITY;
    		else
    			return 0;
    	else if (p == 1)
    		if (numZeros > 0)
    			return Double.POSITIVE_INFINITY;
    		else
    			return 0;
    	else
    		return -(numOnes * Math.log(p) + numZeros * Math.log(1-p))
    				+ _negativeLogFactorialN
    				+ org.apache.commons.math3.special.Gamma.logGamma(numOnes + 1)
    				+ org.apache.commons.math3.special.Gamma.logGamma(numZeros + 1);
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
    public final boolean hasConstantNParameter()
    {
    	return _NParameterConstant;
    }
    public final int getN()
    {
    	return _N;
    }
}
