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
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.DomainList;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;

@ThreadSafe
public abstract class FactorFunction extends FactorFunctionBase
{
	/*-------
	 * State
	 */
	
	// Cache of factor tables for this function by domain.
	private AtomicReference<ConcurrentMap<JointDomainIndexer, IFactorTable>> _factorTables =
		new AtomicReference<ConcurrentMap<JointDomainIndexer, IFactorTable>>();
	
	/*--------------
	 * Construction
	 */
	
	protected FactorFunction()
	{
		this(null);
	}
	
    protected FactorFunction(String name)
    {
		super(name);
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
    
	public boolean factorTableExists(Factor factor)
	{
		return factorTableExists(factor.getDomainList().asJointDomainIndexer());
	}
	
    @Override
	public final IFactorTable getFactorTable(Domain [] domains)
    {
    	return getFactorTable(DomainList.create(domains).asJointDomainIndexer());
    }
    
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
    
    public IFactorTable getFactorTable(Factor factor)
    {
    	return getFactorTable(factor.getDomainList().asJointDomainIndexer());
    }

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
    
    public IFactorTable getFactorTableIfExists(Factor factor)
    {
    	return getFactorTableIfExists(factor.getDomainList().asJointDomainIndexer());
    }
    
    /**
     * The maximum number of variable updates beyond which {@link #updateDeterministic}
     * should not be called.
     * <p>
     * Default implementation returns 0, indicating that {@link #updateDeterministic} should
     * never be called.
     */
    public int updateDeterministicLimit()
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
     * The default implementation delegates back to {@link #evalDeterministicFunction(Object[])}, which
     * will do a full update.
     * <p>
     * @param values is the array of output and input values that are maintained persistently. When this
     * method is called, it may be assumed that the contents contains the current values of all input
     * variables and the last computed values of all output variables (which were based on previous values
     * of inputs).
     * @param oldValues contains descriptions of the variable number and old value of each input. Only indexes
     * of input variables should be specified. This list should not contain more than
     * {@link #updateDeterministicLimit()} elements.
     * 
     * @return true if update was done incrementally (i.e not all inputs were processed), false if full
     * update was done.
     * 
     * @throws IndexOutOfBoundsException if an index in {@code oldValues} does not refer to an input variable.
     */
    public boolean updateDeterministic(Value[] values, Collection<IndexedValue> oldValues)
    {
		Object[] tmp = Value.toObjects(values);
		evalDeterministicFunction(tmp);
		Value.copyFromObjects(tmp, values);
		return false;
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
    			evalDeterministicFunction(elements);
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

 }
