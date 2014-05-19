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

package com.analog.lyric.dimple.factorfunctions;

/**
 * 
 * @since 0.06
 * @author Jake
 */
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;


// Poisson distribution corresponding to p(obs|act), where obs is the observed number of counts
// and act is the actual number of counts.
//
// The conjugate prior for p is the Gamma distribution. It may be necessary to use a conjugate
// prior, depending on your choice of solver.
//
// The variables in the argument are as follows:
//
// 1) "y" is the observed number of counts
// 2) "n" is the true number of counts



public class Poisson extends FactorFunction
{

	//Declaring variables
	protected int _y;
	protected double _n;
	//protected double _cutoff;
	protected double _negativeLogFactorialy;
	protected double _Logn;
	protected boolean _yParameterConstant = false;
	private int _firstDirectedToIndex = 1;
	
	//For variable y
	public Poisson() {super();}
	//For fixed y
	public Poisson(int y)
	{
		this();
		_y=y;
		if (_y < 0) throw new DimpleException("y must be a non-negative value.");
		_negativeLogFactorialy = -org.apache.commons.math3.special.Gamma.logGamma((double)(_y + 1));
		_yParameterConstant=true;
		_firstDirectedToIndex=0;		
	}
	
	//Evaluating the energy
	@Override
	public double evalEnergy(Object... arguments)
	{
		int index = 0;
		//First argument of the factor: y
		if (!_yParameterConstant)
		{
			_y = FactorFunctionUtilities.toInteger(arguments[index++]);
			_negativeLogFactorialy = -org.apache.commons.math3.special.Gamma.logGamma((double)(_y + 1));
			if (_y < 0) return Double.POSITIVE_INFINITY;
		}
		
		//Second argument of the factor: n
		_n=FactorFunctionUtilities.toDouble(arguments[index++]);
		_Logn = Math.log(_n);
		if (_n < 0) return Double.POSITIVE_INFINITY;
		if (_n > 0)

			return -(-_n + _y*_Logn + _negativeLogFactorialy);

			else if (_n==0 && _y!=0)

			return Double.POSITIVE_INFINITY;

			else if (_n==0 && _y==0)

			return 0;


			return Double.POSITIVE_INFINITY;
		
	}
	@Override
	public final boolean isDirected() {return true;}
	@Override
	public final int[] getDirectedToIndices(int numEdges)
	{
		return FactorFunctionUtilities.getListOfIndices(_firstDirectedToIndex, numEdges-1);
	}
	
	//Factor-specific methods
	public final boolean hasConstantyParameter()
	{
		return _yParameterConstant;
	}
	public final int gety()
	{
		return _y;
	}
	
	
}
