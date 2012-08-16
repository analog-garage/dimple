/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

import java.util.ArrayList;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SVariableBase;

public class SRealVariable extends SVariableBase
{
	protected Double[] _particleValues;
	protected int _numParticles = 1;
	protected int _resamplingUpdatesPerSample = 1;
	protected double _resamplingProposalStdDev = 1;
	protected double _initialParticleMin = 0;
	protected double _initialParticleMax = 0;
	protected boolean _initialParticleRangeSet = false;
	protected FactorFunction _input;
	protected RealDomain _domain;
	double[][] _inPortMsgs;
	double[][] _logInPortMsgs;
	ParticleBPSolverVariableToFactorMessage[] _outMsgArray;
	double [] _logWeight;
	boolean _initCalled = true;
	protected double _beta = 1;



	public SRealVariable(VariableBase var)  
	{
		super(var);

		if (!(var.getDomain() instanceof RealDomain))
			throw new DimpleException("Expected real domain");

		_domain = (RealDomain)var.getDomain();

		initialize();
	}

	// Default factor-to-variable (input) message
	public Object getDefaultMessage(Port port) {return getDefaultFactorToVariableMessage(port);}
	public Object getDefaultFactorToVariableMessage(Port port)
	{
		double[] retVal = new double[_numParticles];
		double val = 1.0/retVal.length;
		for (int i = 0; i < retVal.length; i++)
			retVal[i] = val;
		return retVal;
	}

	// Default variable-to-factor (output) message (this function is used by the factor)
	public Object getDefaultVariableToFactorMessage(Port port)
	{
		ParticleBPSolverVariableToFactorMessage message = new ParticleBPSolverVariableToFactorMessage(_numParticles);
		message.particleValues = _particleValues;		// All the output particle values are the same
		return message;
	}

