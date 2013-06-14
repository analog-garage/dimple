package com.analog.lyric.dimple.FactorFunctions.core;

import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.parameters.IParameterList;

public interface IFactorFunction
{
	public abstract String getName();

	/*------------------------
	 * Categorization methods
	 */
	
	/**
	 * @return true if function is deterministic, i.e. {@link #evalDensity} always returns either 0 or 1 for
	 * all arguments and {@link #evalEnergy} returns either zero or infinity.
	 */
	public abstract boolean isDeterministic();
	
	/**
	 * @return true if function is inherently directed such that some of its arguments can
	 * be considered to be "outputs" of its other arguments.
	 */
	public abstract boolean isDirected();

	/**
	 * @return true if all arguments to function must have {@link DiscreteDomain}.
	 */
	public abstract boolean isDiscrete();
	
	/**
	 * Indicates whether function is defined in terms of one or more parameters, which
	 * may be learned.
	 */
	public abstract boolean isParametric();

	/**
	 * @return true if function is known to produce normalized probability density, i.e.
	 * sum/integral over variable domains is equal to one. May be false even if
	 * function is in fact in normalized form.
	 */
	public abstract boolean isNormalized();
	
	/**
	 * @return true if all arguments to function must have {@link RealDomain}.
	 */
	public abstract boolean isReal();
	
	/*------------------
	 * Argument methods
	 */
	
	/**
	 * @return the maximum number of arguments to the function.
	 */
	public abstract int getMaxArguments();
	
	/**
	 * @return the minimum number of arguments to the function.
	 */
	public abstract int getMinArguments();
	
	/**
	 * The number of output arguments, if this is a directed function.
	 */
	public abstract int getNumberOfOutputs();
	
	/**
	 * The indexes of all output arguments in increasing order or an empty array if there are no outputs.
	 * The size of the returned indexes must equal {@link #getNumberOfOutputs}.
	 * @see #getOutputIndex
	 */
	public abstract int[] getOutputIndices();
	
	/**
	 * The indexes of all output arguments in increasing order given the specified number of arguments
	 * or an empty array if there are no outputs. The size of the returned indexes must equal
	 * {@link #getNumberOfOutputs}.
	 * 
	 * @see #getOutputIndex
	 */
	public abstract int[] getOutputIndices(int nArguments);

	/**
	 * The index of the first output argument.
	 * @see #getOutputIndices()
	 */
	public abstract int getOutputIndex();
	
	/*-------------------
	 * Parameter methods
	 */
	
	// REFACTOR: Do we need this on all factor functions?
	// Should this just be a special case of getParameters()?
	public abstract FactorTable getFactorTable(Domain[] domainList);

	// REFACTOR: does this need a variant that takes a domain list?
	public abstract IParameterList<?> getParameters();

	/**
	 * Returns the default conjugate prior function for this function's
	 * non-fixed parameters or null if there is none.
	 */
	public abstract IFactorFunction makeDefaultConjugatePrior();
	
	/*--------------------
	 * Evaluation methods
	 */
	
	/**
	 * Evaluates the possibly unnormalized negative log probability density of the arguments.
	 * <p>
	 * This must be equivalent to exp(-{@link #evalEnergy}({@code arguments})).
	 */
	public abstract double evalDensity(Object ... arguments);
	
	/**
	 * Evaluates the possibly unnormalized negative log probability density of the arguments.
	 * <p>
	 * This must be equivalent to -log({@link #evalDensity}({@code arguments})).
	 */
	public abstract double evalEnergy(Object ... arguments);
	
	/**
	 * Evaluates and returns the output of function if it {@link #isDeterministic()} and
	 * {@link #getNumberOfOutputs()} is one.
	 * 
	 * @param arguments all arguments excluding the output argument itself.
	 */
	public abstract Object evalDeterministicOutput(Object ... arguments);
	
	/**
	 * Evaluates outputs of function if it {@link #isDeterministic()} writing new values
	 * back into {@code arguments} array.
	 */
	public abstract void evalDeterministicOutputs(Object[] arguments);
	
	/*------------------------
	 * Transformation methods
	 */
	
	public abstract IFactorFunction toFixedParameterForm(double ... parameters);
	
	public abstract IFactorFunction toVariableParameterForm();
	
	public abstract IFactorFunction getDerivative();
	public abstract IFactorFunction getIntegral();
	public abstract IFactorFunction getInverse();

}