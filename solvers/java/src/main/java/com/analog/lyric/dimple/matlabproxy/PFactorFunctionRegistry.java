/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.matlabproxy;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionRegistry;
import com.analog.lyric.util.misc.Matlab;

/**
 * MATLAB proxy interface for {@link FactorFunctionRegistry}
 * <p>
 * This is a thin veneer over {@link FactorFunctionRegistry}. It does not have
 * any independent state.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@Matlab(wrapper="FactorFunctionRegistry")
public class PFactorFunctionRegistry extends PObject
{
	/*-------
	 * State
	 */
	
	private final FactorFunctionRegistry _registry;
	
	/*--------------
	 * Construction
	 */
	
	PFactorFunctionRegistry(FactorFunctionRegistry registry)
	{
		_registry = registry;
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public FactorFunctionRegistry getDelegate()
	{
		return _registry;
	}

	/*---------------------------------
	 * PFactorFunctionRegistry methods
	 */

	/**
	 * Adds a factor function package to the end of the search list.
	 * @param packageName a fully qualified name of a Java package containing
	 * one or more {@link FactorFunction} implementations.
	 * @since 0.08
	 */
	public void addPackage(String packageName)
	{
		_registry.addPackage(packageName);
	}
	
	/**
	 * Looks for fully qualified name of {@link FactorFunction} class given its simple name.
	 * <p>
	 * Match is case-sensitive.
	 * <p>
	 * @param simpleClassName is the unqualified {@linkplain Class#getSimpleName() simple name} of
	 * a {@link FactorFunction} class.
	 * @return fully qualified name or null if no match is found.
	 * @since 0.08
	 */
	public @Nullable String getClass(String simpleClassName)
	{
		Class<?> cl = _registry.getClassOrNull(simpleClassName);
		return cl != null ? cl.getName() : null;
	}
	
	/**
	 * Returns a sorted list of classes currently loaded in the repository.
	 * <p>
	 * Returns fully qualified class names in lexicographic order.
	 * <p>
	 * This does not force classes to be loaded, so it may not list all
	 * available classes unless {@link #loadAll()}.
	 * <p>
	 * @since 0.08
	 */
	public String[] getClasses()
	{
		Set<String> classes = _registry.keySet();
		String[] result = classes.toArray(new String[classes.size()]);
		Arrays.sort(result);
		return result;
	}
	
	/**
	 * Returns a list of package names indexed by the registry.
	 * <p>
	 * Lists the packages whose {@link FactorFunction} classes will be included in the registry.
	 * Packages are returned in the order in which they were added, which is also the order in
	 * which they will be searched.
	 * <p>
	 * @since 0.08
	 */
	public String[] getPackages()
	{
		return _registry.getPackages();
	}
	
	/**
	 * Loads all {@link FactorFunction} classes found in package directories.
	 * <p>
	 * Normally classes are loaded lazily as they are requested. This loads them
	 * immediately, so that a complete list of classes can be seen. This is called
	 * <p>
	 * @since 0.08
	 */
	public void loadAll()
	{
		_registry.loadAll();
	}
	
	/**
	 * {@linkplain FactorFunctionRegistry#reset resets} the underlying registry.
	 * <p>
	 * Clears all cached entries and removes any added packages.
	 * <p>
	 * @since 0.08
	 */
	public void reset()
	{
		_registry.reset();
	}
}
