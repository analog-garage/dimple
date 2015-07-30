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

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.UnaryFactorFunction;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;

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

	protected DiscreteMessage _parameters;
	protected boolean _parametersConstant;
	protected int _firstDirectedToIndex;

	/*--------------
	 * Construction
	 */
	
	protected CategoricalBase(DiscreteMessage parameters, int firstDirectedToIndex)
	{
		super((String)null);
		_parameters = parameters;
		_parametersConstant = firstDirectedToIndex == 0;
		_firstDirectedToIndex = firstDirectedToIndex;
	}
	
	protected CategoricalBase(DiscreteMessage parameters)
	{
		this(parameters, 0);
	}
	
	protected CategoricalBase(CategoricalBase other)
	{
		super(other);
		_parameters = other._parameters.clone();
		_parametersConstant = other._parametersConstant;
		_firstDirectedToIndex = other._firstDirectedToIndex;
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
				_parameters.objectEquals(that._parameters);
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
			parameters.put("alpha", getParameters().clone());
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
				return getParameters().clone();
			}
		}
		return null;
	}

	@Override
	public final boolean hasConstantParameters()
	{
		return _parametersConstant;
	}
	
	@Override
	public DiscreteMessage getParameterizedMessage()
	{
		return _parameters;
	}

	public final double[] getParameters()
	{
		return _parameters.representation();
	}

	public final int getDimension()
	{
		return _parameters.size();
	}

}