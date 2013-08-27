package com.analog.lyric.dimple.solvers.gibbs.samplers;

public class GammaParameters
{
	private double _alpha = 1;
	private double _beta = 1;
	
	public GammaParameters() {}
	public GammaParameters(double alpha, double beta)
	{
		_alpha = alpha;
		_beta = beta;
	}
	
	public final double getAlpha() {return _alpha;}
	public final double getBeta() {return _beta;}

	public final void setAlpha(double alpha) {_alpha = alpha;}
	public final void setBeta(double beta) {_beta = beta;}
}
