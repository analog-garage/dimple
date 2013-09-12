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

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.JointDomainIndexer;


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
	public boolean convertFactorTable(JointDomainIndexer oldDomains, JointDomainIndexer newDomains)
    {
    	boolean converted = false;
    	
    	if (oldDomains != null && newDomains != null)
    	{
    		if (_factorTable != null)
    		{
    			_factorTable.setConditional(newDomains.getOutputSet());
				converted = true;
    		}
    	}
    	
    	return converted;
    }

    @Override
	public boolean factorTableExists(JointDomainIndexer domains)
	{
    	if (domains.size() != _factorTable.getDimensions())
    	{
    		return false;
    	}
    	// FIXME:
//    		throw new RuntimeException("domain lists don't match sizes.  argument size: " + domains.size() +
//    			" factorTable's domain size: " + _factorTable.getDomains().length);
    	    	
    	return true;
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
	public IFactorTable getFactorTable(JointDomainIndexer domains)
    {
    	//first step, convert domains to DiscreteDOmains
    	//make sure domain lists match
    	if (domains.size() != _factorTable.getDimensions())
    		throw new RuntimeException("domain lists don't match sizes.  argument size: " + domains.size() +
    			" factorTable's domain size: " + _factorTable.getDimensions());
    	    	
    	return _factorTable;
    }
    
    @Override
	public IFactorTable getFactorTableIfExists(JointDomainIndexer domains)
    {
    	return factorTableExists(domains) ? _factorTable : null;
    }
    
	// For directed factors...
	@Override
	public boolean isDirected() {return _factorTable.isDirected();}
	@Override
	protected int[] getDirectedToIndices() {return _factorTable.getDomainIndexer().getOutputDomainIndices();}
	
	// For deterministic directed factors...
	// This means that for any given input, only one of its outputs has non-zero value (equivalently, finite energy)
	@Override
	public boolean isDeterministicDirected() {return _factorTable.isDeterministicDirected();}
	
	// For deterministic directed factors, evaluate the deterministic function output(s) given only the inputs
	// The arguments are in the same order as eval and evalEnergy, but in this case the output values should be overridden by new values
	@Override
	public void evalDeterministicFunction(Object[] arguments){_factorTable.evalDeterministic(arguments);}
	
	@Override
	public boolean verifyValidForDirectionality(int [] directedTo, int [] directedFrom)
	{
		return true;
	}

}
