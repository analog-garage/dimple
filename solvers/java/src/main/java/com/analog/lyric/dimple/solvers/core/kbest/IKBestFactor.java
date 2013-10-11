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

package com.analog.lyric.dimple.solvers.core.kbest;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.factors.Factor;

/*
 * Solver Factors that need to support kbest can implement this interface
 * and use the KBestFactorTableEngine and KBestFactorEngine
 */
public interface IKBestFactor
{
	//ArrayList<Port> getPorts();
	public double [][] getInPortMsgs();
	public double [][] getOutPortMsgs();
	public Factor getFactor();
	FactorFunction getFactorFunction();
	double initAccumulator();
	double accumulate(double oldVal,double newVal);
	double combine(double oldVal,double newVal);
	void normalize(double [] outputMsg);
	IFactorTable getFactorTable();
	double evalFactorFunction(Object [] inputs);
	void initMsg(double [] msg);
	double getFactorTableValue(int index);
	int [] findKBestForMsg(double [] msg,int k);
}
