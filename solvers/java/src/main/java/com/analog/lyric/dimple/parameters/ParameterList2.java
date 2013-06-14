package com.analog.lyric.dimple.parameters;


import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public abstract class ParameterList2<Key extends IParameterKey> extends SmallParameterListBase<Key>
{
	private static final long serialVersionUID = 1L;
	
	protected double _parameter0;
	protected double _parameter1;
	
	protected ParameterList2(double value0, double value1)
	{
		super(false);
		_parameter0 = value0;
		_parameter1 = value1;
	}
	
	protected ParameterList2(ParameterList2<Key> that)
	{
		super(that);
		_parameter0 = that._parameter0;
		_parameter1 = that._parameter1;
		_fixedMask = that._fixedMask;
	}
	
	@Override
	public abstract ParameterList2<Key> clone();
	
	@Override
	public final double get(int index)
	{
		switch (index)
		{
		case 0:
			return _parameter0;
		case 1:
			return _parameter1;
		default:
			throw indexOutOfRange(index);
		}
	}
	
	@Override
	public void set(int index, double value)
	{
		switch (index)
		{
		case 0:
			_parameter0 = value;
			break;
		case 1:
			_parameter1 = value;
			break;
		default:
			throw indexOutOfRange(index);
		}
	}

	@Override
	public int size()
	{
		return 2;
	}
}
