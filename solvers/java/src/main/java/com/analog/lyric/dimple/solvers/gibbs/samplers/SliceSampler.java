/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.gibbs.samplers;

import com.analog.lyric.dimple.solvers.core.SolverRandomGenerator;


public class SliceSampler implements IRealSampler
{
	private double _initialSliceWidth = 1;	// Default value
	private double _maximumDoublings = 10;	// Default value
	
	@Override
	public double nextSample(double currentSampleValue, ISampleScorer sampleScorer)
	{
		double y = sampleVerticalSlice(sampleScorer);
		double x = sampleHorizontalSlice(currentSampleValue, y, sampleScorer);
		return x;
	}

	// Sample vertical slice in log domain
	// In probability domain this would be uniform from 0 to the value at the current horizontal location (in probability representation)
	public double sampleVerticalSlice(ISampleScorer sampleScorer)
	{
		double yValue = sampleScorer.getCurrentSampleScore();
		return yValue - Math.log(SolverRandomGenerator.rand.nextDouble());
	}

	// Sample horizontal slice using doubling method
	public double sampleHorizontalSlice(double x, double y, ISampleScorer sampleScorer)
	{
		// First finding slice using doubling method
		double L = x - _initialSliceWidth * SolverRandomGenerator.rand.nextDouble();
		double R = L + _initialSliceWidth;
		double fL = sampleScorer.getSampleScore(L);
		double fR = sampleScorer.getSampleScore(R);
		for (int k = 0; k < _maximumDoublings; k++)
		{
			if (y <= fL && y <= fR)
				break;
			if (SolverRandomGenerator.rand.nextBoolean())	// Flip a coin
			{
				L -= (R - L);
				fL = sampleScorer.getSampleScore(L);
			}
			else
			{
				R += (R - L);
				fR = sampleScorer.getSampleScore(R);
			}
		}
		
		// Next, shrink the interval as necessary
		double xSample;
		double Ls = L;
		double Rs = R;
		while (true)
		{
			xSample = Ls + (Rs - Ls) * SolverRandomGenerator.rand.nextDouble();
			double fSample = sampleScorer.getSampleScore(xSample);
			
			if (y >= fSample && accept(xSample, x, y, L, R, sampleScorer))
				break;	// Accept
			
			// Not accepted yet, shrink the interval
			if (xSample < x)
				Ls = xSample;
			else
				Rs = xSample;
		}
		
		return xSample;
	}
	
	private boolean accept(double xSample, double x, double y, double L, double R, ISampleScorer sampleScorer)
	{
		boolean D = false;
		double fL = sampleScorer.getSampleScore(L);
		double fR = sampleScorer.getSampleScore(R);
		
		while (R - L > 1.1 * _initialSliceWidth)
		{
			double M = (L + R) * 0.5;	// Mid-point
			
			if ((x < M && xSample >= M) || (x >= M && xSample < M))
				D = true;
			
			if (xSample < M)
			{
				R = M;
				fR = sampleScorer.getSampleScore(R);
			}
			else
			{
				L = M;
				fL = sampleScorer.getSampleScore(L);
			}
			
			if (D && (y <= fL) && (y <= fR))
				return false;	// The new point is not acceptable
		}
		
		return true;	// The new point is acceptable
	}

	public double getInitialSliceWidth()
	{
		return _initialSliceWidth;
	}

	public void setInitialSliceWidth(double initialSliceWidth)
	{
		_initialSliceWidth = initialSliceWidth;
	}

	public double getMaximumDoublings()
	{
		return _maximumDoublings;
	}

	public void setMaximumDoublings(double maximumDoublings)
	{
		_maximumDoublings = maximumDoublings;
	}

}
