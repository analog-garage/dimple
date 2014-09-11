/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.junctiontree;

import static java.util.Objects.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.IntArrayList;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransformMap;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransformMap.AddedJointVariable;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

/**
 * @since 0.05
 * @author Christopher Barber
 *
 */
public class JunctionTreeSolverFactor extends SFactorBase
{
	/*-------
	 * State
	 */
	
	private final JunctionTreeSolverGraphBase<?> _root;
	
	private @Nullable ISolverFactor _delegate;
	private @Nullable JointDomainReindexer _reindexer;
	private boolean _reindexerComputed = false;
	
	/*--------------
	 * Construction
	 */
	
	JunctionTreeSolverFactor(Factor modelFactor, JunctionTreeSolverGraphBase<?> root)
	{
		super(modelFactor);
		_root = root;
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public double getBetheEntropy()
	{
		double sum = 0;
		
		final double [] beliefs = getBelief();
		for (double belief : beliefs)
		{
			sum -= belief * Math.log(belief);
		}
		
		return sum;
	}
	
	@Override
	public Object getInputMsg(int portIndex)
	{
		throw unsupported("getInputMsg");
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		throw unsupported("getOutputMsg");
	}

	@Override
	public JunctionTreeSolverGraphBase<?> getRootGraph()
	{
		return _root;
	}
	
	@Override
	public double getScore()
	{
		throw unsupported("getScore");
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		throw unsupported("moveMessages");
	}

	@Override
	public void resetEdgeMessages(int portNum)
	{
		throw unsupported("resetEdgeMessages");
	}

	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		throw unsupported("setInputMsg");
	}

	@Override
	public void setOutputMsg(int portIndex, Object obj)
	{
		throw unsupported("setOutputMsg");
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		throw unsupported("setInputMsgValues");
	}

	@Override
	public void setOutputMsgValues(int portIndex, Object obj)
	{
		throw unsupported("setOutputMsgValues");
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		throw unsupported("updateEdge");
	}

	/*-----------------------
	 * ISolverFactor methods
	 */
	
	@Override
	public void createMessages()
	{
	}

	@Override
	public double[] getBelief()
	{
		final ISolverFactor delegate = requireNonNull(getDelegate());
		final double[] beliefs = (double[]) delegate.getBelief();
		
		final JointDomainReindexer reindexer = getDelegateReindexer();
		if (reindexer == null)
		{
			// No conversion necessary
			return beliefs;
		}
		
		final IFactorTable delegateTable = ((STableFactorBase)delegate).getFactorTable();
		delegateTable.getDomainIndexer();
		
		final IFactorTable beliefTable = FactorTable.create(delegateTable.getDomainIndexer());
		beliefTable.setWeightsSparse(delegateTable.getIndicesSparseUnsafe(), beliefs);
		
		final IFactorTable convertedTable = FactorTable.convert(beliefTable, reindexer);
		convertedTable.setDirected(null);
		convertedTable.normalize();
		
		return convertedTable.getWeightsSparseUnsafe();
	}
	
	@Override
	public int[][] getPossibleBeliefIndices()
	{
		final ISolverFactor delegate = requireNonNull(getDelegate());
		
		final JointDomainReindexer reindexer = getDelegateReindexer();
		if (reindexer == null)
		{
			// No conversion necessary
			return delegate.getPossibleBeliefIndices();
		}
		
		// TODO: perhaps we should cache this state with getBelief()
		
		final IFactorTable delegateTable = ((STableFactorBase)delegate).getFactorTable();
		delegateTable.getDomainIndexer();
		
		final IFactorTable beliefTable = FactorTable.create(delegateTable.getDomainIndexer());
		beliefTable.setWeightsSparse(delegateTable.getIndicesSparseUnsafe(), (double[])delegate.getBelief());
		
		final IFactorTable convertedTable = FactorTable.convert(beliefTable, reindexer);
		
		return convertedTable.getIndicesSparseUnsafe();
	}
	
