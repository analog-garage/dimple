/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * More Apache Matrix utilities ala {@linkplain org.apache.commons.math3.linear.MatrixUtils MatrixUtils}.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class MoreMatrixUtils
{
	/**
	 * Returns matrix as 2D row-major array.
	 * <p>
	 * If {@code matrix} is a {@link Array2DRowRealMatrix}, this returns its underlying array,
	 * otherwise returns a newly allocated array.
	 * <p>
	 * @since 0.08
	 */
	public static double[][] matrixGetDataRef(RealMatrix matrix)
	{
		if (matrix instanceof Array2DRowRealMatrix)
		{
			return ((Array2DRowRealMatrix)matrix).getDataRef();
		}
		
		return matrix.getData();
	}

	/**
	 * Returns vector as a an array.
	 * <p>
	 * If {@code vector} is a {@link ArrayRealVector}, this returns its underlying array,
	 * otherwise returns a newly allocated array.
	 * @since 0.08
	 */
	public static double[] vectorGetDataRef(RealVector vector)
	{
		if (vector instanceof ArrayRealVector)
		{
			return ((ArrayRealVector)vector).getDataRef();
		}
		
		return vector.toArray();
	}
	
	/**
	 * Return row-real matrix that uses specified underlying representation.
	 * <p>
	 * Simply shorthand for invoking {@link Array2DRowRealMatrix#Array2DRowRealMatrix(double[][], boolean)}
	 * constructor with {@code copyArray} argument set to false.
	 * @since 0.08
	 */
	public static Array2DRowRealMatrix wrapRealMatrix(double[][] data)
	{
		return new Array2DRowRealMatrix(data, false);
	}
	
	/**
	 * Return real vector that uses specified underlying representation.
	 * <p>
	 * Simply shorthand for invoking {@link ArrayRealVector#ArrayRealVector(double[], boolean)}
	 * constructor with {@code copyArray} argument set to false.
	 * @since 0.08
	 */
	public static ArrayRealVector wrapRealVector(double[] data)
	{
		return new ArrayRealVector(data, false);
	}
	
}
