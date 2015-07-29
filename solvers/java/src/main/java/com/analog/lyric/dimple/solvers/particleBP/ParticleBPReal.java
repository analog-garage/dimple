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

import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.solvers.core.PriorAndCondition;
import com.analog.lyric.dimple.solvers.core.SRealVariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.Proposal;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.options.OptionDoubleList;
import com.analog.lyric.options.OptionValidationException;
import com.analog.lyric.util.misc.Matlab;

/**
 * Solver variable for Real variables under Particle BP solver.
 * 
 * @since 0.07
 */
public class ParticleBPReal extends SRealVariableBase implements IParticleBPVariable
{
	/*-------
	 * State
	 */
	
	protected RealValue[] _particleValues;

	protected int _numParticles = 1;
	protected int _resamplingUpdatesPerSample = 1;
	protected @Nullable IProposalKernel _proposalKernel;
	/**
	 * True if {@link #_proposalKernel} was set explicitly via {@link #setProposalKernel(IProposalKernel)}
	 * and should not be overridden by option settings.
	 */
	protected boolean _explicitProposalKernel;
	protected RealDomain _initialParticleDomain;
	protected RealDomain _domain;
	double [] _particleEnergy;
	protected double _beta = 1;

	/*--------------
	 * Construction
	 */

	public ParticleBPReal(Real var, ParticleBPSolverGraph parent)
	{
		super(var, parent);

		// Since numParticles is used to configure the message sizes, we set this at construction
		// time to make it less likely that the messages will have to be recreated at initialize time.
		_numParticles = getOptionOrDefault(ParticleBPOptions.numParticles);
		_initialParticleDomain = _domain = var.getDomain();
		_particleValues = new RealValue[_numParticles];
		for (int i = 0; i < _numParticles; ++i)
		{
			_particleValues[i] = RealValue.create();
		}
		_particleEnergy = new double[_numParticles];
	}

	@Override
	public void initialize()
	{
		_resamplingUpdatesPerSample = getOptionOrDefault(ParticleBPOptions.resamplingUpdatesPerParticle);
		updateNumParticles(getOptionOrDefault(ParticleBPOptions.numParticles));

		if (!_explicitProposalKernel)
		{
			Class<? extends IProposalKernel> kernelClass = getOptionOrDefault(ParticleBPOptions.proposalKernel);
			if (_proposalKernel == null || kernelClass != requireNonNull(_proposalKernel).getClass())
			{
				try
				{
					_proposalKernel = kernelClass.getConstructor().newInstance();
				}
				catch (Exception ex)
				{
					// Option validation should already have made sure that the constructor
					// exists, so this should only happen if the constructor itself throws an exception.
					DimpleEnvironment.logError("Could not create proposal kernel instance for '%s': %s",
						kernelClass, ex.toString());
				}
			}
		}
		requireNonNull(_proposalKernel).configureFromOptions(this);
		
		OptionDoubleList range = getOptionOrDefault(ParticleBPOptions.initialParticleRange);
		RealDomain initialDomain = RealDomain.create(range.get(0), range.get(1));
		_initialParticleDomain = initialDomain.isSubsetOf(_domain) ? initialDomain : _domain;
		
		double particleMin = 0;
		double particleMax = 0;
		
		RealDomain domain = _initialParticleDomain;

		if (domain.isBounded())
		{
			particleMin = domain.getLowerBound();
			particleMax = domain.getUpperBound();
		}

		int length = _particleValues.length;
    	double particleIncrement = (length > 1) ? (particleMax - particleMin) / (length - 1) : 0;
    	double particleValue = particleMin;
    	
    	for (int i = 0; i < length; i++)
    	{
    		_particleValues[i].setDouble(particleValue);
    		particleValue += particleIncrement;
    	}
		super.initialize();
	}
	
	@Override
	protected void doUpdateEdge(int outPortNum)
	{
		final double maxEnergy = 100;
		int M = _numParticles;
		int D = getSiblingCount();
		double minEnergy = Double.POSITIVE_INFINITY;


		final double[] outMsgs = getSiblingEdgeState(outPortNum).varToFactorMsg.representation();

		PriorAndCondition known = getPriorAndCondition();
		
		for (int m = 0; m < M; m++)
		{
			double prior = known.evalEnergy(_particleValues[m]);
			
			// FIXME: why does infinity get turned into minLog but values between minLog
			// and infinity not?
			
			double out = (prior == Double.POSITIVE_INFINITY) ? maxEnergy : prior * _beta;

			for (int d = 0; d < D; d++)
			{
				if (d != outPortNum)		// For all ports except the output port
				{
					double tmp = getSiblingEdgeState(d).factorToVarMsg.getEnergy(m);
					out += (tmp == Double.POSITIVE_INFINITY) ? maxEnergy : tmp;
				}
			}

			// Subtract particle energy
			out -= _particleEnergy[m];

			if (out < minEnergy) minEnergy = out;
			outMsgs[m] = out;
		}

		known = known.release();
		
		//create sum
		double sum = 0;
		for (int m = 0; m < M; m++)
		{
			double out = energyToWeight(outMsgs[m] - minEnergy);
			outMsgs[m] = out;
			sum += out;
		}

		//calculate message by dividing by sum
		for (int m = 0; m < M; m++)
			outMsgs[m] /= sum;

	}

