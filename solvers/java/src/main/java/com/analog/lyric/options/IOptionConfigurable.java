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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for non-option-holder objects that can be configured using options.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public interface IOptionConfigurable
{
	/**
	 * Configures state/parameters of object from option values obtained through given option holder.
	 * <p>
	 * @param optionHolder is the option holder that can be used to look up option settings.
	 * <p>
	 * @since 0.07
	 */
	public void configureFromOptions(IOptionHolder optionHolder);
	
	/**
	 * Returns a list of {@link Option} objects describing the configurable settings of this object.
	 * <p>
	 * @param list if non-null, options should be appended to this list. Otherwise a new list will
	 * be created.
	 * @return {@code list} if non-null, otherwise returns a newly constructed mutable list containing
	 * option objects that describe the settings of this object that can be controlled by options. May
	 * return null if no options pertain to this object (but only if {@code list} was null!).
	 * @since 0.07
	 */
	public @Nullable List<Option<?>> getOptionConfiguration(@Nullable List<Option<?>> list);
}
