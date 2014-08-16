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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.Immutable;

import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.collect.ReleasableIterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An immutable map of option keys declared in a single class.
 * <p>
 * This provides a way to look up related option keys by their identifier name. However,
 * when working with keys that can declared in multiple classes, you will typically want to make use
 * of an {@link OptionRegistry} for looking up options by name.
 * <p>
 * This object maps simple identifier strings to {@link IOptionKey} instances declared in the
 * {@link #declaringClass()} associated with this object. This will include mappings for
 *  every publicly accessible static final fields of {@link IOptionKey} that refer to
 *  a "canonical instance" of the key (an instance for which {@link OptionKey#getCanonicalInstance} returns
 *  the instance itself).
 * <p>
 * Instances are obtained using {@link #declaredInClass(Class)}.
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public final class OptionKeys extends AbstractMap<String, IOptionKey<?>>
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
	
	/**
	 * The keys sorted in order of their {@link IOptionKey#name} attribute.
	 */
	private final List<IOptionKey<?>> _keys;
	
	private final Map<String,IOptionKey<?>> _nameToKey;
	
	private final int _hashCode;
	
	/*--------------
	 * Construction
	 */
	
	OptionKeys(Class<?> declaringClass)
	{
		_declaringClass = declaringClass;
		_nameToKey = new HashMap<String,IOptionKey<?>>();
		
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
						if (option == OptionKey.getCanonicalInstance(option))
						{
							String name = field.getName();
							_nameToKey.put(name, option);
							if (name == option.name())
							{
								// If names match, then this is the canonical instance of the name/key mapping.
								keys.add(option);
							}
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

		Collections.sort(keys, IOptionKey.CompareByName.INSTANCE);
		_keys = Collections.unmodifiableList(keys);
		_hashCode = _nameToKey.hashCode();
	}
	
	/*----------------
	 * Static methods
	 */
	
	/**
	 * Option keys declared publicly in given class.
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
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public int hashCode()
	{
		// The object is immutable, so there is no reason to compute the hash code every time.
		return _hashCode;
	}

	/*--------------
	 * Map methods
	 */

	@Override
	public OptionKeys clone()
	{
		// There is only one instance per declaring class so this is just the same object.
		return this;
	}
	
	@NonNullByDefault(false)
	@Override
	public boolean containsKey(Object name)
	{
		return _nameToKey.containsKey(name);
	}
	
	@NonNullByDefault(false)
	@Override
	public boolean containsValue(Object value)
	{
		return value instanceof IOptionKey<?> && get(((IOptionKey<?>)value).name()) == value;
	}
	
	@Override
	public Set<Map.Entry<String, IOptionKey<?>>> entrySet()
	{
		return Collections.unmodifiableMap(_nameToKey).entrySet();
	}
	
	@Override
	public Set<String> keySet()
	{
		return Collections.unmodifiableSet(_nameToKey.keySet());
	}
	
	/**
	 * The number of name to {@link IOptionKey} mappings in this object.
	 * <p>
	 * Note that if there can be more than one name mapped to the same key (although only
	 * one will be the canonical one), so this number can be higher than {@link #uniqueSize()}.
	 */
	@Override
	public int size()
	{
		return _nameToKey.size();
	}
	
	/**
	 * Returns an immutable list of unique option keys in the map.
	 * <p>
	 * The list is backed by an array, so random access is fast, and
	 * keys are ordered by their {@linkplain IOptionKey#name name}.
	 */
	@Override
	public List<IOptionKey<?>> values()
	{
		return _keys;
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
	 * @param name specifies the value of the option key's {@linkplain IOptionKey#name() name} attribute.
	 * @return option key with given name or else null.
	 * @since 0.07
	 */
	public @Nullable IOptionKey<?> get(String name)
	{
		return _nameToKey.get(name);
	}
	
	/**
	 * The number of unique keys in this object.
	 * <p>
	 * This is the same as the size of the {@link #values()} list and is the same
	 * as the number of mappings for which the name matches the key's {@linkplain IOptionKey#name name}.
	 * @since 0.07
	 */
	public int uniqueSize()
	{
		return _keys.size();
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
}
