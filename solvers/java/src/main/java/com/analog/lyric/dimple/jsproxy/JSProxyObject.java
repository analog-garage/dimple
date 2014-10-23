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

package com.analog.lyric.dimple.jsproxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;


/**
 * Base JavaScript/Dimple proxy wrapper.
 * <p>
 * @param <Delegate> is the type of the object to which this delegates.
 * <p>
 * Except for {@link DimpleApplet}, all Java objects that comprise the Javascript interface
 * are subclasses of this class. This wraps an underlying object from the standard Java API
 * of Dimple for use in Java and exposes the object to Javascript users through the {@link #getDelegate()}
 * method, although this should only be used as a last resort.
 * <p>
 * The {@link #equals(Object)} and {@link #hashCode()} methods have been overridden to implement
 * equality based on the underlying delegate object. It is expected that there may be multiple proxy
 * objects that refer to the same delegate, so users should make sure to use {@link #equals} for comparison.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public abstract class JSProxyObject<Delegate>
{
	final Delegate _delegate;
	
	/*--------------
	 * Construction
	 */
	
	JSProxyObject(Delegate delegate)
	{
		_delegate = delegate;
	}
	
	/*----------------
	 * Object methods
	 */
	
	/**
	 * True if {@code obj} is itself a {@code JSProxyObject} with the same value for {@link #getDelegate()}.
	 */
	@NonNullByDefault(false)
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof JSProxyObject<?>)
		{
			obj = ((JSProxyObject<?>)obj).getDelegate();
		}
		
		return _delegate.equals(obj);
	}
	
	/**
	 * Returns hash code of underlying {@linkplain #getDelegate delegate} object.
	 */
	@Override
	public int hashCode()
	{
		return _delegate.hashCode();
	}
	
	@Override
	public String toString()
	{
		return _delegate.toString();
	}
	
	/*-----------------------
	 * JSProxyObject methods
	 */
	
	/**
	 * Returns the dimple object wrapped by this proxy and which does the real work.
	 * <p>
	 * This can be used to access functionality that is not otherwise exposed by the
	 * proxy layer. It is strongly recommended that the proxy API is used instead of
	 * going through the delegate object whenever possible.
	 * <p>
	 * @since 0.07
	 */
	public Delegate getDelegate()
	{
		return _delegate;
	}

	/**
	 * The applet instance under which the proxy was created.
	 * <p>
	 * This can only be null when testing this API without an applet.
	 * @since 0.07
	 */
	public abstract @Nullable DimpleApplet getApplet();
}
