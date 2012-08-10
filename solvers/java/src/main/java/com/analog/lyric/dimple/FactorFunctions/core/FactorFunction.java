package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.ArrayList;
import java.util.HashMap;

import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Domain;

public abstract class FactorFunction extends FactorFunctionBase
{	
	private FactorTableFactory _factory;
    
    public FactorFunction(String name) 
    {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public abstract double eval(Object ... input);

    public FactorTable getFactorTable(Domain [] domainList)
    {
    	//first step, convert domains to DiscreteDOmains
    	
    	DiscreteDomain [] dds = new DiscreteDomain[domainList.length];
    	
    	for (int i = 0; i < domainList.length; i++)
    	{
    		if (!( domainList[i] instanceof DiscreteDomain))
    			throw new DimpleException("only support getFactorTable for discrete domains");
    		
    		dds[i] = (DiscreteDomain)domainList[i];
    	}
    	
    	if (_factory == null)
    		_factory = new FactorTableFactory();
    	
    	return (FactorTable)_factory.getTable(this, dds)[0];
    }
       	
	protected class FactorTableFactory
	{
		protected HashMap<String, FunctionEntry> _name2FunctionEntry = new HashMap<String, FunctionEntry>();

		public FactorTableFactory(){}

		
		protected class FunctionEntry
		{
	        FactorFunction _factorFunction;
	        ArrayList<FactorTable> _tables = new ArrayList<FactorTable>();
	        
	        public FunctionEntry(FactorFunction factorFunction)
	        {
	        	_factorFunction = factorFunction;
	        }
	        
	        public Object [] getCombinationTable(DiscreteDomain [] domains)
	        {
	        	FactorTable table = null;
    			boolean matched = false;
    			
	        	//Check all tables
    			for(int i = 0; i < _tables.size() && !matched; ++i)
	        	{
    				FactorTable currTable = _tables.get(i);
	        		
	        		//Check this table
	        		if(domains.length == currTable.getDomains().length)
	        		{
	        			
	        			//Check variable by variable	
		        		matched = true;
	        			for(int variableIdx = 0; 
	        				variableIdx < domains.length && matched; 
		        			++variableIdx)
		        		{		        			
		        			Object[] tableDomain = currTable.getDomains()[variableIdx].getElements();
		        			Object[] variableDomain = domains[variableIdx].getElements();
		        			matched = tableDomain.length == variableDomain.length;
		        			if(matched)
		        			{
		        				//Check domain member by domain member
		        				for(int memberIdx = 0; 
		        					memberIdx < tableDomain.length && matched; 
		        					++memberIdx)
		        				{
		        					Object tableDomainMember = tableDomain[memberIdx];
		        					Object variableDomainMember = variableDomain[memberIdx];
		        					matched = tableDomainMember.equals(variableDomainMember);
			        			}//end domain member x member check
		        			}//end domain length check
		        		}//end all variables check
	        		}//end variables length check
	        		if(matched)
	        		{
	        			table = currTable;
	        		}
	        	}//end all table entry check 
    			
    			if(!matched)
    			{
    				table = createCombinationTable(domains);
    			}
	        	return new Object[]{table,!matched};
	        }

	        //private Method getMethodFromFactorFunctionAndVariables()
	        
        	protected FactorTable createCombinationTable(DiscreteDomain [] domain)
	        {
        		
	        	//Variables for computation
	        	//FactorTable table = new FactorTable();
	        	//table.id = -1;
	        	//table.variableDomains = new Object[domain.length][];

	        	//for(int i = 0; i < domain.length; ++i)
	        	//{
	        	//	table.variableDomains[i] = domain[i].getElements().clone();
	        	//}
	        	
	        	ArrayList<int[]> indices = new ArrayList<int[]>();
	        	ArrayList<Double> values = new ArrayList<Double>();
	        	
	        	//initialize indices to all zeros
	        	int[] currIndicesToInput = new int[domain.length];
	        	Object [] currInput = new Object[domain.length];	        	
	        	int[] domainLengths = new int[domain.length];
	        	
	        	for (int i = 0; i < domainLengths.length; i++)
	        		domainLengths[i] = domain[i].getElements().length;
	        	
	        	double currValue;
	        	
	        	while (true)
	        	{
	        		//get values for indices
	        		for (int i = 0; i < currIndicesToInput.length; i++)
	        			currInput[i] = domain[i].getElements()[currIndicesToInput[i]];
	        		
	        		//eval func for indices
	        		currValue = _factorFunction.eval(currInput);
	        		
	        		//if non zero, add to table
	        		 if (currValue != 0)
	        		 {
	        			 indices.add(currIndicesToInput.clone());
	        			 values.add(currValue);
	        		 }
	        		 
	        		//increment indices
        			 int carry = 1;
        			 
        			 for (int i = 0; i < currIndicesToInput.length; i++)
        			 {
        				 int newIndex = currIndicesToInput[i]+carry;
        				 
        				 if (newIndex >= domainLengths[i])
        				 {
        					 currIndicesToInput[i] = 0;
        					 carry = 1;
        					 
        				 }
        				 else
        				 {
        					 currIndicesToInput[i] = newIndex;
        					 carry = 0;
        				 }
        				
        			 }
	        		
        			 if (carry == 1)
        				 break;
	        	}


	        	//FactorTable table = 
	        	int [][] rIndices = new int [indices.size()][];
	        	double [] rValues  = new double[indices.size()];
	        	        	
	        	for (int i = 0; i < indices.size(); i++)
	        	{
	        		rIndices[i] = indices.get(i).clone();
	        		rValues[i] = values.get(i);
	        	}
	        	FactorTable table = new FactorTable(rIndices,rValues,false,domain); 
	        	_tables.add(table);
	        	
	    		return table;
	        }
			
		}
		
		public Object[] getTable(FactorFunction factorFunction, DiscreteDomain [] domain) 
		{
			FunctionEntry fe = _name2FunctionEntry.get(factorFunction.getName());
			
			if(fe == null)
			{
				fe = new FunctionEntry(factorFunction);
				_name2FunctionEntry.put(factorFunction.getName(), fe);
			}
			
			Object [] tmp = fe.getCombinationTable(domain);
			FactorTable table = (FactorTable)tmp[0];
			Boolean isNewTable = (Boolean)tmp[1];
			Object[] ret = new Object[2];
			ret[0] = table;
			ret[1] = isNewTable;
			return ret;
		}
	}
	
	
 }
