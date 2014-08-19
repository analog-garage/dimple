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

package com.analog.lyric.dimple.solvers.core.proposalKernels;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.options.ClassOptionKey;
import com.analog.lyric.options.OptionValidationException;


/**
 * Option key for identifying proposal kernel.
 * <p>
 * Extends {@link ClassOptionKey} with {@link #convertValue} method that looks up {@link IProposalKernel}
 * subclasses in {@linkplain DimpleEnvironment#proposalKernels() proposal kernel registry} for
 * {@linkplain DimpleEnvironment#active active environment}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class ProposalKernelOptionKey extends ClassOptionKey<IProposalKernel>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a proposal kernel option key.
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @since 0.07
	 */
	public ProposalKernelOptionKey(Class<?> declaringClass, String name, Class<? extends IProposalKernel> defaultValue)
	{
		super(declaringClass, name, IProposalKernel.class, defaultValue);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the {@code value} is a string, this will attempt to look it up in the
	 * {@linkplain DimpleEnvironment#proposalKernels() proposal kernel registry} for
	 * {@linkplain DimpleEnvironment#active active environment}.
	 */
	@Override
	public Class<? extends IProposalKernel> convertValue(Object value)
	{
		if (value instanceof String)
		{
			Class<? extends IProposalKernel> kernelClass =
				DimpleEnvironment.active().proposalKernels().getClass((String)value);
			if (kernelClass != null)
			{
				return kernelClass;
			}
		}
		
		return super.convertValue(value);
	}
	
	@Override
	public Class<? extends IProposalKernel> validate(Class<? extends IProposalKernel> value)
	{
		Class<? extends IProposalKernel> kernelClass = super.validate(value);
		
		// Verify that class has a no-argument constructor
		try
		{
			kernelClass.getConstructor();
		}
		catch (Exception ex)
		{
			throw new OptionValidationException("Class '%s' does not have an accessible no argument constructor.",
				kernelClass);
		}
		
		return kernelClass;
	}
}