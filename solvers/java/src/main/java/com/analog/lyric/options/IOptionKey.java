package com.analog.lyric.options;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

@Immutable
public interface IOptionKey<T> extends Serializable
{
	/*-------------------------
	 * Enum-compatible methods
	 * 
	 * These do not need to be defined when implementation is an enum type.
	 */
	
	public abstract Class<?> getDeclaringClass();
	
	/**
	 * The unqualified name of the option.
	 * <p>
	 * Should be a valid Java identifier.
	 */
	public abstract String name();
	
	/*---------------
	 * Other methods
	 */
	
	/**
	 * The type of the option's value.
	 */
	public abstract Class<T> type();
	
	/**
	 * The default value of the option if not set.
	 */
	public abstract T defaultValue();
	
	/**
	 * Lookup the value of the option in the given {@code holder}.
	 */
	public abstract T lookup(IOptionHolder holder);
	
	/**
	 * Set the option value locally on the given {@code holder}.
	 */
	public abstract void set(IOptionHolder holder, T value);
	
	/**
	 * Unset the option locally on the given {@code holder}.
	 */
	public abstract void unset(IOptionHolder holder);
}
