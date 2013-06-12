package com.analog.lyric.options;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public interface IOptionHolder
{
	public void clearLocalOptions();
	
	public ConcurrentMap<IOptionKey<?>,Object> getLocalOptions(boolean create);
	
	public IOptionHolder getOptionParent();
	
	/**
	 * Return a list of option keys that are relevant to this object, i.e. ones whose values affect
	 * the behavior of the object.
	 */
	public Set<IOptionKey<?>> getRelevantOptionKeys();
	
	public IOptions options();
}
