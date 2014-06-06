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

package com.analog.lyric.math;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class Functions
{
	private static enum ApproximateFactorialHelper
	{
		INSTANCE;
		
		private static final int CACHE_SIZE = 100;
		private final double [] _cache = new double[CACHE_SIZE];
		
		public double run(int n)
		{
			n = Math.abs(n);
			
			if (n < CACHE_SIZE)
				return getFromCache(n);
			else
			{
				//stirling
				//log(n!)=nlog(n) - n +1/2 log(2pi n)+ 1/(12 n)
				return n*Math.log(n)- n + 1.0/2.0*Math.log(2.0*Math.PI*n) + 1.0/(12.0*n);
			}
		}
		
		public double getFromCache(int n)
		{
			if (n <= 2)
			{
				_cache[2] = Math.log(n);
				return _cache[n];
			}
			else
			{
				if (_cache[n] == 0)
				{
					_cache[n] = getFromCache(n-1)+ Math.log(n);
				}
				return _cache[n];
			}
		}
	}
	public static double logfactorial(int n)
	{
		return ApproximateFactorialHelper.INSTANCE.run(n);
	}
	
	public static double [][] transpose(double [][] mat)
	{
		double [][] retval = new double[mat[0].length][];
		for (int i = 0; i < retval.length; i++)
		{
			retval[i] = new double[mat.length];
			for (int j = 0; j < retval[i].length; j++)
				retval[i][j] = mat[j][i];
		}
		return retval;
	}
}
