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


public class AdditiveNoise extends FactorFunction
{
	protected double _invSigmaSquared = 1;
	public AdditiveNoise(double sigma) {super(); _invSigmaSquared = 1/(sigma*sigma);}
	
    @Override
    public double evalEnergy(Object... arguments)
    {
    	double var1 = FactorFunctionUtilities.toDouble(arguments[0]);
    	double var2 = FactorFunctionUtilities.toDouble(arguments[1]);
    	
    	double diff = var2 - var1;
    	double potential = diff*diff*_invSigmaSquared/2;
    	
    	return potential;
    }
}
