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

import org.eclipse.jdt.annotation.Nullable;

/**
 * An extension of standard {@link Thread} class that inherits its active dimple environment from its parent thread.
 * <p>
 * Use this in place of the standard {@link Thread} class to ensure that new threads inherit
 * the same {@link DimpleEnvironment#active()} value as their creating thread.
 * <p>
 * @see DimpleThreadFactory
 * @since 0.07
 * @author Christopher Barber
 */
public class DimpleThread extends Thread implements IDimpleEnvironmentHolder
{
	private final DimpleEnvironment _env;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Sets {@linkplain #getEnvironment environment} to {@link DimpleEnvironment#active()}.
	 * Otherwise is the same as {@link Thread#Thread()} constructor.
	 * @since 0.07
	 */
	public DimpleThread()
	{
		super();
		_env = DimpleEnvironment.active();
	}
	
	/**
	 * Sets {@linkplain #getEnvironment environment} to {@link DimpleEnvironment#active()}.
	 * Otherwise is the same as {@link Thread#Thread(Runnable)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(Runnable target)
	{
		this(DimpleEnvironment.active(), target);
	}
	
	/**
	 * Sets {@linkplain #getEnvironment environment} to specified value.
	 * Otherwise is the same as {@link Thread#Thread(Runnable)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(DimpleEnvironment env, Runnable target)
	{
		super(target);
		_env = env;
	}
	
	/**
	 * Sets {@linkplain #getEnvironment environment} to {@link DimpleEnvironment#active()}.
	 * Otherwise is the same as {@link Thread#Thread(ThreadGroup, Runnable)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(ThreadGroup group, Runnable target)
	{
		this(DimpleEnvironment.active(), group, target);
	}
	
	/**
	 * Sets {@linkplain #getEnvironment environment} to specified value.
	 * Otherwise is the same as {@link Thread#Thread(ThreadGroup, Runnable)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(DimpleEnvironment env, ThreadGroup group, Runnable target)
	{
		super(group, target);
		_env = env;
	}

	/**
	 * Sets {@linkplain #getEnvironment environment} to {@link DimpleEnvironment#active()}.
	 * Otherwise is the same as {@link Thread#Thread(String)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(String name)
	{
		this(null, null, name, 0L, null);
	}
	
	/**
	 * Sets {@linkplain #getEnvironment environment} to {@link DimpleEnvironment#active()}.
	 * Otherwise is the same as {@link Thread#Thread(ThreadGroup,String)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(ThreadGroup group, String name)
	{
		this(group, null, name, 0L, null);
	}
	
	/**
	 * Sets {@linkplain #getEnvironment environment} to {@link DimpleEnvironment#active()}.
	 * Otherwise is the same as {@link Thread#Thread(Runnable,String)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(Runnable target, String name)
	{
		this(null, target, name, 0L, null);
	}

	/**
	 * Sets {@linkplain #getEnvironment environment} to specified value.
	 * Otherwise is the same as {@link Thread#Thread(Runnable,String)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(DimpleEnvironment env, Runnable target, String name)
	{
		this(null, target, name, 0L, env);
	}

	/**
	 * Sets {@linkplain #getEnvironment environment} to {@link DimpleEnvironment#active()}.
	 * Otherwise is the same as {@link Thread#Thread(ThreadGroup,Runnable,String)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(ThreadGroup group, Runnable target, String name)
	{
		this(group, target, name, 0L, null);
	}
	
	/**
	 * Sets {@linkplain #getEnvironment environment} to specified value.
	 * Otherwise is the same as {@link Thread#Thread(ThreadGroup,Runnable,String)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(DimpleEnvironment env, ThreadGroup group, Runnable target, String name)
	{
		this(group, target, name, 0L, env);
	}

	/**
	 * Sets {@linkplain #getEnvironment environment} to {@link DimpleEnvironment#active()}.
	 * Otherwise is the same as {@link Thread#Thread(ThreadGroup,Runnable,String,long)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(ThreadGroup group, Runnable target, String name, long stackSize)
	{
		this(group, target, name, stackSize, null);
	}

	/**
	 * Sets {@linkplain #getEnvironment environment} to specified value.
	 * Otherwise is the same as {@link Thread#Thread(ThreadGroup,Runnable,String,long)} constructor.
	 * @since 0.07
	 */
	public DimpleThread(DimpleEnvironment env, ThreadGroup group, Runnable target, String name, long stackSize)
	{
		this(group, target, name, stackSize, env);
	}

	private DimpleThread(
		@Nullable ThreadGroup group,
		@Nullable Runnable target,
		String name,
		long stackSize,
		@Nullable DimpleEnvironment env)
	{
		super(group, target, name, stackSize);
		_env = env != null ? env : DimpleEnvironment.active();
	}
	
	/*----------------------------------
	 * IDimpleEnvironmentHolder methods
	 */
	
	/**
	 * The {@linkplain DimpleEnvironment#active active dimple environment} for this thread.
	 * <p>
	 * This is set during construction and is either an explicitly specified environment or is
	 * the active environment of the thread in which this thread object was created.
	 * @see #run
	 * @since 0.07
	 */
	@Override
	public DimpleEnvironment getEnvironment()
	{
		return _env;
	}
	
	/*----------------
	 * Thread methods
	 */

	/**
	 * Runs the body of the thread.
	 * <p>
	 * Sets the thread's {@linkplain DimpleEnvironment#active active dimple environment} to
	 * {@linkplain #getEnvironment() environment specified in constructor} and then invokes
	 * {@link Thread#run}.
	 * <p>
	 * If overridden, the implementation must begin with:
	 * <blockquote>
	 * <pre>
	 * super.run();
	 * </pre>
	 * </blockquote>
	 */
	@Override
	public void run()
	{
		DimpleEnvironment.setActive(_env);
		super.run();
	}
}
