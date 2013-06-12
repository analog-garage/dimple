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
	
	public void add(IOptionKey<?> option)
	{
		_optionMap.put(OptionKey.qualifiedName(option), option);
	}
	
	/**
	 * Registers statically declared option instances found reflectively in specified class.
	 * Adds all statically declared, final fields of type {@link IOptionKey} and if the
	 * class is an enum type that implements {@link IOptionKey}, it will add all of its
	 * instances.
	 */
	public int addFromClass(Class<?> c)
	{
		int nAdded = 0;
		if (c.isEnum() && IOptionKey.class.isAssignableFrom(c))
		{
			// Enum implements IOptionKey, so all of its instances are options
			// that can be registered.
			for (Object option : c.getEnumConstants())
			{
				add((IOptionKey<?>)option);
				++nAdded;
			}
		}
		
		
		for (Field field : c.getFields())
		{
			if ((field.getModifiers() & publicStaticFinal) == publicStaticFinal &&
				IOptionKey.class.isAssignableFrom(field.getType()))
			{
				try
				{
					add((IOptionKey<?>)field.get(c));
					++nAdded;
				}
				catch (IllegalAccessException ex)
				{
					// This shouldn't happen for public fields, but turn into RuntimeException if it does
					throw new RuntimeException(ex);
				}
			}
		}
		
		for (Class<?> innerClass : c.getDeclaredClasses())
		{
			if ((innerClass.getModifiers() & publicStatic) == publicStatic)
			{
				addFromClass(innerClass);
			}
		}
		
		return nAdded;
	}
	
	public <T> IOptionKey<T> addFromQualifiedName(String qualifiedName)
	{
		IOptionKey<T> key = OptionKey.forQualifiedName(qualifiedName);
		add(key);
		return key;
	}
	
	/**
	 * Returns view of the contents of the registry as a sorted ummodifiable map.
	 */
	public SortedMap<String, IOptionKey<?>> asSortedMap()
	{
		return Collections.unmodifiableSortedMap(_optionMap);
	}
	
	public void clear()
	{
		synchronized(this)
		{
			_optionMap.clear();
		}
	}
	
	public IOptionKey<?> get(String key)
	{
		return _optionMap.get(key);
	}
	
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
	
	public SortedMap<String, IOptionKey<?>> getAllMatching(String regexp)
	{
		return getAllMatching(Pattern.compile(regexp));
	}
	
	public SortedMap<String, IOptionKey<?>> getAllStartingWith(String prefix)
	{
		return Collections.unmodifiableSortedMap(_optionMap.subMap(prefix, prefix + Character.MAX_VALUE));
	}
	
	public IOptionKey<?> remove(String key)
	{
		return _optionMap.remove(key);
	}
	
	public boolean remove(IOptionKey<?> option)
	{
		return _optionMap.remove(OptionKey.qualifiedName(option), option);
	}
}
