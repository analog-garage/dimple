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

package com.analog.lyric.dimple.solvers.sumproduct.customFactors;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;

/*
 * This class is for backward compatibility only.
 * This is to work with the deprecated "linear" custom factor.
 * This has been replaced by the LinearEquation factor function, which has a different
 * interface to the "linear" custom factor, and is associated with
 * the CustomGaussianLinearEquation custom factor.
 */
public class CustomGaussianLinear extends GaussianFactorBase
{
	private double [] _constants;
	private double _total;

	public CustomGaussianLinear(Factor factor)
	{
		super(factor);
		
		//Make sure this is of the form a = b*c where either b or c is a constant.
		
		Object[] constants = factor.getFactorFunction().getConstants();
		
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
		
		if (factor.getSiblingCount() != _constants.length)
			throw new DimpleException("Length of constants must equal the size of the number of variables");
		
	}

	
	
	@Override
	public void doUpdateEdge(int outPortNum)
	{
		double mu;
		double sigma2;
		
		if (_constants[outPortNum] == 0)
		{
			mu = 0;
			sigma2 = Double.POSITIVE_INFINITY;
		}
		else
		{
			// mui = 1/constanti (sum j!=i muj * cj) + total/constanti
			// sigma2 = 1/constanti^2 * (sumj!=i constantj^2 sigmaj^2)

			mu = _total;
			sigma2 = 0;
			
			for (int i = 0; i < _inputMsgs.length; i++)
			{
				if (i != outPortNum)
				{
					NormalParameters msg = _inputMsgs[i];
					mu -= msg.getMean() * _constants[i];
					sigma2 += _constants[i]*_constants[i]*msg.getVariance();
				}
			}
			mu /= _constants[outPortNum];
			sigma2 /= (_constants[outPortNum]*_constants[outPortNum]);
		}
		 
		NormalParameters msg = _outputMsgs[outPortNum];
		msg.setMean(mu);
		msg.setVariance(sigma2);
	}

}
