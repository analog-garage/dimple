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
 * Extended Statically typed hierarchical option support for Dimple.
 * <p>
 * This package extends the core option functionality provided by the {@link com.analog.lyric.options} package
 * for use with Dimple.
 * 
 * <h2>Option definitions</h2>
 * <p>
 * Most Dimple option keys are declared in classes specifically created for that purpose and that are
 * descended from the base class {@linkplain com.analog.lyric.dimple.options.DimpleOptions DimpleOptions}.
 * This allows developers to browse most options by exploring the class hierarchy rooted at the class
 * using their IDE. For instance, in Eclipse pressing Ctrl+Shift+H and typing "DimpleOptions" in the dialog
 * box will display the Type Hierarchy view. Some options are defined directly in the class that uses them.
 * This is the case with options that configure {@linkplain com.analog.lyric.dimple.solvers.core.proposalKernels
 * proposal kernels} and {@linkplain com.analog.lyric.dimple.solvers.gibbs.samplers.generic generic samplers}
 * for the Gibbs solver. You can see an enumeration of the classes that define options in the documentation
 * for the {@link com.analog.lyric.dimple.options.DimpleOptionRegistry DimpleOptionRegistry}.
 * <p>
 * Most options are solver specific and can be found in the corresponding options declaration class for that solver.
 * For instance, most options affecting the configuration of the Gibbs solver can be found in
 * {@link com.analog.lyric.dimple.solvers.gibbs.GibbsOptions GibbsOptions}.
 * 
 * <h2>Option lookup</h2>
 * <p>
 * The {@linkplain com.analog.lyric.dimple.options.DimpleOptionHolder DimpleOptionHolder} class extends
 * the core {@linkplain com.analog.lyric.options.LocalOptionHolder LocalOptionHolder} implementation to use a more
 * complex option lookup strategy than
 * the default one, which simply iterates over the
 * {@linkplain com.analog.lyric.options.IOptionHolder#getOptionParent option parent} chain.
 * Instead Dimple solver objects will look for options set on the corresponding model object before recursing
 * to look at the option parent. This allows users to set options on model factors and variables that will
 * take effect on their corresponding solver variables and have those options take priority over those set
 * on the solver graph or model graph. Users may set solver options directly on solver variables or factors that will
 * take precedence over those set on the model, but that will rarely be needed.
 * <p>
 * {@linkplain com.analog.lyric.dimple.model.core.FactorGraph FactorGraph} objects that are not subgraphs of
 * another graph will return their {@linkplain com.analog.lyric.dimple.environment.DimpleEnvironment DimpleEnvironment}
 * as their {@linkplain com.analog.lyric.options.IOptionHolder#getOptionParent option parent}, consequently the
 * environment object will serve as the root object in the option hierarchy and can be used to set default values
 * for options to be shared across all graphs in the environment.
 * <p>
 * The exact lookup iteration algorithm is described in more detail in
 * {@linkplain com.analog.lyric.dimple.events.EventSourceIterator EventSourceIterator}, but can
 * be summarized succinctly considering how an option value for a solver variable is found. In that case, the following
 * objects are examined in order until one is found that contains a local setting for that option:
 * <ol>
 * <li>The solver variable itself
 * <li>The corresponding model variable
 * <li>The solver factor graph that immediately contains the solver variable
 * <li>The corresponding model factor graph that contains the model variable
 * <li>If the graph has a parent graph, the solver parent graph will be looked at followed by the model parent
 * graph and so on recursively until there are no more parent graphs
 * <li>Finally, the environment object of the root model graph will be examined.
 * </ol>
 * If the option is not set anywhere in the hierarchy, then most code that depends on options will use
 * the {@linkplain com.analog.lyric.options.IOptionKey#defaultValue default value} defined by the key, but
 * there are some exceptions
 * (e.g. {@linkplain com.analog.lyric.dimple.options.DimpleOptions#randomSeed DimpleOptions.randomSeed}).
 * 
 * <h2>Examples</h2>
 * <p>
 * Here is a toy example that illustrates several common usage patterns for options in Dimple.
 * <p>
 * Start with the following simple model consisting of a square of discrete variables connected by some
 * arbitrary factors:
 * <p>
 * <blockquote>
 * <pre>
 * //
 * // (d1)---[f12]---(d2)
 * //   |              |
 * // [f13]          [f24]
 * //   |              |
 * // (d3)---[f34]---(d4)
 * //
 * FactorGraph fg = FactorGraph();
 * DiscreteDomain digit = DiscreteDomain.range(0,9);
 * Discrete d1 = new Discrete(digit), d2 = new Discrete(digit);
 * Discrete d3 = new Discrete(digit), d4 = new Discrete(digit);
 * Factor f12 = fg.addFactor(new HorizontalFactor(<em>horizontalArgs</em>), d1, d2);
 * Factor f34 = fg.addFactor(new HorizontalFactor(<em>horizontalArgs</em>), d3, d4);
 * Factor f13 = fg.addFactor(new VerticalFactor(<em>verticalArgs</em>), d1, d3);
 * Factor f24 = fg.addFactor(new VerticalFactor(<em>verticalArgs</em>), d2, d4);
 * </pre>
 * </blockquote>
 * <p>
 * If we were to use the min-sum solver on this model, we might want to enable damping
 * and specify the number of iterations to run. This can be done by setting options on the
 * model graph object:
 * <blockquote>
 * <pre>
 * fg.setOption(BPOptions.damping, .9);
 * fg.setOption(SolverOptions.iterations, 20);
 * </pre>
 * </blockquote>
 * Later, when a solver is created for the graph, it and its component objects will inherit these settings:
 * <blockquote>
 * <pre>
 * MinSumSolverGraph minSumGraph = fg.setSolverFactory(new MinSumSolver());
 * assert(minSumGraph.getOption(SolverOptions.iterations) == 20);
 * ISolverVariable sd1 = minSumGraph.getSolverVariable(d1);
 * assert(sd1.getOption(BPOptions.damping) == .9);
 * </pre>
 * </blockquote>
 * If you find you need to override the damping value for a particular variable or factor, you
 * can set it on the model object:
 * <blockquote>
 * <pre>
 * d1.setOption(BPOptions.damping, .7);
 * </pre>
 * </blockquote>
 * You may also set options directly on solver objects as well, but that is usually less convenient
 * and should rarely be necessary:
 * <blockquote>
 * <pre>
 * sd1.setOption(BPOptions.damping, .7);
 * </pre>
 * </blockquote>
 * The {@linkplain com.analog.lyric.options.IOptionHolder#setOption setOption} method only accepts a single
 * value of the appropriate type for the option. For some types of options, it can be slightly easier to set
 * the option using methods on the option key object itself. For instance, when setting options that have
 * list values, the key usually has a {@code set} method that takes multiple values:
 * <blockquote>
 * <pre>
 * // These two statements are equivalent:
 * d1.setOption(BPOptions.nodeSpecificDamping, new OptionDoubleList(.7, .8));
 * BPOptions.nodeSpecificDamping.set(d1, .7, .8);
 * </pre>
 * </blockquote>
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@org.eclipse.jdt.annotation.NonNullByDefault
package com.analog.lyric.dimple.options;

