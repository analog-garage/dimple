package com.analog.lyric.dimple.parameters;


import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public abstract class ParameterList1<Key extends IParameterKey> extends SmallParameterListBase<Key>
{
	private static final long serialVersionUID = 1L;
	
	protected double _parameter0;
	
	protected ParameterList1()
	{
		this(Double.NaN);
	}
	
	protected ParameterList1(double value)
	{
		super(false);
		_parameter0 = value;
	}
	
	protected ParameterList1(ParameterList1<Key> that)
	{
		super(that);
		_parameter0 = that._parameter0;
	}
	
	@Override
	public abstract ParameterList1<Key> clone();
	
	@Override
	public double get(int index)
	{
		if (index == 0)
		{
			return _parameter0;
		}
		else
		{
			throw indexOutOfRange(index);
		}
	}
	
	@Override
	public void set(int index, double value)
	{
		if (index == 0)
		{
			_parameter0 = value;
		}
		else
		{
			throw indexOutOfRange(index);
		}
	}

	@Override
	public int size()
	{
		return 1;
	}
}
