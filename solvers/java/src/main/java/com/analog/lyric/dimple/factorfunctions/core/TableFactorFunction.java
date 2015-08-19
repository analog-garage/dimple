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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;

/**
 * A factor function based on a factor table.
 * <p>
 */
public class TableFactorFunction extends FactorFunction
{
	/*-------
	 * State
	 */
	
	private final IFactorTable _factorTable;
	
	private final int[] _argNumberToTableDimension;
	private final List<Value> _constants;
	
	/*
	 * Construction
	 */
	
	private TableFactorFunction(IFactorTable factorTable, int[] argNumberToTableDimension, List<Value> constants)
	{
		super();
		_factorTable = factorTable;
		_argNumberToTableDimension = argNumberToTableDimension;
		_constants = constants;
	}
	
	public TableFactorFunction(String name, IFactorTable factorTable)
	{
		super(name);
		_factorTable = factorTable;
		_argNumberToTableDimension = ArrayUtil.EMPTY_INT_ARRAY;
		_constants = Collections.emptyList();
	}
	
	public static TableFactorFunction forFactor(Factor factor, IFactorTable factorTable)
	{
		return new TableFactorFunction(factorTable,	factor.getFactorArgumentToSiblingNumberMapping(),
			factor.getConstantValues());
	}
	
	public TableFactorFunction(String name, int [][] indices, double [] probs, DiscreteDomain ... domains)
	{
		this(name,FactorTable.create(indices, probs, domains));
	}
	
	public TableFactorFunction(String name, int [][] indices, double [] probs, Discrete... discretes)
	{
		this(name,FactorTable.create(indices, probs, discretes));
	}
	
	/*------------------------
	 * FactorFunction methods
	 */
	
    @Override
	public boolean convertFactorTable(@Nullable JointDomainIndexer oldDomains, @Nullable JointDomainIndexer newDomains)
    {
    	boolean converted = false;
    	
    	if (oldDomains != null && newDomains != null)
    	{
    		_factorTable.setConditional(Objects.requireNonNull(newDomains.getOutputSet()));
    		converted = true;
    	}
    	
    	return converted;
    }

    @Override
	public boolean factorTableExists(@Nullable JointDomainIndexer domains)
	{
    	if (domains ==null || domains.size() != _factorTable.getDimensions())
    	{
    		return false;
    	}
    	    	
    	return true;
	}

	@Override
	public double evalEnergy(Value[] input)
	{
		final int[] argToTableDimension = _argNumberToTableDimension;
		
		if (argToTableDimension.length > 0)
		{
			final int nDims = _factorTable.getDimensions();
			Value[] tmp = new Value[nDims];
			for (int i = argToTableDimension.length; --i>=0;)
			{
				final Value value = input[i];
				int j = argToTableDimension[i];
				if (j < nDims)
				{
					tmp[j] = value;
				}
				else
				{
					final Value constant = _constants.get(j - nDims);
					if (!constant.valueEquals(value))
					{
						return Double.POSITIVE_INFINITY;
					}
				}
			}
			input = tmp;
		}
		
		return _factorTable.getEnergyForValues(input);
	}
	
	@Override
	public JointDomainIndexer getDomains()
	{
		return _factorTable.getDomainIndexer();
	}

	public IFactorTable getFactorTable()
	{
		return _factorTable;
	}
	
    @Override
	public IFactorTable getFactorTable(@Nullable JointDomainIndexer domains)
    {
    	if (domains == null)
    	{
    		// Invoke superclass version to throw error
    		return super.getFactorTable((JointDomainIndexer)null);
    	}
    	
    	//first step, convert domains to DiscreteDOmains
    	//make sure domain lists match
    	if (domains.size() != _factorTable.getDimensions())
    		throw new RuntimeException("domain lists don't match sizes.  argument size: " + domains.size() +
    			" factorTable's domain size: " + _factorTable.getDimensions());
    	    	
    	return _factorTable;
    }
    
    @Override
	public @Nullable IFactorTable getFactorTableIfExists(@Nullable JointDomainIndexer domains)
    {
    	return factorTableExists(domains) ? _factorTable : null;
    }
    
	// For directed factors...
	@Override
	public boolean isDirected()
	{
		return _factorTable.isDirected();
	}
	
	@Override
	protected @Nullable int[] getDirectedToIndices()
	{
		// FIXME Constant - convert to arg indices
		return _factorTable.getDomainIndexer().getOutputDomainIndices();
	}
	
	// For deterministic directed factors...
	// This means that for any given input, only one of its outputs has non-zero value (equivalently, finite energy)
	@Override
	public boolean isDeterministicDirected() {return _factorTable.isDeterministicDirected();}
	
	// For deterministic directed factors, evaluate the deterministic function output(s) given only the inputs
	// The arguments are in the same order as eval and evalEnergy, but in this case the output values should be overridden by new values
	@Override
	public void evalDeterministic(Value[] arguments)
	{
		final int[] argToTableDimension = _argNumberToTableDimension;
		
		if (argToTableDimension.length > 0)
		{
			final int nDims = _factorTable.getDimensions();
			final Value[] tmp = new Value[nDims];
			for (int i = argToTableDimension.length; --i>=0;)
			{
				final int j = argToTableDimension[i];
				if (j < nDims)
				{
					tmp[j] = arguments[i];
				}
			}
			arguments = tmp;
		}
		
		_factorTable.evalDeterministic(arguments);
	}
	
}
