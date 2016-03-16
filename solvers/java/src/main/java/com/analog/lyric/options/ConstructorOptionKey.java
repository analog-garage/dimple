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

package com.analog.lyric.options;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ConstructorRegistry;

import net.jcip.annotations.Immutable;

/**
 * Key for options with literal Java class values with accessible no-argument constructor.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public abstract class ConstructorOptionKey<SuperClass> extends ClassOptionKey<SuperClass>
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct a class option key.
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param superClass is the superclass of all valid values. Should be same as the declared
	 * &lt;SuperClass&gt; parameter.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @since 0.07
	 */
	public ConstructorOptionKey(Class<?> declaringClass,
		String name,
		Class<SuperClass> superClass,
		Class<? extends SuperClass> defaultValue)
	{
		super(declaringClass, name, superClass, defaultValue);
	}

	/*--------------------
	 * IOptionKey methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns simple name of class {@code value} if in {@linkplain #getRegistry() registry}
	 * and otherwise returns the fully qualified class name.
	 */
	@Override
	public Object convertToExternal(Class<? extends SuperClass> value)
	{
		String name = value.getSimpleName();
		if (getRegistry().getClassOrNull(name) != value)
		{
			name = value.getName();
		}
		
		return name;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * If the {@code value} is a string, this will attempt to look the class up in this
	 * key's {@linkplain #getRegistry() registry} and otherwise will attempt
	 * to load the class using {@linkplain Class#forName(String, boolean, ClassLoader) Class.forName}
	 * with context class loader.
	 */
	@Override
	public Class<? extends SuperClass> convertToValue(@Nullable Object value)
	{
		if (value instanceof String)
		{
			Class<? extends SuperClass> c = getRegistry().getClassOrNull((String)value);
			if (c != null)
			{
				return c;
			}
		}
		
		return super.convertToValue(value);
	}
	
	@Override
	public Class<? extends SuperClass> validate(Class<? extends SuperClass> value, @Nullable IOptionHolder optionHolder)
	{
		Class<? extends SuperClass> c = super.validate(value, optionHolder);
		
		// Verify that class has a no-argument constructor
		try
		{
			c.getConstructor();
		}
		catch (Exception ex)
		{
			throw new OptionValidationException("Class '%s' does not have an accessible no argument constructor.", c);
		}
		
		return c;
	}
	
	/*---------------
	 * Local methods
	 */
	
	/**
	 * The constructor registry associated with this key, if any.
	 * <p>
	 * @see #convertToValue(Object)
	 * @since 0.07
	 */
	public abstract ConstructorRegistry<SuperClass> getRegistry();

	/**
	 * Instantiates new instance of class that is the current value of this option.
	 * <p>
	 * Looks up value of class using {@link #getOrDefault} and instantiates
	 * it using {@link Class#newInstance()} with no arguments.
	 * <p>
	 * @param holder is a non-null option holder.
	 * @since 0.07
	 */
	public SuperClass instantiate(IOptionHolder holder)
	{
		try
		{
			return getOrDefault(holder).newInstance();
		}
		catch (InstantiationException | IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns instance of class that is the current value of this option, instantiating if
	 * different from existing value.
	 * <p>
	 * This is similar to {@link #instantiate} but will return {@code prev} if it its
	 * non-null and is already an instance of the class retrieved by {@link #getOrDefault}.
	 * <p>
	 * @param holder is a non-null option holder.
	 * @param prev is a possibly-null existing instance.
	 * @since 0.07
	 */
	public SuperClass instantiateIfDifferent(IOptionHolder holder, @Nullable SuperClass prev)
	{
		Class<? extends SuperClass> c = getOrDefault(holder);
		
		if (prev != null && prev.getClass() == c)
		{
			return prev;
		}
		
		try
		{
			return c.newInstance();
		}
		catch (InstantiationException | IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
