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
import java.util.HashMap;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Domain;

public abstract class FactorFunction extends FactorFunctionBase
{
	private FactorTableFactory _factory;
    
	public FactorFunction()
	{
		super();
	}
    public FactorFunction(String name)
    {
		super(name);
	}

	public boolean factorTableExists(Domain [] domainList)
	{
    	//first step, convert domains to DiscreteDOmains
		if (_factory == null)
			return false;
		
    	DiscreteDomain [] dds = new DiscreteDomain[domainList.length];
    	
    	for (int i = 0; i < domainList.length; i++)
    	{
    		if (!( domainList[i] instanceof DiscreteDomain))
    			return false;
    		
    		dds[i] = (DiscreteDomain)domainList[i];
    	}
    	return _factory.tableExists(this, dds);
	}
	
    @Override
	public IFactorTable getFactorTable(Domain [] domainList)
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
    	
    	return (IFactorTable)_factory.getTable(this, dds)[0];
    }
       	
	protected class FactorTableFactory
	{
		protected HashMap<String, FunctionEntry> _name2FunctionEntry = new HashMap<String, FunctionEntry>();

		public FactorTableFactory(){}

		
		protected class FunctionEntry
		{
	        FactorFunction _factorFunction;
	        ArrayList<IFactorTable> _tables = new ArrayList<IFactorTable>();
	        
	        public FunctionEntry(FactorFunction factorFunction)
	        {
	        	_factorFunction = factorFunction;
	        }
	        
	        public Object [] getCombinationTable(DiscreteDomain [] domains)
	        {
	        	return getCombinationTable(domains,true);
	        }
	        
	        public Object [] getCombinationTable(DiscreteDomain [] domains,boolean create)
	        {
	        	IFactorTable table = null;
    			boolean matched = false;
    			
	        	//Check all tables
    			for(int i = 0; i < _tables.size() && !matched; ++i)
	        	{
    				IFactorTable currTable = _tables.get(i);
	        		
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
    				if (!create)
    					return null;
    				
    				table = createCombinationTable(domains);
    			}
	        	return new Object[]{table,!matched};
	        }

	        //private Method getMethodFromFactorFunctionAndVariables()
	        
        	protected IFactorTable createCombinationTable(DiscreteDomain [] domain)
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
	        		domainLengths[i] = domain[i].size();
	        	
	        	double currValue;
	        	
	        	while (true)
	        	{
	        		//get values for indices
	        		for (int i = 0; i < currIndicesToInput.length; i++)
	        			currInput[i] = domain[i].getElement(currIndicesToInput[i]);
	        		
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
	        	IFactorTable table = FactorTable.create(rIndices, rValues, false, domain);
	        	_tables.add(table);
	        	
	    		return table;
	        }
			
		}
		
		public boolean tableExists(FactorFunction factorFunction, DiscreteDomain [] domain)
		{
			FunctionEntry fe = _name2FunctionEntry.get(factorFunction.getName());
			if(fe == null)
			{
				return false;
			}
			if (fe.getCombinationTable(domain,false)==null)
				return false;
			else
				return true;
				
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
			IFactorTable table = (IFactorTable)tmp[0];
			Boolean isNewTable = (Boolean)tmp[1];
			Object[] ret = new Object[2];
			ret[0] = table;
			ret[1] = isNewTable;
			return ret;
		}
	}
	
	
 }
