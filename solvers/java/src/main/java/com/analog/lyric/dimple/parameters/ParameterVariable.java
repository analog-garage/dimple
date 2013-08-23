package com.analog.lyric.dimple.parameters;

import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;

/**
 * A {@link Real} variable that implements a parameter of a parametric factor
 * function.
 */
public abstract class ParameterVariable extends Real
{
	/*--------------
	 * Construction
	 */
	
	protected ParameterVariable(RealDomain domain)
	{
		super(domain);
	}
	
	/*---------------------------
	 * ParameterVariable methods
	 */
	
	public abstract int getParameterIndex();
	
	public abstract IParameterKey getParameterKey();
	
	/*-----------------
	 * Implementations
	 */
	
	public static class WithKey extends ParameterVariable
	{
		private final IParameterKey _key;
		
		public WithKey(IParameterKey key)
		{
			super(key.domain());
			_key = key;
		}
		
		@Override
		public final int getParameterIndex()
		{
			return _key.ordinal();
		}
		
		@Override
		public final IParameterKey getParameterKey()
		{
			return _key;
		}
	}
	
	public static class WithIndex extends ParameterVariable
	{
		private final int _index;
		
		public WithIndex(int index)
		{
			super(RealDomain.unbounded());
			_index = index;
		}
		
		public WithIndex(int index, RealDomain domain)
		{
			super(domain);
			_index = index;
		}
		
		@Override
		public final int getParameterIndex()
		{
			return _index;
		}
		
		@Override
		public final IParameterKey getParameterKey()
		{
			return null;
		}
	}
}
