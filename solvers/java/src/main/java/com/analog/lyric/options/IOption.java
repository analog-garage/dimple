package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

@Immutable
public interface IOption<T>
{
	public abstract IOptionKey<T> key();
	
	public abstract T value();
}
