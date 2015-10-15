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

package com.analog.lyric.dimple.solvers.core;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.options.IOptionValue;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Extendible solver factor creation mappings for a Dimple solver.
 * <p>
 * This class maps strings representing factor functions to ordered lists of functional objects that
 * can create an appropriate solver factor. The keys are either the fully qualified name of a
 * {@link FactorFunction} class or an unqualified alias (mostly for backward compatibility).
 * <p>
 * Dimple solvers that support user-defined custom solver factors should extend this class
 * and define an option using {@link CustomFactorsOptionKey}.
 * <p>
 * @param <SFactor> is the base class for solver factors that may be created
 * @param <SGraph> is the base class for the solver graph for which solver factor is created
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class CustomFactors<SFactor extends ISolverFactor, SGraph extends ISolverFactorGraph>
	implements IOptionValue, Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;

	/*---------------
	 * Inner classes
	 */
	
	/**
	 * Implementation of {@link ISolverFactorCreator} that invokes target class's constructor reflectively.
	 * <p>
	 * @since 0.08
	 */
	protected static class ConstructorFactory<SFactor extends ISolverFactor, SGraph extends ISolverFactorGraph>
		implements ISolverFactorCreator<SFactor, SGraph>
	{
		private final Constructor<? extends SFactor> _constructor;

		ConstructorFactory(Class<? extends SFactor> customClass, Class<SGraph> sgraphClass)
			throws NoSuchMethodException, SecurityException
		{
			_constructor = 	customClass.getDeclaredConstructor(Factor.class, sgraphClass);
		}
		
		@Override
		public String toString()
		{
			return _constructor.getDeclaringClass().getName();
		}
		
		@Override
		public @NonNull SFactor create(Factor factor, SGraph sgraph)
		{
			try
			{
				return _constructor.newInstance(factor, sgraph);
			}
			catch (InvocationTargetException ex)
			{
				Throwable cause = ex.getCause();
				if (cause instanceof RuntimeException)
				{
					throw (RuntimeException)cause;
				}
				else
				{
					throw new RuntimeException(cause);
				}
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException ex)
			{
				throw new SolverFactorCreationException(ex, "Could not invoke %s reflectively: %s", _constructor, ex);
			}
		}
	}
	
	/*-------
	 * State
	 */

	/**
	 * Base type of solver factors supported by this object.
	 */
	private final Class<SFactor> _sfactorClass;
	
	/**
	 * Base type of solver graphs for which solver factors are to be created.
	 */
	private final Class<SGraph> _sgraphClass;
	
	/**
	 * Maps factor function names to creation objects. The key is either the fully-qualified class name
	 * of a {@link FactorFunction} class or an unqualified alias name.
	 */
	private ListMultimap<String, ISolverFactorCreator<SFactor, SGraph>> _map = LinkedListMultimap.create();
	
	/*--------------
	 * Construction
	 */

	/**
	 * Base constructor
	 * <p>
	 * @param sfactorClass is the concrete class type of {@code SFactor}
	 * @param sgraphClass is the concrete class type of {@code SGraph}
	 * @since 0.08
	 */
	protected CustomFactors(Class<SFactor> sfactorClass, Class<SGraph> sgraphClass)
	{
		_sfactorClass = sfactorClass;
		_sgraphClass = sgraphClass;
	}
	
	/**
	 * Copy constructor.
	 * <p>
	 * Subclass copy constructors should invoke this.
	 * <p>
	 * @since 0.08
	 */
	protected CustomFactors(CustomFactors<SFactor,SGraph> other)
	{
		this(other._sfactorClass, other._sgraphClass);
		_map.putAll(other._map);
	}
	
	@Override
	public abstract CustomFactors<SFactor,SGraph> clone();
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(getClass().getSimpleName());
		sb.append("(");
		boolean first = true;
		for (Map.Entry<String, ISolverFactorCreator<SFactor,SGraph>> entry : _map.entries())
		{
			if (first)
				first = false;
			else
				sb.append(",");
			sb.append(String.format("\n\t%s = %s", entry.getKey(), entry.getValue()));
		}
		sb.append(")\n");
		
		return sb.toString();
	}
	
	/*----------------------
	 * IOptionValue methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns true unless state has been frozen, which should be the case when used as default
	 * value for {@link CustomFactorsOptionKey}.
	 */
	@Override
	public boolean isMutable()
	{
		return !(_map instanceof ImmutableListMultimap);
	}
	
	/*------------------
	 * Abstract methods
	 */
	
	/**
	 * Add built-in solver factors supported by concrete subclass.
	 * <p>
	 * This is used by {@link CustomFactorsOptionKey} to populate the default {@link CustomFactors}
	 * instance for a given key before freezing it.
	 * <p>
	 * Note that built-ins are not added automatically by the constructor.
	 * <p>
	 * @since 0.08
	 */
	public abstract void addBuiltins();
	
	/**
	 * Create default solver factor.
	 * <p>
	 * This will be invoked when no other creator can be found to construct the
	 * solver factor.
	 * <p>
	 * @param factor is the factor for which the solver factor is to be created
	 * @param sgraph will be the immediate parent of the new solver factor
	 * @throw SolverFactorCreationException
	 * @since 0.08
	 */
	public abstract SFactor createDefault(Factor factor, SGraph sgraph);

	/*---------------
	 * Local methods
	 */
	
	/**
	 * Add custom factor function mapping.
	 * <p>
	 * If there are already creators associated with the input key (after it has been fully qualified),
	 * this will be added to the end of the list.
	 * <p>
	 * @param factorFunction is either the name of a {@link FactorFunction} class or an alias,
	 * see {@link #qualifiedFactorFunctionName(String)} for details.
	 * @param constructor is the custom factor creator to be associated with the specified key.
	 * @return this object, which allows adds to be chained.
	 * @since 0.08
	 */
	public CustomFactors<SFactor,SGraph> add(String factorFunction, ISolverFactorCreator<SFactor,SGraph> constructor)
	{
		return addInternal(qualifiedFactorFunctionName(factorFunction), constructor);
	}
	
	/**
	 * Add custom factor function mapping.
	 * <p>
	 * If there are already creators associated with the input key (after it has been fully qualified),
	 * this will be added to the end of the list.
	 * <p>
	 * @param funcClass is the type of the factor function, its {@linkplain Class#getName() qualified name}
	 * will be used as the key.
	 * @param constructor is the custom factor creator to be associated with the specified key.
	 * @return this object, which allows adds to be chained.
	 * @since 0.08
	 */
	public CustomFactors<SFactor,SGraph> add(Class<? extends FactorFunction> funcClass,
		ISolverFactorCreator<SFactor,SGraph> constructor)
	{
		return addInternal(funcClass.getName(), constructor);
	}

	/**
	 * Add custom factor function mapping.
	 * <p>
	 * If there are already creators associated with the input key (after it has been fully qualified),
	 * this will be added to the end of the list.
	 * <p>
	 * @param factorFunction is either the name of a {@link FactorFunction} class or an alias,
	 * see {@link #qualifiedFactorFunctionName(String)} for details.
	 * @param customClass is the concrete solver factor class to be created for the specified key. Because it
	 * will be invoked using reflection, it must be public and have a public constructor taking the arguments
	 * {@code SFactor} and {@code SGraph}.
	 * @return this object, which allows adds to be chained.
	 * @since 0.08
	 */
	public CustomFactors<SFactor,SGraph> add(String factorFunction, Class<? extends SFactor> customClass)
	{
		return add(factorFunction, defaultCreator(customClass));
	}
	
	/**
	 * Add custom factor function mapping.
	 * <p>
	 * If there are already creators associated with the input key (after it has been fully qualified),
	 * this will be added to the end of the list.
	 * <p>
	 * @param funcClass is the type of the factor function, its {@linkplain Class#getName() qualified name}
	 * will be used as the key.
	 * @param customClass is the concrete solver factor class to be created for the specified key. Because it
	 * will be invoked using reflection, it must be public and have a public constructor taking the arguments
	 * {@code SFactor} and {@code SGraph}.
	 * @return this object, which allows adds to be chained.
	 * @since 0.08
	 */
	public CustomFactors<SFactor,SGraph> add(Class<? extends FactorFunction> funcClass,
		Class<? extends SFactor> customClass)
	{
		return add(funcClass, defaultCreator(customClass));
	}
	
	/**
	 * Add custom factor function mapping.
	 * <p>
	 * If there are already creators associated with the input key (after it has been fully qualified),
	 * this will be added to the end of the list.
	 * <p>
	 * @param factorFunction is either the name of a {@link FactorFunction} class or an alias,
	 * see {@link #qualifiedFactorFunctionName(String)} for details.
	 * @param customClassName identifies the concrete solver factor class to be created for the specified key.
	 * It will be resolved using the {@link #getFactorClass(String)} method.
	 * Because it will be invoked using reflection, the class must be public and have a public constructor taking the
	 * arguments {@code SFactor} and {@code SGraph}.
	 * @return this object, which allows adds to be chained.
	 * @since 0.08
	 */
	public CustomFactors<SFactor,SGraph> add(String factorFunction, String customClassName)
	{
		return add(factorFunction, getFactorClass(customClassName));
	}
	
	/**
	 * Prepend custom factor function mapping.
	 * <p>
	 * If there are already creators associated with the input key (after it has been fully qualified),
	 * this will be added to the beginning of the list for that key.
	 * <p>
	 * @param factorFunction is either the name of a {@link FactorFunction} class or an alias,
	 * see {@link #qualifiedFactorFunctionName(String)} for details.
	 * @param constructor is the custom factor creator to be associated with the specified key.
	 * @since 0.08
	 */
	public CustomFactors<SFactor,SGraph> addFirst(String factorFunction,
		ISolverFactorCreator<SFactor,SGraph> constructor)
	{
		return addFirstInternal(qualifiedFactorFunctionName(factorFunction), constructor);
	}
	
	/**
	 * Prepend custom factor function mapping.
	 * <p>
	 * If there are already creators associated with the input key (after it has been fully qualified),
	 * this will be added to the beginning of the list for that key.
	 * <p>
	 * @param funcClass is the type of the factor function, its {@linkplain Class#getName() qualified name}
	 * will be used as the key.
	 * @param constructor is the custom factor creator to be associated with the specified key.
	 * @return this object, which allows adds to be chained.
	 * @since 0.08
	 */
	public CustomFactors<SFactor,SGraph> addFirst(Class<? extends FactorFunction> funcClass,
		ISolverFactorCreator<SFactor,SGraph> constructor)
	{
		return addFirstInternal(funcClass.getName(), constructor);
	}

	/**
	 * Prepend custom factor function mapping.
	 * <p>
	 * If there are already creators associated with the input key (after it has been fully qualified),
	 * this will be added to the beginning of the list for that key.
	 * <p>
	 * @param factorFunction is either the name of a {@link FactorFunction} class or an alias,
	 * see {@link #qualifiedFactorFunctionName(String)} for details.
	 * @param customClass is the concrete solver factor class to be created for the specified key. Because it
	 * will be invoked using reflection, it must be public and have a public constructor taking the arguments
	 * {@code SFactor} and {@code SGraph}.
	 * @return this object, which allows adds to be chained.
	 * @since 0.08
	 */
	public CustomFactors<SFactor,SGraph> addFirst(String factorFunction, Class<? extends SFactor> customClass)
	{
		return addFirst(factorFunction, defaultCreator(customClass));
	}
	
	/**
	 * Prepend custom factor function mapping.
	 * <p>
	 * If there are already creators associated with the input key (after it has been fully qualified),
	 * this will be added to the beginning of the list for that key.
	 * <p>
	 * @param funcClass is the type of the factor function, its {@linkplain Class#getName() qualified name}
	 * will be used as the key.
	 * @param customClass is the concrete solver factor class to be created for the specified key. Because it
	 * will be invoked using reflection, it must be public and have a public constructor taking the arguments
	 * {@code SFactor} and {@code SGraph}.
	 * @return this object, which allows adds to be chained.
	 * @since 0.08
	 */
	public CustomFactors<SFactor,SGraph> addFirst(Class<? extends FactorFunction> funcClass,
		Class<? extends SFactor> customClass)
	{
		return addFirst(funcClass, defaultCreator(customClass));
	}
	
	/**
	 * Prepend custom factor function mapping.
	 * <p>
	 * If there are already creators associated with the input key (after it has been fully qualified),
	 * this will be added to the beginning of the list for that key.
	 * <p>
	 * @param factorFunction is either the name of a {@link FactorFunction} class or an alias,
	 * see {@link #qualifiedFactorFunctionName(String)} for details.
	 * @param customClassName identifies the concrete solver factor class to be created for the specified key.
	 * It will be resolved using the {@link #getFactorClass(String)} method.
	 * Because it will be invoked using reflection, the class must be public and have a public constructor taking the
	 * arguments {@code SFactor} and {@code SGraph}.
	 * @return this object, which allows adds to be chained.
	 * @since 0.08
	 */
	public CustomFactors<SFactor,SGraph> addFirst(String factorFunction, String customClassName)
	{
		return addFirst(factorFunction, getFactorClass(customClassName));
	}

	/**
	 * Get the ordered list of solver factor creators registered for the key.
	 * <p>
	 * @param factorFunction is either a fully qualified name of a {@link FactorFunction} or an unqualified alias.
	 * @return Ordered list of creators for {@code factorFunction} key. Returns empty list if there are none.
	 * @since 0.08
	 */
	public List<ISolverFactorCreator<SFactor, SGraph>> get(String factorFunction)
	{
		return _map.get(factorFunction);
	}
	
	/**
	 * Resolves class for given name.
	 * <p>
	 * @param customClassName is either the fully qualified name of a {@link FactorFunction} class or
	 * a simple class name, in which case it is assumed to be in the {@code customFactors} subpackage of
	 * the package containing the definition of the concrete implementation of this class. For instance,
	 * if invoked on an instance of class {@code com.mycompany.myproject.MyCustomFactors} it will look
	 * in package {@code com.mycompany.myproject.customFactors}.
	 * @throws IllegalArgumentException if no class is found for given name
	 * @throws ClassCastException if class found for name is not a subclass of {@code SFactor}
	 * @since 0.08
	 */
	public Class<? extends SFactor> getFactorClass(String customClassName)
	{
		if (!customClassName.contains("."))
		{
			customClassName = String.format("%s.customFactors.%s", getClass().getPackage().getName(), customClassName);
		}
		try
		{
			return Class.forName(customClassName).asSubclass(_sfactorClass);
		}
		catch (ClassNotFoundException ex)
		{
			throw new IllegalArgumentException(ex);
		}
	}
	
	/**
	 * Returns a solver factor creator that invokes the constructor by reflection.
	 * <p>
	 * @param customClassName identifies the Java class of the solver factor implementation,
	 * which will be resolved by {@link #getFactorClass(String)}.
	 * @return creator generated by {@link #defaultCreator(Class)}
	 * @since 0.08
	 */
	public ISolverFactorCreator<SFactor,SGraph> defaultCreator(String customClassName)
	{
		return defaultCreator(getFactorClass(customClassName));
	}
	
	/**
	 * Returns a solver factor creator that invokes the constructor by reflection.
	 * <p>
	 * @param customClass
	 * @since 0.08
	 */
	public ISolverFactorCreator<SFactor,SGraph> defaultCreator(Class<? extends SFactor> customClass)
	{
		try
		{
			return new ConstructorFactory<>(customClass, _sgraphClass);
		}
		catch (NoSuchMethodException | SecurityException ex)
		{
			throw new DimpleException(ex);
		}
	}

	/**
	 * The set of factor function keys with registered creators.
	 * <p>
	 * The keys are not in any particular order.
	 * @since 0.08
	 */
	public Set<String> keySet()
	{
		return _map.keySet();
	}
	
	/**
	 * Converts input factor function name to fully qualified name if possible.
	 * <p>
	 * Looks up factor function in {@link DimpleEnvironment#factorFunctions()} cache in active environment
	 * to determine fully qualified name. If not found and name is not qualified, it will simply be returned.
	 * <p>
	 * @param factorFunction is one of:
	 * <ul>
	 * <li>a fully qualified class name of a {@link FactorFunction} subclass
	 * <li>a simple class name of a {@link FactorFunction} subclass that exists in a package registered with
	 * the environment's factor function cache.
	 * <li>an unqualified alias
	 * </ul>
	 * @throws IllegalArgumentException if input {@code factorFunction} name is qualified by a "." and no
	 * matching {@link FactorFunction} class can be loaded with that name.
	 * @since 0.08
	 */
	public String qualifiedFactorFunctionName(String factorFunction)
	{
		final Class<?> functionClass = DimpleEnvironment.active().factorFunctions().getClassOrNull(factorFunction);
		if (functionClass != null)
		{
			return functionClass.getName();
		}
		
		if (factorFunction.contains("."))
		{
			throw new IllegalArgumentException(
				String.format("Cannot find factor function with qualified class name '%s'",	factorFunction));
		}
		
		return factorFunction;
	}

	/*-------------------
	 * Protected methods
	 */
	
	protected CustomFactors<SFactor,SGraph> addInternal(String factorFunction,
		ISolverFactorCreator<SFactor,SGraph> constructor)
	{
		get(factorFunction).add(constructor);
		return this;
	}
	
	protected CustomFactors<SFactor,SGraph> addFirstInternal(String factorFunction,
		ISolverFactorCreator<SFactor,SGraph> constructor)
	{
		get(factorFunction).add(0, constructor);
		return this;
	}

	/**
	 * Freezes state of object, preventing further changes.
	 * <p>
	 * Intended for use in defining built-in instances for {@link CustomFactorsOptionKey} default values.
	 * <p>
	 * @since 0.08
	 */
	protected void freeze()
	{
		_map = ImmutableListMultimap.copyOf(_map);
	}
	
}
