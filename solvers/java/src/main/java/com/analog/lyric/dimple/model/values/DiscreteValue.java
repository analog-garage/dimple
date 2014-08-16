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

package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Base class for {@link Value} implementations for {@link DiscreteDomain}s.
 */
public abstract class DiscreteValue extends Value
{
	private static final long serialVersionUID = 1L;

	@Override
	public abstract DiscreteValue clone();
	
	@Override
	public abstract DiscreteDomain getDomain();
	
	@Override
	public abstract int getIndex();
	
	@Override
	public abstract @NonNull Object getObject();
	
	@Override
	public abstract void setIndex(int index);
}
