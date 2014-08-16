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

import com.analog.lyric.dimple.model.domains.ObjectDomain;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Holder for arbitrary values represented as {@link Object}.
 */
public class ObjectValue extends Value
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	private @Nullable Object _object;
	
	/*--------------
	 * Construction
	 */

	ObjectValue(@Nullable Object value)
	{
		_object = value;
	}
	
	ObjectValue()
	{
		this(null);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public ObjectValue clone()
	{
		return new ObjectValue(this._object);
	}

	@Override
	public ObjectDomain getDomain()
	{
		return ObjectDomain.instance();
	}
	
	@Override
	public @Nullable Object getObject()
	{
		return _object;
	}

	@Override
	public void setObject(@Nullable Object value)
	{
		_object = value;
	}
	
}
