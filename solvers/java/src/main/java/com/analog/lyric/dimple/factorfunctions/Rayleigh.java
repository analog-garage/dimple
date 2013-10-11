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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;


/**
 * Rayleigh distribution. The variables in the argument list are ordered as follows:
 * 
 * 1) Sigma parameter (non-negative)
 * 2...) An arbitrary number of real variables
 * 
 * The sigma parameter may optionally be specified as constant in the constructor.
 * In this case, it is not included in the list of arguments.
 * 
 */
public class Rayleigh extends FactorFunction
{
	protected double _sigma;
	protected double _inverseSigmaSquared;
	protected double _halfInverseSigmaSquared;
	protected boolean _parametersConstant = false;
	protected int _firstDirectedToIndex = 1;

	public Rayleigh() {super();}
	public Rayleigh(double sigma)
	{
		this();
		_sigma = sigma;
		_inverseSigmaSquared = 1/(_sigma*_sigma);
		_halfInverseSigmaSquared = _inverseSigmaSquared * 0.5;
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
    	if (_sigma < 0) throw new DimpleException("Negative sigma value. This must be a non-negative value.");
	}
	
    @Override
	public double evalEnergy(Object... arguments)
    {
    	int index = 0;
    	if (!_parametersConstant)
    	{
    		_sigma = FactorFunctionUtilities.toDouble(arguments[index++]);				// First variable is sigma
    		_inverseSigmaSquared = 1/(_sigma*_sigma);
    		_halfInverseSigmaSquared = _inverseSigmaSquared * 0.5;
    		if (_sigma < 0) throw new DimpleException("Negative sigma value. Domain must be restricted to non-negative values.");
    	}
    	int length = arguments.length;
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		double x = FactorFunctionUtilities.toDouble(arguments[index]);				// Remaining inputs are Rayleigh variables
        	if (x < 0)
        		return Double.POSITIVE_INFINITY;
        	else
        		sum += x*x*_halfInverseSigmaSquared - Math.log(x*_inverseSigmaSquared);
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
}
