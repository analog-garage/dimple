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

package com.analog.lyric.dimple.model.variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Equality;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.NodeId;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.solvers.core.SNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.Internal;



public abstract class VariableBase extends Node implements Cloneable
{
	protected Object _input = null;
	protected Object _fixedValue = null;
	protected String _modelerClassName;
	protected ISolverVariable _solverVariable = null;
	protected Map<String,Object> _properties = null;
	private Domain _domain;
    private boolean _hasFixedValue = false;

	
	public VariableBase(Domain domain)
	{
		this(NodeId.getNext(), "Variable", domain);
	}
	public VariableBase(Domain domain, String modelerClassName)
	{
		this(NodeId.getNext(), modelerClassName, domain);
	}
	
	protected VariableBase(int id, String modelerClassName, Domain domain)
	{
		super(id);
		
		_modelerClassName = modelerClassName;
		_domain = domain;
	}

	
	public final Discrete asDiscreteVariable()
	{
		return (Discrete)this;
	}
	
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

    /**
     * Returns the solver-specific variable instance associated with this model variable if any.
     */
	@Override
	public ISolverVariable getSolver()
	{
		return _solverVariable;
	}
	
    /**
     * Returns the solver-specific variable instance associated with this model variable if it is
     * an instance of the specified {@code solverVariableClass}, otherwise returns null.
     */
	public <T extends ISolverVariable> T getSolverIfType(Class<? extends T> solverVariableClass)
	{
		T svar = null;
		
		if (_solverVariable != null && solverVariableClass.isAssignableFrom(_solverVariable.getClass()))
		{
			svar = solverVariableClass.cast(_solverVariable);
		}
		
		return svar;
	}
	
    /**
     * Returns the solver-specific variable instance associated with this model variable if it is
     * an instance of the specified {@code solverVariableClass} and has {@link SNode#getParentGraph()} equal to
     * {@code solverGraph}, otherwise returns null.
     */
	public <T extends ISolverVariable> T getSolverIfTypeAndGraph(
		Class<? extends T> solverVariableClass,
		ISolverFactorGraph solverGraph)
	{
		T svar = getSolverIfType(solverVariableClass);
		if (svar != null && svar.getParentGraph() != solverGraph)
		{
			svar = null;
		}
		return svar;
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

	@Override
	public double getBetheEntropy()
	{
		if (_solverVariable == null)
			throw new DimpleException("solver needs to be set");
		
		return _solverVariable.getBetheEntropy();
	}
	
	@Override
	public double getInternalEnergy()
	{
		if (_solverVariable == null)
			throw new DimpleException("solver needs to be set");
		
		return _solverVariable.getInternalEnergy();
		
	}
	
	public void moveInputs(VariableBase other)
	{
		_input = other._input;
		_fixedValue = other._fixedValue;
		_hasFixedValue = other._hasFixedValue;
		_solverVariable.setInputOrFixedValue(_input,_fixedValue,_hasFixedValue);
	}

	
	public void createSolverObject(ISolverFactorGraph factorGraph)
	{
		if (factorGraph != null)
		{
			_solverVariable = factorGraph.createVariable(this);
			_solverVariable.createNonEdgeSpecificState();
			_solverVariable.setInputOrFixedValue(_input,_fixedValue,_hasFixedValue);
		}
		else
		{
			_solverVariable = null;
		}
	}
	
	public void setProperty(String key,Object value)
	{
		if (_properties == null)
		{
			_properties = new HashMap<String,Object>();
		}
		_properties.put(key, value);
	}
	public Object getProperty(String key)
	{
		return _properties == null ? null : _properties.get(key);
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

	
	
	public Object getFixedValueObject()
	{
		return _fixedValue;
	}

	public void setFixedValueObject(Object value)
	{
		setFixedValueObject(value,false);
	}
	
	private void inputOrFixedValueChanged()
	{
		if (_solverVariable != null)
    		_solverVariable.setInputOrFixedValue(_input,_fixedValue,_hasFixedValue);
		
	}
	
	protected void setFixedValueObject(Object value,boolean leaveInput)
	{
		_hasFixedValue = true;
		if (!leaveInput)
			_input = null;
		_fixedValue = value;
		inputOrFixedValueChanged();
	}

    public void setInputObject(Object value)
    {
    	_hasFixedValue = false;		// In case the value had previously been fixed, then un-fix it
    	_input = value;
    	_fixedValue = null;
    	inputOrFixedValueChanged();
    }
    
    
    // For setting the variable to a fixed value in lieu of an input
	public final boolean hasFixedValue()
	{
		return _hasFixedValue;
	}
	
	protected final void fixValue()
	{
		_hasFixedValue = true;
	}
	protected final void unfixValue()
	{
		_hasFixedValue = false;
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
    		return getInputObject();
    }
   
    
	@Override
	public void initialize(int portNum)
	{
		if (_solverVariable != null)
			_solverVariable.resetEdgeMessages(portNum);
	}
    
    @Override
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
		
		
		if (_solverVariable != null)
		{
			createSolverObject(getParentGraph().getSolver());
			
			for (Factor f : getFactors())
				f.createSolverObject(getParentGraph().getSolver());
		}
	}
	
	@Override
	public void update()
	{
		checkSolverNotNull();
		_solverVariable.update();
	}

	@Override
	public void updateEdge(int outPortNum)
	{
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
    	
    	fg.addFactor(new Equality(), this,mycopy);
    	
    	//for each factor to be moved
    	for (int i = 0; i < factorsToBeMovedToCopy.length; i++)
    	{
    		//Replace the connection from this variable to the copy in the factor
    		for (int j = 0; j < factorsToBeMovedToCopy[i].getSiblings().size(); j++)
    		{
    			if (factorsToBeMovedToCopy[i].getConnectedNodeFlat(j) == this)
    			{
    				remove(factorsToBeMovedToCopy[i]);
    				mycopy.connect(factorsToBeMovedToCopy[i]);
    				factorsToBeMovedToCopy[i].replace(this,mycopy);
    			}
    		}
    		
    	}
    	
    	//set the solvers to null for this variable, the copied variable, and all the factors that were moved.
    	ISolverFactorGraph sfg = fg.getSolver();
    		
    	if (sfg != null)
    	{
			createSolverObject(fg.getSolver());
			mycopy.createSolverObject(fg.getSolver());
	    	
	    	for (int i = 0; i < factorsToBeMovedToCopy.length; i++)
	    		factorsToBeMovedToCopy[i].createSolverObject(fg.getSolver());
    	}
    	return mycopy;
    }
    
    
    /**
     * Creates a new variable that combines the domains of this variable with additional {@code variables}.
     * <p>
     * For use by {@link FactorGraph#join(VariableBase...)}. Currently only supported for {@link Discrete}
     * variables.
     * <p>
     * @param variables specifies at least one additional variables to join with this one. As a convenience, this
     * may begin with this variable, in which case there must be at least one other variable.
     */
    @Internal
    public VariableBase createJointNoFactors(VariableBase ... variables)
    {
    	throw new DimpleException("not implemented");
    }
    
}
