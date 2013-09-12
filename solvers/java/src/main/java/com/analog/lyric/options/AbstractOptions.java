package com.analog.lyric.options;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

// FIXME: make thread safe
public abstract class AbstractOptions implements IOptions
{
	/*-----------------------
	 * IOptionHolder methods
	 */
	
	@Override
	public IOptions options()
	{
		return this;
	}

	/*-------------
	 * Map methods
	 */
	
	@Override
	public void clear()
	{
		clearLocalOptions();
	}

	@Override
	public boolean containsKey(Object key)
	{
		Map<IOptionKey<?>,Object> map = getLocalOptions(false);
		return map != null && map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		Map<IOptionKey<?>,Object> map = getLocalOptions(false);
		return map != null && map.containsValue(value);
	}

	@Override
	public Set<Map.Entry<IOptionKey<?>, Object>> entrySet()
	{
		Map<IOptionKey<?>,Object> map = getLocalOptions(false);
		return map != null ? map.entrySet() : Collections.<Map.Entry<IOptionKey<?>,Object>>emptySet();
	}

	@Override
	public Object get(Object key)
	{
		Map<IOptionKey<?>,Object> map = getLocalOptions(false);
		return map != null ? map.get(key) : null;
	}

	@Override
	public boolean isEmpty()
	{
		Map<IOptionKey<?>,Object> map = getLocalOptions(false);
		return map == null || map.isEmpty();
	}

	@Override
	public Set<IOptionKey<?>> keySet()
	{
		Map<IOptionKey<?>,Object> map = getLocalOptions(false);
		return map != null ? map.keySet() : Collections.<IOptionKey<?>>emptySet();
	}

	@Override
	public Object put(IOptionKey<?> key, Object value)
	{
		return getLocalOptions(true).put(key, value);
	}

	@Override
	public void putAll(Map<? extends IOptionKey<?>, ? extends Object> m)
	{
		getLocalOptions(true).putAll(m);
	}

	@Override
	public Object remove(Object key)
	{
		Map<IOptionKey<?>,Object> map = getLocalOptions(false);
		return map != null ? map.remove(key) : null;
	}

	@Override
	public int size()
	{
		Map<IOptionKey<?>,Object> map = getLocalOptions(false);
		return map != null ? map.size() : 0;
	}

	@Override
	public Collection<Object> values()
	{
		Map<IOptionKey<?>,Object> map = getLocalOptions(false);
		return map != null ? map.values() : Collections.emptyList();
	}
	
	/*--------------------
	 * IOptions methods
	 */
	
	@Override
	public <T> T get(IOptionKey<T> key)
	{
		Map<IOptionKey<?>,Object> map = getLocalOptions(false);
		return map != null ? key.type().cast(map.get(key)) : null;
	}
	
	@Override
	public <T> IOption<T> getOption(IOptionKey<T> key)
	{
		T value = get(key);
		return value != null ? new Option<T>(key, value) : null;
	}

	@Override
	public <T> T lookup(IOptionKey<T> key)
	{
		return Options.lookup(this, key);
	}
	
	@Override
	public <T> T lookupOrNull(IOptionKey<T> key)
	{
		return Options.lookupOrNull(this, key);
	}

	@Override
	public <T> IOption<T> lookupOption(IOptionKey<T> key)
	{
		T value = lookup(key);
		return value != null ? new Option<T>(key, value) : null;
	}
	
	@Override
	public <T> void set(IOptionKey<T> key, T value)
	{
		put(key, value);
	}
	
	@Override
	public void setOption(IOption<?> option)
	{
		put(option.key(), option.value());
	}

	@Override
	public void unset(IOptionKey<?> key)
	{
		remove(key);
	}
}
