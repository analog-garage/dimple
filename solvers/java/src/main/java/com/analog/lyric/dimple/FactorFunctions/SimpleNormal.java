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


public class SimpleNormal extends FactorFunction
{
	protected double _mean = 0;
	protected double _invSigmaSquared = 1;
	public SimpleNormal(double mean, double sigma) {super("SimpleNormal"); _mean = mean; _invSigmaSquared = 1/(sigma*sigma);}
	
    @Override
    public double evalEnergy(Object ... input)
    {
    	int length = input.length;
    	double potential = 0;
    	for (int i = 0; i < length; i++)
    	{
    		double relInput = (Double)input[i] - _mean;
    		potential += relInput*relInput*_invSigmaSquared;
    	}
    	return potential/2;
    }
}
