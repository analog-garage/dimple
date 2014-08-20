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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.Option;

public class NormalProposalKernel implements IProposalKernel
{
	protected double _standardDeviation = 1;
	private boolean _explicitStandardDeviation = false;
	
	/**
	 * Standard deviation parameter option.
	 * <p>
	 * Default value is 1.0
	 * <p>
	 * @see #configureFromOptions(IOptionHolder)
	 */
	public static final DoubleOptionKey standardDeviation =
		new DoubleOptionKey(NormalProposalKernel.class, "standardDeviation", 1.0, 0.0, Double.POSITIVE_INFINITY);
	
	@Override
	public Proposal next(Value currentValue, Domain variableDomain)
	{
		return new Proposal(currentValue.getDouble() + _standardDeviation * DimpleRandomGenerator.rand.nextGaussian());
	}
	
	@Deprecated
	@Override
	public void setParameters(Object... parameters)
	{
		_standardDeviation = (Double)parameters[0];
	}
	
	@Deprecated
	@Override
	public Object[] getParameters()
	{
		Object[] parameters = new Object[1];
		parameters[0] = _standardDeviation;
		return parameters;
	}
	
	@Override
	public List<Option<?>> getOptionConfiguration(@Nullable List<Option<?>> list)
	{
		if (list == null)
		{
			list = new LinkedList<Option<?>>();
		}
		
		list.add(new Option<Double>(standardDeviation, _standardDeviation));
		
		return list;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets {@linkplain #getStandardDeviation() standard deviation} from {@linkplain #standardDeviation corresponding
	 * option value}.
	 */
	@Override
	public void configureFromOptions(IOptionHolder optionHolder)
	{
		if (!_explicitStandardDeviation)
		{
			_standardDeviation = optionHolder.getOptionOrDefault(standardDeviation);
		}
	}
	
	/**
	 * @deprecated Will be removed in future release. Instead set {@link #standardDeviation} option on
	 * variables or graphs that will be using this proposal kernel.
	 */
	@Deprecated
	public void setStandardDeviation(double standardDeviation)
	{
		_standardDeviation = standardDeviation;
		_explicitStandardDeviation = true;
	}
	
	public double getStandardDeviation()
	{
		return _standardDeviation;
	}
}