	@Override
	public void moveMessages(ISolverNode other)
	{
		throw unsupported("moveMessages");
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private @Nullable ISolverFactor getDelegate()
	{
		final ISolverFactor delegate = _delegate;
		if (delegate != null)
		{
			return delegate;
		}
		else
		{
			final Factor sourceFactor = getFactor();
			final Factor targetFactor = requireNonNull(_root.getTransformMap()).sourceToTargetFactor(sourceFactor);
			return _delegate = targetFactor.getSolver();
		}
	}

	private @Nullable JointDomainReindexer getDelegateReindexer()
	{
		if (!_reindexerComputed)
		{
			_reindexerComputed = true;
			
			// TODO: detect when no conversion is needed
			
			final JunctionTreeTransformMap transformMap = requireNonNull(_root.getTransformMap());
			final Factor sourceFactor = getFactor();
			final Factor targetFactor = transformMap.sourceToTargetFactor(sourceFactor);

			// Create mapping from target vars to their index in source factor
			final int nSourceVars = sourceFactor.getSiblingCount();
			final Map<Variable,Integer> targetVarToSourceIndex =
				new LinkedHashMap<Variable, Integer>(nSourceVars);
			for (int si = 0; si < nSourceVars; ++si)
			{
				final Variable sourceVar = sourceFactor.getSibling(si);
				final Variable targetVar = transformMap.sourceToTargetVariable(sourceVar);
				
				if (null != targetVarToSourceIndex.put(targetVar, si))
				{
					// FIXME - junction tree support duplicate variables
					throw new DimpleException("junction tree does not support factor with duplicate variables");
				}
			}
			
			final int nTargetVars = targetFactor.getSiblingCount();
			
			int removeIndex = nSourceVars;
			final IntArrayList targetToSourceIndex = new IntArrayList(nTargetVars);
			final IntArrayList targetVarsToSplit = new IntArrayList();
			for (int ti = 0; ti < nTargetVars; ++ti)
			{
				final Variable targetVar = targetFactor.getSibling(ti);
				final AddedJointVariable<?> addedJointVar =
					transformMap.getAddedDeterministicVariable(targetVar);
				if (addedJointVar == null)
				{
					Integer index = targetVarToSourceIndex.remove(targetVar);
					if (index != null)
					{
						targetToSourceIndex.add(index);
					}
					else
					{
						targetToSourceIndex.add(removeIndex++);
					}
				}
				else
				{
					final int nInputs = addedJointVar.getInputCount();
					for (int i = 0; i < nInputs; ++i)
					{
						final Variable inputVar = addedJointVar.getInput(i);
						Integer index = targetVarToSourceIndex.remove(inputVar);
						if (index != null)
						{
							targetToSourceIndex.add(index);
						}
						else
						{
							targetToSourceIndex.add(removeIndex++);
						}
					}
					
					targetVarsToSplit.add(ti);
				}
			}
			
			@NonNull JointDomainIndexer fromDomains = targetFactor.getFactorTable().getDomainIndexer();
			// TODO: get target domains without forcing instantiation of factor table?
			JointDomainIndexer toDomains = sourceFactor.getFactorTable().getDomainIndexer();
			
			if (!targetVarsToSplit.isEmpty())
			{
				targetVarsToSplit.trimToSize();
				JointDomainReindexer reindexer = _reindexer =
					JointDomainReindexer.createSplitter(fromDomains, targetVarsToSplit.elements());
				fromDomains = reindexer.getToDomains();
			}
			
			// Remaining entries in targetVarToSourceIndex should be conditioned variables
			// that were removed from the factor
			final int nConditioned = targetVarToSourceIndex.size();
			if (nConditioned > 0)
			{
				final int fromSize = fromDomains.size();
				final int[] conditionedValues = new int[fromSize + nConditioned];
				Arrays.fill(conditionedValues, -1);
				final DiscreteDomain[] conditionedDomains = new DiscreteDomain[nConditioned];
				
				int i = 0;
				for (Entry<Variable, Integer> entry : targetVarToSourceIndex.entrySet())
				{
					final Discrete variable = entry.getKey().asDiscreteVariable();
					final int sourceIndex = entry.getValue();

					targetToSourceIndex.add(sourceIndex);
					conditionedValues[i + fromSize] = variable.getFixedValueIndex();
					conditionedDomains[i] = variable.getDomain();
					++i;
				}

				fromDomains =
					JointDomainIndexer.concatNonNull(fromDomains, JointDomainIndexer.create(conditionedDomains));
				
				JointDomainReindexer deconditioner =
					JointDomainReindexer.createConditioner(fromDomains, conditionedValues).getInverse();
				_reindexer = deconditioner.appendTo(_reindexer);
			}

			targetToSourceIndex.trimToSize();
			final JointDomainReindexer permuter =
				JointDomainReindexer.createPermuter(fromDomains, toDomains, targetToSourceIndex.elements());
			
			_reindexer = permuter.appendTo(_reindexer);
		}
		return _reindexer;
	}
	
	private RuntimeException unsupported(String method)
	{
		return DimpleException.unsupportedMethod(getClass(), method);
	}

}
