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

import java.util.Map;

import net.jcip.annotations.ThreadSafe;

import com.analog.lyric.util.misc.Nullable;

@ThreadSafe
public interface IOptions extends IOptionHolder, Map<IOptionKey<?>,Object>
{
	public @Nullable <T> T get(IOptionKey<T> key);

	public @Nullable <T> IOption<T> getOption(IOptionKey<T> key);

	public IOptionHolder getOptionHolder();
	
	public @Nullable <T> T lookup(IOptionKey<T> key);

	public @Nullable <T> T lookupOrNull(IOptionKey<T> key);
	
	public @Nullable <T> IOption<T> lookupOption(IOptionKey<T> key);
	
	public <T> void set(IOptionKey<T> key, T value);
	
	public void setOption(IOption<?> option);

	public void unset(IOptionKey<?> key);
}
