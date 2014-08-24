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

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;


public class TableFactorFunction extends FactorFunction
{
	
	private IFactorTable _factorTable;

	public TableFactorFunction(String name,IFactorTable factorTable)
	{
		super(name);
		_factorTable = factorTable;
	}
	
	public TableFactorFunction(String name, int [][] indices, double [] probs, DiscreteDomain ... domains)
	{
		this(name,FactorTable.create(indices, probs, domains));
	}
	public TableFactorFunction(String name, int [][] indices, double [] probs, Discrete... discretes)
	{
		this(name,FactorTable.create(indices, probs, discretes));
	}
	

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
		return _factorTable.getEnergyForValues(input);
	}

	@Override
	public double evalEnergy(Object... input)
	{
		return _factorTable.getEnergyForElements(input);
	}
	
	@Override
	public double eval(Value[] input)
	{
		return _factorTable.getWeightForValues(input);
	}
	
	@Override
	public double eval(Object... input)
	{
		return _factorTable.getWeightForElements(input);
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
	public boolean isDirected() {return _factorTable.isDirected();}
	@Override
	protected @Nullable int[] getDirectedToIndices() {return _factorTable.getDomainIndexer().getOutputDomainIndices();}
	
	// For deterministic directed factors...
	// This means that for any given input, only one of its outputs has non-zero value (equivalently, finite energy)
	@Override
	public boolean isDeterministicDirected() {return _factorTable.isDeterministicDirected();}
	
	// For deterministic directed factors, evaluate the deterministic function output(s) given only the inputs
	// The arguments are in the same order as eval and evalEnergy, but in this case the output values should be overridden by new values
	@Override
	public void evalDeterministic(Value[] arguments){_factorTable.evalDeterministic(arguments);}
	
}
