package com.analog.lyric.dimple.parameters;

public abstract class SmallParameterListBase<Key extends IParameterKey> extends AbstractParameterList<Key>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	protected byte _fixedMask;

	/*--------------
	 * Construction
	 */
	
	protected SmallParameterListBase(boolean fixed)
	{
		_fixedMask = fixed ? (byte)-1 : (byte)0 ;
	}

	protected SmallParameterListBase(SmallParameterListBase<Key> that)
	{
		_fixedMask = that._fixedMask;
	}
	
	/*------------------------
	 * IParameterList methods
	 */
	
	@Override
	public final boolean isFixed(int index)
	{
		assertIndexInRange(index);
		int bit = 1 << index;
		return (_fixedMask & bit) != 0;
	}

	@Override
	public void setFixed(int index, boolean fixed)
	{
		assertIndexInRange(index);
		int bit = 1 << index;
		if (fixed)
		{
			_fixedMask |= bit;
		}
		else
		{
			_fixedMask &= ~bit;
		}
	}

}