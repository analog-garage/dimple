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

import netscape.javascript.JSException;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.util.misc.Internal;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Factory for creating {@link JSDomain} instances.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class JSDomainFactory
{
	final @Nullable DimpleApplet _applet;
	private final Cache<Object, JSProxyObject<?>> _proxyCache;
	
	/*--------------
	 * Construction
	 */
	
	JSDomainFactory(DimpleApplet applet, Cache<Object, JSProxyObject<?>> proxyCache)
	{
		_applet = applet;
		_proxyCache = proxyCache;
	}
	
	/**
	 * For tests purposes only.
	 */
	@SuppressWarnings("null")
	@Internal
	public JSDomainFactory()
	{
		_applet = null;
		_proxyCache = CacheBuilder.newBuilder().build();
	}
	
	/**
	 * Returns instance of discrete domain with values {0, 1}.
	 * @since 0.07
	 */
	public JSDiscreteDomain bit()
	{
		return wrap(DiscreteDomain.bit());
	}

	/**
	 * Returns instance of discrete domain with values {false, true}.
	 * @since 0.07
	 */
	public JSDiscreteDomain bool()
	{
		return wrap(DiscreteDomain.bool());
	}

	/**
	 * Returns instance of discrete domain with specified elements in given order.
	 * @since 0.07
	 */
	public JSDiscreteDomain discrete(Object[] elements)
	{
		return wrap(DiscreteDomain.create(elements));
	}
	
	/**
	 * Returns instance of discrete domain with integer values in range [start, end].
	 * @param start specifies the lowest integer value in the domain
	 * @param end specifies the highest integer value in the domain. It must not be less than {@code start}.
	 * @since 0.07
	 */
	public JSDiscreteDomain range(int start, int end)
	{
		return wrap(DiscreteDomain.range((double)start, (double)end));
	}
	
	/**
	 * Returns instance of unbounded real domain.
	 * @since 0.07
	 */
	public JSRealDomain real()
	{
		return wrap(RealDomain.unbounded());
	}

	/**
	 * Returns instance of bounded real domain.
	 * @param lowerBound specifies the lower bound (inclusive) of the domain.
	 * @param upperBound specifies the upper bound (inclusive) of the domain. It must not be less than
	 * {@code lowerBound}.
	 * @since 0.07
	 */
	public JSRealDomain real(double lowerBound, double upperBound)
	{
		return wrap(RealDomain.create(lowerBound, upperBound));
	}
	
	/**
	 * Returns instance of an unbounded n-dimensional real joint domain.
	 * @param N is the number of dimensions, which must be greater than one.
	 * @since 0.07
	 */
	public JSRealJointDomain realN(int N)
	{
		return wrap(RealJointDomain.create(N));
	}
	
	/**
	 * Returns instance of an n-dimensional real joint domain with specified dimensions.
	 * @param domains specifies the scalar real domains that make up the dimensions of the real joint domain.
	 * @since 0.07
	 */
	public JSRealJointDomain realN(JSRealDomain[] domains)
	{
		RealDomain[] unwrappedDomains = new RealDomain[domains.length];
		for (int i = domains.length; --i >= 0;)
		{
			unwrappedDomains[i] = domains[i]._delegate;
		}
		return wrap(RealJointDomain.create(unwrappedDomains));
	}
	
	/**
	 * Wraps a raw Dimple {@link Domain} object with appropriate {@link JSDomain}.
	 * @since 0.07
	 */
	@SuppressWarnings("unchecked")
	public <D extends JSDomain<?>> D wrap(Domain domain)
	{
		JSProxyObject<?> jsdomain = _proxyCache.getIfPresent(domain);
		if (jsdomain == null)
		{
			if (domain.isDiscrete())
			{
				jsdomain = new JSDiscreteDomain(this, (DiscreteDomain)domain);
			}
			else if (domain.isReal())
			{
				jsdomain = new JSRealDomain(this, (RealDomain)domain);
			}
			else if (domain.isRealJoint())
			{
				jsdomain = new JSRealJointDomain(this, (RealJointDomain)domain);
			}
			else
			{
				throw new JSException("Unsupported domain type: " + domain.getClass().getSimpleName());
			}
			
			_proxyCache.put(domain, jsdomain);
		}
		return (D)(JSDomain<?>)jsdomain;
	}
}
