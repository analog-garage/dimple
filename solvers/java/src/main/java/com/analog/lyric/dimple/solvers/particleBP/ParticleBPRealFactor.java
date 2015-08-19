/*******************************************************************************
*   Copyright 2012-2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.particleBP;


import static com.analog.lyric.math.Utilities.*;
import static java.util.Objects.*;

import com.analog.lyric.collect.CombinatoricIterator;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Constant;
import com.analog.lyric.dimple.model.variables.IConstantOrVariable;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SDiscreteWeightEdge;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductDiscreteEdge;

/**
 * Real solver factor under Particle BP solver.
 * 
 * @since 0.07
 */
public class ParticleBPRealFactor extends SFactorBase
{
	protected double _beta = 1;
	
	ParticleBPRealFactor(Factor factor, ISolverFactorGraph parent)
	{
		super(factor, parent);
	}
	
	@Override
	public ParticleBPSolverGraph getParentGraph()
	{
		return (ParticleBPSolverGraph)_parent;
	}
	
	public double getMarginalPotential(double value, int outPortIndex)
	{
		final int nEdges = getSiblingCount();
		FactorFunction factorFunction = _model.getFactorFunction();

        double marginal = 0;
        
        CombinatoricIterator<Value> iter = getCombinatoricIterator(value, outPortIndex);
        final int[] variableIndices = iter.indices();
        
        final double[][] inputWeightsPerEdge = new double[nEdges][];
    	for (int i = 0; i < nEdges; ++i)
    	{
    		inputWeightsPerEdge[i] = getSiblingEdgeState(i).varToFactorMsg.representation();
    	}
    	
        while (iter.hasNext())
        {
        	double prob = factorFunction.eval(iter.next());
			if (_beta != 1) prob = Math.pow(prob, _beta);

        	for (int i = 0; i < outPortIndex; ++i)
        	{
        		prob *= inputWeightsPerEdge[i][variableIndices[i]];
        	}
        	for (int i = outPortIndex + 1; i < nEdges; ++i)
        	{
        		prob *= inputWeightsPerEdge[i][variableIndices[i]];
        	}

        	marginal += prob;
        }
        
        // FIXME: Should do bounds checking
	    return weightToEnergy(marginal);
	}
	
	
	@Override
	public void doUpdateEdge(int outPortNum)
	{
		final int nEdges = getSiblingCount();
		FactorFunction factorFunction = _model.getFactorFunction();

		final DiscreteMessage outputMsg = getSiblingEdgeState(outPortNum).factorToVarMsg;
		outputMsg.setWeightsToZero();
        final double[] outputWeights = outputMsg.representation();
        
        final CombinatoricIterator<Value> iter = getCombinatoricIterator();
        final int[] variableIndices = iter.indices();
        while (iter.hasNext())
        {
        	Value[] values = iter.next();
        	double prob = factorFunction.eval(values);
			
			if (_beta != 1) prob = Math.pow(prob, _beta);

        	for (int inPortNum = 0; inPortNum < nEdges; inPortNum++)
        	{
        		if (inPortNum != outPortNum)
        		{
        			prob *= getSiblingEdgeState(inPortNum).varToFactorMsg.getWeight(variableIndices[inPortNum]);
        		}
        	}

        	outputWeights[variableIndices[outPortNum]] += prob;
        }
        
        outputMsg.normalize();
	}
	
	
	@Override
	protected void doUpdate()
	{
		FactorFunction factorFunction = _model.getFactorFunction();

        final CombinatoricIterator<Value> iter = getCombinatoricIterator();
        final int[] variableIndices = iter.indices();
        
		for (int outPortNum = 0, n = getSiblingCount(); outPortNum < n; outPortNum++)
		{
			final DiscreteMessage outputMsg = getSiblingEdgeState(outPortNum).factorToVarMsg;
			outputMsg.setWeightsToZero();
			
			final double[] outputWeights = outputMsg.representation();

			iter.reset();
			while (iter.hasNext())
			{
				Value[] variableValues = iter.next();
				double prob = 1;
				prob = factorFunction.eval(variableValues);
				if (_beta != 1) prob = Math.pow(prob, _beta);

				for (int inPortNum = 0; inPortNum < outPortNum; inPortNum++)
				{
					prob *= getSiblingEdgeState(inPortNum).varToFactorMsg.getWeight(variableIndices[inPortNum]);
				}
				for (int inPortNum = outPortNum + 1; inPortNum < n; inPortNum++)
				{
					prob *= getSiblingEdgeState(inPortNum).varToFactorMsg.getWeight(variableIndices[inPortNum]);
				}

				outputWeights[variableIndices[outPortNum]] += prob;
			}

			outputMsg.normalize();
		}
	}
	
    public void setBeta(double beta)	// beta = 1/temperature
    {
    	_beta = beta;
    }

    @Override
	public void initialize()
	{
		super.initialize();

	}

    @Deprecated
	@Override
	public Object getInputMsg(int portIndex)
	{
		return getSiblingEdgeState(portIndex).varToFactorMsg.representation();
	}

    @Deprecated
	@Override
	public Object getOutputMsg(int portIndex)
	{
		return getSiblingEdgeState(portIndex).factorToVarMsg.representation();
	}

	@SuppressWarnings("null")
	@Override
	public SDiscreteWeightEdge getSiblingEdgeState(int siblingIndex)
	{
		return (SDiscreteWeightEdge)getSiblingEdgeState_(siblingIndex);
	}

	@SuppressWarnings("null")
	protected SumProductDiscreteEdge getDiscreteEdge(int siblingIndex)
	{
		return (SumProductDiscreteEdge)getSiblingEdgeState_(siblingIndex);
	}

	@SuppressWarnings("null")
	protected ParticleBPRealEdge getRealEdge(int siblingIndex)
	{
		return (ParticleBPRealEdge)getSiblingEdgeState_(siblingIndex);
	}

	@Override
	public IParticleBPVariable getSibling(int edge)
	{
		return (IParticleBPVariable) super.getSibling(edge);
	}
	
	/**
	 * Returns an iterator over all combination of variable values
	 */
	private CombinatoricIterator<Value> getCombinatoricIterator()
	{
		final int nEdges = getSiblingCount();
		final Value[][] particlesPerVar = new Value[nEdges][];
		for (int i = 0; i < nEdges; ++i)
		{
			particlesPerVar[i] = getSibling(i).getParticleValueObjects();
		}
		return new CombinatoricIterator<>(Value.class, particlesPerVar);
	}
	
	/**
	 * Returns an iterator over all combination of variable values except for edge
	 */
	private CombinatoricIterator<Value> getCombinatoricIterator(double frozenValue, int frozenEdge)
	{
		final Factor factor = _model;
		final int nArgs = factor.getArgumentCount();
		final Value[][] particlesPerVar = new Value[nArgs][];
		for (int i = 0; i < nArgs; ++i)
		{
			IConstantOrVariable arg = factor.getArgument(i);
			if (arg instanceof Constant)
			{
				particlesPerVar[i] = new Value[] { ((Constant)arg).value() };
			}
			else if (i == frozenEdge)
			{
				particlesPerVar[i] = new Value[] { RealValue.create(frozenValue) };
			}
			else
			{
				Variable var = (Variable)arg;
				IParticleBPVariable svar = requireNonNull(getParentGraph().getSolverVariable(var));
				particlesPerVar[i] = svar.getParticleValueObjects();
			}
		}
		return new CombinatoricIterator<>(Value.class, particlesPerVar);
	}
}
