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

package com.analog.lyric.options.tests.bar;

import com.analog.lyric.options.StringOptionKey;
import com.analog.lyric.options.tests.TestOptionRegistry;

/**
 * For use by {@link TestOptionRegistry}
 * @since 0.07
 * @author Christopher Barber
 */
public class SomeOptions
{
	public static final StringOptionKey A = new StringOptionKey(SomeOptions.class, "A", "a");
	public static final StringOptionKey B = new StringOptionKey(SomeOptions.class, "B", "b");
	public static final StringOptionKey C = new StringOptionKey(SomeOptions.class, "C", "c");
}