	@Override
	protected void doUpdate()
	{
		final double maxEnergy = 100;
		final int M = _numParticles;
		final int D = _model.getSiblingCount();

		PriorAndCondition known = getPriorAndCondition();

		// FIXME - handle fixed values
		
		//Compute alphas
        final double[] logInPortMsgs = DimpleEnvironment.doubleArrayCache.allocateAtLeast(M*D);
        final double[] alphas = DimpleEnvironment.doubleArrayCache.allocateAtLeast(M);
		for (int m = 0; m < M; m++)
		{
			double prior = known.evalEnergy(_particleValues[m]);
			double alpha = (prior == Double.POSITIVE_INFINITY) ? maxEnergy : prior * _beta;

			for (int d = 0, i = m; d < D; d++, i += M)
			{
				double tmp = getSiblingEdgeState(d).factorToVarMsg.getEnergy(m);
				double logtmp = (tmp == Double.POSITIVE_INFINITY) ? maxEnergy : tmp;
				logInPortMsgs[i] = logtmp;
				alpha += logtmp;
			}
			alphas[m] = alpha;
		}
		known = known.release();
		
		//Now compute output messages for each outgoing edge
		for (int out_d = 0, dm = 0; out_d < D; out_d++, dm += M )
		{
			final DiscreteMessage outMsg = getSiblingEdgeState(out_d).varToFactorMsg;
			final double[] outWeights = outMsg.representation();

			double minEnergy = Double.POSITIVE_INFINITY;

			//set outMsgs to alpha - mu_d,m
			//find max alpha
			for (int m = 0; m < M; m++)
			{
				double out = alphas[m] - logInPortMsgs[dm + m];

				// Subtract particle energy
				out -= _particleEnergy[m];

				if (out < minEnergy) minEnergy = out;
				outWeights[m] = out;
			}

			//create sum
			double sum = 0;
			for (int m = 0; m < M; m++)
			{
				double out = energyToWeight(outWeights[m] - minEnergy);
				outWeights[m] = out;
				sum += out;
			}

			//calculate message by dividing by sum
			for (int m = 0; m < M; m++)
			{
				outWeights[m] /= sum;
			}

		}

		DimpleEnvironment.doubleArrayCache.release(logInPortMsgs);
	    DimpleEnvironment.doubleArrayCache.release(alphas);
	}

	public void resample()
	{
		final FactorGraph fg = _model.requireParentGraph();
		int numPorts = _model.getSiblingCount();
		Domain varDomain = _model.getDomain();
		double _lowerBound = _domain.getLowerBound();
		double _upperBound = _domain.getUpperBound();
		int M = _numParticles;

		PriorAndCondition known = getPriorAndCondition();
		
		final IProposalKernel kernel = requireNonNull(_proposalKernel);

		// For each sample value
		for (int m = 0; m < M; m++)
		{
			final RealValue sampleValue = _particleValues[m];
			// Start with the potential for the current particle value
			double potential = known.evalEnergy(sampleValue) * _beta;
			double potentialProposed = 0;

			for (int portIndex = 0; portIndex < numPorts; portIndex++)
			{
				EdgeState edge = _model.getSiblingEdgeState(portIndex);
				int factorPortNumber = edge.getSibling(_model).indexOfSiblingEdgeState(edge);
				
				ParticleBPRealFactor factor = (ParticleBPRealFactor)getSibling(portIndex);
				potential += factor.getMarginalPotential(sampleValue.getDouble(), factorPortNumber);
			}


			// Now repeat resampling this sample
			for (int update = 0; update < _resamplingUpdatesPerSample; update++)
			{
				Proposal proposal = kernel.next(sampleValue, varDomain);
				double proposalValue = proposal.value.getDouble();

				// If outside the bounds, then reject
				if (proposalValue < _lowerBound) continue;
				if (proposalValue > _upperBound) continue;

				// Sum up the potentials from the input and all connected factors
				potentialProposed = known.evalEnergy(proposal.value) * _beta;
				
				for (int portIndex = 0; portIndex < numPorts; portIndex++)
				{
					EdgeState edge = _model.getSiblingEdgeState(portIndex);
					int factorPortNumber = edge.getSibling(_model).indexOfSiblingEdgeState(edge);
					ParticleBPRealFactor factor = (ParticleBPRealFactor)getSibling(portIndex);
					potentialProposed += factor.getMarginalPotential(proposalValue, factorPortNumber);
				}


				// Accept or reject
				double rejectionThreshold = Math.exp(potential - potentialProposed + proposal.forwardEnergy - proposal.reverseEnergy);
				if (Double.isNaN(rejectionThreshold))	// Account for invalid forward or reverse proposals
				{
					if (potentialProposed != Double.POSITIVE_INFINITY && proposal.forwardEnergy != Double.POSITIVE_INFINITY)
						rejectionThreshold = Double.POSITIVE_INFINITY;
					else
						rejectionThreshold = 0;
				}
				if (DimpleRandomGenerator.rand.nextDouble() < rejectionThreshold)
				{
					sampleValue.setDouble(proposalValue);
					potential = potentialProposed;
				}
			}

			_particleEnergy[m] = potential;			// Sum-product code uses log(p) instead of -log(p)


			// Update the incoming messages for the new particle value
			SolverNodeMapping solvers = getSolverMapping();
			for (int d = 0; d < numPorts; d++)
			{
				final EdgeState edge = _model.getSiblingEdgeState(d);
				Factor factorNode = edge.getFactor(fg);
				int factorPortNumber = edge.getFactorToVariableEdgeNumber();
				ParticleBPRealFactor factor = (ParticleBPRealFactor)(solvers.getSolverFactor(factorNode));
				getSiblingEdgeState(d).factorToVarMsg.setWeight(m,
					Math.exp(factor.getMarginalPotential(sampleValue.getDouble(), factorPortNumber)));
			}
		}

		known.release();
		
		// Update the outgoing messages associated with the new particle locations
		doUpdate();
	}


