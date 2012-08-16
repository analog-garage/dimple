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

package com.analog.lyric.dimple.solvers.minsum;

public class MessageConverter
{
	
	private static final double maxPotential = 1000;	// Clip the potential (-log(P)) to this value
	
    public static double[] fromProb(double[] in)
    {
    	int numValue = in.length;
    	double[] out = new double[numValue];
    	
    	for (int i = 0; i < numValue; i++) 
    		out[i] = Math.min(-Math.log(in[i]),maxPotential);
    	return out;
    }
    
    public static double[] toProb(double[] in)
    {
    	int numValue = in.length;
    	double[] out = new double[numValue];
    	double minPotential = Double.POSITIVE_INFINITY;
        double sum = 0;
        for (int i = 0; i < numValue; i++)
        	if (in[i] < minPotential) minPotential = in[i];
        for (int i = 0; i < numValue; i++)
        {
            out[i] = Math.exp(-(in[i] - minPotential));
            sum += out[i];
        }
        for (int i = 0; i < numValue; i++) out[i] /= sum;		// Normalize to probability values
        return out;
    }
    
    public static double[] initialValue(int numValue)
    {
    	double[] out = new double[numValue];
    	for (int i = 0; i < numValue; i++) out[i] = 0;
    	return out;
    }
    
}
