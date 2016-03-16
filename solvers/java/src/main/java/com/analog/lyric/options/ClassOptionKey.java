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

import net.jcip.annotations.Immutable;

/**
 * Key for options with literal Java class values.
 * <p>
 * @param <SuperClass> is the super class value. Values of this option must be a
 * subclass of this class. The superclass instance may be obtained using the {@link #superClass()} method.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class ClassOptionKey<SuperClass> extends OptionKey<Class<? extends SuperClass>>
{
	private static final long serialVersionUID = 1L;
	
	private final Class<SuperClass> _superClass;
	private final Class<? extends SuperClass> _defaultValue;
	
	/**
	 * Construct a class option key.
	 * @param declaringClass is the class containing the static field declaration for this key.
	 * @param name is the name of static field declaration for this key.
	 * @param superClass is the superclass of all valid values. Should be same as the declared
	 * &lt;SuperClass&gt; parameter.
	 * @param defaultValue is the default value of the option. Used when option is not set.
	 * @since 0.07
	 */
	public ClassOptionKey(Class<?> declaringClass, String name, Class<SuperClass> superClass,
		Class<? extends SuperClass> defaultValue)
	{
		super(declaringClass, name);
		_superClass = superClass;
		_defaultValue = defaultValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Class<? extends SuperClass>> type()
	{
		return (Class<Class<? extends SuperClass>>) _superClass.getClass();
	}

	@Override
	public Class<? extends SuperClass> defaultValue()
	{
		return _defaultValue;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns fully qualified name of the class {@code value}.
	 */
	@Override
	public Object convertToExternal(Class<? extends SuperClass> value)
	{
		return value.getName();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * If {@code value} is a string. This will attempt to load the class from the string using
	 * {@linkplain Class#forName(String, boolean, ClassLoader) Class.forName} with context class loader.
	 * Subclasses may want to extend this to allow construction from simple
	 * class names.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends SuperClass> convertToValue(@Nullable Object value)
	{
		if (value instanceof String)
		{
			try
			{
				value = Class.forName((String)value, false, getClass().getClassLoader());
			}
			catch (ClassNotFoundException ex)
			{
				throw new OptionValidationException(ex, "Could not construct class from string '%s': %s", value, ex);
			}
		}
		
		return super.convertToValue(value);
	}
	
	@Override
	public Class<? extends SuperClass> validate(Class<? extends SuperClass> value, @Nullable IOptionHolder optionHolder)
	{
		if (!_superClass.isAssignableFrom(value))
		{
			throw new OptionValidationException("'%s' is not a subclass of '%s'", value, _superClass);
		}
		
		return value;
	}
	
	/**
	 * The value of the &lt;SuperClass&gt; parameter.
	 * @since 0.07
	 */
	public Class<SuperClass> superClass()
	{
		return _superClass;
	}
	
}
