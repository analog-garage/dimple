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

package com.analog.lyric.dimple.model.domains;

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

// REFACTOR: I think this should eventually be folded up into DiscreteDomain.
public abstract class TypedDiscreteDomain<Element> extends DiscreteDomain implements Iterable<Element>
{
	private static final long serialVersionUID = 1L;

	protected TypedDiscreteDomain(int hashCode)
	{
		super(hashCode);
	}
	
	@Override
	public abstract Element getElement(int i);

	/*------------------
	 * Iterable methods
	 */
	
	@Override
	public Iterator<Element> iterator()
	{
		return new Iterator<Element>() {

			private volatile int _next = 0;
			
			@Override
			public boolean hasNext()
			{
				return _next < size();
			}

			@Override
			public Element next()
			{
				return getElement(_next++);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
	
	/*------------------------
	 * DiscreteDomain methods
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public @Nullable <T> TypedDiscreteDomain<T> asTypedDomain(Class<T> elementClass)
	{
		return (TypedDiscreteDomain<T>) (elementClass.isAssignableFrom(getElementClass()) ? this : null) ;
	}
}
