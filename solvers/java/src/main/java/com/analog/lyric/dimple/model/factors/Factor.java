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
import java.util.BitSet;

import cern.colt.list.IntArrayList;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.JointFactorFunction;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.DomainList;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.util.misc.Internal;

public class Factor extends FactorBase implements Cloneable
{
	private String _modelerFunctionName = "";
	protected ISolverFactor _solverFactor = null;
	private FactorFunction _factorFunction;
	int [] _directedTo = null;
	int [] _directedFrom = null;
	
	
	@Override
	public final Factor asFactor()
	{
		return this;
	}
	
	@Override
	public final boolean isFactor()
	{
		return true;
	}
	
	public String getModelerFunctionName()
	{
		return _modelerFunctionName;
	}
	
	public Factor(int id,FactorFunction factorFunc, VariableBase [] variables)
	{
		super(id);

		
		_factorFunction = factorFunc;
		_modelerFunctionName = factorFunc.getName();
		
		for (int i = 0; i < variables.length; i++)
			addVariable(variables[i]);
	}
	
	public Factor(int id,VariableBase [] variables, String modelerFunctionName)
	{
		super(id);
				
		_modelerFunctionName = modelerFunctionName;
		for (int i = 0; i < variables.length; i++)
		{
			connect(variables[i]);
			variables[i].connect(this);
		}
	}

	public IFactorTable getFactorTable()
	{
		return getFactorFunction().getFactorTable(this);
	}
	
	public boolean hasFactorTable()
	{
		return getFactorFunction().factorTableExists(this);
	}

