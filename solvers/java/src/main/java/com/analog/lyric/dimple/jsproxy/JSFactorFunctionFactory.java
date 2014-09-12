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

import com.analog.lyric.dimple.factorfunctions.Abs;
import com.analog.lyric.dimple.factorfunctions.And;
import com.analog.lyric.dimple.factorfunctions.Bernoulli;
import com.analog.lyric.dimple.factorfunctions.Beta;
import com.analog.lyric.dimple.factorfunctions.Categorical;
import com.analog.lyric.dimple.factorfunctions.Dirichlet;
import com.analog.lyric.dimple.factorfunctions.Divide;
import com.analog.lyric.dimple.factorfunctions.Equals;
import com.analog.lyric.dimple.factorfunctions.Exp;
import com.analog.lyric.dimple.factorfunctions.Gamma;
import com.analog.lyric.dimple.factorfunctions.GreaterThan;
import com.analog.lyric.dimple.factorfunctions.GreaterThanOrEqual;
import com.analog.lyric.dimple.factorfunctions.LessThan;
import com.analog.lyric.dimple.factorfunctions.LessThanOrEqual;
import com.analog.lyric.dimple.factorfunctions.Log;
import com.analog.lyric.dimple.factorfunctions.Multinomial;
import com.analog.lyric.dimple.factorfunctions.Multiplexer;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.Not;
import com.analog.lyric.dimple.factorfunctions.Or;
import com.analog.lyric.dimple.factorfunctions.Power;
import com.analog.lyric.dimple.factorfunctions.Product;
import com.analog.lyric.dimple.factorfunctions.Sqrt;
import com.analog.lyric.dimple.factorfunctions.Subtract;
import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class JSFactorFunctionFactory
{
	final DimpleApplet _applet;
	private final Cache<FactorFunction, JSFactorFunction> _proxyCache;
	
	JSFactorFunctionFactory(DimpleApplet applet)
	{
		_applet = applet;
		_proxyCache = CacheBuilder.newBuilder().build();
	}
	
	public JSFactorFunction abs()
	{
		return wrap(new Abs());
	}
	
	public JSFactorFunction and()
	{
		return wrap(new And());
	}
	
	public JSFactorFunction bernoulli()
	{
		return wrap(new Bernoulli());
	}
	
	public JSFactorFunction bernoulli(double p)
	{
		return wrap(new Bernoulli(p));
	}
	
	public JSFactorFunction beta()
	{
		return wrap(new Beta());
	}
	
	public JSFactorFunction beta(double alpha, double beta)
	{
		return wrap(new Beta(alpha, beta));
	}
	
	public JSFactorFunction categorical()
	{
		return wrap(new Categorical());
	}

	public JSFactorFunction categorical(double[] alphas)
	{
		return wrap(new Categorical(alphas));
	}
	
	public JSFactorFunction dirichlet()
	{
		return wrap(new Dirichlet());
	}
	
	public JSFactorFunction dirichlet(double[] alphas)
	{
		return wrap(new Dirichlet(alphas));
	}
	
	public JSFactorFunction divide()
	{
		return wrap(new Divide());
	}
	
	/**
	 * Numeric equality.
	 * 
	 * @since 0.07
	 */
	public JSFactorFunction equals()
	{
		return wrap(new Equals());
	}
	
	/**
	 * e<sup>x</sup>
	 * 
	 * @since 0.07
	 */
	public JSFactorFunction exp()
	{
		return wrap(new Exp());
	}
	
	public JSFactorFunction gamma(double alpha, double beta)
	{
		return wrap(new Gamma(alpha, beta));
	}
	
	public JSFactorFunction greaterThan()
	{
		return wrap(new GreaterThan());
	}
	
	public JSFactorFunction greaterThanOrEqual()
	{
		return wrap(new GreaterThanOrEqual());
	}

	public JSFactorFunction lessThan()
	{
		return wrap(new LessThan());
	}
	
	public JSFactorFunction lessThanOrEqual()
	{
		return wrap(new LessThanOrEqual());
	}

	public JSFactorFunction log()
	{
		return wrap(new Log());
	}
	
	public JSFactorFunction multinomial(int size)
	{
		return wrap(new Multinomial(size));
	}
	
	public JSFactorFunction multiplexer()
	{
		return wrap(new Multiplexer());
	}
	
	public JSFactorFunction normal()
	{
		return wrap(new Normal());
	}
	
	public JSFactorFunction normalWithPrecision(double mean, double precision)
	{
		return wrap(new Normal(mean, precision));
	}
	
	public JSFactorFunction normalWithStandardDeviation(double mean, double standardDeviation)
	{
		return wrap(new Normal(mean, 1 / (standardDeviation * standardDeviation)));
	}
	
	public JSFactorFunction normalWithVariance(double mean, double variance)
	{
		return wrap(new Normal(mean, 1 / variance));
	}
	
	public JSFactorFunction not()
	{
		return wrap(new Not());
	}
	
	public JSFactorFunction or()
	{
		return wrap(new Or());
	}
	
	/**
	 * x<sup>y</sup>
	 * @since 0.07
	 */
	public JSFactorFunction power()
	{
		return wrap(new Power());
	}
	
	public JSFactorFunction product()
	{
		return wrap(new Product());
	}
	
	public JSFactorFunction sqrt()
	{
		return wrap(new Sqrt());
	}
	
	public JSFactorFunction subtract()
	{
		return wrap(new Subtract());
	}
	
	public JSFactorFunction sum()
	{
		return wrap(new Sum());
	}

	/*------------------
	 * Internal methods
	 */
	
	JSFactorFunction wrap(FactorFunction function)
	{
		JSFactorFunction jsfunction = _proxyCache.getIfPresent(function);
		if (jsfunction == null)
		{
			jsfunction = new JSFactorFunction(this, function);
			_proxyCache.put(function, jsfunction);
		}
		return jsfunction;
	}
}
