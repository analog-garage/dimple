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

package com.analog.lyric.collect;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.reflect.ClassPath;

/**
 * Simple registry mapping class names to no-argument constructor instances.
 * <p>
 * Note that {@link Map} methods such as {@link #containsKey}, {@link #containsValue},
 * {@link #keySet}, {@link #values}, {@link #entrySet}, and {@link #size} will only reflect keys
 * that have been explicitly looked up using one of {@link #get}, {@link #getClass},
 * {@link #instantiate} or {@link #loadAll()}.
 * <p>
 * 
 * @param <T> is the super class of all of the classes in the registry.
 * @since 0.07
 * @author Christopher Barber
 */
@ThreadSafe
public class ConstructorRegistry<T> extends AbstractMap<String, Constructor<T>>
{
	private final Class<? super T> _superClass;

	@GuardedBy("this")
	protected final ArrayList<String> _packages;

	@GuardedBy("this")
	protected final Map<String, Constructor<T>> _nameToConstructor;

	/*--------------
	 * Construction
	 */

	/**
	 * Constructs a new registry instance for given super class.
	 * <p>
	 * The registry will automatically search for classes in the same package in which
	 * {@code superClass} is declared. Additional packages may be added using {@link #addPackage}.
	 * <p>
	 * 
	 * @param superClass is the runtime class type corresponding to the declared parameter type
	 *        {@code T}.
	 * @since 0.07
	 */
	public ConstructorRegistry(Class<? super T> superClass)
	{
		this(superClass, new String[] { superClass.getPackage().getName() });
	}

	/**
	 * Constructs a new registry instance for given super class.
	 * <p>
	 * 
	 * @param superClass is the runtime class type corresponding to the declared parameter type
	 *        {@code T}.
	 * @param packages are the packages in which to search for subclass implementations.
	 * @since 0.07
	 */
	public ConstructorRegistry(Class<? super T> superClass, String... packages)
	{
		_superClass = superClass;
		_packages = new ArrayList<String>();
		for (String packageName : packages)
		{
			_packages.add(packageName);
		}

		_nameToConstructor = new ConcurrentHashMap<>();
	}

	/*-------------
	 * Map methods
	 */

	@NonNullByDefault(false)
	@Override
	public boolean containsKey(Object simpleClassName)
	{
		return _nameToConstructor.containsKey(simpleClassName);
	}

	@Override
	@NonNullByDefault(false)
	public boolean containsValue(Object value)
	{
		return _nameToConstructor.containsValue(value);
	}

	@Override
	public Set<Map.Entry<String, Constructor<T>>> entrySet()
	{
		return Collections.unmodifiableSet(_nameToConstructor.entrySet());
	}

	/**
	 * Looks up no-argument constructor for named class.
	 * <p>
	 * Searches all of the registry's packages (see {@link #getPackages()}) for class with given
	 * {@code simpleClassName} and an accessible constructor that takes no arguments. Returns first
	 * match or null if none is found.
	 * <p>
	 * 
	 * @param simpleClassName is the unqualified name of the class whose constructor is sought.
	 */
	@SuppressWarnings("unchecked")
	@NonNullByDefault(false)
	@Override
	public synchronized @Nullable Constructor<T> get(Object simpleClassName)
	{
		String name = (String) simpleClassName;
		Constructor<T> constructor = _nameToConstructor.get(name);

		if (constructor == null)
		{
			ClassLoader loader = getClass().getClassLoader();

			for (String packageName : _packages)
			{
				String fullQualifiedName = packageName + "." + name;
				try
				{
					@SuppressWarnings("unchecked")
					Class<?> c = Class.forName(fullQualifiedName, false, loader);
					constructor = getConstructor(c);
					if (constructor != null)
					{
						_nameToConstructor.put(name, constructor);
						break;
					}
				}
				catch (Exception e)
				{
				}
			}
		}

		return constructor;
	}

	@Override
	public Set<String> keySet()
	{
		return Collections.unmodifiableSet(_nameToConstructor.keySet());
	}

	@Override
	public int size()
	{
		return _nameToConstructor.size();
	}

	@Override
	public Collection<Constructor<T>> values()
	{
		return Collections.unmodifiableCollection(_nameToConstructor.values());
	}

	/*---------------
	 * Local methods
	 */

	/**
	 * Adds entry for specified class.
	 * <p>
	 * 
	 * @param newClass
	 * @throws IllegalArgumentException if class is not a subclass of registry's
	 *         {@linkplain #getSuperClass() super class} or if it does not have a publicly
	 *         accessible constructor that takes no arguments.
	 * @since 0.07
	 * @see #addPackage
	 */
	public synchronized void addClass(Class<?> newClass)
	{
		final String name = newClass.getSimpleName();

		if (!_superClass.isAssignableFrom(newClass))
		{
			throw new IllegalArgumentException(String.format("%s is not a subclass of %s",
				name, _superClass.getSimpleName()));
		}
		
		Constructor<T> constructor = getConstructor(newClass);
		if (constructor == null)
		{
			throw new IllegalArgumentException(String.format("%s does not have an accessible no-argument constructor",
				name));
		}
		_nameToConstructor.put(name, constructor);
	}

