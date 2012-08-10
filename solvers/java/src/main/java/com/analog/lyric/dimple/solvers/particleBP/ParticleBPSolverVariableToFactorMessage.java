package com.analog.lyric.dimple.solvers.particleBP;

public class ParticleBPSolverVariableToFactorMessage
{
	public int length;
	public int resamplingVersion;
	public Double[] particleValues;
	public double[] messageValues;
	
	public ParticleBPSolverVariableToFactorMessage(int numParticles) {this(numParticles, 0, 0);}
	public ParticleBPSolverVariableToFactorMessage(int numParticles, double initialParticleMin, double initialParticleMax)
	{
		length = numParticles;
		resamplingVersion = 0;
		particleValues = new Double[numParticles];
		messageValues = new double[numParticles];
    	double initialMessageValue = 1.0/numParticles;
    	double particleIncrement = (numParticles > 1) ? (initialParticleMax - initialParticleMin) / (numParticles - 1) : 0;
    	double particleValue = initialParticleMin;
    	for (int i = 0; i < numParticles; i++)
    	{
    		particleValues[i] = particleValue;
    		messageValues[i] = initialMessageValue;
    		particleValue += particleIncrement;
    	}
	}
}
