package com.analog.lyric.dimple.FactorFunctions.core;

public abstract class DeterministicRealUnaryFactorFunction extends FactorFunction
{
	/**
	 * Deterministic unary factor function. This is a deterministic directed factor
	 * (if smoothing is not enabled).
	 * 
	 * Optional smoothing may be applied, by providing a smoothing value in
	 * the constructor. If smoothing is enabled, the distribution is
	 * smoothed by exp(-difference^2/smoothing), where difference is the
	 * distance between the output value and the deterministic output value
	 * for the corresponding inputs.
	 * 
	 * The variables are ordered as follows in the argument list:
	 * 
	 * 1) Output
	 * 2) Input
	 * 
	 */
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	public DeterministicRealUnaryFactorFunction() {this(0);}
	public DeterministicRealUnaryFactorFunction(double smoothing)
	{
		super();
		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
		}
	}
	
	// Abstract method--override this
	protected abstract double myFunction(double in);

	@Override
	public double evalEnergy(Object ... arguments)
	{
		double out = FactorFunctionUtilities.toDouble(arguments[0]);
		double in = FactorFunctionUtilities.toDouble(arguments[1]);
		double value = myFunction(in);

		if (_smoothingSpecified)
		{
			double diff = value - out;
			double potential = diff*diff;
			return potential*_beta;
		}
		else
		{
			return (value == out) ? 0 : Double.POSITIVE_INFINITY;
		}
	}


	@Override
	public final boolean isDirected()	{return true;}
	@Override
	public final int[] getDirectedToIndices() {return new int[]{0};}
	@Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
	@Override
	public final void evalDeterministicFunction(Object... arguments)
	{
		arguments[0] = myFunction(FactorFunctionUtilities.toDouble(arguments[1]));		// Replace the output value
	}

}
