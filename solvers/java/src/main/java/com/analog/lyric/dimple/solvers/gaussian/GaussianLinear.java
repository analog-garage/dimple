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

package com.analog.lyric.dimple.solvers.gaussian;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;

public class GaussianLinear extends GaussianFactorBase
{
	private double [] _constants;
	private double _total;

	public GaussianLinear(Factor factor) 
	{
		super(factor);
		
		//Make sure this is of the form a = b*c where either b or c is a constant.
		
		FactorFunctionWithConstants ff = (FactorFunctionWithConstants)factor.getFactorFunction();
		Object [] constants = ff.getConstants();
		
		if (constants.length < 1 || constants.length > 2)
			throw new DimpleException("Need to specify vector of constants");

		if ( !(constants[0] instanceof double[]))
			throw new DimpleException("First parameter must be an array of constants");
		
		_constants = (double[]) constants[0];
		_total = 0;
		
		
		if (constants.length == 2)
		{
			if (! (constants[1] instanceof Double))
				throw new DimpleException("Second parameter must be a double");
			_total = (Double)constants[1];
		}
		
		if (factor.getSiblings().size() != _constants.length)
			throw new DimpleException("Length of constants must equal the size of the number of variables");
		
	}

	
	
	@Override
	public void updateEdge(int outPortNum) 
	{

		double mu;
		double sigma;
		
		if (_constants[outPortNum] == 0)
		{
			mu = 0;
			sigma = Double.POSITIVE_INFINITY;
			
		}
		else
		{
			//mu = _total / _constants[outPortNum];
			mu = _total;
			double sigma2 = 0;
			
			for (int i = 0; i < _inputMsgs.length; i++)
			{
				if (i != outPortNum)
				{
					double [] msg = _inputMsgs[i];
					mu -= msg[0] * _constants[i];
					sigma2 += _constants[i]*_constants[i]*msg[1]*msg[1];
				}
			//mui = 1/constanti (sum j!=i muj * cj) + total/constanti
			
			//sigma2 = 1/constanti^2 * (sumj!=i constantj^2 sigmaj^2)
			}
			mu /= _constants[outPortNum];
			sigma2 /= (_constants[outPortNum]*_constants[outPortNum]);
			sigma = Math.sqrt(sigma2);
		}
		 
		double [] msg = _outputMsgs[outPortNum];
		msg[0] = mu;
		msg[1] = sigma;
	}

}
