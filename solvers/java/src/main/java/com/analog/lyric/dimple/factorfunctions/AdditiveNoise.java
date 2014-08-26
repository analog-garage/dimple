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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;


public class AdditiveNoise extends FactorFunction
{
	protected double _invSigmaSquared = 1;
	public AdditiveNoise(double sigma) {super(); _invSigmaSquared = 1/(sigma*sigma);}
	
    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	final double var1 = arguments[0].getDouble();
    	final double var2 = arguments[1].getDouble();
    	
    	final double diff = var2 - var1;
    	final double potential = diff*diff*_invSigmaSquared/2;
    	
    	return potential;
    }
}
