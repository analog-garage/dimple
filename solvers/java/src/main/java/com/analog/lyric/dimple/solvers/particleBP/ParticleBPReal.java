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

package com.analog.lyric.dimple.solvers.particleBP;

import static java.util.Objects.*;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.values.RealValue;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SRealVariableBase;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.Proposal;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.options.OptionDoubleList;
import com.analog.lyric.options.OptionValidationException;

/**
 * Solver variable for Real variables under Particle BP solver.
 * 
 * @since 0.07
 */
public class ParticleBPReal extends SRealVariableBase
{
	protected Double[] _particleValues;
	protected int _numParticles = 1;
	protected int _resamplingUpdatesPerSample = 1;
	protected @Nullable IProposalKernel _proposalKernel;
	/**
	 * True if {@link #_proposalKernel} was set explicitly via {@link #setProposalKernel(IProposalKernel)}
	 * and should not be overridden by option settings.
	 */
	protected boolean _explicitProposalKernel;
	protected RealDomain _initialParticleDomain;
	protected @Nullable FactorFunction _input;
	protected RealDomain _domain;
	double[][] _inPortMsgs = new double[0][];
	double[][] _logInPortMsgs = new double[0][];
	ParticleBPSolverVariableToFactorMessage[] _outMsgArray = new ParticleBPSolverVariableToFactorMessage[0];
	double [] _logWeight;
	protected double _beta = 1;



	public ParticleBPReal(Variable var)
	{
		super(var);

		if (!(var.getDomain() instanceof RealDomain))
			throw new DimpleException("Expected real domain");

		// Since numParticles is used to configure the message sizes, we set this at construction
		// time to make it less likely that the messages will have to be recreated at initialize time.
		_numParticles = getOptionOrDefault(ParticleBPOptions.numParticles);
		_initialParticleDomain = _domain = (RealDomain)var.getDomain();
		_particleValues = new Double[_numParticles];
		_logWeight = new double[_numParticles];
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
		
		super.initialize();
	}
	
