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

package com.analog.lyric.dimple.solvers.core.proposalKernels;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.math.DimpleRandomGenerator;

public class NormalProposalKernel implements IProposalKernel
{
	protected double _standardDeviation = 1;
	
	public Proposal next(Value currentValue, Domain variableDomain)
	{
		return new Proposal(currentValue.getDouble() + _standardDeviation * DimpleRandomGenerator.rand.nextGaussian());
	}
	
	public void setParameters(Object... parameters)
	{
		_standardDeviation = (Double)parameters[0];
	}
	
	public Object[] getParameters()
	{
		Object[] parameters = new Object[1];
		parameters[0] = _standardDeviation;
		return parameters;
	}
	
	public void setStandardDeviation(double standardDeviation)
	{
		_standardDeviation = standardDeviation;
	}
	
	public double getStandardDeviation()
	{
		return _standardDeviation;
	}
}
