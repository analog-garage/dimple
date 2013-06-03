package com.analog.lyric.options;

import java.util.concurrent.ConcurrentMap;

import net.jcip.annotations.ThreadSafe;

import com.google.common.collect.ImmutableList;

@ThreadSafe
public class Options extends AbstractOptions
{
	private final IOptionHolder _holder;
	
	/*--------------
	 * Construction
	 */
	
	public Options(IOptionHolder holder)
	{
		_holder = holder;
	}
	
	/*--------------------
	 * IOptionHolder methods
	 */
	
	@Override
	public void clearLocalOptions()
	{
		_holder.clearLocalOptions();
	}

	@Override
	public ConcurrentMap<IOptionKey<?>, Object> getLocalOptions(boolean create)
	{
		return _holder.getLocalOptions(create);
	}
	
	@Override
	public IOptionHolder getOptionParent()
	{
		return _holder.getOptionParent();
	}

	@Override
	public ImmutableList<IOptionKey<?>> getRelevantOptionKeys()
	{
		return _holder.getRelevantOptionKeys();
	}

	/*--------------------
	 * IOptionMap methods
	 */
	
	@Override
	public IOptionHolder getOptionHolder()
	{
		return _holder;
	}
}
