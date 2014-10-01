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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.Nullable;


/**
 * Provides a minimal stateless implementation of {@link IOptionHolder} methods.
 * <p>
 * This implementation does not support local options.
 * <p>
 * @see LocalOptionHolder
 * @since 0.07
 * @author Christopher Barber
 */
public abstract class StatelessOptionHolder extends AbstractOptionHolder
{
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation does nothing.
	 */
	@Override
	public void clearLocalOptions()
	{
	}

	@Override
	public Collection<IOption<? extends Serializable>> getLocalOptions()
	{
		return Collections.emptyList();
	}
	
	@Override
	@Nullable
	public <T extends Serializable> T getLocalOption(IOptionKey<T> key)
	{
		return null;
	}

	@Override
	public <T extends Serializable> void setOption(IOptionKey<T> key, T value)
	{
		throw noLocalOptions();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns false.
	 */
	@Override
	public boolean supportsLocalOptions()
	{
		return false;
	}

	@Override
	public void unsetOption(IOptionKey<?> key)
	{
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private RuntimeException noLocalOptions()
	{
		return new UnsupportedOperationException(String.format("%s does not support local options",
			getClass().getSimpleName()));
	}
}
