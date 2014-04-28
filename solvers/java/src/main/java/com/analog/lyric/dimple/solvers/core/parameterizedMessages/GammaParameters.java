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


public class GammaParameters implements IParameterizedMessage
{
	private double _alphaMinusOne = 0;
	private double _beta = 0;
	
	public GammaParameters() {}
	public GammaParameters(double alphaMinusOne, double beta)
	{
		_alphaMinusOne = alphaMinusOne;
		_beta = beta;
	}
	public GammaParameters(GammaParameters other)		// Copy constructor
	{
		this(other._alphaMinusOne, other._beta);
	}
	
	public GammaParameters clone()
	{
		return new GammaParameters(this);
	}


	// Natural parameters are alpha-1 and beta
	public final double getAlphaMinusOne() {return _alphaMinusOne;}
	public final double getBeta() {return _beta;}

	public final void setAlphaMinusOne(double alphaMinusOne) {_alphaMinusOne = alphaMinusOne;}
	public final void setBeta(double beta) {_beta = beta;}
	
	// Ordinary alpha parameter
	public final double getAlpha() {return _alphaMinusOne + 1;}
	public final void setAlpha(double alpha) {_alphaMinusOne = alpha - 1;}

	
	@Override
	public final void setNull()
	{
		_alphaMinusOne = 0;
		_beta = 0;
	}
}
