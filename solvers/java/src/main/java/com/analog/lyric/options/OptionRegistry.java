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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.ObjectArrays;

/**
 * A registry of known option keys indexed by qualified name.
 * <p>
 * For use in looking up option keys by string either for use in configuration files or in
 * implementing external APIs (e.g. MATLAB).
 * <p>
 * @since 0.07
 */
@ThreadSafe
public class OptionRegistry implements Iterable<IOptionKey<?>>
{
	/*-------
	 * State
	 */
	
	/**
	 * Maps canonical class names to {@link OptionKeys}.
	 * <p>
	 * Writes are protected by lock. Reads are not.
	 */
	@GuardedBy("this")
	private final ConcurrentNavigableMap<String, OptionKeys> _canonicalMap;
	
	/**
	 * Maps simple class names to {@link OptionKeys}
	 * <p>
	 * Unlike {@link #_canonicalMap}, this may map name to keys for multiple
	 * classes with the same simple name.
	 * <p>
	 * Writes are protected by lock. Reads are not.
	 */
	@GuardedBy("this")
	private final ConcurrentNavigableMap<String, OptionKeys[]> _simpleMap;
	
	private final boolean _autoLoad;
	
	@GuardedBy("this")
	private volatile int _size;
	
	private static final int publicStatic = Modifier.PUBLIC | Modifier.STATIC;

	/*--------------
	 * Construction
	 */
	
	/**
	 * Create a new empty registry.
	 * <p>
	 * @param autoLoad determines setting of {@link #autoLoadKeys()}.
	 * @since 0.07
	 */
	public OptionRegistry(boolean autoLoad)
	{
		_autoLoad = autoLoad;
		_canonicalMap = new ConcurrentSkipListMap<String, OptionKeys>();
		_simpleMap = new ConcurrentSkipListMap<String,OptionKeys[]>();
	}
	
	/**
	 * Create a new empty registry with autoloading enabled.
	 * @see #OptionRegistry(boolean)
	 * @since 0.07
	 */
	public OptionRegistry()
	{
		this(true);
	}
	
	/*------------------
	 * Iterable methods
	 */
	
	/**
	 * Iterates over option keys that are currently registered with this object in
	 * order of {@linkplain OptionKey#canonicalName(IOptionKey) canonical name}.
	 */
	@Override
	public Iterator<IOptionKey<?>> iterator()
	{
		return new OptionIterator();
	}
	
	private class OptionIterator extends AbstractIterator<IOptionKey<?>>
	{
		private final Iterator<OptionKeys> _keysIterator;
		private volatile Iterator<IOptionKey<?>> _keyIterator;
		
		private OptionIterator()
		{
			_keysIterator = _canonicalMap.values().iterator();
			_keyIterator = Iterators.emptyIterator();
		}
		
		@Override
		protected @Nullable IOptionKey<?> computeNext()
		{
			Iterator<IOptionKey<?>> keyIter = _keyIterator;
				
			while (!keyIter.hasNext())
			{
				if (_keysIterator.hasNext())
				{
					_keyIterator = keyIter = _keysIterator.next().values().iterator();
				}
				else
				{
					endOfData();
					return null;
				}
			}
			
			return keyIter.next();
		}
	}
	
	/*------------------------
	 * OptionRegistry methods
	 */
	
	/**
	 * Adds option keys to registry.
	 * <p>
	 * @return true if registry was changed (keys were not already in registry).
	 * @since 0.07
	 */
	public boolean add(OptionKeys keys)
	{
		Class<?> declaringClass = keys.declaringClass();
		
		synchronized (this)
		{
			if (null != _canonicalMap.put(declaringClass.getCanonicalName(), keys))
			{
				return false;
			}
			
			OptionKeys[] array = new OptionKeys[] { keys };
			final String classname = declaringClass.getSimpleName();
			
			OptionKeys[] existingArray = _simpleMap.get(classname);
			if (existingArray == null)
			{
				_simpleMap.put(classname, array);
			}
			else
			{
				_simpleMap.put(classname, ObjectArrays.concat(existingArray, keys));
			}
			
			_size += keys.values().size();
		}

		return true;
	}
	
	/**
	 * Registers statically declared option key instances found reflectively in specified classes.
	 * Adds all statically declared, final fields of type {@link IOptionKey} whose {@linkplain IOptionKey#name() name}
	 * attribute matches its declared name. Also will recursively add from public nested classes.
	 * 
	 * @return the number of unique option keys that were added
	 * @since 0.07
	 */
	public int addFromClasses(Class<?> ... declaringClasses)
	{
		int nAdded = 0;
		
		for (Class<?> declaringClass : declaringClasses)
		{
			OptionKeys keys = OptionKeys.declaredInClass(declaringClass);
			int nAddedFromClass = keys.values().size();

			if (nAddedFromClass > 0 && !add(keys))
			{
				nAddedFromClass = 0;
			}

			for (Class<?> innerClass : declaringClass.getDeclaredClasses())
			{
				if ((innerClass.getModifiers() & publicStatic) == publicStatic)
				{
					nAddedFromClass += addFromClasses(innerClass);
				}
			}
			
			nAdded += nAddedFromClass;
		}
		
		return nAdded;
	}
	
