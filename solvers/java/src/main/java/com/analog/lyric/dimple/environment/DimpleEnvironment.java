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

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ConstructorRegistry;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionRegistry;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.dimple.options.DimpleOptionRegistry;
import com.analog.lyric.dimple.solvers.core.DimpleSolverRegistry;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IGenericSampler;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptionKey;

/**
 * Shared environment for Dimple
 * 
 * <h2>Overview</h2>
 * 
 * This object holds shared state for a Dimple session. Typically
 * there will only be one instance of this class that is global across
 * all threads, but it is possible to configure different instances in
 * different threads in the rare case where multiple segregated Dimple
 * sessions need to be run at the same time.
 * 
 * <h2>Accessing the environment</h2>
 * 
 * When working with an object that implements the {@link IDimpleEnvironmentHolder} interface,
 * which includes both model and solver factor graph, variable and factor objects, you can
 * reference the appropriate environment through its {@linkplain IDimpleEnvironmentHolder#getEnvironment()}
 * method. Root {@link FactorGraph} objects store a pointer to the active environment when they
 * are constructed, and children of a graph will return the environment of the root graph.
 * If the environment is not otherwise available, you can access the active environment for the current thread
 * using the static {@link DimpleEnvironment#active()} method.
 * 
 * <h2>Options</h2>
 * 
 * The environment is the root of the option hierarchy for Dimple. It implements
 * the {@link IOptionHolder} interface, and you can set options on it that will be
 * inherited by all graphs that share that environment. You can use this to set default
 * option values without having to explicitly set them on newly created graphs. For instance,
 * in order to enable multi-threading for all graphs, you could simply write:
 * <blockquote>
 * <pre>
 * DimpleEnvironment env = DimpleEnvironment.active();
 * env.setOption(SolverOptions.enableMultithreading, true);
 * </pre>
 * </blockquote>
 * Any options that are set on the graph objects will override the values set on the environment.
 * <p>
 * For more details options see the {@link com.analog.lyric.options} and {@link com.analog.lyric.dimple.options}
 * packages as well as the Dimple User Manual.
 * 
 * <h2>Event handling</h2>
 * 
 * Likewise, the environment is also the root of the event source hierarchy for Dimple and holds the
 * instance of the {@link DimpleEventListener} responsible for dispatching events. Event handling is
 * activated by {@linkplain #createEventListener() creating an event listener} on the environment and
 * {@linkplain DimpleEventListener#register(com.analog.lyric.dimple.events.IDimpleEventHandler, Class, IDimpleEventSource)
 * registering} it to dispatch specified events to designated handlers. For instance, the following
 * code gets the listener for the environment, creating if necessary, and registers a handler for
 * variable creation events on the given graph:
 * <blockquote>
 * <pre>
 * env.createEventListener().register(handler, VariableAddEvent.class, factorGraph);
 * </pre>
 * </blockquote>
 * For more details on events, see the {@link com.analog.lyric.dimple.events} package and the Dimple User Manual.
 * 
 * <h2>Logging</h2>
 * 
 * The environment also provides a simple logging interface that may be used by Dimple solver implementations to
 * log various non-fatal conditions. Although intended primarily for use in implementations of solvers and custom
 * factors, it is also available for use by by users of Dimple. The environment provides an instance of the
 * standard Java {@link Logger} class that is created using {@link Logger#getLogger} with the string
 * {@code "com.analog.lyric.dimple"}. This may be configured using Java system properties and config files
 * as described in the Java {@linkplain java.util.logging.LogManager LogManager} class documentation; relevant
 * properties are documented in the various handler classes (e.g.
 * {@linkplain java.util.logging.ConsoleHandler ConsoleHandler},
 * {@linkplain java.util.logging.FileHandler FileHandler}). The default configuration usually will just log
 * to {@link System#err}.
 * <p>
 * This class provides some simple methods for logging {@linkplain #logError(String, Object...) errors} and
 * {@linkplain #logWarning(String, Object...) warnings}. The environment's {@link #logger()} instance can
 * be used directly for cases when these methods are not sufficient.
 * 
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
	
	// Hack to determine if class was loaded from within the MATLAB environment. We do this by
	// checking to see if class loader comes from a package whose name starts with "com.mathworks".
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
	
	private final FactorFunctionRegistry _factorFunctions = new FactorFunctionRegistry();
	
	private final ConstructorRegistry<IGenericSampler> _genericSamplers =
		new ConstructorRegistry<IGenericSampler>(IGenericSampler.class);

	private final ConstructorRegistry<IProposalKernel> _proposalKernels =
		new ConstructorRegistry<>(IProposalKernel.class);
	
	@SuppressWarnings("deprecation")
	private final DimpleOptionRegistry _optionRegistry = new DimpleOptionRegistry();
	
	private final DimpleSolverRegistry _solverRegistry = new DimpleSolverRegistry();
	
	@GuardedBy("_eventListenerLock")
	private volatile @Nullable DimpleEventListener _eventListener = null;
	private final Object _eventListenerLock = new Object();
	
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
	 * for newly created threads that are not {@link DimpleThread}s.
	 * Most users should use {@link #active} instead of this.
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * @see #setEventListener(DimpleEventListener)
	 * @see #createEventListener()
	 */
	@Override
	public @Nullable DimpleEventListener getEventListener()
	{
		// For speed, we do not synchronize reads.
		return _eventListener;
	}
	
	/**
	 * Sets event listener to specified value.
	 * <p>
	 * If you just want to force creation of a default listener instance, then use
	 * {@link #createEventListener()} instead.
	 * <p>
	 * This will invoke notify all event sources registered in the previous and
	 * new listeners that the listener has changed by invoking their
	 * {@link IDimpleEventSource#notifyListenerChanged()} methods.
	 * <p>
	 * @param listener
	 * @see #getEventListener()
	 * @see #createEventListener()
	 * @since 0.07
	 */
	public synchronized void setEventListener(@Nullable DimpleEventListener listener)
	{
		synchronized(_eventListenerLock)
		{
			DimpleEventListener prevListener = _eventListener;
			if (prevListener != listener)
			{
				_eventListener = listener;
				if (prevListener != null)
				{
					prevListener.notifyListenerChanged();
				}
				if (listener != null)
				{
					listener.notifyListenerChanged();
				}
				notifyListenerChanged();
			}
		}
	}

	/**
	 * Returns event listener, creating it if previously null.
	 * @see #setEventListener(DimpleEventListener)
	 * @since 0.07
	 */
	public synchronized DimpleEventListener createEventListener()
	{
		synchronized(_eventListenerLock)
		{
			DimpleEventListener listener = _eventListener;
			if (listener == null)
			{
				_eventListener = listener = new DimpleEventListener();
			}
			
			return listener;
		}
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Currently this method does not do anything on {@code DimpleEnvironment}.</b>
	 */
	@Override
	public void notifyListenerChanged()
	{
		// TODO: this does not do anything right now. But if the environment ever ends up storing
		// references to the models it contains, we might want to pass this on to them...
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
	
	/*--------------------
	 * Various registries
	 */
	
	/**
	 * Registry of factor function classes for this environment.
	 * <p>
	 * Supports lookup of {@link FactorFunction} implementations by class name.
	 * Primarily for use in dynamic language implementations of Dimple.
	 * <p>
	 * @see FactorFunctionRegistry
	 * @since 0.07
	 */
	public FactorFunctionRegistry factorFunctions()
	{
		return _factorFunctions;
	}
	
	/**
	 * Registry of generic sampler classes for this environment.
	 * <p>
	 * Supports lookup of {@link IGenericSampler} implementations by class name.
	 * Primarily for use in dynamic language implementations of Dimple.
	 * <p>
	 * @see ConstructorRegistry
	 * @since 0.07
	 */
	public ConstructorRegistry<IGenericSampler> genericSamplers()
	{
		return _genericSamplers;
	}
	
	/**
	 * Registry of known option keys for this environment.
	 * <p>
	 * Supports lookup of Dimple {@linkplain IOptionKey option key} instances by name.
	 * Primarily for use in dynamic language implementations of Dimple.
	 * <p>
	 * @see DimpleOptionRegistry
	 * @since 0.07
	 */
	public DimpleOptionRegistry optionRegistry()
	{
		return _optionRegistry;
	}
	
	/**
	 * Registry of proposal kernel classes for this environment.
	 * <p>
	 * Supports lookup of {@link IProposalKernel} implementations by class name.
	 * Primarily for use in dynamic language implementations of Dimple.
	 * <p>
	 * @see ConstructorRegistry
	 * @since 0.07
	 */
	public ConstructorRegistry<IProposalKernel> proposalKernels()
	{
		return _proposalKernels;
	}

	/**
	 * Registry of solver factory classes for this environment.
	 * <p>
	 * Supports lookup of {@link IFactorGraphFactory} implementations by class name.
	 * Primarily for use in dynamic language implementations of Dimple.
	 * <p>
	 * @see DimpleSolverRegistry
	 * @since 0.07
	 */
	public DimpleSolverRegistry solvers()
	{
		return _solverRegistry;
	}
}
