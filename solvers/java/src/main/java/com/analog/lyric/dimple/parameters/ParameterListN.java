package com.analog.lyric.dimple.parameters;

import java.util.Arrays;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public abstract class ParameterListN<Key extends IParameterKey>
	extends AbstractParameterList<Key>
{
	private static final long serialVersionUID = 1L;
	
	protected final double[] _values;
	protected final byte[] _fixedMask;
	
	/*--------------
	 * Construction
	 */
	
	protected ParameterListN(ParameterListN<Key> that)
	{
		_values = that._values.clone();
		_fixedMask = that._fixedMask.clone();
	}
	
	protected ParameterListN(boolean fixed, double ... values)
	{
		_values = values;
		_fixedMask = new byte[(values.length+7)/8];
		if (fixed)
		{
			Arrays.fill(_fixedMask, (byte)-1);
		}
	}
	
	protected ParameterListN(int size, double defaultValue)
	{
		this(makeArray(size, defaultValue));
	}
	
	private static double[] makeArray(int size, double value)
	{
		double[] values = new double[size];
		if (value != 0.0)
		{
			Arrays.fill(values, value);
		}
		return values;
	}
	
	protected ParameterListN(int size)
	{
		this(size, Double.NaN);
	}
	
	protected ParameterListN(double ... values)
	{
		this(false, values);
	}
	
	@Override
	public abstract ParameterListN<Key> clone();
	
	/*------------------------------
	 * IFactorParameterList methods
	 */
	
	@Override
	public final double get(int index)
	{
		return _values[index];
	}

	@Override
	public final boolean isFixed(int index)
	{
		int bit = 1 << (index & 7);
		int byteIndex = index >>> 3;
		
		return (_fixedMask[byteIndex] & bit) != 0;
	}
	
	@Override
	public void set(int index, double value)
	{
		_values[index] = value;
	}

	@Override
	public final void setFixed(int index, boolean fixed)
	{
		int bit = 1 << (index & 7);
		int byteIndex = index >>> 3;
		
		if (fixed)
		{
			_fixedMask[byteIndex] |= bit;
		}
		else
		{
			_fixedMask[byteIndex] &= ~bit;
		}
	}
	
	@Override
	public final int size()
	{
		return _values.length;
	}

}
