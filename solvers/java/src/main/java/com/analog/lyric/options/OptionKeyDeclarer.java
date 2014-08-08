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

import com.analog.lyric.collect.ReleasableIterator;

/**
 * Base for classes that declare option keys.
 * <p>
 * This can be used as a base class for classes that contain {@linkplain IOptionKey option key}
 * declarations. Although, such classes typically only contain static option declarations and
 * will not usually be instantiated, it can still be useful to inherit from this class:
 * <ul>
 * <li>It will provide a way to organize option declaration classes in a hierarchy.
 * <li>It will allow developers to visualize the option hierarchy and view available options
 * using their IDE's type hierarchy viewer.
 * </ul>It provides additional methods for exploring the option hierarchy programmatically.
 * <p>
 * It is recommended that a project using this option package declare a subclass of this
 * class as the root for their project's options.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public abstract class OptionKeyDeclarer
{
	/**
	 * Returns keys declared directly in concrete class of this instance.
	 * <p>
	 * This will not include keys declared in superclasses.
	 * <p>
	 * @since 0.07
	 */
	public final OptionKeys getOptionKeys()
	{
		return OptionKeys.declaredInClass(getClass());
	}
	
	/**
	 * Returns hierarchical iterator over option keys starting with class of this instance.
	 * <p>
	 * @since 0.07
	 */
	public final ReleasableIterator<OptionKeys> getHierarchicalOptionKeys()
	{
		return OptionKeys.declaredInHierarchy(getClass());
	}
	
}
