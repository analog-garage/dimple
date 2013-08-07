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
import com.analog.lyric.dimple.model.Domain;


public class TableFactorFunction extends FactorFunction
{
	
	private IFactorTable _factorTable;

	@Override
	public boolean factorTableExists(Domain [] domainList)
	{
    	if (domainList.length != _factorTable.getDomains().length)
    		throw new RuntimeException("domain lists don't match sizes.  argument size: " + domainList.length + " factorTable's domain size: " + _factorTable.getDomains().length);
    	    	
    	return true;
	}

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
	public double eval(Object... input)
	{
		return _factorTable.evalAsFactorFunction(input);
	}
	

	public IFactorTable getFactorTable()
	{
		return _factorTable;
	}
	
    @Override
	public IFactorTable getFactorTable(Domain [] domainList)
    {
    	//first step, convert domains to DiscreteDOmains
    	//make sure domain lists match
    	if (domainList.length != _factorTable.getDomains().length)
    		throw new RuntimeException("domain lists don't match sizes.  argument size: " + domainList.length + " factorTable's domain size: " + _factorTable.getDomains().length);
    	    	
    	return _factorTable;
    }
    
    
	// For directed factors...
	@Override
	public boolean isDirected() {return _factorTable.isDirected();}
	@Override
	protected int[] getDirectedToIndices() {return _factorTable.getDirectedTo();}
	
	// For deterministic directed factors...
	// This means that for any given input, only one of its outputs has non-zero value (equivalently, finite energy)
	@Override
	public boolean isDeterministicDirected() {return _factorTable.isDeterministicDirected();}
	
	// For deterministic directed factors, evaluate the deterministic function output(s) given only the inputs
	// The arguments are in the same order as eval and evalEnergy, but in this case the output values should be overridden by new values
	@Override
	public void evalDeterministicFunction(Object... arguments){_factorTable.evalDeterministicFunction(arguments);}
	
	@Override
	public boolean verifyValidForDirectionality(int [] directedTo, int [] directedFrom)
	{
		return true;
	}

}
