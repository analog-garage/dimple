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

package com.analog.lyric.dimple.environment;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ConstructorRegistry;
import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IGenericSampler;

/**
 * Shared environment for Dimple
 * <p>
 * This object holds shared state for a Dimple session. Typically
 * there will only be one instance of this class that is global across
 * all threads, but it is possible to configure different instances in
 * different threads in the rare case where multiple segregated instances
 * of Dimple need to be run at the same time.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@ThreadSafe
public class DimpleEnvironment extends DimpleOptionHolder
{
	/*--------------
	 * Static state
	 */
	
	private static final AtomicReference<DimpleEnvironment> _globalInstance =
		new AtomicReference<>(new DimpleEnvironment());
	
	private static final ThreadLocal<DimpleEnvironment> _threadInstance = new ThreadLocal<DimpleEnvironment>() {
		@Override
		protected DimpleEnvironment initialValue()
		{
			return defaultEnvironment();
		}
	};
	
	private static enum LoadedFromMATLAB
	{
		INSTANCE;
		
		private final boolean _fromMATLAB;
		
		private LoadedFromMATLAB()
		{
			_fromMATLAB = getClass().getClassLoader().getClass().getPackage().getName().startsWith("com.mathworks");
		}
	}
	
	/*----------------
	 * Instance state
	 */
	
	// NOTE: although the environment is typically accessed through a thread-local
	// variable, there will typically be only one environment shared across all threads
	// so care should be taken to ensure that the code is thread safe.
	
	/**
	 * Logging instance for this environment.
	 */
	private final AtomicReference<Logger> _logger = new AtomicReference<>();
	
	private final ConstructorRegistry<FactorFunction> _factorFunctions =
		new ConstructorRegistry<FactorFunction>(FactorFunction.class);
	
	private final ConstructorRegistry<IGenericSampler> _genericSamplers =
		new ConstructorRegistry<IGenericSampler>(IGenericSampler.class);

	private final ConstructorRegistry<IProposalKernel> _proposalKernels =
		new ConstructorRegistry<>(IProposalKernel.class);
	
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs a new instance of a Dimple environment.
	 * <p>
	 * @since 0.07
	 */
	public DimpleEnvironment()
	{
		Logger logger = getDefaultLogger();
		
		if (loadedFromMATLAB() &&
			logger.getHandlers().length == 0 &&
			(logger.getLevel() == null || logger.getLevel().equals(Level.OFF)))
		{
			// HACK: The default configuration on MATLAB does not log anything to the console, so tweak it
			// to output WARNING and above to console.
			ConsoleHandler handler = new ConsoleHandler();
			handler.setLevel(Level.ALL);
			logger.addHandler(handler);
			logger.setLevel(Level.WARNING);
			logger.setUseParentHandlers(false);
		}

		_logger.set(logger);
	}
	
	/*----------------
	 * Static methods
	 */
	
	/**
	 * The default global instance of the dimple environment.
	 * <p>
	 * This is used as the initial value of the {@link #active} environment
	 * for newly created threads. Most users should use {@link #active} instead of this.
	 * <p>
	 * @see #setDefaultEnvironment(DimpleEnvironment)
	 * @since 0.07
	 */
	public static DimpleEnvironment defaultEnvironment()
	{
		return _globalInstance.get();
	}
	
	/**
	 * The active instance of the dimple environment.
	 * <p>
	 * This is initialized to the {@link #defaultEnvironment} the
	 * first time this is invoked, but can be overridden on a per-thread baseis.
	 * Users should remember that modifications to this environment will affect
	 * other threads unless the environment has been set to a value unique to the
	 * current thread.
	 * <p>
	 * @see #setActive(DimpleEnvironment)
	 * @since 0.07
	 */
	public static DimpleEnvironment active()
	{
		return _threadInstance.get();
	}
	
	/**
	 * Sets the default dimple environment to a new instance.
	 * @param env is a non-null environment instance.
	 * @see #defaultEnvironment()
	 * @since 0.07
	 */
	public static void setDefaultEnvironment(DimpleEnvironment env)
	{
		_globalInstance.set(env);
	}
	
	/**
	 * Sets the active dimple environment to a new instance for the current thread.
	 * <p>
	 * This sets the value of {@link #active()} for the current thread only.
	 * <p>
	 * @param env is a non-null environment instance.
	 * @since 0.07
	 */
	public static void setActive(DimpleEnvironment env)
	{
		_threadInstance.set(env);
	}
	
	/*----------------------------
	 * IDimpleEventSource methods
	 */

	@Override
	public @Nullable FactorGraph getContainingGraph()
	{
		return null;
	}

	@Override
	public @Nullable IDimpleEventListener getEventListener()
	{
		// TODO: move event listener here from FactorGraph
		return null;
	}

	@Override
	public @Nullable IDimpleEventSource getEventParent()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns the string {@code "DimpleEnvironment"}.
	 */
	@Override
	public String getEventSourceName()
	{
		return "DimpleEnvironment";
	}

	@Override
	public @Nullable IModelEventSource getModelEventSource()
	{
		return null;
	}

	@Override
	public void notifyListenerChanged()
	{
	}
	
	/*----------------------------
	 * System environment methods
	 */
	
	/**
	 * Indicates whether Dimple is being run from MATLAB.
	 * <p>
	 * This returns true if the {@link ClassLoader} class used to load this class
	 * has a name beginning with "com.mathworks".
	 * <p>
	 * @since 0.07
	 */
	public static boolean loadedFromMATLAB()
	{
		return LoadedFromMATLAB.INSTANCE._fromMATLAB;
	}
	
	/*-----------------
	 * Logging methods
	 */
	
	/**
	 * Returns default logger instance.
	 * <p>
	 * This is obtained by invoking
	 * {@linkplain Logger#getLogger(String) Logger.getLogger("com.analog.lyric.dimple")}.
	 * <p>
	 * @since 0.07
	 */
	public static Logger getDefaultLogger()
	{
		return Logger.getLogger("com.analog.lyric.dimple");
	}
	
	/**
	 * Logs a warning message using the thread-specific Dimple logger.
	 * <p>
	 * This is simply shorthand for:
	 * <blockquote>
     *   local().logger().warning(String.format(format, args));
	 * </blockquote>
	 * <p>
	 * @param format a non-null String for use with {@link String#format}.
	 * @param args zero or more format arguments.
	 * @since 0.07
	 */
	public static void logWarning(String format, Object ... args)
	{
		logWarning(String.format(format, args));
	}
	
	/**
	 * Logs a warning message using the thread-specific Dimple logger.
	 * <p>
	 * This is simply shorthand for:
	 * <blockquote>
     *   local().logger().warning(message);
	 * </blockquote>
	 * <p>
	 * @since 0.07
	 */
	public static void logWarning(String message)
	{
		active().logger().warning(message);
	}
	
	/**
	 * Logs an error message using the thread-specific Dimple logger.
	 * <p>
	 * This is simply shorthand for:
	 * <blockquote>
     *   local().logger().severe(String.format(format, args));
	 * </blockquote>
	 * <p>
	 * @param format a non-null String for use with {@link String#format}.
	 * @param args zero or more format arguments.
	 * @since 0.07
	 */
	public static void logError(String format, Object ... args)
	{
		logError(String.format(format, args));
	}
	
	/**
	 * Logs a warning message using the thread-specific Dimple logger.
	 * <p>
	 * This is simply shorthand for:
	 * <blockquote>
     *   local().logger().severe(message);
	 * </blockquote>
	 * <p>
	 * @since 0.07
	 */
	public static void logError(String message)
	{
		active().logger().log(ExtendedLevel.ERROR, message);
	}
	
	/**
	 * The environment-specific logger instance.
	 * <p>
	 * By default this is set to {@link #getDefaultLogger()} when the environment is initialized.
	 * <p>
	 * Logging is typically configured using Java system properties, which is described
	 * in the documentation for {@link java.util.logging.LogManager}. You can also configure
	 * the logger object directly.
	 * <p>
	 * @see #setLogger
	 * @see #logWarning
	 * @see #logError
	 * @see java.util.logging.Logger
	 * @see java.util.logging.LogManager
	 * @since 0.07
	 */
	public Logger logger()
	{
		return _logger.get();
	}
	
	/**
	 * Sets the environment-specific {@link #logger} instance.
	 * <p>
	 * This can be used to replace the logger with special implementation
	 * for test purposes when testing logging behavior.
	 * <p>
	 * @param logger
	 * @return the previous logger instance.
	 * @since 0.07
	 */
	public Logger setLogger(Logger logger)
	{
		return _logger.getAndSet(logger);
	}
	
	/*------------------------
	 * Constructor registries
	 */
	
	public ConstructorRegistry<FactorFunction> factorFunctions()
	{
		return _factorFunctions;
	}
	
	public ConstructorRegistry<IGenericSampler> genericSamplers()
	{
		return _genericSamplers;
	}
	
	/**
	 * Registry of proposal kernel classes for this environment.
	 */
	public ConstructorRegistry<IProposalKernel> proposalKernels()
	{
		return _proposalKernels;
	}

}
