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

import Jama.Matrix;
import Jama.SingularValueDecomposition;

import com.analog.lyric.dimple.exceptions.DimpleException;

public class LyricSingularValueDecomposition extends SingularValueDecomposition 
{
	private boolean _isFat;
	
	public LyricSingularValueDecomposition(Matrix arg0)  
	{
		super(checkMatrix(arg0));
		
		if (arg0.getColumnDimension() > arg0.getRowDimension())
			_isFat = true;		
		
	}
	
	private static Matrix checkMatrix(Matrix m) 
	{
		for (int i = 0; i < m.getRowDimension(); i++)
		{
			for (int j = 0; j < m.getColumnDimension(); j++)
			{
				if (Double.isNaN(m.get(i,j)) || Double.isInfinite(m.get(i,j)))
				{
					throw new DimpleException("cannot do SVD on matrix that contains NaN or infinite");
				}
			}
		}
		
		if (m.getColumnDimension() > m.getRowDimension())
			return m.transpose();
		else
			return m;
	}

	@Override
	public Matrix getS()
	{
		return super.getS();
	}
	
	@Override
	public double [] getSingularValues()
	{
		return super.getSingularValues();
	}
	
	@Override
	public double cond()
	{
		return super.cond();
	}
	
	@Override
	public Matrix getU()
	{
		if (_isFat)
			return super.getV().transpose();
		else
			return super.getU();
	}
	
	@Override
	public Matrix getV()
	{
		if (_isFat)	
			return super.getU().transpose();
		else
			return super.getV();
	}
	
	@Override
	public double norm2()
	{
		return super.norm2();
	}
	
	@Override
	public int rank()
	{
		return super.rank();
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
