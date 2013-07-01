package com.analog.lyric.dimple.parameters;


import net.jcip.annotations.ThreadSafe;

/**
 * Abstract base class for {@link IParameterList} implementation with a single parameter.
 */
@ThreadSafe
public abstract class ParameterList1<Key extends IParameterKey> extends SmallParameterListBase<Key>
{
	private static final long serialVersionUID = 1L;
	
	protected volatile SharedParameterValue _parameter0;
	
	/*--------------
	 * Construction
	 */
	
	protected ParameterList1()
	{
		this(Double.NaN);
	}
	
	protected ParameterList1(double value)
	{
		super(false, false);
		_parameter0 = new SharedParameterValue(value);
	}
	
	protected ParameterList1(SharedParameterValue value)
	{
		super(false, true);
		_parameter0 = value;
	}
	
	protected ParameterList1(ParameterList1<Key> that)
	{
		super(that);
		_parameter0 = isShared(0) ? that._parameter0 : new SharedParameterValue(that.get(0));
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public abstract ParameterList1<Key> clone();
	
	/*------------------------
	 * IParameterList methods
	 */
	
	@Override
	public final double get(int index)
	{
		if (index == 0)
		{
			return _parameter0.get();
		}
		else
		{
			throw indexOutOfRange(index);
		}
	}
	
	@Override
	public final SharedParameterValue getSharedValue(int index)
	{
		synchronized(this)
		{
			return isShared(index) ? _parameter0 : null;
		}
	}
	
	@Override
	public void set(int index, double value)
	{
		assertNotFixed(index);
		_parameter0.set(value);
		valueChanged(index);
	}

	@Override
	public final void setSharedValue(int index, SharedParameterValue value)
	{
		assertNotFixed(index);
		synchronized(this)
		{
			setShared(index, value != null);
			_parameter0 = value == null ? _parameter0.clone() : value;
		}
		valueChanged(index);
	}
	
	@Override
	public final int size()
	{
		return 1;
	}
}
