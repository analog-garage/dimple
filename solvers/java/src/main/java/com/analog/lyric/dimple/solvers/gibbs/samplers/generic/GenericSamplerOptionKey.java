/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.gibbs.samplers.generic;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ConstructorRegistry;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.options.ConstructorOptionKey;
import com.analog.lyric.options.IOptionHolder;

/**
 * Option key for identifying sampler.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class GenericSamplerOptionKey extends ConstructorOptionKey<IGenericSampler>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a proposal kernel option key.
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @since 0.07
	 */
	public GenericSamplerOptionKey(Class<?> declaringClass,
		String name,
		Class<? extends IGenericSampler> defaultValue)
	{
		super(declaringClass, name, IGenericSampler.class, defaultValue);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns {@linkplain DimpleEnvironment#genericSamplers() generic sampler registry} for
	 * {@linkplain DimpleEnvironment#active active environment}.
	 */
	@Override
	public ConstructorRegistry<IGenericSampler> getRegistry()
	{
		return DimpleEnvironment.active().genericSamplers();
	}
	
	@Override
	public Class<? extends IGenericSampler> validate(Class<? extends IGenericSampler> value,
		@Nullable IOptionHolder optionHolder)
	{
		return super.validate(value, optionHolder);
	}
}
