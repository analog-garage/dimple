package com.analog.lyric.dimple.solvers.gibbs.samplers;

public class NormalParameters
{
	private double _mean = 1;
	private double _precision = 1;
	
	public NormalParameters() {}
	public NormalParameters(double mean, double precision)
	{
		_mean = mean;
		_precision = precision;
	}
	
	public final double getMean() {return _mean;}
	public final double getPrecision() {return _precision;}
	public final double getVariance() {return 1/_precision;}
	public final double getStandardDeviation() {return 1/Math.sqrt(_precision);}
	
	public final void setMean(double mean) {_mean = mean;}
	public final void setPrecision(double precision) {_precision = precision;}
	public final void setVariance(double variance) {_precision = 1/variance;}
	public final void setStandardDeviation(double standardDeviation) {_precision = 1/(standardDeviation*standardDeviation);}

}
