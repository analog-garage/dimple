package com.analog.lyric.dimple.parameters;


import net.jcip.annotations.ThreadSafe;

/**
 * Abstract base class for {@link IParameterList} implementation with two parameters.
 */
@ThreadSafe
public abstract class ParameterList2<Key extends IParameterKey> extends SmallParameterListBase<Key>
{
	/*--------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	protected volatile SharedParameterValue _parameter0;
	protected volatile SharedParameterValue _parameter1;
	
	/*--------------
	 * Construction
	 */
	
	protected ParameterList2()
	{
		this(Double.NaN, Double.NaN);
	}
	
	protected ParameterList2(double value0, double value1)
	{
		super(false, false);
		_parameter0 = new SharedParameterValue(value0);
		_parameter1 = new SharedParameterValue(value1);
	}
	
	protected ParameterList2(SharedParameterValue value0, SharedParameterValue value1)
	{
		super(false, true);
		_parameter0 = value0;
		_parameter1 = value1;
	}
	
	protected ParameterList2(ParameterList2<Key> that)
	{
		super(that);
		_parameter0 = isShared(0) ? that._parameter0 : that._parameter0.clone();
		_parameter1 = isShared(1) ? that._parameter1 : that._parameter1.clone();
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public abstract ParameterList2<Key> clone();
	
	/*------------------------
	 * IParameterList methods
	 */
	
	@Override
	public final double get(int index)
	{
		switch (index)
		{
		case 0:
			return _parameter0.get();
		case 1:
			return _parameter1.get();
		default:
			throw indexOutOfRange(index);
		}
	}
	
	@Override
	public final SharedParameterValue getSharedValue(int index)
	{
		SharedParameterValue value = null;
		synchronized(this)
		{
			if (isShared(index))
			{
				value = index == 0 ? _parameter0 : _parameter1;
			}
		}
		return value;
	}
	
	@Override
	public void set(int index, double value)
	{
		assertNotFixed(index);
		switch (index)
		{
		case 0:
			_parameter0.set(value);
			break;
		case 1:
			_parameter1.set(value);
			break;
		}
		valueChanged(index);
	}

	@Override
	public final void setSharedValue(int index, SharedParameterValue value)
	{
		assertNotFixed(index);
		synchronized(this)
		{
			setShared(index, value != null);
			if (index == 0)
			{
				_parameter0 = value == null ? _parameter0.clone() : value;
			}
			else
			{
				_parameter1 = value == null ? _parameter1.clone() : value;
			}
		}
		valueChanged(index);
	}
	
	@Override
	public int size()
	{
		return 2;
	}
}
