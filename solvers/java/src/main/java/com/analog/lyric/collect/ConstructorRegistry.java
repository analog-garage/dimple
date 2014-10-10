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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.reflect.ClassPath;

/**
 * Simple registry mapping class names to constructor instances.
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
	protected final ArrayListMultimap<String, Constructor<T>> _nameToConstructors;

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

		_nameToConstructors = ArrayListMultimap.create();
	}

	/*-------------
	 * Map methods
	 */

	@NonNullByDefault(false)
	@Override
	public boolean containsKey(Object simpleClassName)
	{
		return _nameToConstructors.containsKey(simpleClassName);
	}

	@Override
	@NonNullByDefault(false)
	public boolean containsValue(Object value)
	{
		return _nameToConstructors.containsValue(value);
	}

	@Override
	public Set<Map.Entry<String, Constructor<T>>> entrySet()
	{
		return Collections.unmodifiableSet(new HashSet<>(_nameToConstructors.entries()));
	}

	/**
	 * Looks up no-argument constructor for named class.
	 * <p>
	 * If {@code className} is a fully qualified name referring to a class with an accessible constructor
	 * that takes no arguments, that constructor will be returned. Otherwise, this will
	 * searches all of the registry's packages (see {@link #getPackages()}) for class with given
	 * {@code className} and compatible constructor. Returns first match or null if none is found.
	 * <p>
	 * @param className is either the unqualified or fully qualified name of the class whose constructor is sought.
	 * @see #get(String, Class[])
	 */
	@NonNullByDefault(false)
	@Override
	public final @Nullable Constructor<T> get(Object className)
	{
		if (className instanceof String)
		{
			return get((String)className, ArrayUtil.EMPTY_CLASS_ARRAY);
		}
		
		return null;
	}
	
	@Override
	public Set<String> keySet()
	{
		return Collections.unmodifiableSet(_nameToConstructors.keySet());
	}

	@Override
	public int size()
	{
		return _nameToConstructors.size();
	}

	@Override
	public Collection<Constructor<T>> values()
	{
		return Collections.unmodifiableCollection(_nameToConstructors.values());
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
	public void addClass(Class<?> newClass)
	{
		final String name = newClass.getSimpleName();

		if (!_superClass.isAssignableFrom(newClass))
		{
			throw new IllegalArgumentException(String.format("%s is not a subclass of %s",
				name, _superClass.getSimpleName()));
		}
		
		if (addConstructorsFrom(newClass, true).isEmpty())
		{
			throw new IllegalArgumentException(String.format("%s does not have an accessible constructor",
				name));
		}
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
	 * Looks up constructor for named class with specified formal parameters.
	 * <p>
	 * If {@code className} is a fully qualified name referring to a class with an accessible constructor
	 * that takes no arguments, that constructor will be returned. Otherwise, this will
	 * searches all of the registry's packages (see {@link #getPackages()}) for class with given
	 * {@code className} and compatible constructor. Returns first match or null if none is found.
	 * <p>
	 * @param className is either the unqualified or fully qualified name of the class whose constructor is sought.
	 * @param formalParameters are the declared types of the parameters to the constructor. Note that these must match
	 * exactly.
	 * @see #get(String, Class[])
	 */
	public final @Nullable Constructor<T> get(String className, Class<?>[] formalParameters)
	{
		List<Constructor<T>> constructors = getAll(className);

		for (Constructor<T> constructor : constructors)
		{
			if (Arrays.equals(formalParameters, constructor.getParameterTypes()))
			{
				return constructor;
			}
		}
		
		return null;
	}
	

	/**
	 * Returns list of all public constructors for class with given name.
	 * @param className is either a simple or fully qualified class name. If a simple name and the class
	 * has not already been loaded, this will search {@link #getPackages()} for a match.
	 * @return non-null list of constructors, which may be empty.
	 * @see #get
	 * @since 0.07
	 */
	public List<Constructor<T>> getAll(String className)
	{
		String name = className;
		
		List<Constructor<T>> constructors = Collections.emptyList();
		
		synchronized(this)
		{
			constructors = _nameToConstructors.get(name);
		}

		if (constructors.isEmpty())
		{
			ClassLoader loader = getClass().getClassLoader();

			if (name.indexOf('.') > 0)
			{
				// Looks like it is qualified with a package name.
				try
				{
					constructors = addConstructorsFrom(Class.forName(name, false, loader), false);
				}
				catch (Exception e)
				{
				}
			}
			
			// Search packages for a matching class
			for (String packageName : _packages)
			{
				String fullQualifiedName = packageName + "." + name;
				try
				{
					constructors = addConstructorsFrom(Class.forName(fullQualifiedName, false, loader), true);
					if (!constructors.isEmpty())
					{
						break;
					}
				}
				catch (Exception e)
				{
				}
			}
		}

		return constructors;
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
		List<Constructor<T>> constructors = getAll(simpleClassName);
		if (!constructors.isEmpty())
		{
			return constructors.get(0).getDeclaringClass();
		}
		return null;
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
	 * Instantiates an instance of named class using no-argument constructor.
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
	 * Instantiates an instance of named class using no-argument constructor.
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
				addConstructorsFrom(info.load(), true);
			}
		}
	}

	/*-----------------
	 * Private methods
	 */
	
	@NonNullByDefault(false)
	private static enum ConstructorComparator implements Comparator<Constructor<?>>
	{
		INSTANCE;

		@Override
		public int compare(Constructor<?> c1, Constructor<?> c2)
		{
			return Integer.compare(c1.getParameterTypes().length, c2.getParameterTypes().length);
		}
	}

	/**
	 * Adds public constructors from given type indexed by qualified name
	 * @param addSimpleName if true, then constructors will also be indexed by the classes simple name.
	 * @return the constructors that were added
	 * @since 0.07
	 */
	@SuppressWarnings("unchecked")
	private synchronized List<Constructor<T>> addConstructorsFrom(Class<?> type, boolean addSimpleName)
	{
		List<Constructor<T>> constructors = Collections.emptyList();
		
		int modifiers = type.getModifiers();
		if (Modifier.isPublic(modifiers) && !Modifier.isAbstract(modifiers) && _superClass.isAssignableFrom(type))
		{
			try
			{
				final Constructor<T>[] array = (Constructor<T>[]) type.getConstructors();
				Arrays.sort(array, ConstructorComparator.INSTANCE);
				constructors = Arrays.asList(array);
				if (addSimpleName)
				{
					_nameToConstructors.putAll(type.getSimpleName(), constructors);
				}
				_nameToConstructors.putAll(type.getCanonicalName(), constructors);
			}
			catch (SecurityException ex)
			{
				// Ignore
			}
		}
		
		return constructors;
	}
	
	protected RuntimeException noMatchingClass(String simpleClassName)
	{
		return new RuntimeException(String.format(
			"Cannot find class named '%s' with accessible constructor with appropriate signature", simpleClassName));
	}

}