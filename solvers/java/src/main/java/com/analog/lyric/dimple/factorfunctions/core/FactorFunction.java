/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

import static java.util.Objects.*;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.MatrixProduct;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.DomainList;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.util.misc.Matlab;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public abstract class FactorFunction implements IFactorFunction
{
	// FIXME remove default FactorFunction.getParameterizedMessage implementation
	// This is only here to shut up errors until I get around to implementing this on
	// implementors of IParametricFactorFunction
	public @Nullable IParameterizedMessage getParameterizedMessage()
	{
		return null;
	}
	
	/*-------
	 * State
	 */
	
	// Cache of factor tables for this function by domain.
	private AtomicReference<ConcurrentMap<JointDomainIndexer, IFactorTable>> _factorTables =
		new AtomicReference<ConcurrentMap<JointDomainIndexer, IFactorTable>>();
	private final String _name;
	
	/*--------------
	 * Construction
	 */
	
	protected FactorFunction()
	{
		this((String)null);
	}
	
    protected FactorFunction(@Nullable String name)
    {
		_name = name != null ? name : getClass().getSimpleName();
	}
    
    protected FactorFunction(FactorFunction other)
    {
    	_name = other._name;
    }
    
    @Override
    public FactorFunction clone() throws CloneNotSupportedException
    {
    	throw new CloneNotSupportedException(String.format("%s objects do not support cloning",
    		getClass().getSimpleName()));
    }

    /*------------------
     * Abstract methods
     */
    
	@Override
	public abstract double evalEnergy(Value[] values);

    /*------------------------
     * FactorFunction methods
     */

	/**
	 * Single argument version of {@link #evalEnergy(Value[])}
	 * <p>
	 * Simply wraps the {@code value} in an an array and calls the former.
	 * <p>
	 * The existence of this method helps to avoid accidental invocation of {@link #evalEnergy(Object...)}.
	 * @since 0.08
	 */
	public double evalEnergy(Value value)
	{
		return evalEnergy(new Value[] { value });
	}
	
	// Evaluate the factor function energy using unwrapped object arguments
	@Matlab
	public double evalEnergy(Object... arguments)
	{
		final int size = arguments.length;
		final Value[] values = new Value[size];
		for (int i = 0; i < size; ++i)
			values[i] = Value.create(arguments[i]);
		
		final double energy = evalEnergy(values);
		if (energy != energy)	// Faster isNaN
			return Double.POSITIVE_INFINITY;
		return energy;
	}
	
	@Matlab
	public double evalEnergy(Object value)
	{
		final double energy = evalEnergy(Value.create(value));
		return energy == energy ? energy : Double.POSITIVE_INFINITY;
	}

	// Evaluate the factor and return a weight rather than an energy value
	@Override
	public double eval(Value[] values)
	{
		final double energy = evalEnergy(values);
		if (energy != energy)	// Faster isNaN
			return Double.POSITIVE_INFINITY;
		return Math.exp(-energy);
	}

	/**
	 * Single argument version of {@link #eval(Value[])}
	 * <p>
	 * Simply wraps the {@code value} in an an array and calls the former.
	 * <p>
	 * The existence of this method helps to avoid accidental invocation of {@link #eval(Object...)}.
	 * @since 0.08
	 */
	public double eval(Value value)
	{
		return eval(new Value[] { value });
	}
	
	/**
	 *  Evaluate the factor and return a weight value using unwrapped object arguments
	 * @since 0.08
	 */
	@Matlab
	public double eval(Object... arguments)
	{
		return Math.exp(-evalEnergy(arguments));
	}
	
	/**
	 * For deterministic-directed factor functions, set the value of the output variables given the input variables,
	 *<p>
	 * The default implementation does nothing; any deterministic-directed factor function must override this method.
	 */
	@Override
	public void evalDeterministic(Value[] arguments)
	{
	}
	
	// Used by MATLAB core Dimple code for discrete variables only
    @Matlab
	public @Nullable Object getDeterministicFunctionValue(Object... arguments)
	{
		Value[] fullArgumentList = new Value[arguments.length + 1];
		for (int i = 0; i < arguments.length; i++)
			fullArgumentList[i + 1] = Value.create(arguments[i]);
		fullArgumentList[0] = RealValue.create();	// Ok to use RealValue since it will be a number, but we don't know what
		evalDeterministic(fullArgumentList);
		return fullArgumentList[0].getObject();
	}
    
    /**
     * Run {@link #evalDeterministic} without modifying the arguments.
     * @param arguments will be copied to return
     * @return freshly allocated array of {@link Value}s holding references the original {@code Value}
     * for each input argument, and new clones for output arguments.
     */
    public Value[] evalDeterministicToCopy(Value[] arguments)
    {
    	final Value[] copy = cloneOutputArguments(arguments);
    	evalDeterministic(copy);
    	return copy;
    }

	/**
	 * @since 0.05
	 */
   public boolean factorTableExists(@Nullable JointDomainIndexer domains)
    {
    	boolean exists = false;
    	if (domains != null)
    	{
    		ConcurrentMap<JointDomainIndexer, IFactorTable> factorTables = _factorTables.get();
    		exists = factorTables != null && factorTables.containsKey(domains);
    	}
    	return exists;
    }
    
	/**
	 * @since 0.05
	 */
	public boolean factorTableExists(Factor factor)
	{
		return factorTableExists(factor.getDomainList().asJointDomainIndexer());
	}

	
    public boolean convertFactorTable(@Nullable JointDomainIndexer oldDomains, @Nullable JointDomainIndexer newDomains)
    {
    	boolean converted = false;
    	
    	if (oldDomains != null && newDomains != null)
    	{
    		ConcurrentMap<JointDomainIndexer, IFactorTable> tables = _factorTables.get();
    		
    		if (tables != null)
    		{
    			IFactorTable table = tables.get(oldDomains);
    			if (table != null)
    			{
    				table.setConditional(Objects.requireNonNull(newDomains.getOutputSet()));
    			}
    		}
    	}
    	
    	return converted;
    }
    
	public @Nullable int[] getDirectedToIndices(int numEdges)
	{return getDirectedToIndices();}	// May depend on the number of edges

	protected @Nullable int[] getDirectedToIndices()
	{return null;}	// This can be overridden instead, if result doesn't depend on the number of edges

	/**
	 * Returns the output indices that can be changed when specified input is changed or else null
	 * if the same as the full set of output edges.
	 * <p>
	 * The default implementation returns null.
	 * <p>
	 * This may be overridden for functions that have multiple outputs and inputs for which
	 * a single input may only affect a subset of the full outputs (e.g. {@link MatrixProduct}).
	 * 
	 * @since 0.05
	 */
	public @Nullable int[] getDirectedToIndicesForInput(Factor factor, int inputEdge)
	{
		return null;
	}
	
	/**
	 * @since 0.05
	 */
	public final IFactorTable getFactorTable(Domain [] domains)
    {
    	return getFactorTable(DomainList.create(domains).asJointDomainIndexer());
    }
    
    /**
     * @since 0.05
     */
    public IFactorTable getFactorTable(@Nullable JointDomainIndexer domains)
    {
    	if (domains == null)
    	{
    		throw new DimpleException("only support getFactorTable for discrete domains");
    	}

    	ConcurrentMap<JointDomainIndexer, IFactorTable> factorTables = _factorTables.get();
    	if (factorTables == null)
    	{
    		_factorTables.compareAndSet(null, new ConcurrentHashMap<JointDomainIndexer, IFactorTable>());
    		factorTables = _factorTables.get();
    	}

    	IFactorTable factorTable = factorTables.get(domains);
    	
    	if (factorTable == null)
    	{
    		IFactorTable newTable = createTableForDomains(domains);
    		factorTable = factorTables.putIfAbsent(domains, newTable);
    		if (factorTable == null)
    		{
    			factorTable = newTable;
    		}
    	}
    	
    	return factorTable;
    }
    
    /**
     * @since 0.05
     */
    public IFactorTable getFactorTable(Factor factor)
    {
    	return getFactorTable(factor.getDomainList().asJointDomainIndexer());
    }

    /**
     * @since 0.05
     */
    public @Nullable IFactorTable getFactorTableIfExists(@Nullable JointDomainIndexer domains)
    {
    	IFactorTable factorTable = null;
    	if (domains != null)
    	{
    		ConcurrentMap<JointDomainIndexer, IFactorTable> factorTables = _factorTables.get();
    		if (factorTables != null)
    		{
    			factorTable = factorTables.get(domains);
    		}
    	}
    	return factorTable;
    }
    
    /**
     * @since 0.05
     */
    public @Nullable IFactorTable getFactorTableIfExists(Factor factor)
    {
    	return getFactorTableIfExists(factor.getDomainList().asJointDomainIndexer());
    }
    
	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public boolean isDeterministicDirected()
	{return false;}

	@Override
	public boolean isDirected()
	{return false;}
	
	@Override
	public boolean isParametric()
	{
		return IParametricFactorFunction.class.isInstance(this);
	}
	
    /**
     * The maximum number of variable updates beyond which {@link #updateDeterministic}
     * should not be called.
     * <p>
     * Default implementation returns 0, indicating that {@link #updateDeterministic} should
     * never be called.
     * <p>
     * @param numEdges is the number of edges (variables) to consider. It corresponds to the
     * size of the first argument to {@link #updateDeterministic}.
     * 
     * @since 0.05
     */
    public int updateDeterministicLimit(int numEdges)
    {
    	return 0;
    }
    
    /**
     * Deterministically update output values in {@code values} array incrementally based on changed input
     * values.
     * <p>
     * For functions that support it, this can allow for more efficient computation when there are many
     * inputs and or outputs and only a small subset of inputs have changed (e.g. one when doing a single
     * Gibbs update).
     * <p>
     * The default implementation delegates back to {@link #evalDeterministic(Value[])}, which
     * will do a full update.
     * <p>
     * @param values is the array of output and input values that are maintained persistently. When this
     * method is called, it may be assumed that the contents contains the current values of all input
     * variables and the last computed values of all output variables (which were based on previous values
     * of inputs).
     * @param oldValues contains descriptions of the variable number and old value of each input. Only indexes
     * of input variables should be specified. This list should not contain more than
     * {@link #updateDeterministicLimit(int)} elements.
     * @param changedOutputsHolder should be set by the function to contain the list of indexes of output variables
     * that were changed or else set to contain null if all of the outputs were modified.
     * @return true if update was done incrementally (i.e not all inputs were processed), false if full
     * update was done.
     * 
     * @throws IndexOutOfBoundsException if an index in {@code oldValues} does not refer to an input variable.
     */
    public boolean updateDeterministic(Value[] values, Collection<IndexedValue> oldValues,
    	AtomicReference<int[]> changedOutputsHolder)
    {
		evalDeterministic(values);
		changedOutputsHolder.set(null);
		return false;
    }
    
    /**
     * Run {@link #updateDeterministic} without modifying the output arguments.
     * @param arguments will be copied to return
     * @return freshly allocated array of {@link Value}s holding references the original {@code Value}
     * for each input argument, and new clones for output arguments.
     */
    public Value[] updateDeterministicToCopy(Value[] arguments, Collection<IndexedValue> oldValues,
    	AtomicReference<int[]> changedOutputsHolder)
    {
    	final Value[] copy = cloneOutputArguments(arguments);
    	updateDeterministic(copy, oldValues, changedOutputsHolder);
    	return copy;
    }

    /**
     * Indicates whether to use {@link #updateEnergy}
     * <p>
     * Functions that override {@link #updateEnergy} should override this method to return
     * true when that function is expected to provide a performance benefit over {@link #evalEnergy(Value[])}.
     * <p>
     * The default implementation returns false.
     * <p>
     * @param values the values that will be passed to {@code updateEnergy}.
     * @param nChangedValues the number of {@code oldValues} that will be provided to {@code updateEnergy}.
     * @since 0.08
     */
    public boolean useUpdateEnergy(Value[] values, int nChangedValues)
    {
    	return false;
    }
    
    /**
     * Optimized energy evaluation based on previously computed value.
     * <p>
     * Implementations of this method should produce the same energy value you
     * would get by calling {@link #evalEnergy} with the specified {@code values},
     * and that is what the default implementation does.
     * <p>
     * When the same function needs to be evaluated many times with only small changes
     * in the input arguments (as is the case in the Gibbs solver), the energy can
     * sometimes be more efficiently computed based on the previous energy values together
     * with information about the value changes.
     * <p>
     * Functions that implement this method should also implement {@code #useUpdateEnergy}.
     * <p>
     * @param values the argument values for which the energy is to be computed
     * @param oldValues specifies one or more old values. Each {@link IndexedValue} argument
     * indicates the value and its position in the argument list.
     * @param oldEnergy the previous energy computed from {@code values} except for those
     * overridden by {@code oldValues}.
     * 
     * @since 0.08
     */
    public double updateEnergy(Value[] values, IndexedValue[] oldValues, double oldEnergy)
    {
    	return evalEnergy(values);
    }
    
    /*-------------------
     * Protected methods
     */
    
    /**
     * Generate a factor table for this function over the given domains.
     * <p>
     * Invoked implicitly by {@link #getFactorTable(JointDomainIndexer)} the first time
     * a factor table is needed for specified domains.
     */
    protected IFactorTable createTableForDomains(JointDomainIndexer domains)
    {
    	return FactorTable.create(this, domains);
    }
    
	/*********
	 * Methods from FactorFunctionWithConstants that allow calling even if there are no constants
	 */
	
	/**
	 * Return whether or not there are constants in the factor function instance
	 * 
	 * @since 0.05
	 */
	public boolean hasConstants()
	{
		return false;
	}
	
	/**
	 * Return number of constants built into the factor function instance.
	 * <p>
	 * Default implementation returns zero.
	 * 
	 * @since 0.05
	 */
	public int getConstantCount()
	{
		return 0;
	}
	
	/**
	 * Return the innermost FactorFunction object, in case it is wrapped in a containing class.
	 * In this case, there is no containing class
	 * 
	 * @since 0.05
	 */
	public FactorFunction getContainedFactorFunction()
	{
		return this;
	}
	
	/**
	 * Returns constant at edge identified by {@code index} or null if specified
	 * edge is not a constant.
	 * <p>
	 * Default implementation returns null.
	 * 
	 * @since 0.05
	 */
	public @Nullable Object getConstantByIndex(int index)
	{
		return null;
	}
	
	/**
	 * Returns whether or not the index corresponds to a constant
	 * @since 0.05
	 */
	public boolean isConstantIndex(int index)
	{
		return false;
	}
	
	/**
	 * Returns whether or not the index range corresponds to a constant
	 * @since 0.06
	 */
	public boolean hasConstantsInIndexRange(int minIndex, int maxIndex)
	{
		return false;
	}
	/**
	 * Returns whether or not the index corresponds to a constant
	 * @since 0.05
	 */
	public boolean hasConstantAtOrAboveIndex(int index)
	{
		return false;
	}
	
	/**
	 * Returns whether or not the index corresponds to a constant
	 * @since 0.05
	 */
	public boolean hasConstantAtOrBelowIndex(int index)
	{
		return false;
	}
	
	/**
	 * Returns whether or not the index corresponds to a constant
	 * @since 0.05
	 */
	public int numConstantsInIndexRange(int minIndex, int maxIndex)
	{
		return 0;
	}
	
	/**
	 * Returns whether or not the index corresponds to a constant
	 * @since 0.05
	 */
	public int numConstantsAtOrAboveIndex(int index)
	{
		return 0;
	}
	
	/**
	 * Returns whether or not the index corresponds to a constant
	 * @since 0.05
	 */
	public int numConstantsAtOrBelowIndex(int index)
	{
		return 0;
	}


	/**
	 * Return the edge number associated with the specified factor index.
	 * For factors with not constants, these are identical.
	 * 
	 * @since 0.05
	 */
	public int getEdgeByIndex(int index)
	{
		return index;
	}
	
	/**
	 * Return all edges within the range of indices specified
	 * @since 0.06
	 */
	public @Nullable int[] getEdgesByIndexRange(int minIndex, int maxIndex)
	{
		int[] edges = new int[maxIndex - minIndex + 1];
		for (int i = 0, index = minIndex; index <= maxIndex; i++, index++)
			edges[i] = index;
		return edges;
	}

	/**
	 * @since 0.05
	 */
	public int getIndexByEdge(int edge)
	{
		return edge;
	}

	/**
	 * @since 0.05
	 */
	public Object[] getConstants()
	{
		return new Object[] {};
	}

	/**
	 * @since 0.05
	 */
	public int[] getConstantIndices()
	{
		return new int[] {};
	}
	
    /*---------------------------
     * Parameter utility methods
     */
    
	/**
	 * Looks up a value or default from a map.
	 * <p>
	 * Returns result from {@code map.get(key)} if non-null, otherwise
	 * returns {@code defaultValue}.
	 * <p>
	 * This can be used to read parameters in FactorFunction constructors that take a parameter  map.
	 * <p>
	 * @since 0.07
	 * @see #getFirstOrDefault(Map, Object, Object...)
	 */
	public static <K,V> V getOrDefault(Map<K,V> map, K key, V defaultValue)
	{
		final V value = map.get(key);
		return value != null ? value : defaultValue;
	}

	/**
	 * Looks up a value using multiple keys
	 * <p>
	 * Returns first non-null result from {@code map.get(key}} for each key
	 * in {@code keys}. If none is found, null is returned.
	 * @since 0.07
	 * @see #getOrDefault(Map, Object, Object)
	 */
	@SafeVarargs
	public static @Nullable <K,V> V getFirst(Map<K,V> map, K ... keys)
	{
		for (K key : keys)
		{
			V value = map.get(key);
			if (value != null)
			{
				return value;
			}
		}
		
		return null;
	}

	/**
	 * Looks up a value using multiple keys and returns value or default.
	 * <p>
	 * Returns first non-null result from {@code map.get(key}} for each key
	 * in {@code keys}. If none is found, the {@code defaultValue} is returned instead.
	 * @since 0.07
	 * @see #getOrDefault(Map, Object, Object)
	 */
	@SafeVarargs
	public static <K,V> V getFirstOrDefault(Map<K,V> map, V defaultValue, K ... keys)
	{
		V value = getFirst(map, keys);
		return value != null ? value : defaultValue;
	}

	@SafeVarargs
	public static <K,V> V require(Map<K,V> map, K ... keys)
	{
		V value = getFirst(map, keys);
		if (value != null)
		{
			return value;
		}

		throw new IllegalArgumentException(String.format("Expected parameter named '%s'", keys[0]));
	}
	
	/*-----------------
	 * Private methods
	 */
	
    /**
     * Returns a copy of arguments with output arguments replaced with clones.
     * @since 0.08
     */
    private Value[] cloneOutputArguments(Value[] arguments)
    {
    	final Value[] copy = arguments.clone();
    	
    	// Clone the Values for output indices only
    	for (int i : requireNonNull(getDirectedToIndices(arguments.length)))
    	{
    		copy[i] = copy[i].clone(); // Assumes a deep clone
    	}
    	
    	return copy;
    }
	
}
