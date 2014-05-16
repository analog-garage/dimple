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

import java.util.Arrays;


public class DirichletParameters implements IParameterizedMessage
{
	// The parameters used are the natural additive parameters, (alpha-1)
	private double[] _alphaMinusOne;
	
	public DirichletParameters()
	{
		_alphaMinusOne = new double[0];
	}
	public DirichletParameters(int length)
	{
		_alphaMinusOne = new double[length];
		setNull();
	}
	public DirichletParameters(double[] alphaMinusOne)
	{
		setAlphaMinusOne(alphaMinusOne);
	}
	public DirichletParameters(DirichletParameters other)	// Copy constructor
	{
		this(other._alphaMinusOne);
	}
	
	public DirichletParameters clone()
	{
		return new DirichletParameters(this);
	}
	
	
	public final int getSize() {return _alphaMinusOne.length;}
	public final void setSize(int size) {_alphaMinusOne = new double[size];}
	
	public final double getAlphaMinusOne(int index) {return _alphaMinusOne[index];}
	public final void setAlphaMinusOne(double[] alphaMinusOne)
	{ 
		int length = alphaMinusOne.length;
		if (_alphaMinusOne == null || length != _alphaMinusOne.length)
			_alphaMinusOne = new double[length];
		System.arraycopy(alphaMinusOne, 0, _alphaMinusOne, 0, length);
	}
	public final void fillAlphaMinusOne(double alphaMinusOne)
	{
		Arrays.fill(_alphaMinusOne, alphaMinusOne);	// Replicate a single value into all entries
	}

	// Operations on the parameters
	public final void increment(int index)
	{
		_alphaMinusOne[index]++;
	}
	public final void add(int index, double value)
	{
		_alphaMinusOne[index] += value;
	}
	public final void add(double[] values)
	{
		int length = values.length;
		for (int i = 0; i < length; i++)
			_alphaMinusOne[i] += values[i];
	}
	public final void add(int[] values)
	{
		int length = values.length;
		for (int i = 0; i < length; i++)
			_alphaMinusOne[i] += values[i];
	}
	public final void add(DirichletParameters parameters)
	{
		add(parameters._alphaMinusOne);
	}
	@Override
	public final void setNull()
	{
		Arrays.fill(_alphaMinusOne, 0);
	}
	public final void setNull(int size)
	{
		setSize(size);	// Create the array if it isn't already there, or change the size
		Arrays.fill(_alphaMinusOne, 0);
	}

}
