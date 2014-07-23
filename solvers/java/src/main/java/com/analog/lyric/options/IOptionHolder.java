/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.options;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.util.misc.Nullable;

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
	 * is false or if object does not support local option values.
	 * @see #createLocalOptions()
	 */
	public @Nullable ConcurrentMap<IOptionKey<?>,Object> getLocalOptions(boolean create);
	
	/**
	 * Returns map containing the options that have been set directly on this
	 * object, creating if necessary.
	 * @since 0.06
	 * @throws UnsupportedOperationException if object does not support local option values.
	 */
	public ConcurrentMap<IOptionKey<?>,Object> createLocalOptions();
	
	/**
	 * Iterates over option holders in order in which lookup should be delegated.
	 * <p>
	 * The default implementations provided by {@link AbstractOptionHolder} and
	 * {@link AbstractOptions} returns a {@link OptionParentIterator} instance,
	 * which simply walks up the chain of option parents.
	 * <p>
	 * @since 0.07
	 */
	public ReleasableIterator<IOptionHolder> getOptionDelegates();
	
	/**
	 * The "parent" of this option holder to which option lookup will be delegated for option
	 * keys that are not set on this object. Used by {@link Options#lookupOrNull(IOptionKey)}.
	 * <p>
	 * Implementors should ensure that chain of parents is not circular!
	 * <p>
	 * @return the parent object or null if there is none.
	 */
	public @Nullable IOptionHolder getOptionParent();
	
	/**
	 * Return a list of option keys that are relevant to this object, i.e. ones whose values affect
	 * the behavior of the object.
	 * @return set of option keys.
	 */
	public Set<IOptionKey<?>> getRelevantOptionKeys();
	
	/**
	 * Returns object used for getting/setting options.
	 */
	public IOptions options();
}