	public void updateEdge(int outPortNum)
	{
		final double minLog = -100;
		int M = _numParticles;
		int D = _var.getPorts().size();
		double maxLog = Double.NEGATIVE_INFINITY;

		for (int d = 0; d < D; d++) 
			_inPortMsgs[d] = (double[])_var.getPorts().get(d).getInputMsg();

		//        double[] outMsgs = _outMsgArray[outPortNum].messageValues;
		double[] outMsgs = ((ParticleBPSolverVariableToFactorMessage)(_var.getPorts().get(outPortNum).getOutputMsg())).messageValues;

		for (int m = 0; m < M; m++)
		{
			double prior = 1;
			if (_input != null)
				try {prior = _input.eval(new Object[]{_particleValues[m]});} catch (Exception e) {e.printStackTrace(); System.exit(1);}
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

	public void update()
	{
		final double minLog = -100;
		int M = _numParticles;
		int D = _var.getPorts().size();

		for (int d = 0; d < D; d++) 
		{
			_inPortMsgs[d] = (double[])_var.getPorts().get(d).getInputMsg();
			_outMsgArray[d] = (ParticleBPSolverVariableToFactorMessage)(_var.getPorts().get(d).getOutputMsg());
		}

		//Compute alphas
		double[] alphas = new double[M];
		for (int m = 0; m < M; m++)
		{
			double prior = 1;
			if (_input != null)
				try {prior = _input.eval(new Object[]{_particleValues[m]});} catch (Exception e) {e.printStackTrace(); System.exit(1);}
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
		int numPorts = _var.getPorts().size();
		double _lowerBound = _domain.getLowerBound();
		double _upperBound = _domain.getUpperBound();
		int M = _numParticles;


		// For each sample value
		for (int m = 0; m < M; m++)
		{
			double sampleValue = _particleValues[m];
			double potential = 0;
			double potentialProposed = 0;


			// Start with the potential for the current particle value
			if (_input != null)
				try {
					potential = -Math.log(_input.eval(new Object[]{sampleValue})) * _beta;
				} 
				catch (Exception e) 
				{
					e.printStackTrace(); System.exit(1);
				}

			for (int portIndex = 0; portIndex < numPorts; portIndex++)
			{
				INode factorNode = _var.getPorts().get(portIndex).getConnectedNode();
				int factorPortNumber = 0;
				try {
					factorPortNumber = factorNode.getPortNum(_var);
					} 
				catch (Exception e) 
				{
					e.printStackTrace(); System.exit(1);
				}
				
				SRealFactor factor = (SRealFactor)(_var.getPorts().get(portIndex).getConnectedNode().getSolver());
				potential += factor.getMarginalPotential(sampleValue, factorPortNumber);
			}


			// Now repeat resampling this sample
			for (int update = 0; update < _resamplingUpdatesPerSample; update++)
			{
				double proposalDelta = _resamplingProposalStdDev * ParticleBPSolverRandomGenerator.rand.nextGaussian();
				double proposalValue = sampleValue + proposalDelta;

				// If outside the bounds, then reject
				if (proposalValue < _lowerBound) continue;
				if (proposalValue > _upperBound) continue;

				// Sum up the potentials from the input and all connected factors
				potentialProposed = 0;
				if (_input != null)
					try {potentialProposed = -Math.log(_input.eval(new Object[]{proposalValue})) * _beta;} catch (Exception e) {e.printStackTrace(); System.exit(1);}

					for (int portIndex = 0; portIndex < numPorts; portIndex++)
					{
						INode factorNode = _var.getPorts().get(portIndex).getConnectedNode();
						int factorPortNumber = 0;
						try {factorPortNumber = factorNode.getPortNum(_var);} catch (Exception e) {e.printStackTrace(); System.exit(1);}
						SRealFactor factor = (SRealFactor)(_var.getPorts().get(portIndex).getConnectedNode().getSolver());
						potentialProposed += factor.getMarginalPotential(proposalValue, factorPortNumber);
					}


					// Accept or reject
					double rejectionThreshold = Math.exp(potential - potentialProposed);	// Note, no Hastings factor if Gaussian proposal distribution
					if (ParticleBPSolverRandomGenerator.rand.nextDouble() < rejectionThreshold)
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
				INode factorNode = _var.getPorts().get(d).getConnectedNode();
				int factorPortNumber = 0;
				try {factorPortNumber = factorNode.getPortNum(_var);} catch (Exception e) {e.printStackTrace(); System.exit(1);}
				SRealFactor factor = (SRealFactor)(_var.getPorts().get(d).getConnectedNode().getSolver());
				_inPortMsgs[d][m] = Math.exp(factor.getMarginalPotential(sampleValue, factorPortNumber));
			}


		}

		// Update the outgoing messages associated with the new particle locations
		update();

		// Indicate that the particles have been updated
		for (int iPort = 0; iPort < numPorts; iPort++)
			_outMsgArray[iPort].resamplingVersion++;
	}


	public Object getBelief() 
	{
		final double minLog = -100;
		int M = _numParticles;
		int D = _var.getPorts().size();
		double maxLog = Double.NEGATIVE_INFINITY;

		for (int d = 0; d < D; d++) 
			_inPortMsgs[d] = (double[])_var.getPorts().get(d).getInputMsg();

		double[] outBelief = new double[M];

		for (int m = 0; m < M; m++)
		{
			double prior = 1;
			if (_input != null)
				prior = _input.eval(new Object[]{_particleValues[m]});
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
		int D = _var.getPorts().size();
		double maxLog = Double.NEGATIVE_INFINITY;

		for (int d = 0; d < D; d++) 
			_inPortMsgs[d] = (double[])_var.getPorts().get(d).getInputMsg();

		double[] outBelief = new double[M];

		for (int m = 0; m < M; m++)
		{
			double value = valueSet[m];
			double prior = 1;
			if (_input != null)
				prior = _input.eval(new Object[]{value});
			double out = (prior == 0) ? minLog : Math.log(prior) * _beta;

			for (int d = 0; d < D; d++)
			{
				INode factorNode = _var.getPorts().get(d).getConnectedNode();
				int factorPortNumber = factorNode.getPortNum(_var);
				SRealFactor factor = (SRealFactor)(_var.getPorts().get(d).getConnectedNode().getSolver());
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
		_numParticles = numParticles;

		// Re-initialize
		initialize();
	}
	public int getNumParticles() {return _numParticles;}

	public void setResamplingUpdatesPerParticle(int updatesPerParticle) {_resamplingUpdatesPerSample = updatesPerParticle;}
	public int getResamplingUpdatesPerParticle() {return _resamplingUpdatesPerSample;}

	public void setProposalStandardDeviation(double stdDev) {_resamplingProposalStdDev = stdDev;}
	public double getProposalStandardDeviation() {return _resamplingProposalStdDev;}

	// Sets the range of initial particle values
	// Overrides the domain (if one is specified) in determining the initial particle values
	public void setInitialParticleRange(double min, double max)
	{
		_initialParticleRangeSet = true;
		_initialParticleMin = min;
		_initialParticleMax = max;
	}

	public void setBeta(double beta)	// beta = 1/temperature
	{
		_beta = beta;
	}

	public void setInput(Object input) 
	{
		_input = (FactorFunction)input;
	}


	public void initialize()
	{
		_initCalled = true;

		_particleValues = new Double[_numParticles];
		_logWeight = new double[_numParticles];
		double particleMin = 0;
		double particleMax = 0;
		if (_initialParticleRangeSet)
		{
			particleMin = _initialParticleMin;
			particleMax = _initialParticleMax;
		}
		else if (_var.getDomain() != null)
		{
			if (!Double.isInfinite(_domain.getLowerBound()) && !Double.isInfinite(_domain.getUpperBound()))
			{
				particleMin = _domain.getLowerBound();
				particleMax = _domain.getUpperBound();
			}
		}
		double particleIncrement = (_numParticles > 1) ? (particleMax - particleMin) / (_numParticles - 1) : 0;
		double particleValue = particleMin;
		for (int i = 0; i < _numParticles; i++)
		{
			_logWeight[i] = 0;
			_particleValues[i] = particleValue;
			particleValue += particleIncrement;
		}

		int numPorts = _var.getPorts().size();
		_inPortMsgs = new double[numPorts][];
		_logInPortMsgs = new double[numPorts][];
		_outMsgArray = new ParticleBPSolverVariableToFactorMessage[numPorts];
		ArrayList<Port> ports = _var.getPorts();
		for (int iPort = 0; iPort < numPorts; iPort++)
		{
			Port port = ports.get(iPort);
			_inPortMsgs[iPort] = (double[])port.getInputMsg();
			_logInPortMsgs[iPort] = new double[_numParticles];

			// Overwrite the output message so that it can be created in the correct form
			ParticleBPSolverVariableToFactorMessage outMessage = new ParticleBPSolverVariableToFactorMessage(_numParticles, particleMin, particleMax);
			outMessage.particleValues = _particleValues;		// All the output particle values are the same, so overwrite the particle array
			_outMsgArray[iPort] = outMessage;
			port.setOutputMsg(outMessage);
		}

	}

	public void remove(Factor factor)
	{
		_initCalled = true;
	}

	public double getEnergy()  
	{
		throw new DimpleException("getEnergy not yet supported for ParticleBP");
	}


	public Object getGuess() 
	{
		throw new DimpleException("get and set guess not supported for this solver");
	}

	public void setGuess(Object guess) 
	{
		throw new DimpleException("get and set guess not supported for this solver");
	}

}
