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

package com.analog.lyric.dimple.solvers.particleBP;

public class ParticleBPSolverVariableToFactorMessage
{
	public int length;
	public int resamplingVersion;
	public Double[] particleValues;
	public double[] messageValues;
	
	
	public ParticleBPSolverVariableToFactorMessage(int numParticles)
	{
		length = numParticles;
		resamplingVersion = 0;
		particleValues = new Double[numParticles];
		messageValues = new double[numParticles];
		
    	double initialMessageValue = 1.0/numParticles;
    	
    	for (int i = 0; i < numParticles; i++)
    		messageValues[i] = initialMessageValue;
	}
	
}
