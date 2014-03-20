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
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * A registry of known option keys indexed by qualified name. For use in looking up option keys
 * by string either for use in configuration files or in implementing external APIs (e.g. MATLAB).
 * 
 * @see OptionKey#qualifiedName(IOptionKey)
 */
@ThreadSafe
public class OptionRegistry
{
	/*-------
	 * State
	 */
	
	@GuardedBy("this")
	private final ConcurrentNavigableMap<String,IOptionKey<?>> _optionMap;
	
	private static final int publicStatic = Modifier.PUBLIC | Modifier.STATIC;
	private static final int publicStaticFinal = publicStatic | Modifier.FINAL;

	/*--------------
	 * Construction
	 */
	
	public OptionRegistry()
	{
		_optionMap = new ConcurrentSkipListMap<String,IOptionKey<?>>();
	}
	
	/*------------------------
	 * OptionRegistry methods
	 */
	
	/**
	 * Adds specified option key to the registry.
	 */
	public void add(IOptionKey<?> option)
	{
		_optionMap.put(OptionKey.qualifiedName(option), option);
	}
	
	/**
	 * Registers statically declared option key instances found reflectively in specified class.
	 * Adds all statically declared, final fields of type {@link IOptionKey} and if the
	 * class is an enum type that implements {@link IOptionKey}, it will add all of its
	 * instances. Also will recursively add from public nested classes.
	 * 
	 * @return the number of option keys that were added
	 */
	public int addFromClass(Class<?> declaringClass)
	{
		int nAdded = 0;
		if (declaringClass.isEnum() && IOptionKey.class.isAssignableFrom(declaringClass))
		{
			// Enum implements IOptionKey, so all of its instances are options
			// that can be registered.
			for (Object option : declaringClass.getEnumConstants())
			{
				add((IOptionKey<?>)option);
				++nAdded;
			}
		}
		
		
		for (Field field : declaringClass.getFields())
		{
			if ((field.getModifiers() & publicStaticFinal) == publicStaticFinal &&
				IOptionKey.class.isAssignableFrom(field.getType()))
			{
				try
				{
					add((IOptionKey<?>)field.get(declaringClass));
					++nAdded;
				}
				catch (IllegalAccessException ex)
				{
					// This shouldn't happen for public fields, but turn into RuntimeException if it does
					throw new RuntimeException(ex);
				}
			}
		}
		
		for (Class<?> innerClass : declaringClass.getDeclaredClasses())
		{
			if ((innerClass.getModifiers() & publicStatic) == publicStatic)
			{
				nAdded += addFromClass(innerClass);
			}
		}
		
		return nAdded;
	}
	
	/**
	 * Registers option key loaded using {@link OptionKey#forQualifiedName(String)}.
	 */
	public <T> IOptionKey<T> addFromQualifiedName(String qualifiedName)
	{
		IOptionKey<T> key = OptionKey.forQualifiedName(qualifiedName);
		add(key);
		return key;
	}
	
	/**
	 * Returns view of the contents of the registry as a sorted unmodifiable map.
	 */
	public SortedMap<String, IOptionKey<?>> asSortedMap()
	{
		return Collections.unmodifiableSortedMap(_optionMap);
	}
	
	/**
	 * Removes the entire contents of the registry.
	 */
	public void clear()
	{
		synchronized(this)
		{
			_optionMap.clear();
		}
	}
	
	/**
	 * Returns option key with specified name or null if not found.
	 * @see OptionKey#qualifiedName(IOptionKey)
	 */
	public IOptionKey<?> get(String qualifiedName)
	{
		return _optionMap.get(qualifiedName);
	}
	
	/**
	 * Returns a sorted map containing all keys whose qualified name matches the given
	 * regular expression pattern.
	 */
	public SortedMap<String, IOptionKey<?>> getAllMatching(Pattern pattern)
	{
		Matcher matcher = pattern.matcher("");
		SortedMap<String, IOptionKey<?>> results = new TreeMap<String, IOptionKey<?>>();
		
		for (Map.Entry<String,IOptionKey<?>> entry : _optionMap.entrySet())
		{
			final String key = entry.getKey();
			matcher.reset(key);
			if (matcher.matches())
			{
				results.put(key, entry.getValue());
			}
		}
		
		return results;
	}
	
	/**
	 * Returns a sorted map containing all keys whose qualified name matches the given
	 * regular expression pattern.
	 */
	public SortedMap<String, IOptionKey<?>> getAllMatching(String regexp)
	{
		return getAllMatching(Pattern.compile(regexp));
	}
	
	/**
	 * Returns a sorted map containing all keys whose qualified name begins with the specified string.
	 */
	public SortedMap<String, IOptionKey<?>> getAllStartingWith(String prefix)
	{
		return Collections.unmodifiableSortedMap(_optionMap.subMap(prefix, prefix + Character.MAX_VALUE));
	}
	
	/**
	 * Removes and returns key with given qualified name. Returns null if it was not present.
	 */
	public IOptionKey<?> remove(String qualifiedName)
	{
		return _optionMap.remove(qualifiedName);
	}

	/**
	 * Removes option key from registry.
	 * @return false if option key was not present.
	 */
	public boolean remove(IOptionKey<?> option)
	{
		return _optionMap.remove(OptionKey.qualifiedName(option), option);
	}
}
