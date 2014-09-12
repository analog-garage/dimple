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

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.google.common.cache.Cache;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class JSDomainFactory
{
	final DimpleApplet _applet;
	private final Cache<Object, JSProxyObject<?>> _proxyCache;
	
	JSDomainFactory(DimpleApplet applet, Cache<Object, JSProxyObject<?>> proxyCache)
	{
		_applet = applet;
		_proxyCache = proxyCache;
	}
	
	public JSDiscreteDomain bit()
	{
		return wrap(DiscreteDomain.bit());
	}
	
	public JSDiscreteDomain bool()
	{
		return wrap(DiscreteDomain.bool());
	}
	
	public JSDiscreteDomain discrete(Object[] elements)
	{
		return wrap(DiscreteDomain.create(elements));
	}
	
	public JSDiscreteDomain range(int start, int end)
	{
		return wrap(DiscreteDomain.range((double)start, (double)end));
	}
	
	public JSRealDomain real()
	{
		return wrap(RealDomain.unbounded());
	}
	
	public JSRealDomain real(double lowerBound, double upperBound)
	{
		return wrap(RealDomain.create(lowerBound, upperBound));
	}
	
	public JSRealJointDomain realN(int N)
	{
		return wrap(RealJointDomain.create(N));
	}
	
	public JSRealJointDomain realN(JSRealDomain[] domains)
	{
		RealDomain[] unwrappedDomains = new RealDomain[domains.length];
		for (int i = domains.length; --i >= 0;)
		{
			unwrappedDomains[i] = domains[i]._delegate;
		}
		return wrap(RealJointDomain.create(unwrappedDomains));
	}
	
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
	
	/*-----------------
	 * Private methods
	 */
	
}
