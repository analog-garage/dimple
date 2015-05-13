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

package com.analog.lyric.dimple.schedulers;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.matlabproxy.PScheduler;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

/**
 * Base class for {@link IScheduler} implementations.
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class SchedulerBase implements IScheduler
{
	private static final long serialVersionUID = 1L;

	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return String.format("[%s]", getClass().getSimpleName());
	}
	
	/*--------------------
	 * IScheduler methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation simply invokes the corresponding
	 * {@link #createSchedule(FactorGraph)} method on the model graph.
	 */
	@Override
	public ISchedule createSchedule(ISolverFactorGraph solverGraph)
	{
		return createSchedule(solverGraph.getModelObject());
	}
	
	@Override
	public boolean isCustomScheduler()
	{
		return false;
	}
	
	@Override
	public boolean isDefaultScheduler()
	{
		return false;
	}
	
	@Override
	public void validateForGraph(FactorGraph graph)
	{
	}
	
	/*-----------------------
	 * Static helper methods
	 */

	/*-----------------
	 * Private methods
	 */
	
	/**
	 * Instantiate a scheduler instance.
	 * <p>
	 * @param env is the {@link DimpleEnvironment} to use for looking up registered
	 * {@linkplain DimpleEnvironment#schedulers schedulers}.
	 * @param value is one of the following:
	 * <ul>
	 * <li>{@link IScheduler}
	 * <li>{@link PScheduler} MATLAB proxy
	 * <li>Java {@link Class} of concrete scheduler implementation.
	 * <li>String name of scheduler class that will be looked up in registry.
	 * </ul>
	 * @since 0.08
	 */
	public static IScheduler instantiate(DimpleEnvironment env, Object value)
	{
		if (value instanceof IScheduler)
		{
			return (IScheduler)value;
		}
		else if (value instanceof PScheduler) // generalize this to avoid explicit MATLAB proxy dependency??
		{
			return ((PScheduler)value).getDelegate();
		}
		
		if (value instanceof String)
		{
			value = env.schedulers().getClass((String)value);
		}

		if (value instanceof Class<?>)
		{
			value = instantiateClass(validateClass((Class<?>)value));
		}

		return (IScheduler)value;
	}
	
	/**
	 * Instantiate a scheduler instance given its class.
	 * <p>
	 * 
	 * <p>
	 * @param schedulerClass must either have a public no-argument constructor be an enum class, in which case
	 * the first instance in the enumeration will be returned.
	 * @since 0.08
	 */
	public static <Scheduler extends IScheduler> Scheduler instantiateClass(Class<Scheduler> schedulerClass)
	{
		if (schedulerClass.isEnum())
		{
			// If this is an enum, just return the first value.
			return schedulerClass.getEnumConstants()[0];
		}
		
		try
		{
			return schedulerClass.newInstance();
		}
		catch (InstantiationException | IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Validate class can be used to construct a scheduler instance.
	 * <p>
	 * @param schedulerClass
	 * @return {@code schedulerClass}
	 * @since 0.08
	 * @throws IllegalArgumentException if {@code schedulerClass} is not derived from {@IScheduler} or does
	 * not have a suitable public constructor.
	 */
	public static Class<? extends IScheduler> validateClass(Class<?> schedulerClass)
	{
		if (!IScheduler.class.isAssignableFrom(schedulerClass))
		{
			throw new IllegalArgumentException(String.format("'%s' is not a subclass of IScheduler", schedulerClass));
		}
		
		if (!schedulerClass.isEnum())
		{
			try
			{
				schedulerClass.getConstructor();
			}
			catch (NoSuchMethodException|SecurityException ex)
			{
				throw new IllegalArgumentException(String.format("'%s' does not have public default constructor.",
					schedulerClass));
			}
		}
		
		@SuppressWarnings("unchecked")
		Class<? extends IScheduler> result = (Class<? extends IScheduler>) schedulerClass;
		return result;
	}

}
