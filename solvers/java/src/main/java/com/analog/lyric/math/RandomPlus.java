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

import java.util.Random;

public class RandomPlus extends Random 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int [] nextPermutation(int n)
	{
		int [] answer = new int[n];

		for (int i = 0; i < n; i++)
			answer[i] = i+1;
		
		//Modern Fisher-Yates shuffle
		for (int i = n-1; i >= 0; i--)
		{
			int j = Math.abs(nextInt())%(i+1);
			int tmp = answer[i];
			answer[i] = answer[j];
			answer[j] = tmp;
		}		
		
		return answer;
	}
	
	public double nextGamma(double alpha, double theta)
	{

		int k = (int) Math.floor(alpha);
		double delta = alpha - k;
		double g = 0;
		
		if (delta > 0)
		{
			double nu = Math.exp(1)/(Math.exp(1)+delta);
			while (true)
			{
				double v0 = nextDouble();
				double v1 = nextDouble();
				double xi = 0;
				double eta = 0;
				
				if (v0 <= nu)
				{
					xi = Math.pow((v0/nu),(1.0/delta));
					eta = v1 * Math.pow(xi,delta-1);
				}
				else
				{
					xi = 1.0 - Math.log((v0-nu)/(1-nu));
					eta = v1*Math.exp(-xi);
				}
				if (eta <= Math.pow(xi,delta-1) * Math.exp(-xi))
				{
					break;
				}	
				g = xi;
			}
		}
		double un = 0;
		for (int i = 0; i < k; i++)
			un += Math.log(nextDouble());
		g = theta * (g-un);
		
		return g;
	}
	
	public double [] nextDirichlet(double [] a)
	{
		int n = a.length;
		double [] r = new double[n];
		double sum = 0;
		for (int i = 0; i < n; i++)
		{
			r[i] = nextGamma(a[i], 1);
			sum += r[i];
		}
		
		for (int i = 0; i < n; i++)
			r[i]=r[i]/sum;

		return r;
	}
}
