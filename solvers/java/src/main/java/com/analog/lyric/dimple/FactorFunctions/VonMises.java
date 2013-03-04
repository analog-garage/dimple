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

import cern.jet.math.Bessel;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.DimpleException;


/**
 * von Mises distribution. The variables in the argument list are ordered as follows:
 * 
 * 1) Mean parameter
 * 2) Inverse variance parameter (non-negative)
 * 3) von Mises distributed real variable
 * 
 * Mean and *standard-deviation* parameters may optionally be specified as constants in the constructor.
 * In this case, the mean and inverse-variance are not included in the list of arguments.
 * 
 */
public class VonMises extends FactorFunction
{
	double _mean;
	double _inverseVariance;
	boolean _meanConstant = false;
	boolean _inverseVarianceConstant = false;
	
	public VonMises() {super("VonMises");}
	public VonMises(double mean, double standardDeviation)
	{
		this();
		_mean = mean;
		_meanConstant = true;
		_inverseVariance = 1/(standardDeviation*standardDeviation);
		_inverseVarianceConstant = true;
    	if (_inverseVariance < 0) throw new DimpleException("Negative standard-deviation value. This must be a non-negative value.");
	}
	
    @Override
	public double evalEnergy(Object... arguments)
    {
    	int index = 0;
    	if (!_meanConstant)
    		_mean = FactorFunctionUtilities.toDouble(arguments[index++]);				// First variable is mean parameter
    	if (!_inverseVarianceConstant)
    	{
    		_inverseVariance = FactorFunctionUtilities.toDouble(arguments[index++]);	// Second variable is inverse variance (must be non-negative)
    		if (_inverseVariance < 0) throw new DimpleException("Negative inverse variance value. Domain must be restricted to non-negative values.");
    	}
    	double x = FactorFunctionUtilities.toDouble(arguments[index++]);				// Third input is the Gamma distributed variable
    	
    	return Math.log(Bessel.i0(_inverseVariance)) - _inverseVariance * Math.cos(x - _mean);
	}
    
    
}
