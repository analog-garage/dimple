/*******************************************************************************
*   Copyright 2014-2015 Analog Devices, Inc.
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ConstructorRegistry;
import com.analog.lyric.collect.DoubleArrayCache;
import com.analog.lyric.collect.IntArrayCache;
import com.analog.lyric.collect.WeakLongHashMap;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionRegistry;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphRegistry;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.dimple.options.DimpleOptionRegistry;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.validator.ScheduleValidator;
import com.analog.lyric.dimple.solvers.core.DimpleSolverRegistry;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IGenericSampler;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.math.DimpleRandom;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.IOptionKey;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

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
 * {@linkplain java.util.logging.FileHandler FileHandler}). The Java logging system is described in more detail
 * in the <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/logging/overview.html">Java Logging Overview</a>
 * in the Java SE Documentation. The default configuration usually will just log to {@link System#err}.
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
	
	@GuardedBy("_allInstances")
	private static final WeakLongHashMap<DimpleEnvironment> _allInstances = new WeakLongHashMap<>();
	
	@GuardedBy("class")
	private static @Nullable DimpleEnvironment _globalInstance;
	
	private static final ThreadLocal<DimpleEnvironment> _threadInstance = new ThreadLocal<DimpleEnvironment>() {
		@Override
		protected DimpleEnvironment initialValue()
		{
			return defaultEnvironment();
		}
	};
	
	/**
	 * Cache of double[] for temporary use.
	 */
	public static DoubleArrayCache doubleArrayCache = new DoubleArrayCache();
	
	/**
	 * Cache of int[] for temporary use.
	 */
	public static IntArrayCache intArrayCache = new IntArrayCache();
	
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
	
	private final long _envId;
	private final UUID _uuid;
	
	/**
	 * Logging instance for this environment.
	 */
	private final AtomicReference<Logger> _logger = new AtomicReference<>();
	
	private final AtomicReference<DimpleRandom> _random = new AtomicReference<>(new DimpleRandom());
	
	private final FactorFunctionRegistry _factorFunctions = new FactorFunctionRegistry();
	
	private final FactorGraphRegistry _factorGraphs = new FactorGraphRegistry();
	
	private final ConstructorRegistry<IGenericSampler> _genericSamplers =
		new ConstructorRegistry<IGenericSampler>(IGenericSampler.class);

	private final ConstructorRegistry<IProposalKernel> _proposalKernels =
		new ConstructorRegistry<>(IProposalKernel.class);
	
	@SuppressWarnings("deprecation")
	private final DimpleOptionRegistry _optionRegistry = new DimpleOptionRegistry();
	
	private final ConstructorRegistry<IScheduler> _schedulers =
		new ConstructorRegistry<>(IScheduler.class);
	
	private final ConstructorRegistry<ScheduleValidator> _scheduleValidators =
		new ConstructorRegistry<>(ScheduleValidator.class);
	
	private final DimpleSolverRegistry _solverRegistry = new DimpleSolverRegistry();
	
	private final AtomicReference<IFactorGraphFactory<?>> _defaultSolverFactory =
		new AtomicReference<IFactorGraphFactory<?>> (new SumProductSolver());
	
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
		this(new SecureRandom().nextLong() & Ids.ENV_ID_MAX);
	}
	
	public DimpleEnvironment(long envId)
	{
		if (envId < Ids.ENV_ID_MIN || envId > Ids.ENV_ID_MAX)
		{
			throw new IllegalArgumentException(String.format("%d is not a valid environment id", envId));
		}
		
		synchronized (_allInstances)
		{
			if (_allInstances.containsKey(envId))
			{
				throw new IllegalStateException(String.format("An environment with id %d already exists", envId));
			}
			_allInstances.put(envId, this);
		}

		_envId = envId;
		_uuid = Ids.makeUUID(envId, 0L);
		
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
	public synchronized static DimpleEnvironment defaultEnvironment()
	{
		DimpleEnvironment env = _globalInstance;
		if (env == null)
		{
			_globalInstance = env = new DimpleEnvironment();
		}
		return env;
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
	 * Returns environment with specified environment id, or null if not found.
	 * <p>
	 * @since 0.08
	 */
	public static @Nullable DimpleEnvironment withId(long envId)
	{
		synchronized(_allInstances)
		{
			return _allInstances.get(envId);
		}
	}
	
	/**
	 * Sets the default dimple environment to a new instance.
	 * @param env is a non-null environment instance.
	 * @see #defaultEnvironment()
	 * @since 0.07
	 */
	public synchronized static void setDefaultEnvironment(DimpleEnvironment env)
	{
		_globalInstance = env;
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
	
	/*------------------------
	 * Identification methods
	 */
	
	/**
	 * Randomly generated unique id for environment instance.
	 * <p>
	 * Identifier will be in range {@link Ids#ENV_ID_MIN} and
	 * {@link Ids#ENV_ID_MAX}.
	 * 
	 * @since 0.08
	 */
	public long getEnvId()
	{
		return _envId;
	}
	
	/**
	 * Randomly generated UUID for environment instance.
	 * <p>
	 * @since 0.08
	 * @see #getEnvId()
	 * @see Ids#makeUUID(long, long)
	 */
	public UUID getUUID()
	{
		return _uuid;
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
	
	/**
	 * @return Version string for this release of Dimple
	 * @since 0.08
	 */
	public static String getVersion()
	{
		InputStream in = System.class.getResourceAsStream("/VERSION");
		if (in == null)
		{
			return "UNKNOWN";
		}
		
		String version = "UNKNOWN";
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in)))
		{
			version = br.readLine();
		}
		catch (Exception e)
		{
			// Ignore errors reading file.
		}
		
		return version;
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

	/*------------
	 * Randomness
	 */

	/**
	 * Random generator belonging to the {@link #active} environment.
	 * @since 0.08
	 * @see #random()
	 */
	public static DimpleRandom activeRandom()
	{
		return active().random();
	}
	
	/**
	 * Random generator belonging to this environment.
	 * @since 0.08
	 * @see #activeRandom()
	 * @see #setRandom(DimpleRandom)
	 */
	public DimpleRandom random()
	{
		return _random.get();
	}
	
	/**
	 * Sets random generator belonging to this environment.
	 * @param newRandom the new {@link DimpleRandom} object to be returned by {@link #random()}
	 * @return the previous random object that was replaced
	 * @since 0.08
	 */
	public DimpleRandom setRandom(DimpleRandom newRandom)
	{
		return _random.getAndSet(newRandom);
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
	 * Logs a message using the thread-specific Dimple logger.
	 * <p>
	 * This is simply shorthand for:
	 * <blockquote><pre>
	 * DimpleEnvironment.{@link #active}().{@link #logger}().{@link Logger#log log}(level, format, args);
	 * </pre></blockquote>
	 * @param level indicates the relative severity/priority of the message. The underlying logger is typically
	 * configured to ignore lower level messages. You can also use {@link ExtendedLevel} values here.
	 * @since 0.08
	 * @see #logger
	 */
	public static void log(Level level, String format, Object ... args)
	{
		active().logger().log(level, format, args);
	}
	
	/**
	 * Logs a warning message using the thread-specific Dimple logger.
	 * <p>
	 * This is simply shorthand for:
	 * <blockquote>
     *   DimpleEnvironment.{@link #active}().{@link #logger}().{@link Logger#warning warning}(String.format(format, args));
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
     *   DimpleEnvironment.{@link #active}().{@link #logger}().{@link Logger#warning warning}(message);
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
     *   DimpleEnvironment.{@link #active}().{@link #logger}().{@link Logger#severe severe}(String.format(format, args));
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
     *   DimpleEnvironment.{@link #active}().{@link #logger}().{@link Logger#severe severe}(message);
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
	
	
	
	/*--------------------
	 * Various registries
	 */
	
	// TODO add conjugate samplers
	
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
	 * Registry of factor graphs associated with this environment.
	 * <p>
	 * @since 0.08
	 * @see FactorGraphRegistry
	 */
	public FactorGraphRegistry factorGraphs()
	{
		return _factorGraphs;
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
	 * Registry of scheduler classes for this environment.
	 * <p>
	 * Supports lookup of {@link IScheduler} implementations by class name.
	 * Primarily for use in dynamic language implementations of Dimple.
	 * <p>
	 * @see ConstructorRegistry
	 * @since 0.08
	 */
	public ConstructorRegistry<IScheduler> schedulers()
	{
		return _schedulers;
	}
	
	/**
	 * Registry of schedule validator classes for this environment.
	 * <p>
	 * Supports lookup of {@link ScheduleValidator} implementations by class name.
	 * Primarily for use in dynamic language implementations of Dimple.
	 * <p>
	 * @see ConstructorRegistry
	 * @since 0.08
	 */
	public ConstructorRegistry<ScheduleValidator> scheduleValidators()
	{
		return _scheduleValidators;
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
	
	/**
	 * The default solver factory used by newly constructed {@link FactorGraph}s
	 * @see #setDefaultSolver(IFactorGraphFactory)
	 * @since 0.08
	 */
	public @Nullable IFactorGraphFactory<?> defaultSolver()
	{
		return _defaultSolverFactory.get();
	}
	
	/**
	 * Restores the system default solver factory for this environment.
	 * <p>
	 * The default solver is currently sum-product.
	 * <p>
	 * @since 0.08
	 */
	public void restoreSystemDefaultSolver()
	{
		_defaultSolverFactory.set(new SumProductSolver());
	}
	
	/**
	 * Set the {@linkplain #defaultSolver default solver factory} used by newly constructed {@link FactorGraph}s
	 * @since 0.08
	 */
	public IFactorGraphFactory<?> setDefaultSolver(@Nullable IFactorGraphFactory<?> solver)
	{
		return _defaultSolverFactory.getAndSet(solver);
	}
	
	/*----------------
	 * Lookup methods
	 */
	
	/**
	 * Looks up node within environment with given global id, or null if not found.
	 * @since 0.08
	 */
	public @Nullable Node getNodeByGlobalId(long globalId)
	{
		FactorGraph graph = _factorGraphs.getGraphWithId(Ids.graphIdFromGlobalId(globalId));
		if (graph != null)
		{
			return graph.getNodeByLocalId(Ids.localIdFromGlobalId(globalId));
		}
		
		return null;
	}
}
