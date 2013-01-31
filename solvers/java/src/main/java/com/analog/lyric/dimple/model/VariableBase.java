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

package com.analog.lyric.dimple.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.analog.lyric.dimple.FactorFunctions.EqualDelta;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;



public abstract class VariableBase extends Node implements Cloneable
{
	protected Object _input = null;
	protected String _modelerClassName;
	protected ISolverVariable _solverVariable = null;
	protected HashMap<String,Object> _properties = new HashMap<String,Object>();
	private Domain _domain;
	
	public VariableBase(Domain domain) 
	{
		this(NodeId.getNext(),"Variable",domain);
	}
	public VariableBase(Domain domain,String modelerClassName) 
	{
		this(NodeId.getNext(),modelerClassName,domain);
	}
	
	public VariableBase(int id, String modelerClassName, Domain domain) 
	{
		//this(id,modelerClassName,new Domain[]{domain});
		super(id);
		
		
		_modelerClassName = modelerClassName;
		_domain = domain;
	}

//	public VariableBase(int id, String modelerClassName, Domain [] domains) 
//	{
//		super(id);
//
//		if (domains.length < 1)
//			throw new DimpleException("expect one or more domains");
//
//		_modelerClassName = modelerClassName;
//		_domain = domains;
//	}

	
	
	@Override
	public final VariableBase asVariable()
	{
		return this;
	}
	
	@Override
	public final boolean isVariable()
	{
		return true;
	}
	
	public Domain getDomain()
	{
		return _domain;
	}
	
	
	// Abstract methods
    public Object getInputObject()
    {
    	return _input;
    }

    
	@Override
	public ISolverVariable getSolver()
	{
		return _solverVariable;
	}
	
    @Override
	public String getClassLabel()
    {
    	return "Variable";
    }
    
    public void setGuess(Object guess) 
    {
    	_solverVariable.setGuess(guess);
    }

    public Object getGuess() 
    {
    	return _solverVariable.getGuess();
    }
    
	@Override
	public double getScore() 
	{
		if (_solverVariable == null)
			throw new DimpleException("solver needs to be set before calculating energy");
		
		return _solverVariable.getScore();
	}

	public double getBetheEntropy()
	{
		if (_solverVariable == null)
			throw new DimpleException("solver needs to be set");
		
		return _solverVariable.getBetheEntropy();
	}
	
	public double getInternalEnergy()
	{
		if (_solverVariable == null)
			throw new DimpleException("solver needs to be set");
		
		return _solverVariable.getInternalEnergy();
		
	}
	
	public void createSolverObject(ISolverFactorGraph factorGraph)
	{
		if (factorGraph != null)
		{		
			_solverVariable = factorGraph.createVariable(this);
			if (_input!=null)
				_solverVariable.setInput(_input);
		}
		else
		{
			_solverVariable = null;
		}
	}
	
	public void setProperty(String key,Object value)
	{
		_properties.put(key, value);
	}
	public Object getProperty(String key)
	{
		return _properties.get(key);
	}

	@Override
	public VariableBase clone()
	{
		/*******
		 * NOTE: Any derived class that defines instance variables that are
		 * objects (rather than primitive types) must implement clone(), which
		 * must first call super.clone(), and then deep-copy those instance
		 * variables to the clone.
		 *******/
		VariableBase v = (VariableBase)(super.clone());		
		v._solverVariable = null;
		return v;
	}
		

	/*
	public void setInput(double ... value) 
	{
		setInputObject((Object)value);
	}
	*/
	
    public void setInputObject(Object value) 
    {
    	_input = value;
    	if (_solverVariable != null)
    		_solverVariable.setInput(value);
    }
    
    
    public String getModelerClassName()
    {
    	return _modelerClassName;
    }
        
    public Object getBeliefObject() 
    {
    	if (_solverVariable != null)
    		return _solverVariable.getBelief();
    	else
    		return _input;
    }
   
    
    public void initialize() 
    {

    	if (_solverVariable != null)
    		_solverVariable.initialize();
    }
    
    public Factor [] getFactors()
    {
    	return getFactorsFlat();
    }
    
	public FactorBase [] getFactors(int relativeNestingDepth)
	{
		FactorBase [] retval = new FactorBase[_siblings.size()];
		
		for (int i = 0; i < _siblings.size(); i++)
		{
			retval[i] = (FactorBase)getConnectedNode(relativeNestingDepth,i);
		}
		return retval;
	}

	public FactorBase [] getFactorsTop()
	{
		return getFactors(0);
	}
	
	public Factor [] getFactorsFlat()
	{
		Factor [] retval = new Factor[_siblings.size()];
		for (int i = 0; i < _siblings.size(); i++)
		{
			retval[i] = (Factor)getConnectedNodeFlat(i);
		}
		return retval;
		
	}
	
	public void remove(Factor factor) 
	{
		ArrayList<INode> siblings = getSiblings();
		
		boolean found=false;
		
		for (int i = 0; i < siblings.size(); i++)
		{
			if (getConnectedNodeFlat(i) == factor)
			{
				found = true;
				siblings.remove(i);
				break;
			}
		}
		
		if (!found)
			throw new DimpleException("Tried to delete factor from variable that does not reference that factor");
		
//		for (int i = 0; i < getSiblings().size(); i++)
//			getPorts().get(i).setId(i);
		
		if (_solverVariable != null)
			_solverVariable.remove(factor);
	}
	
	@Override
	public void update() 
	{
		checkSolverNotNull();
		_solverVariable.update();
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateEdge(int outPortNum) 
	{
		// TODO Auto-generated method stub
		checkSolverNotNull();
		_solverVariable.updateEdge(outPortNum);
	}
	
	private void checkSolverNotNull() 
	{
		if (_solverVariable == null)
			throw new DimpleException("solver must be set before performing this action.");
	}
	
	
    
    
    public VariableBase split(FactorGraph fg,Factor [] factorsToBeMovedToCopy) 
    {
    	//create a copy of this variable
    	VariableBase mycopy = clone();
    	mycopy.createSolverObject(null);
    	mycopy.setInputObject(null);
    	mycopy.setName(null);
    	
    	fg.addFactor(new EqualDelta(), this,mycopy);
    	
    	//for each factor to be moved
    	for (int i = 0; i < factorsToBeMovedToCopy.length; i++)
    	{
    		//Replace the connection from this variable to the copy in the factor
    		//ArrayList<INode> ports = factorsToBeMovedToCopy[i].getSiblings();
    		for (int j = 0; j < factorsToBeMovedToCopy[i].getSiblings().size(); j++)
    		{
    			if (factorsToBeMovedToCopy[i].getConnectedNodeFlat(j) == this)
    			{
    				getSiblings().remove(factorsToBeMovedToCopy[i].getSiblings().get(j));
    				mycopy.connect((Factor)factorsToBeMovedToCopy[i].getSiblings().get(j));
    			}
    		}
    		
    		if (factorsToBeMovedToCopy[i].getSolver() != null)
    			factorsToBeMovedToCopy[i].createSolverObject(fg.getSolver());
    	}
    	
    	//set the solvers to null for this variable, the copied variable, and all the factors that were moved.
    	if (getSolver() != null)
    		createSolverObject(fg.getSolver());
    	if (mycopy.getSolver() != null)
    		mycopy.createSolverObject(fg.getSolver());
    	
    	//attach(null);
    	
    	return mycopy;
    }
    
    
    VariableBase createJointNoFactors(VariableBase otherVariable) 
    {
    	throw new DimpleException("not implemented");
    }
    

//    public boolean isJoint()
//    {
//    	return _domains.length > 1;
//    }

}
