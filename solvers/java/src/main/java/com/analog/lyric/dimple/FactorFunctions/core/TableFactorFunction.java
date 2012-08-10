package com.analog.lyric.dimple.FactorFunctions.core;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Domain;


public class TableFactorFunction extends FactorFunction 
{
	
	//private HashMap<ArrayList<Object>,Double> _lookupTable = null;
	private FactorTable _factorTable;
	//private DiscreteDomain [] _domains;

	public TableFactorFunction(String name,FactorTable factorTable) 
	{		
		super(name);
		
		_factorTable = factorTable;
		//_domains = domains;
		
	}
	
	public TableFactorFunction(String name, int [][] indices, double [] probs, DiscreteDomain ... domains) 
	{
		this(name,new FactorTable(indices,probs,domains));
	}
	public TableFactorFunction(String name, int [][] indices, double [] probs, Discrete... discretes) 
	{
		this(name,new FactorTable(indices,probs,discretes));
	}
	

	@Override
	public double eval(Object... input) 
	{
		return _factorTable.evalAsFactorFunction(input);
	}
	

	public FactorTable getFactorTable()
	{
		return _factorTable;
	}
	
    public FactorTable getFactorTable(Domain [] domainList)
    {
    	//first step, convert domains to DiscreteDOmains
    	//make sure domain lists match
    	if (domainList.length != _factorTable.getDomains().length)
    		throw new RuntimeException("domain lists don't match sizes.  argument size: " + domainList.length + " factorTable's domain size: " + _factorTable.getDomains().length);
    	    	
    	return _factorTable;
    }

}
