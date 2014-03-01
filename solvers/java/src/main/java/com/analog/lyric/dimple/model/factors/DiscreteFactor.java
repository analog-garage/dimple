/*******************************************************************************
*   Copyright 2012-2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model.factors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;

/**
 * A factor with only {@link Discrete} variables.
 */
public class DiscreteFactor extends Factor
{
	/*--------------
	 * Construction
	 */
	
	public DiscreteFactor(int id, FactorFunction factorFunc, VariableBase[] variables)
	{
		super(id, factorFunc, variables);
		
		if (!isDiscrete())
			throw new DimpleException("ack");
	}

	/*--------------
	 * Node methods
	 */
	
	@Override
	public Discrete getConnectedNodeFlat(int i)
	{
		return (Discrete)super.getConnectedNodeFlat(i);
	}
	
	@Override
	public Discrete getSibling(int i)
	{
		return (Discrete)super.getSibling(i);
	}

	/*----------------
	 * Factor methods
	 */
	
	@Override
	public JointDomainIndexer getDomainList()
	{
		int numVariables = getSiblingCount();
		
		DiscreteDomain[] domains = new DiscreteDomain[numVariables];
		
		for (int i = 0; i < numVariables; i++)
		{
			domains[i] = getSibling(i).getDomain();
		}
		
		return JointDomainIndexer.create(_directedTo, domains);
		
	}

	public int[][] getPossibleBeliefIndices()
	{
		return _solverFactor.getPossibleBeliefIndices();
	}
	
	@Override
	protected FactorFunction removeFixedVariablesImpl(
		FactorFunction oldFunction,
		IFactorTable oldFactorTable,
		ArrayList<VariableBase> constantVariables,
		int[] constantIndices)
	{
		final int nRemoved = constantIndices.length;
		final int[] valueIndices = new int[oldFactorTable.getDimensions()];
		Arrays.fill(valueIndices, -1);
		for (int i = 0; i < nRemoved; ++i)
		{
			final Discrete variable = (Discrete) constantVariables.get(i);
			valueIndices[constantIndices[i]] = variable.getFixedValueIndex();
		}
		
		return new TableFactorFunction(oldFunction.getName(), oldFactorTable.createTableConditionedOn(valueIndices));
	}
	
	@Override
	public void replaceVariablesWithJoint(VariableBase [] variablesToJoin, VariableBase newJoint)
	{
		//Support a mixture of variables referred to in this factor and previously not referred to in this factor
		List<INode> ports = getSiblings();
		ArrayList<VariableBase> newVariables = new ArrayList<VariableBase>();
		

		//First we figure out which variables are not currently referred to in this factor.
		for (int i = 0; i < variablesToJoin.length; i++)
		{
			boolean exists = false;
			for (int j = 0; j < ports.size(); j++)
				if (getConnectedNodeFlat(j).equals(variablesToJoin[i]))
				{
					exists = true;
					break;
				}
			
			if (!exists)
			{
				newVariables.add(variablesToJoin[i]);
			}
		}

		//Next we figure out the domain lengths of all the new variables
		DiscreteDomain [] newDomains = new DiscreteDomain[newVariables.size()];
		for (int i = 0; i < newDomains.length; i++)
			newDomains[i] = ((Discrete)newVariables.get(i)).getDiscreteDomain();
		 
		//Now, we modify the combo table to include the new variables.
		if (newDomains.length > 0)
		{
			//getFactorFunction();
			IFactorTable newTable = getFactorTable().createTableWithNewVariables(newDomains);
			setFactorFunction(new TableFactorFunction(getFactorFunction().getName(), newTable));
			
			for (VariableBase v : newVariables)
				addVariable(v);
		}

		//Now get the indices of all the variables
		int [] factorVarIndices = new int[variablesToJoin.length];
		int [] indexToJointIndex = new int[variablesToJoin.length];
		
		//Figure out which are the new variables and store a mapping
		int index = 0;
		for (int i = 0; i < ports.size(); i++)
		{
			for (int j = 0; j < variablesToJoin.length; j++)
			{
				if (getConnectedNodeFlat(i).equals(variablesToJoin[j]))
				{
					factorVarIndices[index] = i;
					indexToJointIndex[j] = index;
					index++;
					break;
				}
			}
		}

		
		//Get all the domain lengths
		DiscreteDomain [] allDomains = new DiscreteDomain[ports.size()];
		for (int i = 0; i < allDomains.length; i++)
			allDomains[i] = getConnectedNodeFlat(i).getDiscreteDomain();
		
		//Create the new combo table
		IFactorTable newTable2 =  getFactorTable().joinVariablesAndCreateNewTable(
				factorVarIndices,
				indexToJointIndex,
				allDomains,
				((Discrete)newJoint).getDiscreteDomain());
		setFactorFunction(new TableFactorFunction(getFactorFunction().getName(),newTable2));
		
		//Remove old ports in descending order (they were added in ascending order above)
		for (int i = factorVarIndices.length; --i>=0;)
		{
			disconnect(factorVarIndices[i]);
		}
		
		//Add the new joint variable
		addVariable(newJoint);

		//Tell all old variables to remove this factor graph.
		for (VariableBase v : variablesToJoin)
			v.remove(this);
		
		
	}

	public String getFactorTableString()
	{
		String s = "TableFactor [" + getLabel() + "] " + getFactorTable().toString();
		return s;
	}
	
	public double [] getBelief()
	{
		return (double[])_solverFactor.getBelief();
	}


}