	@Override
	public double[] getBelief()
	{
		final double maxEnergy = 100;
		int M = _numParticles;
		int D = _model.getSiblingCount();
		double minEnergy = Double.POSITIVE_INFINITY;

		PriorAndCondition known = getPriorAndCondition();
		double[] outBelief = new double[M];
		
		for (int m = 0; m < M; m++)
		{
			double prior = known.evalEnergy(_particleValues[m]);
			double out = (prior == Double.POSITIVE_INFINITY) ? maxEnergy : prior * _beta;

			for (int d = 0; d < D; d++)
			{
				double tmp = getSiblingEdgeState(d).factorToVarMsg.getEnergy(m);
				out += (tmp == Double.POSITIVE_INFINITY) ? maxEnergy : tmp;
			}

			//	        // Subtract the log weight
			//	        out -= _logWeight[m];

			if (out < minEnergy) minEnergy = out;
			outBelief[m] = out;
		}

		known.release();
		
		//create sum
		double sum = 0;
		for (int m = 0; m < M; m++)
		{
			double out = energyToWeight(outBelief[m] - minEnergy);
			outBelief[m] = out;
			sum += out;
		}

		//calculate belief by dividing by sum
		for (int m = 0; m < M; m++)
			outBelief[m] /= sum;

		return outBelief;
	}

	// Alternative belief, returned for a specified set of variable values
	@Matlab
	public double [] getBelief(double[] valueSet)
	{
		final double maxEnergy = 100;
		int M = valueSet.length;
		int D = _model.getSiblingCount();
		double minEnergy = Double.POSITIVE_INFINITY;

		PriorAndCondition known = getPriorAndCondition();
		double[] outBelief = new double[M];

		Value value = Value.create(getDomain());
		
		for (int m = 0; m < M; m++)
		{
			double real = valueSet[m];
			value.setDouble(real);
			double prior = known.evalEnergy(value);
			double out = (prior == Double.POSITIVE_INFINITY) ? maxEnergy : prior * _beta;

			for (int d = 0; d < D; d++)
			{
				int factorPortNumber = _model.getReverseSiblingNumber(d);
				ParticleBPRealFactor factor = (ParticleBPRealFactor)getSibling(d);
				out += factor.getMarginalPotential(real, factorPortNumber);	// Potential is -log(p)
			}

			if (out < minEnergy) minEnergy = out;
			outBelief[m] = out;
		}

		known.release();
		
		//create sum
		double sum = 0;
		for (int m = 0; m < M; m++)
		{
			double out = energyToWeight(outBelief[m] - minEnergy);
			outBelief[m] = out;
			sum += out;
		}

		//calculate belief by dividing by sum
		for (int m = 0; m < M; m++)
			outBelief[m] /= sum;

		return outBelief;
	}

	@Matlab
	public double[] getParticleValues()
	{
		double[] particles = new double[_numParticles];
		for (int i = 0; i < _numParticles; i++)
		{
			particles[i] = _particleValues[i].getDouble();
		}
		return particles;
	}

