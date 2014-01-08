package com.analog.lyric.dimple.solvers.gibbs.samplers.generic;




public interface IDiscreteSamplerClient extends ISamplerClient
{
	public double getSampleScore(int sampleIndex);
	public void setNextSampleIndex(int sampleIndex);
}
