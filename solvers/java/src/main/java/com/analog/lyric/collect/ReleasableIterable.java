/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.collect;

/**
 * An {@link Iterable} whose {@link #iterator} method returns a
 * {@link ReleasableIterator}, which may be released and reused.
 * <p>
 * If you need to perform frequent iteration with such objects,
 * it may be better to directly invoke the iterator and release
 * it when done rather than to implicitly iterate using {@code for}.
 */
public interface ReleasableIterable<T> extends Iterable<T>
{
	@Override
	ReleasableIterator<T> iterator();
}
