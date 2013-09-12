package com.analog.lyric.options;

import java.util.Map;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public interface IOptions extends IOptionHolder, Map<IOptionKey<?>,Object>
{
	public <T> T get(IOptionKey<T> key);

	public <T> IOption<T> getOption(IOptionKey<T> key);

	public IOptionHolder getOptionHolder();
	
	public <T> T lookup(IOptionKey<T> key);

	public <T> T lookupOrNull(IOptionKey<T> key);
	
	public <T> IOption<T> lookupOption(IOptionKey<T> key);
	
	public <T> void set(IOptionKey<T> key, T value);
	
	public void setOption(IOption<?> option);

	public void unset(IOptionKey<?> key);
}
