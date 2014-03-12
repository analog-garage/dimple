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
	private double[] _alpha;
	
	public DirichletParameters()
	{
		_alpha = new double[0];
	}
	public DirichletParameters(int length)
	{
		_alpha = new double[length];
		setNull();
	}
	public DirichletParameters(double[] alpha)
	{
		setAlpha(alpha);
	}
	public DirichletParameters(DirichletParameters other)	// Copy constructor
	{
		this(other._alpha);
	}
	
	public DirichletParameters clone()
	{
		return new DirichletParameters(this);
	}
	
	
	public final int getSize() {return _alpha.length;}
	public final void setSize(int size) {_alpha = new double[size];}
	
	public final double getAlpha(int index) {return _alpha[index];}
	public final void setAlpha(double[] alpha)
	{ 
		int length = alpha.length;
		if (_alpha == null || length != _alpha.length)
			_alpha = new double[length];
		System.arraycopy(alpha, 0, _alpha, 0, length);
	}
	
	// Operations on the parameters
	public final void fill(double alpha)
	{
		Arrays.fill(_alpha, alpha);	// Replicate a single alpha value into all entries
	}
	public final void increment(int index)
	{
		_alpha[index]++;
	}
	public final void add(int index, double value)
	{
		_alpha[index] += value;
	}
	public final void add(double[] values)
	{
		int length = values.length;
		for (int i = 0; i < length; i++)
			_alpha[i] += values[i];
	}
	public final void add(int[] values)
	{
		int length = values.length;
		for (int i = 0; i < length; i++)
			_alpha[i] += values[i];
	}
	public final void add(DirichletParameters parameters)
	{
		add(parameters._alpha);
	}

	@Override
	public final void setNull()
	{
		Arrays.fill(_alpha, 0);
	}
}
