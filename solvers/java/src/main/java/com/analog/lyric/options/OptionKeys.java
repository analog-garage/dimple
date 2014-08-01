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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;

import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.util.misc.NonNullByDefault;
import com.analog.lyric.util.misc.Nullable;

/**
 * An immutable ordered list of option keys declared in a single class.
 * <p>
 * This object holds the {@link IOptionKey} instances declared in the {@link #declaringClass()}.
 * These will consist of the values of publicly accessible static final fields of type
 * {@link IOptionKey} whose name matches the declared name.
 * <p>
 * Construct using {@link #declaredInClass(Class)}.
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public final class OptionKeys extends AbstractList<IOptionKey<?>>
{
	/*------------
	 * Constants
	 */

	private static final int publicStaticFinal = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
	
	/*--------------
	 * Static state
	 */
	
	/**
	 * Used to cache value with declaring class.
	 */
	private static ClassValue<OptionKeys> optionKeysClassValue = new DeclaredOptionKeys();
	
	/*-------------
	 * Local state
	 */
	
	private final Class<?> _declaringClass;
	private final IOptionKey<?>[] _keys;
	
	/*--------------
	 * Construction
	 */
	
	private OptionKeys(Class<?> declaringClass, IOptionKey<?>[] keys)
	{
		_declaringClass = declaringClass;
		_keys = keys;
		Arrays.sort(_keys, IOptionKey.CompareByName.INSTANCE);
	}
	
	OptionKeys(Class<?> declaringClass)
	{
		this(declaringClass, keysFromClass(declaringClass));
	}
	
	/*----------------
	 * Static methods
	 */
	
	/**
	 * List of option keys declared publicly in given class.
	 * <p>
	 * This method will lazily compute the list using reflection the first time
	 * it is invoked for a given class and will subsequently cache and return the
	 * same object.
	 * <p>
	 * @param declaringClass
	 * @since 0.07
	 */
	public static OptionKeys declaredInClass(Class<?> declaringClass)
	{
		return optionKeysClassValue.get(declaringClass);
	}
	
	/**
	 * Iterates over {@link OptionKeys} for classes in hierarchy.
	 * <p>
	 * Returns an iterator that produces the {@link OptionKeys} for the classes starting with the
	 * {@code declaringClass} and on through the chain of superclasses
	 * upto but not including {@link OptionKeyDeclarer}.
	 * <p>
	 * @param declaringClass a subclass of {@link OptionKeyDeclarer}.
	 * @since 0.07
	 */
	public static ReleasableIterator<OptionKeys> declaredInHierarchy(Class<? extends OptionKeyDeclarer> declaringClass)
	{
		return HierarchicalOptionKeyIterator.create(declaringClass);
	}
	
	/*--------------------
	 * Collection methods
	 */

	@Override
	public IOptionKey<?> get(int index)
	{
		if (index < 0 || index >= _keys.length)
		{
			throw new IndexOutOfBoundsException();
		}
		
		return _keys[index];
	}
	
	@Override
	public int size()
	{
		return _keys.length;
	}
	
	/*--------------------
	 * OptionKeys methods
	 */
	
	/**
	 * The class containing the declarations of these option keys.
	 *
	 * @since 0.07
	 */
	public Class<?> declaringClass()
	{
		return _declaringClass;
	}
	
	/**
	 * @param name specifies the value of the option key's {@linkplain IOptionKey#name()} attribute.
	 * @return option key with given name or else null.
	 * @since 0.07
	 */
	public @Nullable IOptionKey<?> get(String name)
	{
		GenericOptionKey<?> key = new GenericOptionKey<Serializable>(_declaringClass, name, Serializable.class, "");
		final int i = Collections.binarySearch(this, key, IOptionKey.CompareByName.INSTANCE);
		return i >= 0 ? _keys[i] : null;
	}
	
	/*------------------------
	 * Private implementation
	 */
	
	private static class DeclaredOptionKeys extends ClassValue<OptionKeys>
	{
		@NonNullByDefault(false)
		@Override
		protected OptionKeys computeValue(Class<?> declaringClass)
		{
			return new OptionKeys(declaringClass);
		}
	}
	
	private static IOptionKey<?>[] keysFromClass(Class<?> declaringClass)
	{
		final List<IOptionKey<?>> keys = new ArrayList<IOptionKey<?>>();
		
		try
		{
			for (Field field : declaringClass.getDeclaredFields())
			{
				if (IOptionKey.class.isAssignableFrom(field.getType()))
				{
					final int modifiers = field.getModifiers();
					
					if (BitSetUtil.isMaskSet(modifiers, publicStaticFinal))
					{
						// public static final
						IOptionKey<?> option = (IOptionKey<?>)field.get(declaringClass);
						if (option.name().equals(field.getName()))
						{
							keys.add(option);
						}
						else
						{
							System.err.format("WARNING: option key name mismatch '%s' vs '%s' in class '%s'\n",
								field.getName(), option.name(), declaringClass.getName());
						}
					}
				}
			}
		}
		catch (IllegalAccessException ex)
		{
			// This shouldn't happen for public fields, but turn into RuntimeException if it does
			throw new RuntimeException(ex);
		}
		
		return keys.toArray(new IOptionKey<?>[keys.size()]);
	}
}
