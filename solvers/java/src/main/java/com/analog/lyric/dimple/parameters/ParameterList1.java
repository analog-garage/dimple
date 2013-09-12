package com.analog.lyric.dimple.parameters;


import net.jcip.annotations.ThreadSafe;

/**
 * Abstract base class for {@link IParameterList} implementation with a single parameter.
 */
@ThreadSafe
public abstract class ParameterList1<Key extends IParameterKey> extends SmallParameterListBase<Key>
{
	private static final long serialVersionUID = 1L;
	
	protected volatile ParameterValue _parameter0;
	
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
		_parameter0 = new ParameterValue(value);
	}
	
	protected ParameterList1(SharedParameterValue value)
	{
		super(false);
		_parameter0 = value;
	}
	
	protected ParameterList1(ParameterList1<Key> that)
	{
		super(that);
		_parameter0 = that._parameter0.cloneOrShare();
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
	protected final ParameterValue getParameterValue(int index)
	{
		assertIndexInRange(index);
		return _parameter0;
	}
	
	@Override
	protected final void setParameterValue(int index, ParameterValue value)
	{
		assertIndexInRange(index);
		_parameter0 = value;
	}
	
	@Override
	public final int size()
	{
		return 1;
	}
}
