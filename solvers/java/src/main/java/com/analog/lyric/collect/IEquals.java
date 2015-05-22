/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Simple interface defining alternate equals method.
 * <p>
 * Classes can implement this interface as an alternative to overriding {@link Object#equals}.
 * <p>
 * Subclasses that implement this interface and also override {@link Object#equals} should implement
 * the same behavior for both methods or else document the difference.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public interface IEquals
{
	public boolean objectEquals(@Nullable Object other);
}