	@Override
	protected void doUpdateEdge(int outPortNum)
	{
		final double minLog = -100;
		int M = _numParticles;
		int D = _var.getSiblingCount();
		double maxLog = Double.NEGATIVE_INFINITY;


		double[] outMsgs = _outMsgArray[outPortNum].messageValues;

		final FactorFunction input = _input;
		
		for (int m = 0; m < M; m++)
		{
			double prior = 1;
			if (input != null)
				try {prior = input.eval(new Object[]{_particleValues[m]});} catch (Exception e) {e.printStackTrace(); System.exit(1);}
				double out = (prior == 0) ? minLog : Math.log(prior) * _beta;

				for (int d = 0; d < D; d++)
				{
					if (d != outPortNum)		// For all ports except the output port
					{
						double tmp = _inPortMsgs[d][m];
						out += (tmp == 0) ? minLog : Math.log(tmp);
					}
				}

				// Subtract the log weight
				out -= _logWeight[m];

				if (out > maxLog) maxLog = out;
				outMsgs[m] = out;
		}

		//create sum
		double sum = 0;
		for (int m = 0; m < M; m++)
		{
			double out = Math.exp(outMsgs[m] - maxLog);
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
		final double minLog = -100;
		int M = _numParticles;
		int D = _var.getSiblingCount();

		final FactorFunction input = _input;
		
		//Compute alphas
		double[] alphas = new double[M];
		for (int m = 0; m < M; m++)
		{
			double prior = 1;
			if (input != null)
				try {prior = input.eval(new Object[]{_particleValues[m]});} catch (Exception e) {e.printStackTrace(); System.exit(1);}
				double alpha = (prior == 0) ? minLog : Math.log(prior) * _beta;

				for (int d = 0; d < D; d++)
				{
					double tmp = _inPortMsgs[d][m];
					double logtmp = (tmp == 0) ? minLog : Math.log(tmp);
					_logInPortMsgs[d][m] = logtmp;
					alpha += logtmp;
				}
				alphas[m] = alpha;
		}

		//Now compute output messages for each outgoing edge
		for (int out_d = 0; out_d < D; out_d++ )
		{
			double[] outMsgs = _outMsgArray[out_d].messageValues;


			double maxLog = Double.NEGATIVE_INFINITY;

			//set outMsgs to alpha - mu_d,m
			//find max alpha
			double[] logInPortMsgsD = _logInPortMsgs[out_d];
			for (int m = 0; m < M; m++)
			{
				double out = alphas[m] - logInPortMsgsD[m];

				// Subtract the log weight
				out -= _logWeight[m];

				if (out > maxLog) maxLog = out;
				outMsgs[m] = out;
			}

			//create sum
			double sum = 0;
			for (int m = 0; m < M; m++)
			{
				double out = Math.exp(outMsgs[m] - maxLog);
				outMsgs[m] = out;
				sum += out;
			}

			//calculate message by dividing by sum
			for (int m = 0; m < M; m++)
			{
				outMsgs[m] /= sum;
			}

		}
	}

	public void resample()
	{
		int numPorts = _var.getSiblingCount();
		Domain varDomain = _var.getDomain();
		double _lowerBound = _domain.getLowerBound();
		double _upperBound = _domain.getUpperBound();
		int M = _numParticles;

		final FactorFunction input = _input;
		
		final IProposalKernel kernel = requireNonNull(_proposalKernel);

		// For each sample value
		for (int m = 0; m < M; m++)
		{
			double sampleValue = _particleValues[m];
			double potential = 0;
			double potentialProposed = 0;


			// Start with the potential for the current particle value
			if (input != null)
				try {
					potential = input.evalEnergy(new Object[]{sampleValue}) * _beta;
				}
				catch (Exception e)
				{
					e.printStackTrace(); System.exit(1);
				}

			for (int portIndex = 0; portIndex < numPorts; portIndex++)
			{
				Factor factorNode = _var.getSibling(portIndex);
				int factorPortNumber = 0;
				try {
					factorPortNumber = factorNode.getPortNum(_var);
				}
				catch (Exception e)
				{
					// FIXME: why is this here? Was this meant to be a temporary debugging hack?
					e.printStackTrace(); System.exit(1);
				}
				
				ParticleBPRealFactor factor = requireNonNull((ParticleBPRealFactor)(factorNode.getSolver()));
				potential += factor.getMarginalPotential(sampleValue, factorPortNumber);
			}


			// Now repeat resampling this sample
			for (int update = 0; update < _resamplingUpdatesPerSample; update++)
			{
				Proposal proposal = kernel.next(RealValue.create(sampleValue), varDomain);
				double proposalValue = proposal.value.getDouble();

				// If outside the bounds, then reject
				if (proposalValue < _lowerBound) continue;
				if (proposalValue > _upperBound) continue;

				// Sum up the potentials from the input and all connected factors
				potentialProposed = 0;
				if (input != null)
					try {potentialProposed = input.evalEnergy(new Object[]{proposalValue}) * _beta;} catch (Exception e) {e.printStackTrace(); System.exit(1);}

					for (int portIndex = 0; portIndex < numPorts; portIndex++)
					{
						Factor factorNode = requireNonNull(_var.getSibling(portIndex));
						int factorPortNumber = 0;
						try {factorPortNumber = factorNode.getPortNum(_var);} catch (Exception e) {e.printStackTrace(); System.exit(1);}
						ParticleBPRealFactor factor = (ParticleBPRealFactor)(factorNode.requireSolver("resample"));
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
						sampleValue = proposalValue;
						potential = potentialProposed;
					}
			}

			_particleValues[m] = sampleValue;	// Keep this sample
			_logWeight[m] = -potential;			// Sum-product code uses log(p) instead of -log(p)


			// Update the incoming messages for the new particle value
			for (int d = 0; d < numPorts; d++)
			{
				Factor factorNode = requireNonNull(_var.getSibling(d));
				int factorPortNumber = 0;
				try {factorPortNumber = factorNode.getPortNum(_var);} catch (Exception e) {e.printStackTrace(); System.exit(1);}
				ParticleBPRealFactor factor = (ParticleBPRealFactor)(factorNode.requireSolver("resample"));
				_inPortMsgs[d][m] = Math.exp(factor.getMarginalPotential(sampleValue, factorPortNumber));
			}


		}

		// Update the outgoing messages associated with the new particle locations
		doUpdate();

		// Indicate that the particles have been updated
		for (int iPort = 0; iPort < numPorts; iPort++)
			_outMsgArray[iPort].resamplingVersion++;
	}


	@Override
	public double[] getBelief()
	{
		final double minLog = -100;
		int M = _numParticles;
		int D = _var.getSiblingCount();
		double maxLog = Double.NEGATIVE_INFINITY;

		final FactorFunction input = _input;
		double[] outBelief = new double[M];
		
		for (int m = 0; m < M; m++)
		{
			double prior = 1;
			if (input != null)
				prior = input.eval(new Object[]{_particleValues[m]});
			double out = (prior == 0) ? minLog : Math.log(prior) * _beta;

			for (int d = 0; d < D; d++)
			{
				double tmp = _inPortMsgs[d][m];
				out += (tmp == 0) ? minLog : Math.log(tmp);
			}

			//	        // Subtract the log weight
			//	        out -= _logWeight[m];

			if (out > maxLog) maxLog = out;
			outBelief[m] = out;
		}

		//create sum
		double sum = 0;
		for (int m = 0; m < M; m++)
		{
			double out = Math.exp(outBelief[m] - maxLog);
			outBelief[m] = out;
			sum += out;
		}

		//calculate belief by dividing by sum
		for (int m = 0; m < M; m++)
			outBelief[m] /= sum;

		return outBelief;
	}

	// Alternative belief, returned for a specified set of variable values
	public double [] getBelief(double[] valueSet)
	{
		final double minLog = -100;
		int M = valueSet.length;
		int D = _var.getSiblingCount();
		double maxLog = Double.NEGATIVE_INFINITY;

		final FactorFunction input = _input;
		double[] outBelief = new double[M];

		for (int m = 0; m < M; m++)
		{
			double value = valueSet[m];
			double prior = 1;
			if (input != null)
				prior = input.eval(new Object[]{value});
			double out = (prior == 0) ? minLog : Math.log(prior) * _beta;

			for (int d = 0; d < D; d++)
			{
				FactorBase factorNode = _var.getSibling(d);
				int factorPortNumber = factorNode.getPortNum(_var);
				ParticleBPRealFactor factor = requireNonNull((ParticleBPRealFactor)(factorNode.getSolver()));
				out -= factor.getMarginalPotential(value, factorPortNumber);	// Potential is -log(p)
			}

			if (out > maxLog) maxLog = out;
			outBelief[m] = out;
		}

		//create sum
		double sum = 0;
		for (int m = 0; m < M; m++)
		{
			double out = Math.exp(outBelief[m] - maxLog);
			outBelief[m] = out;
			sum += out;
		}

		//calculate belief by dividing by sum
		for (int m = 0; m < M; m++)
			outBelief[m] /= sum;

		return outBelief;
	}

	public double[] getParticleValues()
	{
		double[] particles = new double[_numParticles];
		for (int i = 0; i < _numParticles; i++) particles[i] = _particleValues[i];
		return particles;
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
			_numParticles = numParticles;
			for (Factor factor : _var.getFactors())
				factor.requireSolver("setNumParticles").createMessages();
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
	@Deprecated
	public void setProposalStandardDeviation(double stdDev)
	{
		setOption(NormalProposalKernel.standardDeviation, stdDev);
	}
	
	/**
	 * @deprecated instead lookup {@link NormalProposalKernel#standardDeviation} option using
	 * {@link #getOptionOrDefault} method.
	 */
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

	@Override
	public void setInputOrFixedValue(@Nullable Object input, @Nullable Object fixedValue, boolean hasFixedValue)
	{
		if (input == null)
			_input = null;
		else
			_input = (FactorFunction)input;
	}

	@Override
	public double getScore()
	{
		if (_guessWasSet)
			return Objects.requireNonNull(_input).evalEnergy(_guessValue);
		else
			throw new DimpleException("This solver doesn't provide a default value. Must set guesses for all variables.");
	}


	public void remove(Factor factor)
	{
	}
	
	@Override
	public Object[] createMessages(ISolverFactor factor)
	{
		_particleValues = new Double[_numParticles];
		Arrays.fill(_particleValues, 0.0);
		_logWeight = new double[_numParticles];

		int portNum = _var.getPortNum(Objects.requireNonNull(factor.getModelObject()));
		int numPorts = Math.max(_inPortMsgs.length, portNum+1);
		
		_inPortMsgs = Arrays.copyOf(_inPortMsgs,numPorts);
		_logInPortMsgs = Arrays.copyOf(_logInPortMsgs,numPorts);
		_outMsgArray = Arrays.copyOf(_outMsgArray,numPorts);
		

		_inPortMsgs[portNum] = getDefaultFactorToVariableMessage();
		_logInPortMsgs[portNum] = new double[_numParticles];

			// Overwrite the output message so that it can be created in the correct form
		_outMsgArray[portNum] = getDefaultVariableToFactorMessage();
	
		return new Object [] {_inPortMsgs[portNum],_outMsgArray[portNum]};
	}


	@Override
	public void resetEdgeMessages(int portNum)
	{
		_inPortMsgs[portNum] = (double[])resetInputMessage(_inPortMsgs[portNum]);
		_outMsgArray[portNum] = (ParticleBPSolverVariableToFactorMessage)resetOutputMessage(_outMsgArray[portNum]);
	}

	
	public ParticleBPSolverVariableToFactorMessage getDefaultVariableToFactorMessage()
	{
		ParticleBPSolverVariableToFactorMessage message = new ParticleBPSolverVariableToFactorMessage(_numParticles);
		return (ParticleBPSolverVariableToFactorMessage)resetOutputMessage(message);
	}

	public double [] getDefaultFactorToVariableMessage()
	{
		double[] retVal = new double[_numParticles];
		retVal = (double[])resetInputMessage(retVal);
		return retVal;
	}

	@Override
	public Object resetInputMessage(Object message)
	{
		double[] tmp = (double[])message;
		double val = 1.0/tmp.length;
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = val;

		return tmp;
	}

	@Override
	public Object resetOutputMessage(Object message)
	{
		
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
    		_particleValues[i] = particleValue;
    		particleValue += particleIncrement;
    	}

		ParticleBPSolverVariableToFactorMessage m = (ParticleBPSolverVariableToFactorMessage)message;
		
		m.particleValues = _particleValues;
		
		return m;
	}

	
	@Override
	public Object getInputMsg(int portIndex)
	{
		return _inPortMsgs[portIndex];
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outMsgArray[portIndex];
	}

	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		_inPortMsgs[portIndex] = (double[])obj;
		
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum)
	{
		throw new DimpleException("not supported");
		
	}

}
