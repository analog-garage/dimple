package com.analog.lyric.math;

import com.analog.lyric.dimple.model.DimpleException;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class LyricEigenvalueDecomposition extends EigenvalueDecomposition 
{

	public LyricEigenvalueDecomposition(Matrix arg0)  
	{
		super(checkMatrix(arg0));
		
		
		// TODO Auto-generated constructor stub
	}
	
	public static Matrix checkMatrix(Matrix m) 
	{
		for (int i = 0; i < m.getRowDimension(); i++)
		{
			for (int j = 0; j < m.getColumnDimension(); j++)
			{
				if (Double.isNaN(m.get(i,j)) || Double.isInfinite(m.get(i,j)))
				{
					throw new DimpleException("cannot do EigenValueDecomposition on matrix that contains NaN or infinite");
				}
			}
		}
		
		return m;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
