package com.analog.lyric.options;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Interface for object that can hold option values.
 * <p>
 * Users are expected to access options through the {@link #options()} method. The other methods
 * are intended to be used by implementors of {@link IOptions}.
 */
public interface IOptionHolder
{
	public void clearLocalOptions();
	
	/**
	 * Returns map containing the options that have been set directly on this
	 * object.
	 * @param create if set to true forces creation of empty local option map if there
	 * were no local options.
	 * @return local option map. May return null if there are no locally set options and {@code create}
	 * is false.
	 */
	public ConcurrentMap<IOptionKey<?>,Object> getLocalOptions(boolean create);
	
	/**
	 * The "parent" of this option holder to which option lookup will be delegated for option
	 * keys that are not set on this object. Used by {@link Options#lookupOrNull(IOptionKey)}.
	 * <p>
	 * Implementors should ensure that chain of parents is not circular!
	 * <p>
	 * @return the parent object or null if there is none.
	 */
	public IOptionHolder getOptionParent();
	
	/**
	 * Return a list of option keys that are relevant to this object, i.e. ones whose values affect
	 * the behavior of the object.
	 * @return set of option keys. May return null if there aren't any.
	 */
	public Set<IOptionKey<?>> getRelevantOptionKeys();
	
	/**
	 * 
	 */
	public IOptions options();
}
