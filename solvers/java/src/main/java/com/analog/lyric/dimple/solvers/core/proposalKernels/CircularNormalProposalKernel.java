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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.Option;

public class CircularNormalProposalKernel extends NormalProposalKernel
{
	protected double _min = -Math.PI;
	protected double _max = Math.PI;
	protected double _range = _max-_min;
	private boolean _explicitBounds = false;

	/**
	 * Lower bound parameter option.
	 * <p>
	 * Default value is -pi
	 * <p>
	 * @see #configureFromOptions(IOptionHolder)
	 * @since 0.07
	 */
	public static final DoubleOptionKey lowerBound =
		new DoubleOptionKey(CircularNormalProposalKernel.class, "lowerBound", -Math.PI);
	
	/**
	 * Upper bound parameter option.
	 * <p>
	 * Default value is pi.
	 * <p>
	 * @see #configureFromOptions(IOptionHolder)
	 * @since 0.07
	 */
	public static final DoubleOptionKey upperBound =
		new DoubleOptionKey(CircularNormalProposalKernel.class, "upperBound", Math.PI);
	
	@Override
	public Proposal next(Value currentValue, Domain variableDomain)
	{
		double value = currentValue.getDouble() + _standardDeviation * DimpleRandomGenerator.rand.nextGaussian();
		value = ((((value - _min) % _range) + _range) % _range) + _min;		// Wrap from -pi to pi
		return new Proposal(value);
	}
	
	@Deprecated
	@Override
	public void setParameters(Object... parameters)
	{
		_standardDeviation = (Double)parameters[0];
		if (parameters.length > 1)
			_min = (Double)parameters[1];
		if (parameters.length > 2)
			_max = (Double)parameters[2];
		_range = _max-_min;
	}

	@Override
	public List<Option<?>> getOptionConfiguration(@Nullable List<Option<?>> list)
	{
		list = super.getOptionConfiguration(list);
		list.add(new Option<Double>(lowerBound, _min));
		list.add(new Option<Double>(upperBound, _max));
		return list;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets {@linkplain #getStandardDeviation() standard deviation} from corresponding
	 * {@link #standardDeviation} option value, and sets {@linkplain #getLowerBound lower} and
	 * {@linkplain #getUpperBound upper} bounds from {@link #lowerBound} and {@link #upperBound} options.
	 * 
	 * @since 0.07
	 */
	@Override
	public void configureFromOptions(IOptionHolder optionHolder)
	{
		super.configureFromOptions(optionHolder);
		if (!_explicitBounds)
		{
			_min = optionHolder.getOptionOrDefault(lowerBound);
			_max = optionHolder.getOptionOrDefault(upperBound);
			_range = _max-_min;
		}
	}
	
	@Deprecated
	@Override
	public Object[] getParameters()
	{
		Object[] parameters = new Object[3];
		parameters[0] = _standardDeviation;
		parameters[1] = _min;
		parameters[2] = _max;
		return parameters;
	}
	
	/**
	 * @deprecated Will be removed in future release. Instead set {@link #lowerBound} and {@link #upperBound} options on
	 * variables or graphs that will be using this proposal kernel.
	 */
	@Deprecated
	public void setCircularBounds(double lower, double upper)
	{
		_min = lower;
		_max = upper;
		_range = _max-_min;
		_explicitBounds = true;
	}
	
	public double getLowerBound()
	{
		return _min;
	}
	
	public double getUpperBound()
	{
		return _max;
	}
}

