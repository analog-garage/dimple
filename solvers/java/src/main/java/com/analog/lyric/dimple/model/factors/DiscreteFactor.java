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

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.TableFactorFunction;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.model.variables.Constant;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.IConstantOrVariable;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.util.misc.Internal;

/**
 * A factor with only {@link Discrete} variables.
 */
public class DiscreteFactor extends Factor
{
	/*-------
	 * State
	 */
	
	private @Nullable JointDomainIndexer _domainList = null;
	
	private @Nullable JointDomainIndexer _factorArgumentDomains = null;
	
	private @Nullable IFactorTable _factorTable = null;
	
	/*--------------
	 * Construction
	 */
	
	@Internal
	public DiscreteFactor(FactorFunction factorFunc)
	{
		super(factorFunc);
	}

	protected DiscreteFactor(DiscreteFactor that)
	{
		super(that);
		_domainList = that._domainList;
	}
	
	@Override
	public DiscreteFactor clone()
	{
		return new DiscreteFactor(this);
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
	
	@Override
	protected void notifyConnectionsChanged()
	{
		_domainList = null;
		_factorArgumentDomains = null;
		_factorTable = null;
	}
	
	/*----------------
	 * Factor methods
	 */
	
	@Override
	public JointDomainIndexer getDomainList()
	{
		JointDomainIndexer domainList = _domainList;
		
		if (domainList == null)
		{
			int numVariables = getSiblingCount();

			DiscreteDomain[] domains = new DiscreteDomain[numVariables];

			for (int i = 0; i < numVariables; i++)
			{
				domains[i] = getSibling(i).getDomain();
			}

			domainList = JointDomainIndexer.create(getDirectedTo(), domains);
			_domainList = domainList;
		}
		
		return domainList;
	}

	@Override
	public JointDomainIndexer getArgumentDomains()
	{
		JointDomainIndexer domainList = _factorArgumentDomains;
		
		if (domainList == null)
		{
			super.getArgumentDomains();
			int numArgs = getArgumentCount();

			DiscreteDomain[] domains = new DiscreteDomain[numArgs];

			for (int i = 0; i < numArgs; i++)
			{
				IConstantOrVariable arg = getArgument(i);
				if (arg instanceof Constant)
				{
					// Make a single-element discrete domain for the constant value.
					domains[i] = DiscreteDomain.create(requireNonNull(((Constant)arg).value().getObject()));
				}
				else
				{
					domains[i] = ((Discrete)arg).getDomain();
				}
			}

			domainList = JointDomainIndexer.create(getDirectedTo(), domains);
			_factorArgumentDomains = domainList;
		}
		
		return domainList;
	}

	@Override
	public IFactorTable getFactorTable()
	{
		IFactorTable table = _factorTable;
		if (table == null)
		{
			final FactorFunction func = getFactorFunction();
			if (func instanceof TableFactorFunction)
			{
				final TableFactorFunction tableFunc = (TableFactorFunction)func;
				table = tableFunc.getFactorTable();
				if (hasConstants() && table.getDimensions() != getSiblingCount())
				{
					// Convert table to get rid of the constant dimensions
					table.convert(JointDomainReindexer.createRemover(table.getDomainIndexer(), getConstantIndices()));
				}
			}
			else
			{
				table = func.getFactorTable(this);
			}
			
			_factorTable = table;
		}
		return table;
	}
	
	@Override
	public void setFactorFunction(FactorFunction function)
	{
		super.setFactorFunction(function);
		_factorTable = null;
	}
	
	public int[][] getPossibleBeliefIndices()
	{
		return requireSolver("getPossibleBeliefIndices").getPossibleBeliefIndices();
	}
	
	@Override
	public boolean isDiscrete()
	{
		return true;
	}

	@Override
	public void replaceVariablesWithJoint(Variable [] variablesToJoin, Variable newJoint)
	{
		//Support a mixture of variables referred to in this factor and previously not referred to in this factor
		List<? extends Variable> ports = getSiblings();
		ArrayList<Variable> newVariables = new ArrayList<Variable>();
		

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
			setFactorFunction(TableFactorFunction.forFactor(this, newTable));
			
			for (Variable v : newVariables)
			{
				addEdge(this, v);
			}
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
		
		//Remove old edges in descending order (they were added in ascending order above)
		for (int i = factorVarIndices.length; --i>=0;)
		{
			EdgeState edge = getSiblingEdgeState(factorVarIndices[i]);
			removeSiblingEdge(edge);
		}
		
		//Add the new joint variable
		addEdge(this, newJoint);

	}

	public String getFactorTableString()
	{
		String s = "TableFactor [" + getLabel() + "] " + getFactorTable().toString();
		return s;
	}
	
	public double [] getBelief()
	{
		return (double[])requireSolver("getBelief").getBelief();
	}

	void setFactorTable(IFactorTable table)
	{
		setFactorFunction(TableFactorFunction.forFactor(this, table));
		_factorTable = table;
		if (!hasConstants())
		{
			// If there are no domains, then this is the same as the table's
			_factorArgumentDomains = table.getDomainIndexer();
		}
	}
	
}
