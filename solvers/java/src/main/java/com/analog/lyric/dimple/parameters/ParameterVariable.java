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

package com.analog.lyric.dimple.parameters;

import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.variables.Real;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link Real} variable that implements a parameter of a parametric factor
 * function.
 */
public abstract class ParameterVariable extends Real
{
	/*--------------
	 * Construction
	 */
	
	protected ParameterVariable(RealDomain domain)
	{
		super(domain);
	}
	
	/*---------------------------
	 * ParameterVariable methods
	 */
	
	public abstract int getParameterIndex();
	
	public abstract @Nullable IParameterKey getParameterKey();
	
	/*-----------------
	 * Implementations
	 */
	
	public static class WithKey extends ParameterVariable
	{
		private final IParameterKey _key;
		
		public WithKey(IParameterKey key)
		{
			super(key.domain());
			_key = key;
		}
		
		@Override
		public final int getParameterIndex()
		{
			return _key.ordinal();
		}
		
		@Override
		public final IParameterKey getParameterKey()
		{
			return _key;
		}
	}
	
	public static class WithIndex extends ParameterVariable
	{
		private final int _index;
		
		public WithIndex(int index)
		{
			super(RealDomain.unbounded());
			_index = index;
		}
		
		public WithIndex(int index, RealDomain domain)
		{
			super(domain);
			_index = index;
		}
		
		@Override
		public final int getParameterIndex()
		{
			return _index;
		}
		
		@Override
		public final @Nullable IParameterKey getParameterKey()
		{
			return null;
		}
	}
}
