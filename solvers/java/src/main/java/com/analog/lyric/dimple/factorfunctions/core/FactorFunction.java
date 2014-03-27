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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.ThreadSafe;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.MatrixProduct;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.DomainList;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;

@ThreadSafe
public abstract class FactorFunction
{
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
		this(null);
	}
	
    protected FactorFunction(String name)
    {
		_name = name != null ? name : getClass().getSimpleName();
	}

    /*------------------------
     * FactorFunction methods
     */
    
    public boolean convertFactorTable(JointDomainIndexer oldDomains, JointDomainIndexer newDomains)
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
    				table.setConditional(newDomains.getOutputSet());
    			}
    		}
    	}
    	
    	return converted;
    }
    
	// WARNING WARNING WARNING
	// At least one or the other of these must be overridden in a derived class.
	// SHOULD override evalEnergy instead of eval, but for now can override one or the other.
	// TODO: Eventually eval should be made final and so that only evalEnergy can be overridden.
	public double eval(Object... arguments)
	{
		return Math.exp(-evalEnergy(arguments));
	}
	public double evalEnergy(Object... arguments)
	{
		return -Math.log(eval(arguments));
	}

	/**
	 * Default version of evalEnergy that takes values; can be overridden to implement
	 * a more efficient version that doesn't require copying the input array
	 * @since 0.05
	 */
	public double evalEnergy(Value[] values)
	{
		final int size = values.length;
		final Object[] objects = new Object[size];
		for (int i = 0; i < size; ++i)
			objects[i] = values[i].getObject();

		return evalEnergy(objects);
	}


	/**
	 * @since 0.05
	 */
	public void evalDeterministic(Object[] arguments)
	{ }

	/**
	 * @since 0.05
	 */
	public void evalDeterministic(Factor factor, Value[] values)
	{
		final Object[] objects = Value.toObjects(values);
		evalDeterministic(objects);
		final int[] directedTo = factor.getDirectedTo();
		if (directedTo != null)
		{
			for (int to : directedTo)
			{
				values[to].setObject(objects[to]);
			}
		}
	}

	
	/**
	 * @since 0.05
	 */
   public boolean factorTableExists(JointDomainIndexer domains)
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


	public Object getDeterministicFunctionValue(Object... arguments)
	{
		Object[] fullArgumentList = new Object[arguments.length + 1];
		System.arraycopy(arguments, 0, fullArgumentList, 1, arguments.length);
		evalDeterministic(fullArgumentList);
		return fullArgumentList[0];
	}

	public int[] getDirectedToIndices(int numEdges)
	{return getDirectedToIndices();}	// May depend on the number of edges

	protected int[] getDirectedToIndices()
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
	public int[] getDirectedToIndicesForInput(Factor factor, int inputEdge)
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
    public IFactorTable getFactorTable(JointDomainIndexer domains)
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
    public IFactorTable getFactorTableIfExists(JointDomainIndexer domains)
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
    public IFactorTable getFactorTableIfExists(Factor factor)
    {
    	return getFactorTableIfExists(factor.getDomainList().asJointDomainIndexer());
    }
    
	public String getName()
	{
		return _name;
	}

	public boolean isDeterministicDirected()
	{return false;}

	public boolean isDirected()
	{return false;}

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
     * The default implementation delegates back to {@link #evalDeterministic(Object[])}, which
     * will do a full update.
     * <p>
     * @param values is the array of output and input values that are maintained persistently. When this
     * method is called, it may be assumed that the contents contains the current values of all input
     * variables and the last computed values of all output variables (which were based on previous values
     * of inputs).
     * @param oldValues contains descriptions of the variable number and old value of each input. Only indexes
     * of input variables should be specified. This list should not contain more than
     * {@link #updateDeterministicLimit(int)} elements.
     * @param changedOutputsHolder should be set by the function to contains the list of indexes of output variables
     * that were changed or else set to contain null if all of the outputs were modified.
     * @return true if update was done incrementally (i.e not all inputs were processed), false if full
     * update was done.
     * 
     * @throws IndexOutOfBoundsException if an index in {@code oldValues} does not refer to an input variable.
     */
    public boolean updateDeterministic(Value[] values, Collection<IndexedValue> oldValues,
    	AtomicReference<int[]> changedOutputsHolder)
    {
		Object[] tmp = Value.toObjects(values);
		evalDeterministic(tmp);
		Value.copyFromObjects(tmp, values);
		changedOutputsHolder.set(null);
		return false;
    }
    
    // REFACTOR: does anyone use this anymore. Should we remove?
	public boolean verifyValidForDirectionality(int [] directedTo, int [] directedFrom)
	{
		return true;
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
    	final FactorTable table = new FactorTable(domains);

    	final Object[] elements = new Object[domains.size()];

    	if (isDeterministicDirected() && domains.isDirected())
    	{
    		final int maxInput = domains.getInputCardinality();
    		final int[] outputs = new int[maxInput];

    		for (int inputIndex = 0; inputIndex < maxInput; ++inputIndex)
    		{
    			domains.inputIndexToElements(inputIndex, elements);
    			evalDeterministic(elements);
    			outputs[inputIndex] = domains.outputIndexFromElements(elements);
    		}

    		table.setDeterministicOutputIndices(outputs);
    	}
    	else
    	{
    		IntArrayList indexes = new IntArrayList();
    		DoubleArrayList energies = new DoubleArrayList();

    		final int maxJoint = domains.getCardinality();
    		for (int jointIndex = 0; jointIndex < maxJoint; ++ jointIndex)
    		{
    			domains.jointIndexToElements(jointIndex, elements);
    			double energy = evalEnergy(elements);
    			if (!Double.isInfinite(energy))
    			{
    				indexes.add(jointIndex);
    				energies.add(energy);
    			}
    		}

    		if (indexes.size() == maxJoint)
    		{
    			table.setEnergiesDense(Arrays.copyOf(energies.elements(), maxJoint));
    		}
    		else
    		{
    			table.setEnergiesSparse(Arrays.copyOf(indexes.elements(), indexes.size()),
    				Arrays.copyOf(energies.elements(), indexes.size()));
    		}
    	}

    	return table;
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
	public Object getConstantByIndex(int index)
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
	


 }
