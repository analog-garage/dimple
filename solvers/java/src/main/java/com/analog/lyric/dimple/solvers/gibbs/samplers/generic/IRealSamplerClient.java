package com.analog.lyric.dimple.solvers.gibbs.samplers.generic;





public interface IRealSamplerClient extends ISamplerClient
{
	public double getSampleScore(double sampleValue);
	public void setNextSampleValue(double sampleValue);
}