	@Override
	public final RealValue[] getParticleValueObjects()
	{
		return _particleValues;
	}
	
	public void setNumParticles(int numParticles)
	{
		setOption(ParticleBPOptions.numParticles, numParticles);
		updateNumParticles(numParticles);
	}
	
	private void updateNumParticles(int numParticles)
	{
		if (numParticles != _numParticles)
		{
			_particleValues = Arrays.copyOf(_particleValues, numParticles);
			for (int i = _numParticles; i < numParticles; ++i)
			{
				_particleValues[i] = RealValue.create();
			}
			_numParticles = numParticles;
			_particleEnergy = Arrays.copyOf(_particleEnergy, numParticles);
			for (int i  = 0, n = getSiblingCount(); i < n; ++i)
			{
				getSiblingEdgeState(i).resize(numParticles);
			}
		}
	}
	
	public int getNumParticles() {return _numParticles;}

	public void setResamplingUpdatesPerParticle(int updatesPerParticle)
	{
		setOption(ParticleBPOptions.resamplingUpdatesPerParticle, updatesPerParticle);
		_resamplingUpdatesPerSample = updatesPerParticle;
	}
	
	public int getResamplingUpdatesPerParticle() {return _resamplingUpdatesPerSample;}

	/**
	 * @deprecated instead set {@link NormalProposalKernel#standardDeviation} option using
	 * {@link #setOption} method.
	 */
	@Matlab
	@Deprecated
	public void setProposalStandardDeviation(double stdDev)
	{
		setOption(NormalProposalKernel.standardDeviation, stdDev);
	}
	
	/**
	 * @deprecated instead lookup {@link NormalProposalKernel#standardDeviation} option using
	 * {@link #getOptionOrDefault} method.
	 */
	@Matlab
	@Deprecated
	public double getProposalStandardDeviation()
	{
		return getOptionOrDefault(NormalProposalKernel.standardDeviation);
	}
	
	/**
	 * Current proposal kernel for variable.
	 * <p>
	 * May be null if {@link #initialize()} not yet invoked.
	 * @since 0.07
	 */
	public @Nullable IProposalKernel getProposalKernel()
	{
		return _proposalKernel;
	}
	
	/**
	 * @deprecated instead set appropriate proposal-specific options using {@link #setOption}.
	 */
	@Deprecated
	public final void setProposalKernelParameters(Object... parameters)
	{
		requireNonNull(_proposalKernel).setParameters(parameters);
	}
	
	// Override the default proposal kernel
	public final void setProposalKernel(@Nullable IProposalKernel proposalKernel)
	{
		_proposalKernel = proposalKernel;
		_explicitProposalKernel = proposalKernel != null;
	}
	
	public final void setProposalKernel(String proposalKernelName)
	{
		ParticleBPOptions.proposalKernel.convertAndSet(this, proposalKernelName);
	}

	// Sets the range of initial particle values
	// Overrides the domain (if one is specified) in determining the initial particle values
	public void setInitialParticleRange(double min, double max)
	{
		RealDomain domain = RealDomain.create(min, max);
		if (!domain.isSubsetOf(_domain))
		{
			throw new OptionValidationException("Bounds [%g,%g] are not within variable bounds [%g,%g]",
				min, max, _domain.getLowerBound(), _domain.getUpperBound());
		}
		ParticleBPOptions.initialParticleRange.set(this, min, max);
		_initialParticleDomain = domain;
	}

	public void setBeta(double beta)	// beta = 1/temperature
	{
		_beta = beta;
	}

	@Deprecated
	@Override
	public double getScore()
	{
		if (_guessWasSet)
			return super.getScore();
		else
			throw new DimpleException("This solver doesn't provide a default value. Must set guesses for all variables.");
	}


	public void remove(Factor factor)
	{
	}
	
	/*--------------------
	 * Deprecated methods
	 */
	
	@Deprecated
	@Override
	public Object getInputMsg(int portIndex)
	{
		return getSiblingEdgeState(portIndex).factorToVarMsg.representation();
	}

	@Deprecated
	@Override
	public Object getOutputMsg(int portIndex)
	{
		return getSiblingEdgeState(portIndex).varToFactorMsg.representation();
	}

	@Deprecated
	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		final DiscreteMessage message = getSiblingEdgeState(portIndex).factorToVarMsg;
		
		if (obj instanceof DiscreteMessage)
		{
			message.setFrom((DiscreteMessage)obj);
		}
		else
		{
			double[] target  = message.representation();
			System.arraycopy(obj, 0, target, 0, target.length);
		}
	}

	@SuppressWarnings("null")
	@Override
	public ParticleBPRealEdge getSiblingEdgeState(int siblingIndex)
	{
		return (ParticleBPRealEdge)getSiblingEdgeState_(siblingIndex);
	}
}
