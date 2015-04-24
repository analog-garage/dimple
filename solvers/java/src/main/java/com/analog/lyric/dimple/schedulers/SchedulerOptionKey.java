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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ConstructorRegistry;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.validator.ScheduleValidatorOptionKey;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.options.OptionValidationException;

/**
 * Option key type for schedulers.
 * <p>
 * Unlike most options, scheduler options can hold mutable values and the {@link #defaultValue()}
 * is not a singleton constant but a newly created object.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class SchedulerOptionKey extends OptionKey<IScheduler>
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	private final Class<? extends IScheduler> _defaultSchedulerClass;
	private final @Nullable ScheduleValidatorOptionKey _validatorKey;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs a scheduler option key.
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param defaultSchedulerClass is the default value of the option. Used when option is not set.
	 * This class should implement {@link IScheduler#isDefaultScheduler()} to return true.
	 * @throws OptionValidationException if {@code defaultSchedulerClass} does not have a public default
	 * constructor.
	 * @since 0.08
	 */
	@SuppressWarnings("null")
	public SchedulerOptionKey(Class<?> declaringClass,
		String name,
		Class<? extends IScheduler> defaultSchedulerClass)
	{
		this(declaringClass, name, defaultSchedulerClass, null);
	}

	/**
	 * Constructs a scheduler option key.
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param defaultSchedulerClass is the default value of the option. Used when option is not set.
	 * This class should implement {@link IScheduler#isDefaultScheduler()} to return true.
	 * @throws OptionValidationException if {@code defaultSchedulerClass} does not have a public default
	 * constructor.
	 * @since 0.08
	 */
	public SchedulerOptionKey(Class<?> declaringClass,
		String name,
		Class<? extends IScheduler> defaultSchedulerClass,
		ScheduleValidatorOptionKey validatorKey)
	{
		super(declaringClass, name);
		_defaultSchedulerClass = defaultSchedulerClass;
		_validatorKey = validatorKey;
		validateClass(defaultSchedulerClass);
		assert(defaultValue().isDefaultScheduler());
	}

	/*--------------------
	 * IOptionKey methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation supports conversion from strings and {@link IScheduler} class instances.
	 * The string is used to look up the scheduler class from the current {@linkplain #getRegistry() registry}.
	 */
	@Override
	public IScheduler convertToValue(@Nullable Object value)
	{
		if (value instanceof String)
		{
			value = getRegistry().getClass((String)value);
		}

		if (value instanceof Class<?>)
		{
			value = SchedulerBase.instantiateClass(validateClass((Class<?>)value));
		}
		
		return super.convertToValue(value);
	}
	
	/**
	 * Returns newly constructed default instance.
	 * <p>
	 * Note that unlike most option keys, this will return a new instance everytime it is invoked!
	 */
	@Override
	public IScheduler defaultValue()
	{
		return SchedulerBase.instantiateClass(_defaultSchedulerClass);
	}
	
	@Override
	public void set(IOptionHolder holder, IScheduler value)
	{
		super.set(holder, value);
	}
	
	/**
	 * Sets the option locally on the {@code holder} by instantiating a new instance of {@code schedulerClass}.
	 * <p>
	 * @param holder is the object on which the option will be set locally.
	 * @param schedulerClass must either have a public no-argument constructor or must be an enum, in which
	 * case the first enumerated value will be used.
	 * @since 0.08
	 * @see #set(IOptionHolder, IScheduler)
	 */
	public void set(IOptionHolder holder, Class<? extends IScheduler> schedulerClass)
	{
		convertAndSet(holder, schedulerClass);
	}

	/**
	 * Sets the option locally on the {@code holder} by instantiating a new instance of {@code schedulerClass}.
	 * @param holder is the object on which the option will be set locally.
	 * @param schedulerClass is a string that is used to lookup the scheduler class. It may either be a
	 * fully qualified Java class name, or the simple class name listed in the {@linkplain #getRegistry()
	 * scheduler registry.}
	 * @since 0.08
	 * @see #set(IOptionHolder, Class)
	 */
	public void set(IOptionHolder holder, String schedulerClass)
	{
		convertAndSet(holder, schedulerClass);
	}

	@Override
	public Class<IScheduler> type()
	{
		return IScheduler.class;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns false if {@code scheduler} fails to {@linkplain IScheduler#validateForGraph(FactorGraph) validate}
	 * graph containing {@code delegator}.
	 */
	@Override
	public boolean validForDelegator(IScheduler scheduler, IOptionHolder delegator)
	{
		if (delegator instanceof IDimpleEventSource)
		{
			IDimpleEventSource source = (IDimpleEventSource)delegator;
			FactorGraph graph = source.getContainingGraph();
			if (graph != null)
			{
				try
				{
					scheduler.validateForGraph(graph);
				}
				catch (Exception ex)
				{
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public IScheduler validate(IScheduler scheduler, @Nullable IOptionHolder optionHolder)
	{
		FactorGraph graph = optionHolder instanceof FactorGraph ? (FactorGraph)optionHolder :
			optionHolder instanceof ISolverFactorGraph ? ((ISolverFactorGraph)optionHolder).getModelObject() : null;
		
		if (graph != null)
		{
			try
			{
				scheduler.validateForGraph(graph);
			}
			catch (Exception ex)
			{
				throw new OptionValidationException("%s cannot be used with %s: %s", scheduler, graph, ex);
			}
		}

		
		return super.validate(scheduler, optionHolder);
	}
	
	/*----------------------------
	 * SchedulerOptionKey methods
	 */
	
	/**
	 * The {@link Class} of the {@linkplain #defaultValue default scheduler value}.
	 * <p>
	 * Use this to check the default without instantiating an instance.
	 * @since 0.08
	 */
	public Class<? extends IScheduler> defaultClass()
	{
		return _defaultSchedulerClass;
	}
	
	/**
	 * Returns {@linkplain DimpleEnvironment#schedulers() scheduler registry} for
	 * {@linkplain DimpleEnvironment#active active environment}, which is used for
	 * locating known scheduler classes by name.
	 */
	public ConstructorRegistry<IScheduler> getRegistry()
	{
		return DimpleEnvironment.active().schedulers();
	}
	
	/**
	 * The option key, if any, for looking up the validator to be used on schedules for this class of scheduler.
	 * @since 0.08
	 */
	public @Nullable ScheduleValidatorOptionKey getValidatorKey()
	{
		return _validatorKey;
	}

	/*-----------------
	 * Private methods
	 */

	private Class<? extends IScheduler> validateClass(Class<?> schedulerClass)
	{
		try
		{
			return SchedulerBase.validateClass(schedulerClass);
		}
		catch (IllegalArgumentException ex)
		{
			throw new OptionValidationException("Not valid for option '%s': %s", this, ex.getMessage());
		}
	}
}
