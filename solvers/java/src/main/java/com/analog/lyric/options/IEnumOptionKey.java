package com.analog.lyric.options;

/**
 * An {@link IOptionKey} specialization for option keys that are a member of
 * an enumeration.
 */
public interface IEnumOptionKey<T> extends IOptionKey<T>
{
	/**
	 * The ordinal index of the key in its enumeration. If implemented by an
	 * actual enum type, then this method is automatically provided.
	 * @see Enum#ordinal
	 */
	public abstract int ordinal();
}
