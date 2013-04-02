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

package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.DimpleException;


/**
 * Rayleigh distribution. The variables in the argument list are ordered as follows:
 * 
 * 1) Sigma parameter (non-negative)
 * 2) Rayleigh distributed real variable
 * 
 * The sigma parameter may optionally be specified as constant in the constructor.
 * In this case, it is not included in the list of arguments.
 * 
 */
public class Rayleigh extends FactorFunction
{
	double _sigma;
	boolean _sigmaConstant = false;
	int _directedToIndex = 1;

	public Rayleigh() {super();}
	public Rayleigh(double sigma)
	{
		this();
		_sigma = sigma;
		_sigmaConstant = true;
		_directedToIndex = 0;
    	if (_sigma < 0) throw new DimpleException("Negative sigma value. This must be a non-negative value.");
	}
	
    @Override
	public double evalEnergy(Object... arguments)
    {
    	int index = 0;
    	if (!_sigmaConstant)
    	{
    		_sigma = FactorFunctionUtilities.toDouble(arguments[index++]);				// First variable is sigma
    		if (_sigma < 0) throw new DimpleException("Negative sigma value. Domain must be restricted to non-negative values.");
    	}
    	double x = FactorFunctionUtilities.toDouble(arguments[index++]);				// Second input is the Rayleigh distributed variable
    	
    	if (x <= 0)
    		return Double.POSITIVE_INFINITY;
    	else
    		return (x*x)/(2.0*_sigma*_sigma) - Math.log(x/(_sigma*_sigma));
	}
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices() {return new int[]{_directedToIndex};}
}
