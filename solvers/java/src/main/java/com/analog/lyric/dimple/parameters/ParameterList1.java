package com.analog.lyric.dimple.parameters;


import net.jcip.annotations.NotThreadSafe;

/**
 * Abstract base class for {@link IParameterList} implementation with a single parameter.
 */
@NotThreadSafe
public abstract class ParameterList1<Key extends IParameterKey> extends SmallParameterListBase<Key>
{
	private static final long serialVersionUID = 1L;
	
	protected double _parameter0;
	
	/*--------------
	 * Construction
	 */
	
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
	
	/*------------------------
	 * IParameterList methods
	 */
	
	@Override
	public final double get(int index)
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
		assertNotFixed(index);
		if (index == 0)
		{
			_parameter0 = value;
			valueChanged(index);
		}
	}

	@Override
	public final int size()
	{
		return 1;
	}
}
