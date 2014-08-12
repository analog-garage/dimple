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
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.IOptionHolder;

public class CircularNormalProposalKernel implements IProposalKernel
{
	protected double _standardDeviation = 1;
	protected double _min = -Math.PI;
	protected double _max = Math.PI;
	protected double _range = _max-_min;

	/**
	 * Standard deviation parameter option.
	 * <p>
	 * Default value is 1.0.
	 * <p>
	 * @see #setParametersFromOptions(IOptionHolder)
	 * @since 0.07
	 */
	public static final DoubleOptionKey standardDeviation =
		new DoubleOptionKey(CircularNormalProposalKernel.class, "standardDeviation", 1.0);
	
	/**
	 * Lower bound parameter option.
	 * <p>
	 * Default value is -pi
	 * <p>
	 * @see #setParametersFromOptions(IOptionHolder)
	 * @since 0.07
	 */
	public static final DoubleOptionKey lowerBound =
		new DoubleOptionKey(CircularNormalProposalKernel.class, "lowerBound", -Math.PI);
	
	/**
	 * Upper bound parameter option.
	 * <p>
	 * Default value is pi.
	 * <p>
	 * @see #setParametersFromOptions(IOptionHolder)
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
	public void setParametersFromOptions(IOptionHolder optionHolder)
	{
		setStandardDeviation(optionHolder.getOptionOrDefault(standardDeviation));
		setCircularBounds(optionHolder.getOptionOrDefault(lowerBound), optionHolder.getOptionOrDefault(upperBound));
	}
	
	@Override
	public Object[] getParameters()
	{
		Object[] parameters = new Object[3];
		parameters[0] = _standardDeviation;
		parameters[1] = _min;
		parameters[2] = _max;
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

	public void setCircularBounds(double lower, double upper)
	{
		_min = lower;
		_max = upper;
		_range = _max-_min;
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

