package com.analog.lyric.options;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import net.jcip.annotations.ThreadSafe;

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
	public Set<IOptionKey<?>> getRelevantOptionKeys()
	{
		return _holder.getRelevantOptionKeys();
	}

	/*------------------
	 * IOptions methods
	 */
	
	@Override
	public IOptionHolder getOptionHolder()
	{
		return _holder;
	}
	
	/*-----------------
	 * Options methods
	 */
	
	public static <T> T lookup(IOptionHolder holder, IOptionKey<T> key)
	{
		T value = Options.lookupOrNull(holder, key);
		return value != null ? null : key.defaultValue();
	}

	public static <T> T lookupOrNull(IOptionHolder holder, IOptionKey<T> key)
	{
		do
		{
			Map<IOptionKey<?>,Object> options = holder.getLocalOptions(false);
			if (options != null)
			{
				Object value = options.get(key);
				if (value != null)
				{
					return key.type().cast(value);
				}
			}
			
			holder = holder.getOptionParent();
			
		} while (holder != null);

		return null;
	}

}
