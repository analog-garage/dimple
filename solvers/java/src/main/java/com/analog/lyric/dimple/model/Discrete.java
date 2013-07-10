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

import java.util.Arrays;

import com.analog.lyric.dimple.solvers.core.SDiscreteVariableBase;

public class Discrete extends VariableBase
{
	/*--------------
	 * Construction
	 */
	public Discrete(int id,DiscreteDomain domain, String modelerClassName)
	{
		super(id, modelerClassName, domain);
	}
	
	public Discrete(DiscreteDomain domain)
	{
		this(NodeId.getNext(),domain,"Discrete");
	}

	public Discrete(Object ... domain)
	{
		this(NodeId.getNext(),DiscreteDomain.fromElements(domain),"Discrete");

		if(domain.length < 1)
			throw new DimpleException(String.format("ERROR Variable domain length %d must be at least 2", domain.length));

	}

    public double [] getInput()
    {
    	return (double[])getInputObject();
    }
    
    /*----------------------
     * VariableBase methods
     */
    
    @Override
	public DiscreteDomain getDomain()
    {
    	return (DiscreteDomain)super.getDomain();
    }
    
    public DiscreteDomain getDiscreteDomain()
    {
    	return getDomain();
    }
    
    @Override
    public Integer getFixedValueObject()
    {
    	return (Integer)super.getFixedValueObject();
    }
    
    @Override
	protected void setFixedValueObject(Object value, boolean leaveInput)
	{
    	super.setFixedValueObject(value, leaveInput);
	}

    @Override
    public Object getInputObject()
    {
    	Object tmp = super.getInputObject();
    	
    	if (tmp == null)
    		return getDefaultPriors(getDiscreteDomain().getElements());
    	else
    		return tmp;
    }
    
    public double [] getBelief()
    {
    	return (double[])getBeliefObject();
    }
    
    @Override
	public Object getBeliefObject()
    {
    	if (_solverVariable != null)
    		return _solverVariable.getBelief();
    	else
    		return getInputObject();
    }
    
    public Object getValue()
    {
    	if (_solverVariable != null)
    		return _solverVariable.getValue();
    	else
    		return null;
    }
   
    public int getValueIndex()
    {
    	if (_solverVariable != null)
    		return ((SDiscreteVariableBase)_solverVariable).getValueIndex();
    	else
    		return 0;
    }

    
	private double [] getDefaultPriors(Object [] domain)
	{
		double [] retval = new double[domain.length];
		double val = 1.0/domain.length;
		for (int i = 0; i < retval.length; i++)
			retval[i] = val;
		return retval;
	}
	
	
    @Override
	protected VariableBase createJointNoFactors(VariableBase otherVariable)
    {
    	if (! (otherVariable instanceof Discrete))
    	{
    		throw new DimpleException("Not currently supported");
    		
    	}
    	
    	Discrete dOtherVar = (Discrete)otherVariable;
    	
    	//First, create domain
    	int newDomainLength = this.getDiscreteDomain().size()*dOtherVar.getDiscreteDomain().size();
    	
    	Object [] newDomain = new Object[newDomainLength];
    	double [] inputs = new double[newDomainLength];
    	
    	int index = 0;
		for (int j = 0; j <  dOtherVar.getDiscreteDomain().size(); j++)
		{
	    	for (int i = 0; i < getDiscreteDomain().size(); i++)
	    	{
    			Object [] pair =
    				new Object[] {this.getDiscreteDomain().getElement(i),dOtherVar.getDiscreteDomain().getElement(j)};
    			newDomain[index] = pair;
    			inputs[index] = this.getInput()[i]*dOtherVar.getInput()[j];
    			index++;
    		}
    	}
    	
    	Discrete retval =  new Discrete(newDomain);
    	retval.setInput(inputs);
    	return retval;
    }
	
	public void setInput(double ... value)
	{
		setInputObject(value);
	}
	
	
	// Fix the variable to a specific value
	public final int getFixedValueIndex()
	{
		Integer index = getFixedValueObject();
		if (index == null)
			throw new DimpleException("Fixed value not set");
		
		return index;
	}
	
	public final Object getFixedValue()
	{
		Integer index = getFixedValueObject();
		if (index == null)
			throw new DimpleException("Fixed value not set");
		
		return getDomain().getElement(index);
	}
	public void setFixedValueIndex(int fixedValueIndex)
	{
		// In case the solver doesn't directly support fixed-values, convert the fixed-value to an input
		double[] input = new double[getDiscreteDomain().size()];
		Arrays.fill(input, 0);
		input[fixedValueIndex] = 1;
		_input = input;
		setFixedValueObject(fixedValueIndex,true);
		_input = input;
		
	}
	public void setFixedValue(Object fixedValue)
	{
		int index = getDomain().getIndex(fixedValue);
		if (index < 0)
			throw new DimpleException("Attempt to set variable to a fixed value that is not an element of the variable's domain.");
		setFixedValueIndex(index);
	}

}
