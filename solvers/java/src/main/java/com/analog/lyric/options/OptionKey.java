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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;

/**
 * Base implementation of {@link IOptionKey} interface.
 * <p>
 * This is the preferred base class for implementing option key classes and also
 * provides useful static methods for naming and construction.
 * <p>
 * @param <T> is the type of  the values of the option indexed by this key.
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public abstract class OptionKey<T extends Serializable> implements IOptionKey<T>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final Class<?> _declaringClass;
	
	private final String _name;
	
	/*--------------
	 * Construction
	 */
	
	protected OptionKey(Class<?> declaringClass, String name)
	{
		_declaringClass = declaringClass;
		_name = name;
	}
	
	/**
	 * Returns key with specified {@code name} in {@code declaringClass} using Java reflection.
	 * <p>
	 * @see OptionRegistry#addFromClasses
	 * <p>
	 * @since 0.07
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> IOptionKey<T> inClass(Class<?> declaringClass, String name)
	{
		try
		{
			Field field = declaringClass.getDeclaredField(name);
			return (IOptionKey<T>)field.get(declaringClass);
		}
		catch (Exception ex)
		{
			throw new DimpleException("Error loading option key '%s' from '%s': %s",
				name, declaringClass, ex.toString());
		}
	}
	
	/**
	 * Returns key with specified {@code canonicalName} using Java reflection to load the
	 * declaring class.
	 * <p>
	 * Because this uses reflection it is not expected to be very fast so it is better to put keys
	 * in a {@link OptionRegistry} if you need frequent lookup by name.
	 * <p>
	 * @since 0.07
	 */
	public static <T extends Serializable> IOptionKey<T> forCanonicalName(String canonicalName)
	{
		int i = canonicalName.lastIndexOf('.');
		if (i < 0)
		{
			throw new DimpleException("'%s' is not a canonical option key name", canonicalName);
		}
		
		String className = canonicalName.substring(0, i);
		String name = canonicalName.substring(i + 1);
		
		Class<?> declaringClass;
		try
		{
			declaringClass = Class.forName(className, false, OptionKey.class.getClassLoader());
		}
		catch (ClassNotFoundException ex)
		{
			throw new DimpleException(ex);
		}
		
		return inClass(declaringClass, name);
	}
	
	/*-----------------------
	 * Serialization methods
	 */
	
	/**
	 * Replaces deserialized instance with canonical declaration in field.
	 * 
	 * @throws ObjectStreamException
	 */
	protected Object readResolve() throws ObjectStreamException
	{
		IOptionKey<T> canonical = getCanonicalInstance(this);
		return canonical != null ? canonical : this;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return qualifiedName();
	}
	
	/*--------------------
	 * IOptionKey methods
	 */
	
	@Override
	public T convertToValue(@Nullable Object value)
	{
		return type().cast(value);
	}
	
	@Override
	public Object convertToExternal(T value)
	{
		return value;
	}
	
	@Override
	public final Class<?> getDeclaringClass()
	{
		return _declaringClass;
	}
	
	@Override
	public final String name()
	{
		return _name;
	}
	
	@Override
	public T getOrDefault(IOptionHolder holder)
	{
		return holder.getOptionOrDefault(this);
	}
	
	@Override
	public @Nullable T get(IOptionHolder holder)
	{
		return holder.getOption(this);
	}
	
	@Override
	public void set(IOptionHolder holder, T value)
	{
		holder.setOption(this, value);
	}
	
	@Override
	public void unset(IOptionHolder holder)
	{
		holder.unsetOption(this);
	}
	
	@Override
	public T validate(T value, @Nullable IOptionHolder optionHolder)
	{
		return type().cast(value);
	}
	
	/*--------------------
	 * OptionKey methods
	 */
	
	/**
	 * Returns the canonical instance of {@code key}.
	 * <p>
	 * Returns the instance of the key that is publicly declared as
	 * a static field of {@link #getDeclaringClass()} with given {@link #name()}
	 * or returns null if there isn't one.
	 * <p>
	 * This method is implemented using reflection and is not expected to be very fast.
	 * <p>
	 * @since 0.07
	 */
	public static @Nullable<T extends Serializable> IOptionKey<T> getCanonicalInstance(IOptionKey<T> key)
	{
		IOptionKey<T> canonical = null;
		
		Class<?> declaringClass = key.getDeclaringClass();
		String name = key.name();
		
		try
		{
			@SuppressWarnings("unchecked")
			IOptionKey<T> option = (IOptionKey<T>)declaringClass.getDeclaredField(name).get(null);
			if (name.equals(option.name()) && option.type() == key.type())
			{
				canonical = option;
			}
		}
		catch (Exception ex)
		{
		}

		return canonical;
	}
	
	/**
	 * Computes the canonical name of the option.
	 * <p>
	 * Shorthand for
	 * <blockquote>
	 * {@linkplain #canonicalName(IOptionKey) OptionKey.canonicalName}(this)
	 * </blockquote>
	 * @since 0.07
	 */
	public String canonicalName()
	{
		return canonicalName(this);
	}
	
	/**
	 * Computes the fully qualified canonical name of the option.
	 * <p>
	 * This name uniquely identifies the option key. It consists of the fully qualified name of
	 * {@link #getDeclaringClass()} and {@link #name} separated by '.'. For instance:
	 * <blockquote>
	 * "com.analog.lyric.dimple.options.SolverOptions.iterations"
	 * </blockquote>
	 * 
	 * @param option is a non-null option key.
	 * @since 0.07
	 */
	public static String canonicalName(IOptionKey<?> option)
	{
		return String.format("%s.%s", option.getDeclaringClass().getName(), option.name());
	}
	
	/**
	 * Converts value and sets it in given option holder.
	 * <p>
	 * Invokes {@link #set} after value is converted by {@link #convertToValue}.
	 * <p>
	 * @param holder is a non-null option holder.
	 * @param value is a value that will be converted to the target value.
	 * @since 0.07
	 */
	public void convertAndSet(IOptionHolder holder, Object value)
	{
		set(holder, convertToValue(value));
	}
	
	/**
	 * Computes the option name qualified by its simple declaring class name.
	 * <p>
	 * Shorthand for
	 * <blockquote>
	 * {@linkplain #qualifiedName(IOptionKey) OptionKey.qualifiedName}(this)
	 * </blockquote>
	 * @since 0.07
	 */
	public String qualifiedName()
	{
		return qualifiedName(this);
	}
	
	/**
	 * Computes the option name qualified by its simple declaring class name.
	 * <p>
	 * This may be used to disambiguate option keys that may have the same simple name.
	 * The name consits of the {@linkplain Class#getSimpleName() simple name} of the option's
	 * {@linkplain IOptionKey#getDeclaringClass() declaring class} and the {@link #name} separated
	 * by '.'. For instance:
	 * <blockquote>
	 * "SolverOptions.iterations"
	 * </blockquote>
	 * <p>
	 * @param option is a non-null option key.
	 * @since 0.07
	 */
	public static String qualifiedName(IOptionKey<?> option)
	{
		return String.format("%s.%s", option.getDeclaringClass().getSimpleName(), option.name());
	}
}