	public boolean isDiscrete()
	{
		for (INode p : getSiblings())
		{
			VariableBase vb = (VariableBase)p;
			if (! vb.getDomain().isDiscrete())
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	public FactorFunction getFactorFunction()
	{
		return _factorFunction;
	}
	
	public void setFactorFunction(FactorFunction function)
	{
		_factorFunction = function;
		if (_factorFunction.isDirected())	// Automatically set direction if inherent in factor function
			setDirectedTo(_factorFunction.getDirectedToIndices(getSiblingCount()));
	}
	
	protected void addVariable(VariableBase variable)
	{
		connect(variable);
		variable.connect(this);
	}
	

	
	@Override
	public String getLabel()
	{
		String name = _label;
		if(name == null)
		{
			name = _name;
			if(name == null)
			{
				name = getModelerFunctionName() + "_" + getId();
			}
		}
		return name;
	}

	
	@Override
	public ISolverFactor getSolver()
	{
		return _solverFactor;
	}

	@Override
	public String getClassLabel()
    {
    	return "Factor";
    }
	
	public void createSolverObject(ISolverFactorGraph factorGraph)
	{
		if (factorGraph != null)
		{
			_solverFactor = factorGraph.createFactor(this);
			_solverFactor.createMessages();
		}
		else
		{
			_solverFactor = null;
		}
	}
	
	/**
	 * Removes edges to all variables that have fixed values and modifies the
	 * factor function to incorporate those values.
	 * <p>
	 * For {@link DiscreteFactor}s, this will replace the factor function with one using a newly
	 * generated factor table. For other factor types this wraps the factor function with
	 * {@link FactorFunctionWithConstants}.
	 * </p>
	 * @return the number of variable edges that were removed.
	 * 
	 * @see VariableBase#hasFixedValue()
	 */
	public final int removeFixedVariables()
	{
		final int nEdges = getSiblingCount();
		final ArrayList<VariableBase> constantVariables = new ArrayList<VariableBase>(nEdges);
		final IntArrayList constantIndices = new IntArrayList(nEdges);
		
		// Visit in reverse order so that disconnect is safe.
		for (int i = nEdges; --i>=0;)
		{
			final VariableBase var = getSibling(i);
			if (var.hasFixedValue())
			{
				if (constantIndices.isEmpty() && isDiscrete())
				{
					// Before disconnecting siblings, force the factor table
					// to be instantiated if discrete, since it may depend on
					// the original edges.
					getFactorTable();
				}
				var.remove(this);
				constantVariables.add(var);
				constantIndices.add(i);
				disconnect(i);
			}
		}
		
		final int nRemoved = nEdges - constantIndices.size();
		
		if (nRemoved > 0)
		{
			constantIndices.trimToSize();
			final FactorFunction oldFunction = getFactorFunction();
			final FactorFunction newFunction =
				removeFixedVariablesImpl(oldFunction, constantVariables, constantIndices.elements());
			
			int[] newDirectedTo = null;
			if (isDirected() && !oldFunction.isDirected())
			{
				// If factor function is not inherently directed, then update the directed indices.
				newDirectedTo = ArrayUtil.contractSortedIndexList(_directedTo, constantIndices.elements());
			}

			setFactorFunction(newFunction);
			
			if (newDirectedTo != null)
			{
				setDirectedTo(newDirectedTo);
			}
		}
		
		return nRemoved;
	}
	
	/**
	 * Replaces factor function conditioned on a set of variables with fixed values that
	 * have already been removed from the factor. This should only be called from within
	 * {@link #removeFixedVariables()}.
	 * <p>
	 * This is invoked after the fixed value variables have already been disconnected from the
	 * factor, but before the factor function has been replaced.
	 * <p>
	 * @param constantVariables lists the variables
	 * @param constantIndices
	 */
	protected FactorFunction removeFixedVariablesImpl(
		FactorFunction oldFunction,
		ArrayList<VariableBase> constantVariables,
		int[] constantIndices)
	{
		final Object[] constantValues = new Object[constantVariables.size()];
		for (int i = constantValues.length; --i>=0;)
		{
			final VariableBase variable = constantVariables.get(i);
			if (variable.getDomain().isDiscrete())
			{
				// FIXME: clean up fixed value methods so there is a base class method for getting
				// the actual fixed value as opposed to its representation.
				constantValues[i] = variable.asDiscreteVariable().getFixedValue();
			}
			else
			{
				constantValues[i] = variable.getFixedValueObject();
			}
		}
		
		return new FactorFunctionWithConstants(oldFunction,	constantValues,	constantIndices);
	}
	
	public void replace(VariableBase oldVariable, VariableBase newVariable)
	{
		replaceSibling(oldVariable, newVariable);
	}
	
	public DomainList<?> getDomainList()
	{
		int numVariables = getSiblingCount();
		
		Domain [] domains = new Domain[numVariables];
		
		for (int i = 0; i < numVariables; i++)
		{
			domains[i] = getSibling(i).getDomain();
		}
		
		// FIXME: do we need to ensure that _directedTo has been set?
		return DomainList.create(_directedTo, domains);
	}
	

	@Override
	public Factor clone()
	{
		/*******
		 * NOTE: Any derived class that defines instance variables that are
		 * objects (rather than primitive types) must implement clone(), which
		 * must first call super.clone(), and then deep-copy those instance
		 * variables to the clone.
		 *******/
		Factor f = (Factor) super.clone();
		
		//TODO: cloning solver factor?
		
		return f;
	}

	@Override
	public void initialize(int portNum)
	{
		if (_solverFactor != null)
			_solverFactor.resetEdgeMessages(portNum);
	}
    
	/**
	 * Model-specific initialization for factors.
	 * <p>
	 * Assumes that model variables in same graph have already been initialized.
	 * Does NOT invoke solver factor initialize.
	 */
	@Override
	public void initialize()
	{
		if (_factorFunction.isDirected())	// Automatically set direction if inherent in factor function
		{
			setDirectedTo(_factorFunction.getDirectedToIndices(getSiblingCount()));
			if (_factorFunction.isDeterministicDirected())
			{
				for (int to : _directedTo)
				{
					getSibling(to).setDeterministicOutput();
				}
				if (_directedTo.length > 0)
				{
					for (int from : _directedFrom)
					{
						getSibling(from).setDeterministicInput();
					}
				}
			}
		}
	}
	
	@Override
	public void update()
	{
		checkSolverNotNull();
		_solverFactor.update();
	}

	@Override
	public void updateEdge(int outPortNum)
	{
		checkSolverNotNull();
		_solverFactor.updateEdge(outPortNum);
	}
	
	private void checkSolverNotNull()
	{
		if (_solverFactor == null)
			throw new DimpleException("solver must be set before performing this action.");
	}
	
	@Internal
	public void replaceVariablesWithJoint(VariableBase [] variablesToJoin, VariableBase newJoint)
	{
		throw new DimpleException("not implemented");
	}
	
	@Internal
	public Factor join(Factor other)
	{
		ArrayList<VariableBase> varList = new ArrayList<VariableBase>();
		
		//go through variables from first factor and
		ArrayList<Integer> map1 = new ArrayList<Integer>();
		
		final int nSiblings = getSiblingCount();
		final int nOtherSiblings = other.getSiblingCount();
		
		//First get a list of variables in common.
		for (int i = 0; i < nSiblings; i++)
		{
			map1.add(i);
			varList.add(getConnectedNodeFlat(i));
			
		}
		
		ArrayList<Integer> map2 = new ArrayList<Integer>();
		
		int newnuminputs = map1.size();
		
		for (int i = 0; i < nOtherSiblings; i++)
		{
			boolean found = false;
			
			for (int j = 0; j < nSiblings; j++)
			{
				
				if (getConnectedNodeFlat(j) == other.getConnectedNodeFlat(i))
				{
					found = true;
					map2.add(j);
					break;
				}
				
			}
			if (!found)
			{
				map2.add(varList.size());
				newnuminputs++;
				varList.add(other.getConnectedNodeFlat(i));
			}
		}
		
		FactorFunction ff1 = this.getFactorFunction();
		FactorFunction ff2 = other.getFactorFunction();
		JointFactorFunction jff = getParentGraph().getJointFactorFunction(ff1,ff2,map1,map2);
		
		//Remove the two old factors.
		getParentGraph().remove(this);
		getParentGraph().remove(other);
		
		//Create the table factor using the variables
		VariableBase [] vars = new VariableBase[newnuminputs];
		varList.toArray(vars);
		return getParentGraph().addFactor(jff, vars);

	}

	

	@Override
	public double getScore()
	{
		if (_solverFactor == null)
			throw new DimpleException("solver needs to be set before calculating energy");
		
		return _solverFactor.getScore();
	}
	
	@Override
	public double getInternalEnergy()
	{
		if (_solverFactor == null)
			throw new DimpleException("solver needs to be set before calculating energy");
		
		return _solverFactor.getInternalEnergy();
		
	}

	@Override
	public double getBetheEntropy()
	{
		if (_solverFactor == null)
			throw new DimpleException("solver needs to be set before calculating energy");
		
		return _solverFactor.getBetheEntropy();
		
	}


	public boolean isDirected()
	{
		if (_directedTo != null)
			return true;
		else
			return false;
	}

	private void ensureDirectedToSet()
	{
		if(_directedTo==null)
			setFactorFunction(getFactorFunction());
	}
	
	public int [] getDirectedTo()
	{
		// FIXME: this may change the value of isDirected()!
		ensureDirectedToSet();
		return _directedTo;
	}
	public int [] getDirectedFrom()
	{
		// FIXME: this may change the value of isDirected()!
		ensureDirectedToSet();
		return _directedFrom;
	}
	
	public VariableList getDirectedToVariables()
	{
		ensureDirectedToSet();
		
		VariableList vl = null;
		
		if (isDirected())
		{
			vl = new VariableList(_directedTo.length);
			for (int i = 0; i < _directedTo.length; i++)
			{
				vl.add(getSibling(_directedTo[i]));
			}
		}
		else
		{
			vl = new VariableList();
		}
		
		return vl;

	}

	public void setDirectedTo(VariableList vl)
	{
		int [] directedTo = new int[vl.size()];
		for (int i = 0; i < directedTo.length; i++)
			directedTo[i] = getPortNum(vl.getByIndex(i));
		
		setDirectedTo(directedTo);

	}

	public void setDirectedTo(VariableBase ... variables)
	{
		int [] directedTo = new int[variables.length];
		for (int i = 0; i < directedTo.length; i++)
			directedTo[i] = getPortNum(variables[i]);
		setDirectedTo(directedTo);
	}
	
	public void setDirectedTo(int [] directedTo)
	{
		final JointDomainIndexer curDomains = getDomainList().asJointDomainIndexer();

		BitSet toSet = new BitSet(directedTo.length);
		
		final int nVariables = getSiblingCount();
		_directedFrom = new int[nVariables-directedTo.length];
		
		boolean sort = false;
		int prev = -1;
		for (int toVarIndex : directedTo)
		{
			if (toSet.get(toVarIndex) || toVarIndex > nVariables)
				throw new DimpleException("invalid edge");
			if (toVarIndex < prev)
			{
				sort = true;
			}
			prev = toVarIndex;
			toSet.set(toVarIndex);
		}

		for (int i = 0, fromVarIndex = 0;
			(fromVarIndex = toSet.nextClearBit(fromVarIndex)) < nVariables;
			++fromVarIndex, ++i)
		{
			_directedFrom[i] = fromVarIndex;
		}
		
		if (sort)
		{
			Arrays.sort(directedTo);
		}
		
		_directedTo = directedTo;
		
		if (curDomains != null)
		{
			JointDomainIndexer newDomains = getDomainList().asJointDomainIndexer();
			if (!curDomains.equals(newDomains))
			{
				getFactorFunction().convertFactorTable(curDomains, newDomains);
			}
		}

		if (_solverFactor != null)
		{
			_solverFactor.setDirectedTo(directedTo);
		}
		
	}
	
	public boolean isDirectedTo(int edge)
	{
		ensureDirectedToSet();
		
		final int[] to = _directedTo;
		
		if (to == null)
			return false;

		// Assume _directedTo is sorted:
		
		final int toRange = to.length - 1;
		final int first = to[0];
		final int last = to[toRange];
		
		if (last - first == toRange)
		{
			return edge <= last && edge >= first;
		}
		
		return Arrays.binarySearch(to, edge) >= 0;
	}
}
