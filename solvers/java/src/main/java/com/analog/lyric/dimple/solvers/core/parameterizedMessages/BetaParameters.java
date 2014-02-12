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

package com.analog.lyric.dimple.solvers.core.parameterizedMessages;


public class BetaParameters implements IParameterizedMessage
{
	private double _alpha = 0;
	private double _beta = 0;
	
	public BetaParameters() {}
	public BetaParameters(double alpha, double beta)
	{
		_alpha = alpha;
		_beta = beta;
	}
	public BetaParameters(BetaParameters other)		// Copy constructor
	{
		this(other._alpha, other._beta);
	}
	
	public final double getAlpha() {return _alpha;}
	public final double getBeta() {return _beta;}

	public final void setAlpha(double alpha) {_alpha = alpha;}
	public final void setBeta(double beta) {_beta = beta;}
	
	@Override
	public final void setNull()
	{
		_alpha = 0;
		_beta = 0;
	}
}