	/**
	 * Returns key for given qualified name or throws an error.
	 * <p>
	 * @param keyOrName either a {@link IOptionKey} instance which will simply be returned or
	 * a {@link String} compatible with {@link #get(String)}.
	 * @throws NoSuchElementException if key not found or input argument is not the correct type.
	 * @throws IllegalArgumentException if {@code keyOrName} does not have the correct type.
	 * @since 0.07
	 */
	public IOptionKey<?> asKey(Object keyOrName)
	{
		IOptionKey<?> key;
		
		if (keyOrName instanceof String)
		{
			String name = (String)keyOrName;
			key = get(name);
			if (key == null)
			{
				throw new NoSuchElementException(String.format("Unknown option key '%s'", name));
			}
			else
			{
				return key;
			}
		}
		else if (keyOrName instanceof IOptionKey)
		{
			key = (IOptionKey<?>)keyOrName;
		}
		else
		{
			throw new IllegalArgumentException(
				String.format("Expected String or IOptionKey instead of '%s'",
					keyOrName.getClass().getSimpleName()));
		}
		
		return key;
	}

	/**
	 * If true, registry will attempt to automatically load fully qualified keys from classes.
	 * <p>
	 * For instance, if attempting to get key for the string:
	 * 
	 * <blockquote>
	 * "com.mycompany.MyOptions.myOption"
	 * </blockquote>
	 * 
	 * the registry will automatically load all option keys declared in the class {@code MyOptions}.
	 * This will only work for fully qualified names whose classes are on the class path.
	 * <p>
	 * This attribute is set in the constructor.
	 * @since 0.07
	 */
	public boolean autoLoadKeys()
	{
		return _autoLoad;
	}
	
	/**
	 * Returns option key with specified name or null if not found.
	 * <p>
	 * If {@link #autoLoadKeys()} is enabled and {@code name} is a full canonical name then
	 * the key will be loaded automatically by loading the class and adding all of its declared
	 * options.
	 * <p>
	 * @param name is either the {@linkplain OptionKey#canonicalName(IOptionKey) canonical name} or
	 * {@linkplain OptionKey#qualifiedName(IOptionKey) qualified name} of the option.
	 * @throws AmbiguousOptionNameException if more than one possible option key matches
	 * the given name. This will not happen if a canonical name is used.
	 * The list of possible matches can be found in the exception.
	 * @see OptionKey#canonicalName(IOptionKey)
	 * @since 0.07
	 */
	public @Nullable IOptionKey<?> get(String name)
	{
		int i = name.lastIndexOf('.');
	
		if (i >= 0)
		{
			String className = name.substring(0, i);
			String optionName = name.substring(i + 1);

			if (className.indexOf('.') >= 0)
			{
				// Should be canonical class name
				OptionKeys keys = _canonicalMap.get(className);

				if (keys == null && _autoLoad)
				{
					try
					{
						Class<?> c = Class.forName(className, false, getClass().getClassLoader());
						add(keys = OptionKeys.declaredInClass(c));
					}
					catch (ClassNotFoundException ex)
					{
					}
				}
				
				if (keys != null)
				{
					return keys.get(optionName);
				}
			}
			else
			{
				OptionKeys[] keysArray = _simpleMap.get(className);

				if (keysArray != null)
				{
					if (keysArray.length == 1)
					{
						return keysArray[0].get(optionName);
					}
					else
					{
						ArrayList<IOptionKey<?>> options = new ArrayList<IOptionKey<?>>();
						for (OptionKeys keys : keysArray)
						{
							IOptionKey<?> option = keys.get(optionName);
							if (option != null)
							{
								options.add(option);
							}
						}
						
						switch (options.size())
						{
						case 0:
							break;
						case 1:
							return options.get(0);
						default:
							throw new AmbiguousOptionNameException(name, options);
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Returns a newly constructed list of keys matching regular expression.
	 * <p>
	 * The will contain all keys whose {@linkplain OptionKey#qualifiedName(IOptionKey) qualified name} matches the
	 * given regular expression pattern sorted by name.
	 * @since 0.07
	 */
	public ArrayList<IOptionKey<?>> getAllMatching(Pattern pattern)
	{
		Matcher matcher = pattern.matcher("");
		ArrayList<IOptionKey<?>> results = new ArrayList<IOptionKey<?>>();
		
		for (IOptionKey<?> key : this)
		{
			matcher.reset(OptionKey.qualifiedName(key));
			if (matcher.matches())
			{
				results.add(key);
			}
		}
		
		return results;
	}
	
	/**
	 * Returns a newly constructed list of keys matching regular expression.
	 * <p>
	 * The will contain all keys whose {@linkplain OptionKey#qualifiedName(IOptionKey) qualified name} matches the
	 * given regular expression pattern sorted by name.
	 * @since 0.07
	 */
	public ArrayList<IOptionKey<?>> getAllMatching(String regexp)
	{
		return getAllMatching(Pattern.compile(regexp));
	}
	
	/**
	 * Iterates over {@link OptionKeys} registered with this object.
	 * @since 0.07
	 */
	public Collection<OptionKeys> getOptionKeys()
	{
		return Collections.unmodifiableCollection(_canonicalMap.values());
	}
	
	/**
	 * The number of entries in the registry.
	 * <p>
	 * @since 0.07
	 */
	public int size()
	{
		return _size;
	}

}
