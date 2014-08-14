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

package com.analog.lyric.dimple.options;

import com.analog.lyric.options.LongOptionKey;
import com.analog.lyric.options.OptionKeyDeclarer;

/**
 * Base of dimple option hierarchy.
 * <p>
 * Root of hierarchy of classes that contain declaration of option keys for
 * use with dimple.
 * <p>
 * Also contains declarations for option keys that are not specific to any
 * particular component of dimple.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class DimpleOptions extends OptionKeyDeclarer
{
	/**
	 * Random seed for testing purposes.
	 * <p>
	 * @since 0.07
	 */
	public static final LongOptionKey randomSeed =
		new LongOptionKey(DimpleOptions.class, "randomSeed");
	
	
}
