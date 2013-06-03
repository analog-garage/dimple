package com.analog.lyric.options;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

@Immutable
public interface IOptionKey<T> extends Serializable
{
	public abstract String name();
	
	public abstract Class<T> type();
	
	public abstract T defaultValue();
	
	public abstract T lookup(IOptionHolder holder);
	
	public abstract void set(IOptionHolder holder, T value);
	
	public abstract void unset(IOptionHolder holder);
}
