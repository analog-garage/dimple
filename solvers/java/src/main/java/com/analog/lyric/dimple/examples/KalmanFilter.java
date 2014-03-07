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

package com.analog.lyric.dimple.examples;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.RealJoint;

public class KalmanFilter 
{
	public static void main(String [] args)
	{
		double dt = 1;
		double gamma = 1;
		double m = 1;

		double [][] F = new double [][] {
				new double []{1,0,dt,0,(dt*dt)/2,0,0,0},
				new double []{0,1,0,dt,0,(dt*dt)/2,0,0},
				new double []{0,0,1,0,dt/2,0,0,0},
				new double []{0,0,0,1,0,dt/2,0,0},
				new double []{0,0,-gamma/m,0,0,0,0,0},
				new double []{0,0,0,-gamma/m,0,0,0,0},
				new double []{0,0,0,0,0,0,1,0},
				new double []{0,0,0,0,0,0,0,1}
		};

		//H is the matrix that projects down to the observation.
		double [][] H = new double [][] {
				new double [] {1, 0, 0, 0, 0, 0, 0, 0},
				new double [] {0, 1, 0, 0, 0, 0, 0, 0}
		};


		RealJoint fz = new RealJoint(2);
		RealJoint fv = new RealJoint(2);
		RealJoint fznonoise = new RealJoint(2);
		RealJoint fx = new RealJoint(8);
		RealJoint fxnext = new RealJoint(8);

		FactorGraph fg = new FactorGraph();
		fg.addFactor("constmult",fznonoise,H,fx);
		fg.addFactor("add",fz,fv,fznonoise);
		fg.addFactor("constmult",fxnext,F,fx);
	}
}
