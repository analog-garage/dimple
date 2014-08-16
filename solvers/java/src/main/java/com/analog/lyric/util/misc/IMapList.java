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

package com.analog.lyric.util.misc;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

// TODO: extend List<T>
public interface IMapList<T extends IGetId> extends Collection<T>
{
	public abstract void addAll(@Nullable T[] nodes);

	public abstract boolean contains(IGetId node);

	public abstract void ensureCapacity(int minCapacity);

	public abstract @Nullable T getByKey(int id);

	public abstract T getByIndex(int index);
	
	public abstract @Nullable T removeByIndex(int index);

	public abstract List<T> values();
}
