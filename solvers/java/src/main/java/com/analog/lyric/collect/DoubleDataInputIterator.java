package com.analog.lyric.collect;

import java.io.DataInput;
import java.io.IOException;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * {@link PrimitiveIterator.OfDouble} implementation based on an underlying
 * {@link DataInput} object.
 * <p>
 * May not be thread safe if the underlying {@link DataInput} object is not.
 */
@ThreadSafe
public class DoubleDataInputIterator extends AbstractPrimitiveDataInputIterator<Double>
	implements PrimitiveIterator.OfDouble
{
	/*-------
	 * State
	 */
	
	@GuardedBy("this")
	private double _value;
	
	/*--------------
	 * Construction
	 */
	
	public DoubleDataInputIterator(DataInput input)
	{
		super(input);
	}

	/*---------------------------
	 * PrimitiveIterator methods
	 */
	
	@Override
	public final Double next()
	{
		return nextDouble();
	}

	@Override
	public synchronized double nextDouble()
	{
		assertValueWasRead();
		return _value;
	}

	@Override
	protected boolean readNext() throws IOException
	{
		_value = _input.readDouble();
		return true;
	}
}
