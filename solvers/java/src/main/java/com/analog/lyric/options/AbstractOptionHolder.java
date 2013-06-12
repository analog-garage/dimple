/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.analog.lyric.dimple.model.DimpleException;

/**
 * @author cbarber
 *
 */
public abstract class AbstractOptionHolder implements IOptionHolder
{

	/**
	 * {@inheritDoc}
	 * Default implementation invokes {@code clear} method on map returned
	 * by {@link #getLocalOptions(boolean)} if not null.
	 */
	@Override
	public void clearLocalOptions()
	{
		ConcurrentMap<IOptionKey<?>,Object> localOptions = getLocalOptions(false);
		if (localOptions != null)
		{
			localOptions.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 * Default implementation always returns null if {@code create} is false and
	 * throws an exception if it is true.
	 */
	@Override
	public ConcurrentMap<IOptionKey<?>, Object> getLocalOptions(boolean create)
	{
		if (create)
		{
			throw DimpleException.unsupported("getLocalOptions(false)");
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * Default implementation returns null.
	 */
	@Override
	public IOptionHolder getOptionParent()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 * Default implementation returns empty list.
	 */
	@Override
	public Set<IOptionKey<?>> getRelevantOptionKeys()
	{
		return Collections.EMPTY_SET;
	}

	/**
	 * {@inheritDoc}
	 * Default implementation returns new {@link Options} instance for this object.
	 */
	@Override
	public IOptions options()
	{
		return new Options(this);
	}

}
