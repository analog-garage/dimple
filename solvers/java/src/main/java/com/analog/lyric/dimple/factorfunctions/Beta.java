/*******************************************************************************
 *   Copyright 2012-2015 Analog Devices, Inc.
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

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.UnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.BetaParameters;

/**
 * Beta distribution.
 * <p>
 * The variables in the argument list are ordered as follows:
 * <ol>
 * <li>Alpha: Alpha parameter of the Beta distribution (non-negative)
 * <li>Beta: Beta parameter of the Beta distribution (non-negative)
 * <li>An arbitrary number of real variables
 * </ol>
 * Alpha and Beta parameters may optionally be specified as constants in the constructor.
 * In this case, they are not included in the list of arguments.
 */
public class Beta extends UnaryFactorFunction implements IParametricFactorFunction
{
	private static final long serialVersionUID = 1L;

	protected BetaParameters _parameters;
	protected boolean _parametersConstant;
	protected int _firstDirectedToIndex;

	/*--------------
	 * Construction
	 */

	private Beta(BetaParameters parameters, boolean constant)
	{
		super((String)null);
		_parameters = parameters;
		_parametersConstant = constant;
		_firstDirectedToIndex = constant ? 0 : 2;
	}
	
	public Beta()
	{
		this(new BetaParameters(), false);
	}

	public Beta(BetaParameters parameters)
	{
		this(parameters, true);
	}
	
	public Beta(double alpha, double beta)
	{
		this(new BetaParameters(alpha - 1, beta - 1));
		if (alpha < 0)
			throw new DimpleException("Negative alpha parameter. This must be a non-negative value.");
		if (beta < 0)
			throw new DimpleException("Negative beta parameter. This must be a non-negative value.");
	}

	/**
	 * Construct with specified parameters.
	 * <p>
	 * @param parameters the following values are supported:
	 * <ul>
	 * <li>alpha (default 1.0)
	 * <li>beta (default 1.0)
	 * </ul>
	 * @since 0.07
	 */
	public Beta(Map<String, Object> parameters)
	{
		this((double) getOrDefault(parameters, "alpha", 1.0), (double) getOrDefault(parameters, "beta", 1.0));
	}

	protected Beta(Beta other)
	{
		super(other);
		_parameters = other._parameters.clone();
		_parametersConstant = other._parametersConstant;
		_firstDirectedToIndex = other._firstDirectedToIndex;
	}
	
	@Override
	public Beta clone()
	{
		return new Beta(this);
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
		
		if (other instanceof Beta)
		{
			Beta that = (Beta)other;
			return _parametersConstant == that._parametersConstant &&
				_parameters.objectEquals(that._parameters) &&
				_firstDirectedToIndex == that._firstDirectedToIndex;
		}
		
		return false;
	}
	
	/*------------------------
	 * FactorFunction methods
	 */
	
	@Override
	public final double evalEnergy(Value[] arguments)
	{
		int index = 0;
		if (!_parametersConstant)
		{
			double alpha = arguments[index++].getDouble();
			if (alpha < 0)
				return Double.POSITIVE_INFINITY;
			_parameters.setAlpha(alpha);
			double beta = arguments[index++].getDouble();
			if (beta < 0)
				return Double.POSITIVE_INFINITY;
			_parameters.setBeta(beta);
		}
		
		return _parameters.evalNormalizedEnergy(arguments, index);
	}

	@Override
	public final boolean isDirected()
	{
		return true;
	}

	@Override
	public final int[] getDirectedToIndices(int numEdges)
	{
		// All edges except the parameter edges (if present) are directed-to edges
		return FactorFunctionUtilities.getListOfIndices(_firstDirectedToIndex, numEdges - 1);
	}

	/*-----------------------------------
	 * IParametricFactorFunction methods
	 */
	
	@Override
	public int copyParametersInto(Map<String, Object> parameters)
	{
		if (_parametersConstant)
		{
			parameters.put("alpha", _parameters.getAlpha());
			parameters.put("beta", _parameters.getBeta());
			return 2;
		}
		return 0;
	}

	@Override
	public @Nullable Object getParameter(String parameterName)
	{
		if (_parametersConstant)
		{
			switch (parameterName)
			{
			case "alpha":
				return _parameters.getAlpha();
			case "beta":
				return _parameters.getBeta();
			}
		}
		return null;
	}
	
	@Override
	public BetaParameters getParameterizedMessage()
	{
		return _parameters;
	}
	
	@Override
	public final boolean hasConstantParameters()
	{
		return _parametersConstant;
	}

	/*-------------------------
	 * Factor-specific methods
	 */
	
	public final double getAlphaMinusOne()	// The natural additive parameter, alpha - 1
	{
		return _parameters.getAlphaMinusOne();
	}

	public final double getBetaMinusOne()	// The natural additive parameter, beta - 1
	{
		return _parameters.getBetaMinusOne();
	}
}
