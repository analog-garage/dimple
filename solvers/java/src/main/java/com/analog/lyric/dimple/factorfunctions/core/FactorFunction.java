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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.ThreadSafe;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteDomainList;
import com.analog.lyric.dimple.model.DiscreteFactor;
import com.analog.lyric.dimple.model.Domain;
import com.analog.lyric.dimple.model.Factor;

@ThreadSafe
public abstract class FactorFunction extends FactorFunctionBase
{
	/*-------
	 * State
	 */
	
	// Cache of factor tables for this function by domain.
	private AtomicReference<ConcurrentMap<DiscreteDomainList, IFactorTable>> _factorTables =
		new AtomicReference<ConcurrentMap<DiscreteDomainList, IFactorTable>>();
	
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
    
    public boolean factorTableExists(DiscreteDomainList domains)
    {
    	ConcurrentMap<DiscreteDomainList, IFactorTable> factorTables = _factorTables.get();
    	return factorTables != null && factorTables.containsKey(domains);
    }
    
	public boolean factorTableExists(Factor factor)
	{
    	Domain[] domains = factor.getDomains();
    	DiscreteDomain[] discreteDomains = new DiscreteDomain[domains.length];
    	for (int i = domains.length; --i >= 0;)
    	{
    		if ((discreteDomains[i] = domains[i].asDiscrete()) == null)
    		{
    			return false;
    		}
    	}

    	BitSet inputSet = getInputSetForFactor(factor);
    	return factorTableExists(DiscreteDomainList.create(inputSet, discreteDomains));
	}
	
    @Override
	public final IFactorTable getFactorTable(Domain [] domainList)
    {
    	//first step, convert domains to DiscreteDOmains
    	
    	DiscreteDomain [] dds = new DiscreteDomain[domainList.length];
    	
    	for (int i = 0; i < domainList.length; i++)
    	{
    		if (!( domainList[i] instanceof DiscreteDomain))
    			throw new DimpleException("only support getFactorTable for discrete domains");
    		
    		dds[i] = (DiscreteDomain)domainList[i];
    	}
    	
    	return getFactorTable(DiscreteDomainList.create(dds));
    }
    
    public IFactorTable getFactorTable(DiscreteDomainList domains)
    {
    	ConcurrentMap<DiscreteDomainList, IFactorTable> factorTables = _factorTables.get();
    	if (factorTables == null)
    	{
    		_factorTables.compareAndSet(null, new ConcurrentHashMap<DiscreteDomainList, IFactorTable>());
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
    
    private static BitSet getInputSetForFactor(Factor factor)
    {
    	BitSet inputSet = null;
    	
    	if (factor.isDirected())
    	{
    		int[] directedTo = factor.getDirectedTo();
    		if (directedTo != null && directedTo.length > 0)
    		{
    			inputSet = new BitSet(directedTo.length);
    			for (int from : factor.getDirectedFrom())
    			{
    				inputSet.set(from);
    			}
    		}
    	}
    	
    	return inputSet;
    }
    
    public IFactorTable getFactorTable(DiscreteFactor factor)
    {
    	BitSet inputSet = getInputSetForFactor(factor);
    	return getFactorTable(DiscreteDomainList.create(inputSet, factor.getDomains()));
    }
    
    public IFactorTable getFactorTable(Factor factor)
    {
    	Domain[] domains = factor.getDomains();
    	DiscreteDomain[] discreteDomains = new DiscreteDomain[domains.length];
    	for (int i = domains.length; --i >= 0;)
    	{
    		if ((discreteDomains[i] = domains[i].asDiscrete()) == null)
    		{
    			throw new DimpleException("only support getFactorTable for discrete domains");
    		}
    	}
    	
    	BitSet inputSet = getInputSetForFactor(factor);
    	return getFactorTable(DiscreteDomainList.create(inputSet, discreteDomains));
    }

    /*-------------------
     * Protected methods
     */
    
    /**
     * Generate a factor table for this function over the given domains.
     * <p>
     * Invoked implicitly by {@link #getFactorTable(DiscreteDomainList)} the first time
     * a factor table is needed for specified domains.
     */
    protected IFactorTable createTableForDomains(DiscreteDomainList domains)
    {
    	if (FactorTable.useNewFactorTable)
    	{
    		final NewFactorTable table = new NewFactorTable(domains);
    		
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
        		
        		table.setDeterministicOuputIndices(outputs);
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
        			table.setDenseEnergies(Arrays.copyOf(energies.elements(), maxJoint));
        		}
        		else
        		{
        			table.setSparseEnergies(Arrays.copyOf(indexes.elements(), indexes.size()),
        				Arrays.copyOf(energies.elements(), indexes.size()));
        		}
        	}
        	
        	return table;
    	}
    	else
    	{
    		IFactorTable table = FactorTable.create(domains);
    		ArrayList<int[]> indices = new ArrayList<int[]>();
    		DoubleArrayList weights = new DoubleArrayList();
        	final Object[] elements = new Object[domains.size()];
    		
    		final int maxJoint = domains.getCardinality();
    		for (int jointIndex = 0; jointIndex < maxJoint; ++ jointIndex)
    		{
    			domains.jointIndexToElements(jointIndex, elements);
    			double weight = eval(elements);
    			if (weight != 0)
    			{
    				indices.add(domains.jointIndexToIndices(jointIndex, null));
    				weights.add(weight);
    			}
    		}
    		
    		table.change(indices.toArray(new int[indices.size()][]), Arrays.copyOf(weights.elements(), weights.size()));
    		
    		return table;
    	}
    }
 }
