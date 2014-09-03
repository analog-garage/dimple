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

package com.analog.lyric.dimple.environment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Simple thread factory for {@link DimpleThread}s.
 * <p>
 * This creates {@link DimpleThread}s with the factory's {@linkplain #getEnvironment() environment}.
 * Can be used when creating an {@link ExecutorService} to ensure that threads used by the service
 * will have the right Dimple environment.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class DimpleThreadFactory implements ThreadFactory, IDimpleEnvironmentHolder
{
	private final DimpleEnvironment _env;

	/*--------------
	 * Constructors
	 */

	/**
	 * Sets {@linkplain #getEnvironment() environment} to {@link DimpleEnvironment#active()}..
	 * 
	 * @since 0.07
	 */
	public DimpleThreadFactory()
	{
		this(DimpleEnvironment.active());
	}

	/**
	 * Sets {@linkplain #getEnvironment() environment} to specified value.
	 * 
	 * @since 0.07
	 */
	public DimpleThreadFactory(DimpleEnvironment env)
	{
		_env = env;
	}

	/*-----------------------
	 * ThreadFactory methods
	 */

	/**
	 * Returns a newly created {@link DimpleThread} for given {@code target} and
	 * with active environment set to this {@linkplain #getEnvironment() environment}.
	 */
	@NonNullByDefault(false)
	@Override
	public DimpleThread newThread(Runnable target)
	{
		return new DimpleThread(_env, target);
	}

	/*----------------------------------
	 * IDimpleEnvironmentHolder methods
	 */

	/**
	 * Environment to give to newly created threads.
	 * <p>
	 * This is set at construction time either to an explicit value or the
	 * {@linkplain DimpleEnvironment#active active
	 * environment} of the current thread.
	 * 
	 * @since 0.07
	 */
	@Override
	public DimpleEnvironment getEnvironment()
	{
		return _env;
	}
}
