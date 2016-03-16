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

/**
 * Classes for controlling the generation and handling of events specific to Dimple.
 * 
 * <h2>Overview</h2>
 * 
 * Dimple events are intended for use in monitoring changes to a Dimple model, data values
 * or solver state. The events can either by passed to an external monitoring system, logged
 * somewhere, or even used synchronously respond to changes. The event system is is designed to
 * have minimal overhead when there are no listeners active for a given event type.
 * 
 * <h2>Events</h2>
 * 
 * Events are represented by instances of concrete subclasses of the class
 * {@linkplain com.analog.lyric.dimple.events.DimpleEvent DimpleEvent}. There are three classes of event types,
 * represented by distinct abstract subclasses:
 * <ul>
 * <li>{@linkplain com.analog.lyric.dimple.events.ModelEvent ModelEvent}: changes to model such as add or removal of
 * factors or variables.
 * <li>{@linkplain com.analog.lyric.dimple.events.DataEvent DataEvent}: changes to observed data values or variable
 * input distributions.
 * <li>{@linkplain com.analog.lyric.dimple.events.SolverEvent SolverEvent}: solver-specific events such as message
 * updates for belief-propagation solvers or new sample values for sampling solvers.
 * </ul>
 * 
 * A full hierarchy of events can be seen easily using the Type Hierarchy view in Eclipse (press
 * Ctrl+Shift+H, type "DimpleEvent" in the dialog, and hit "Ok").
 * 
 * <h2>Event listeners</h2>
 * 
 * An instance of the {@linkplain com.analog.lyric.dimple.events.DimpleEventListener DimpleEventListener}
 * class is responsible for dispatching of events raised from event sources from the same
 * {@linkplain com.analog.lyric.dimple.environment.DimpleEnvironment Dimple environment}. Almost always the
 * {@linkplain com.analog.lyric.dimple.environment.DimpleEnvironment#active() active} environment is the
 * only environment, but if there is more than one, the event source object knows which environment it belongs
 * to. If there is no listener on the environment, which is the default, then no events should be generated.
 * You may set or clear the listener on the environment using the
 * {@linkplain com.analog.lyric.dimple.environment.DimpleEnvironment#setEventListener setEventListener} method or
 * you can simply force creation of a listener using the
 * {@linkplain com.analog.lyric.dimple.environment.DimpleEnvironment#createEventListener createEventListener} method.
 * <p>
 * Once a listener has been associated with the environment, then one or more handlers may be registered to
 * handle events. The registration must specify the base class of the type of event to
 * be handled and the root object on which events will be raised. It will be easiest to register for
 * events on the root factor graph, but it may sometimes be desirable to set up handlers for specific variables,
 * factors or subgraphs. For instance, to register handlers for various belief propagation messages on
 * a graph, you could write:
 * 
 * <pre>
 *     void registerHandlers(FactorGraph fg)
 *     {
 *         DimpleEventListener listener = fg.getEnvironment().createEventListener();
 *         listener.register(factorMessageHandler, FactorToVariableMessageEvent.class, fg);
 *         listener.register(variableMessageHandler, VariableToFactorMessageEvent.class, fg);
 *     }
 * </pre>
 * 
 * It is also possible to register for events on the environment itself, which will affect all graphs
 * in the environment:
 * 
 * <pre>
 *     DimpleEnvironment env = DimpleEnvironment.active();
 *     DimpleEventListener listener = env.createEventListener();
 *     listener.register(factorMessageHandler, FactorToVariableMessageEvent.class, env);
 *     listener.register(variableMessageHandler, VariableToFactorMessageEvent.class, env);
 * </pre>

 * Handlers can be removed by one of the various {@code unregister} methods on the listener.
 * <p>
 * Changes to event registration or the value of the root listener are not guaranteed to take effect until
 * the next time the affected objects have been initialized or
 * {@linkplain com.analog.lyric.dimple.events.IDimpleEventSource#notifyListenerChanged() notifyListenerChanged()}
 * has been invoked on the object that generates the event. This is important for model events in particular
 * since model changes typically occur prior to initialization.
 * 
 * <h2>Event handlers</h2>
 * 
 * Dimple event handlers are objects that implement the
 * {@linkplain com.analog.lyric.dimple.events.IDimpleEventHandler IDimpleEventHandler}
 * interface. In practice most handlers should simply extend
 * {@linkplain com.analog.lyric.dimple.events.DimpleEventHandler DimpleEventHandler}.
 * For example, here is a simple handler class that simply prints events out to the console with a verbosity of one:
 * 
 * <pre>
 * public class EventPrinter extends DimpleEventHandler&lt;DimpleEvent&gt;
 * {
 *     public void handleEvent(DimpleEvent event)
 *     {
 *         event.println(System.out, 1);
 *     }
 * }
 * </pre>
 * 
 * Handler classes that are specific to a particular event subclass can be parameterized appropriately
 * to avoid the need for downcasts. For example, here is a simple handler that keeps a running total
 * of the total graph score during Gibbs sampling based on sample score differences:
 * 
 * <pre>
 * public class RunningScoreHandler extends DimpleEventHandler&lt;GibbsScoredVariableUpdateEvent&gt;
 * {
 *     public double score;
 * 
 *     RunningScoreHandler(double startingScore)
 *     {
 *         score = startingScore;
 *     }
 * 
 *     public void handleEvent(GibbsScoredVariableUpdateEvent event)
 *     {
 *         score += event.getScoreDifference();
 *     }
 * }
 * </pre>
 * 
 * <h2>Concurrency issues</h2>
 * 
 * Handlers are invoked synchronously as soon as the events have been constructed. Accessing the attributes
 * of the event objects is guaranteed to be thread safe, but calling methods on the event source or model
 * objects referred to in the event may not be safe.
 * 
 * <h2>Serialization</h2>
 * 
 * Dimple events implement the {@link java.io.Serializable} interface, which allows them to be serialized
 * to binary form for persistent storage or transmission to a remote monitor. However, note that events
 * purposely will not attempt to serialize references to model or solver elements, since that would require
 * serializing the entire graph. This means that deserialized event objects will contain null references for
 * any such object. Instead of serializing the actual object, the name and id of the object will be serialized.
 * <p>
 * @since
 * <ul>
 * <li>0.07 - support for DimpleEnvironment
 * <li>0.06 - package was first introduced
 * </ul>
 * @author Christopher Barber
 */
@org.eclipse.jdt.annotation.NonNullByDefault
package com.analog.lyric.dimple.events;

