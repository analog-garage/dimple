package com.analog.lyric.dimple.solvers.core.kbest;

import java.util.ArrayList;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
import com.analog.lyric.dimple.model.Port;

/*
 * Solver Factors that need to support kbest can implement this interface 
 * and use the KBestFactorTableEngine and KBestFactorEngine 
 */
public interface IKBestFactor
{
	ArrayList<Port> getPorts();
	FactorFunction getFactorFunction();
	double initAccumulator();
	double accumulate(double oldVal,double newVal);
	double combine(double oldVal,double newVal);
	void normalize(double [] outputMsg);
	FactorTable getFactorTable();
	double evalFactorFunction(Object [] inputs);
	void initMsg(double [] msg);
	double getFactorTableValue(int index);
	int [] findKBestForMsg(double [] msg,int k);
}