	/**
	 * Adds a package to search for class implementations.
	 * <p>
	 * 
	 * @param packageName is a fully qualified Java package name expected to contain subclasses of
	 *        declared superclass {@code T}.
	 * @see #get(Object)
	 * @since 0.07
	 * @see #addClass
	 * @see #getPackages()
	 */
	public synchronized void addPackage(String packageName)
	{
		_packages.add(packageName);
	}

	/**
	 * Returns class type named by {@code simpleClassName}.
	 * <p>
	 * Simply returns {@linkplain Constructor#getDeclaringClass() declaring class} of value returned
	 * by {@link #get}.
	 * 
	 * @throws RuntimeException if no such class can be found.
	 * @since 0.07
	 */
	public Class<? extends T> getClass(String simpleClassName)
	{
		Class<? extends T> c = getClassOrNull(simpleClassName);
		if (c == null)
		{
			throw noMatchingClass(simpleClassName);
		}
		return c;
	}

	/**
	 * Returns class type named by {@code simpleClassName}.
	 * <p>
	 * Simply returns {@linkplain Constructor#getDeclaringClass() declaring class} of value returned
	 * by {@link #get} or else null.
	 * 
	 * @since 0.07
	 */
	@Nullable
	public Class<? extends T> getClassOrNull(String simpleClassName)
	{
		Constructor<T> constructor = get(simpleClassName);
		return constructor != null ? constructor.getDeclaringClass() : null;
	}

	/**
	 * Returns copy of list of package names searched by this registry.
	 * <p>
	 * 
	 * @since 0.07
	 * @see #addPackage(String)
	 * @see #get(Object)
	 */
	public synchronized String[] getPackages()
	{
		return _packages.toArray(new String[_packages.size()]);
	}

	/**
	 * Class type of declared type {@code T}.
	 * 
	 * @since 0.07
	 */
	public Class<? super T> getSuperClass()
	{
		return _superClass;
	}

	/**
	 * Instantiates an instance of named class.
	 * <p>
	 * Simply invokes {@link Constructor#newInstance} on constructor returned by {@link #get}.
	 * 
	 * @throws RuntimeException if no such class can be found.
	 * @since 0.07
	 */
	public T instantiate(String simpleClassName)
	{
		T instance = instantiateOrNull(simpleClassName);
		if (instance == null)
		{
			throw noMatchingClass(simpleClassName);
		}
		return instance;
	}

	/**
	 * Instantiates an instance of named class.
	 * <p>
	 * Simply invokes {@link Constructor#newInstance} on constructor returned by {@link #get} or
	 * else returns null.
	 * 
	 * @since 0.07
	 */
	@Nullable
	public T instantiateOrNull(String simpleClassName)
	{
		Constructor<T> constructor = get(simpleClassName);
		if (constructor != null)
		{
			try
			{
				return constructor.newInstance();
			}
			catch (InvocationTargetException ex)
			{
				throw new RuntimeException(ex.getCause());
			}
			catch (ReflectiveOperationException ex)
			{
				throw new RuntimeException(ex);
			}
		}

		return null;
	}

	/**
	 * Preloads all subclasses of declared superclass {@code T} found in registry's packages.
	 * <p>
	 * Searches all of the packages in {@link #getPackages()} for subclasses of {@code T} and adds
	 * then to the registry.
	 * <p>
	 * 
	 * @since 0.07
	 */
	public synchronized void loadAll()
	{
		ClassLoader loader = getClass().getClassLoader();

		ClassPath path;
		try
		{
			path = ClassPath.from(loader);
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
		for (String packageName : _packages)
		{
			for (ClassPath.ClassInfo info : path.getTopLevelClasses(packageName))
			{
				Constructor<T> constructor = getConstructor(info.load());
				if (constructor != null)
				{
					_nameToConstructor.put(info.getSimpleName(), constructor);
				}
			}
		}
	}

	/*-----------------
	 * Private methods
	 */

	@SuppressWarnings("unchecked")
	private @Nullable Constructor<T> getConstructor(Class<?> type)
	{
		Constructor<T> constructor = null;

		int modifiers = type.getModifiers();
		if (Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers) && _superClass.isAssignableFrom(type))
		{
			try
			{
				constructor = (Constructor<T>) type.getConstructor();
				modifiers = constructor.getModifiers();
				if (!Modifier.isPublic(modifiers))
				{
					constructor = null;
				}
			}
			catch (Exception ex)
			{
				// Ignore
			}
		}

		return constructor;
	}

	private RuntimeException noMatchingClass(String simpleClassName)
	{
		return new RuntimeException(String.format(
			"Cannot find class named '%s' with accessible no-argument constructor", simpleClassName));
	}

}