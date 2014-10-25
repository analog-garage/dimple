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

package com.analog.lyric.dimple.factorfunctions.core;

import com.analog.lyric.dimple.model.values.Value;

// This is a prototype interface for factor functions to accomodate parameterization.
// I am not sure whether we would want to make this an actual interface of FactorFunction or just modify
// the FactorFunction class accordingly.

/**
 * Abstract interface for factor functions in Dimple.
 * 
 * @since 0.07
 */
public interface IFactorFunction
{
	public abstract String getName();

	/*------------------------
	 * Categorization methods
	 */
	
	/**
	 * @return true if function is deterministic ( i.e. {@link #eval} always returns either 0 or 1 for
	 * all arguments and {@link #evalEnergy} returns either zero or infinity) and directed.
	 * @since 0.07
	 */
	public abstract boolean isDeterministicDirected();
	
	/**
	 * @return true if function is inherently directed such that some of its arguments can
	 * be considered to be "outputs" of its other arguments.
	 */
	public abstract boolean isDirected();

	/**
	 * @return true if all arguments to function must have {@link DiscreteDomain}.
	 */
//	public abstract boolean isDiscrete();
	
	/**
	 * Indicates whether function implements {@link IParametricFactorFunction} interface.
	 * @since 0.07
	 */
	public abstract boolean isParametric();

	/**
	 * @return true if function is known to produce normalized probability density, i.e.
	 * sum/integral over variable domains is equal to one. May be false even if
	 * function is in fact in normalized form.
	 */
//	public abstract boolean isNormalized();
	
	/**
	 * @return true if all arguments to function must have {@link RealDomain}.
	 */
//	public abstract boolean isReal();
	
	/*------------------
	 * Argument methods
	 */
	
	/**
	 * @return the maximum number of arguments to the function.
	 */
//	public abstract int getMaxArguments();
	
	/**
	 * @return the minimum number of arguments to the function.
	 */
//	public abstract int getMinArguments();
	
	/**
	 * The number of output arguments, if this is a directed function.
	 */
//	public abstract int getNumberOfOutputs();
	
	/**
	 * The indexes of all output arguments in increasing order or an empty array if there are no outputs.
	 * The size of the returned indexes must equal {@link #getNumberOfOutputs}.
	 * @see #getOutputIndex
	 */
//	public abstract int[] getOutputIndices();
	
	/**
	 * The indexes of all output arguments in increasing order given the specified number of arguments
	 * or an empty array if there are no outputs. The size of the returned indexes must equal
	 * {@link #getNumberOfOutputs}.
	 * 
	 * @see #getOutputIndex
	 */
//	public abstract int[] getOutputIndices(int nArguments);

	/**
	 * The index of the first output argument.
	 * @see #getOutputIndices()
	 */
//	public abstract int getOutputIndex();
	
	/*-------------------
	 * Parameter methods
	 */
	
	// REFACTOR: Do we need this on all factor functions?
	// Should this just be a special case of getParameters()?
//	public abstract IFactorTable getFactorTable(Domain[] domainList);

	// REFACTOR: does this need a variant that takes a domain list?
//	public abstract IParameterList<?> getParameters();

	/**
	 * Makes default conjugate prior for given keys in order.
	 * <p>
	 * @param keys one or more keys from the list of valid keys for this factor's parameters.
	 */
//	public abstract IFactorFunction makeDefaultConjugatePrior(IParameterKey ... keys);
	
	/*--------------------
	 * Evaluation methods
	 */
	
	/**
	 * Evaluates the possibly unnormalized probability density of the arguments.
	 * <p>
	 * This must be equivalent to exp(-{@link #evalEnergy}({@code values})).
	 * @since 0.07
	 */
	public abstract double eval(Value[] values);
	
	/**
	 * Evaluates the possibly unnormalized negative log probability density of the arguments.
	 * <p>
	 * This must be equivalent to -log({@link #eval}({@code values})).
	 * @since 0.07
	 */
	public abstract double evalEnergy(Value[] values);

	/**
	 *  For deterministic-directed factor functions, set the value of the output variables given the input variables.
	 *  @since 0.07
	 */
	public void evalDeterministic(Value[] arguments);
	
	/*------------------------
	 * Transformation methods
	 */
	
//	public abstract IFactorFunction toFixedParameterForm(double ... parameters);
//
//	public abstract IFactorFunction toVariableParameterForm();
//
//	public abstract IFactorFunction getDerivative();
//	public abstract IFactorFunction getIntegral();
//	public abstract IFactorFunction getInverse();

}