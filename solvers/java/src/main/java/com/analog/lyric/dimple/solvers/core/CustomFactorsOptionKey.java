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

import java.util.logging.Level;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.CustomFactorFunctionWrapper;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.options.OptionLookupIterator;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Special option key for declaring options that let users add custom solver factors.
 * <p>
 * This will be used to lookup and invoke creators for solver factors for the solver
 * for which the option is defined. For instance,
 * {@link com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph SumProductSolverGraphs} use the
 * {@link com.analog.lyric.dimple.solvers.sumproduct.SumProductOptions#customFactors SumProductOptions.customFactors}
 * option to create solver factors. Users can customize solver factor creation for specific factor functions
 * by adding entries to {@link CustomFactors} using the appropriate option. Since custom factors are often not specific
 * to a particular graph, this will often be done by setting the option on the {@linkplain DimpleEnvironment#active()
 * active Dimple environment} so that it will be inherited by all solver graphs. For instance, one might define
 * a new custom factor association for sum-product using:
 * <p>
 * <blockquote><pre>
 * SumProductOptions.customFactors
 *     .getOrCreate(DimpleEnvironment.active()
 *     .add(Xor.class, MyCustomXor.class);
 * </pre></blockquote>
 * <p>
 * @param <SFactor> is the base class of any solver factors that will be created by {@link CustomFactors}
 * values associated with this key.
 * @param <SGraph> is the base class for the solver graph for which solver factor is created
 * @param <CF> is the concrete subclass of {@link CustomFactors} that can be used as this option's values.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class CustomFactorsOptionKey<SFactor extends ISolverFactor, SGraph extends ISolverFactorGraph,
									CF extends CustomFactors<SFactor,SGraph>>
	extends OptionKey<CF>
{
	private static final long serialVersionUID = 1L;

	private final Class<CF> _type;
	private final Supplier<CF> _defaultValueSupplier;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructor intended for use in declaration of static constant field containing option key instance.
	 * <p>
	 * Example:
	 * <pre>
	 * class MyOptions
	 * {
	 *     public static final CustomFactorsOptionKey<MySFactor, MySolverGraph> customFactors =
	 *         new CustomFactorsOptionKey<>(MyOptions.class, "customFactors", MyCustomFactors.class);
	 * }
	 * </pre>
	 * <p>
	 * @param declaringClass is the class in which the static field containing the instance is declared.
	 * @param name is the name of the static field
	 * @param type is the concrete subclass of {@link CustomFactors} stored by this option key.
	 * @since 0.08
	 */
	 public CustomFactorsOptionKey(Class<?> declaringClass, String name, final Class<CF> type)
	{
		super(declaringClass, name);
		_type = type;
		_defaultValueSupplier = Suppliers.memoize(new Supplier<CF>() {
			@Override
			public CF get()
			{
				try
				{
					final CF customFactors = type.newInstance();
					customFactors.addBuiltins();
					customFactors.freeze();
					return customFactors;
				}
				catch (InstantiationException | IllegalAccessException ex)
				{
					throw new DimpleException(ex);
				}
			}
		});
	}

	/*--------------------
	 * IOptionKey methods
	 */
	
	@Override
	public Class<CF> type()
	{
		return _type;
	}

	@Override
	public synchronized CF defaultValue()
	{
		return _defaultValueSupplier.get();
	}
	
	/*---------------
	 * Local methods
	 */
	
	/**
	 * Locally lookup CustomFactors instance for this key, creating and adding if not already set on {@code holder}.
	 * <p>
	 * Creation uses reflection to invoke the default constructor so the concrete {@link #type()} must be
	 * public and have a public no-argument constructor.
	 * <p>
	 * @since 0.08
	 */
	public CF getOrCreate(IOptionHolder holder)
	{
		CF customFactors = holder.getLocalOption(this);
		if (customFactors == null)
		{
			try
			{
				customFactors = type().newInstance();
				set(holder, customFactors);
			}
			catch (InstantiationException | IllegalAccessException ex)
			{
				// This should not happen unless CF class is abstract, not public or doesn't
				// have a default constructor.
				throw new DimpleException(ex);
			}
		}
		return customFactors;
	}

	/**
	 * Creates new solver factor for given factor for given solver graph.
	 * <p>
	 * This is intended to be invoked by implementations of {@link SFactorGraphBase#createFactor(Factor)}, as in:
	 * <pre>
	 *     public ISolverFactor createFactor(Factor factor)
	 *     {
	 *         return MyOptions.customFactors.createFactor(factor, this);
	 *     }
	 * </pre>
	 * <p>
	 * This function will look in {@link CustomFactors} objects set under this key in the
	 * option delegate chain starting with {@code sgraph} and ending with the {@link #defaultValue()} for
	 * this key instance to find matching {@linkplain ISolverFactorCreator}s
	 * and invoke them in turn, catching and saving any exceptions. The first solver factor successfully produced
	 * will be returned. If no solver factor was produced and factor is a {@link CustomFactorFunctionWrapper},
	 * then a {@link SolverFactorCreationException} will be thrown because there is no real factor to fall back on,
	 * otherwise this will use the {@link CustomFactors#createDefault(Factor, ISolverFactorGraph) createDefault}
	 * method of the first {@link CustomFactors} object to create the solver factor, and any exceptions thrown
	 * by that method will be passed through.
	 * <p>
	 * This method will log any exceptions thrown by factor creation routines at log {@link Level#CONFIG}.
	 * This can be used to debug reasons for custom factor rejection.
	 * <p>
	 * @param factor is the factor for which a solver factor is to be created
	 * @param sgraph is the solver graph that will be the immediate parent of the new solver factor.
	 * @throws SolverFactorCreationException if no solver factor could be created
	 * @since 0.08
	 */
	public SFactor createFactor(Factor factor, SGraph sgraph)
	{
		final FactorFunction function = factor.getFactorFunction();
		final Class<?> functionClass = function.getClass();
		
		// If true, there isn't a real factor function. All we have is the name of the wrapper function.
		final boolean isCustomWrapper = functionClass == CustomFactorFunctionWrapper.class;

		final ReleasableIterator<CF> customFactorsIter = OptionLookupIterator.create(sgraph, this);
		
		// Outer loop iterates over CustomFactors instances found in option delegate chain.
		try
		{
			final CF firstCustomFactors = customFactorsIter.next();
			CF customFactors = firstCustomFactors;
			RuntimeException lastFailure = null;
			
			while (true)
			{
				Class<?> functionSuperclass = functionClass;
				String functionName = isCustomWrapper ? function.getName() : functionSuperclass.getName();

				// Middle loop iterates over superclasses of functionClass, if not a custom wrapper.
				while (true)
				{
					// Inner loop iterates over factor creators for given key
					for (ISolverFactorCreator<? extends SFactor,SGraph> creator : customFactors.get(functionName))
					{
						try
						{
							return creator.create(factor, sgraph);
						}
						catch (RuntimeException ex)
						{
							lastFailure = ex;
							DimpleEnvironment.log(Level.CONFIG, "Custom factor rejected for %s: %s", factor, ex);
						}
					}

					final Class<?> superclass = functionSuperclass.getSuperclass();

					if (!isCustomWrapper && FactorFunction.class.isAssignableFrom(superclass))
					{
						// Try again using superclass as long as it is still a subclass of FactorFunction.
						functionSuperclass = superclass.asSubclass(FactorFunction.class);
						functionName = functionSuperclass.getName();
					}
					else
					{
						break;
					}
				}

				if (!customFactorsIter.hasNext())
				{
					if (isCustomWrapper)
					{
						// There isn't a real factor function, so we throw an exception instead of generating a default
						if (lastFailure != null)
						{
							throw new SolverFactorCreationException(lastFailure,
								"Cannot find factor function '%s' and could not produce custom factor: %s",
								functionName, lastFailure);
						}
						else
						{
							// Not a real factor and no custom factor creator found under this name.
							throw new SolverFactorCreationException(
								"Cannot find factor function or custom factor implementation for '%s'", functionName);
						}
					}

					return firstCustomFactors.createDefault(factor, sgraph);
				}

				customFactors = customFactorsIter.next();
			}
		}
		finally
		{
			customFactorsIter.release();
		}
	}
}
