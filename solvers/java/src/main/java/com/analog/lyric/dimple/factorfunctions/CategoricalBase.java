/*******************************************************************************
 *   Copyright 2015 Analog Devices, Inc.
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

import static java.util.Objects.*;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.UnaryFactorFunction;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class CategoricalBase extends UnaryFactorFunction implements IParametricFactorFunction
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	protected double[] _alpha;
	protected boolean _parametersConstant;
	protected int _firstDirectedToIndex;

	/*--------------
	 * Construction
	 */
	
	protected CategoricalBase()
	{
		super((String)null);
		_alpha = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_parametersConstant = false;
		_firstDirectedToIndex = 1;	// Parameter vector is an array (one RealJoint variable)
	}
	
	protected CategoricalBase(int dimension)
	{
		super((String)null);
		_alpha = new double[dimension];
		_firstDirectedToIndex = dimension;
		_parametersConstant = false;
	}
	
	protected CategoricalBase(double[] alpha)
	{
		super((String)null);
		_alpha = alpha.clone();
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
	}
		
	protected CategoricalBase(CategoricalBase other)
	{
		super(other);
		_alpha = other._alpha.clone();
		_parametersConstant = other._parametersConstant;
		_firstDirectedToIndex = other._firstDirectedToIndex;
	}
	
	protected void normalizeAlphas()
	{
		double sum = 0;
		for (double d : _alpha)
		{
			if (d < 0)
			{
				throw new IllegalArgumentException(
					"Non-positive alpha parameter. Domain must be restricted to positive values.");
			}
			sum += d;
		}
		for (int i = 0; i < _alpha.length; i++)		// Normalize the alpha vector in case they're not already normalized
			_alpha[i] /= sum;
	}
	
	/*----------------
	 * IDatum methods
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (getClass().isInstance(other))
		{
			CategoricalBase that = (CategoricalBase)requireNonNull(other);
			return _parametersConstant == that._parametersConstant &&
				_firstDirectedToIndex == that._firstDirectedToIndex &&
				Arrays.equals(_alpha, that._alpha);
		}
		
		return false;
	}

	/*------------------------
	 * FactorFunction methods
	 */
	
	@Override
	public final boolean isDirected()
	{
		return true;
	}

	@Override
	public final int[] getDirectedToIndices(int numEdges)
	{
		// All edges except the parameter edges (if present) are directed-to edges
		return FactorFunctionUtilities.getListOfIndices(_firstDirectedToIndex, numEdges-1);
	}

    /*-----------------------------------
     * IParametricFactorFunction methods
     */

	@Override
	public int copyParametersInto(Map<String, Object> parameters)
	{
		if (_parametersConstant)
		{
			parameters.put("alpha", _alpha.clone());
			return 1;
		}
		return 0;
	}

	@Override
	@Nullable
	public double[] getParameter(String parameterName)
	{
		if (_parametersConstant)
		{
			switch (parameterName)
			{
			case "alpha":
			case "alphas":
				return _alpha.clone();
			}
		}
		return null;
	}

	@Override
	public final boolean hasConstantParameters()
	{
		return _parametersConstant;
	}

	public final double[] getParameters()
	{
		return _alpha;
	}

	public final int getDimension()
	{
		return _alpha.length;
	}

}