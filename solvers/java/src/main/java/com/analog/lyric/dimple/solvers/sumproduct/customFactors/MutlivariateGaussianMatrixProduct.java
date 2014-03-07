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

package com.analog.lyric.dimple.solvers.sumproduct.customFactors;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.math.LyricSingularValueDecomposition;

public class MutlivariateGaussianMatrixProduct 
{

	private int M, N;
	private double [/*M*/][/*N*/] A_clean;  //a form of A with zero singular values set to eps
	private double [/*M*/][/*N*/] A_pinv;   //a (left or right or both) inverse of A where zero singular values are inverted to 1/eps

	private double eps = 0.0000001; //minimum value for small eigenvalues or 1/(max value)


	//Initializer
	public  MutlivariateGaussianMatrixProduct(double[][] A) 
	{
		int i; //,m;		
		M = A.length; N = A[0].length;
		/* Here we precompute and store matrices for future message computations. 
		 * First, compute an SVD of the matrix A using EigenDecompositions of A*A^T and A^T*A
		 * This way, we get nullspaces for free along with regularized inverse.
		 */
		//EigenDecomposition LeftSingular = new EigenDecomposition(M);
		//EigenDecomposition RightSingular = new EigenDecomposition(N);

		Jama.Matrix Amat = new Jama.Matrix(A);
		
		boolean matrixIsFat = Amat.getColumnDimension() > Amat.getRowDimension(); 
		
		//TODO: Because Jama is lame and doesn't handle fat matrices, we have
		//to deal with the transpose.
		if (matrixIsFat)
		{
			Amat = Amat.transpose();
		}
		
		LyricSingularValueDecomposition svd = new LyricSingularValueDecomposition(Amat);
		Jama.Matrix U = svd.getU();
		Jama.Matrix S = svd.getS();
		Jama.Matrix V = svd.getV();

		if (matrixIsFat)
		{
			S = S.transpose();
			Jama.Matrix tmp = U;
			U = V.transpose();
			V = tmp;
		}
		
		//printVector(svd.getSingularValues());
		
		Jama.Matrix tmp = V.transpose();
		tmp = S.times(tmp);
		tmp = U.times(tmp);
		A_clean = tmp.getArray();
		
		Jama.Matrix Sinv = S.transpose();
		
		//int M = A.length;
		//int N = A[0].length;
		
		int numS = Math.min(Sinv.getColumnDimension(),Sinv.getRowDimension());
		for (i = 0; i < numS; i++)
		{
			double d = Sinv.get(i,i);
			if (d < eps)
				d = eps;
			Sinv.set(i,i, 1.0/d);
		}
		
		A_pinv = V.times(Sinv.times(U.transpose())).getArray();
		
	}

	
	
	public void ComputeMsg(MultivariateNormalParameters inMsg, MultivariateNormalParameters outMsg, char direction /* F for Forward, R for Reverse */) 
	{
		int m,n;
		//multiGaBPMsg outMsg;

		assert (direction == 'F' || direction == 'R'); //only two directions possible

		//TODO: this is really hacky!  the conversion to and from the inverse makes sure the matrix doesn't
		//grow too large.
		inMsg.getCovariance();
		inMsg.getInformationMatrix();
		

		if(direction == 'F') //Forward matrix multiply
		{
			//outMsg =  new multiGaBPMsg(M); //Output matrix is MxM

			assert(inMsg.getVectorLength() == N); //dimensions should match for it to be a valid forward matrix multiply

			if(!inMsg.isInInformationForm()) //We were given a covariance form inMsg
			{
				//outMsg.Type = 0; //We give the same output form (covariance)
				//Calculate A * V * A^T
				double [][] covar = inMsg.getCovariance();
				double [][] tmpMat = MatrixMult(A_clean, MatrixMult(covar, Transpose(A_clean)));
				//Incorporate left nullspace term: C*C^T*eps
				
				for (int i = 0; i < tmpMat.length; i++)
					tmpMat[i][i] += eps;
				
//				if(LeftNullTerm != null)
//					for(m=0;m<M;m++)
//						for(n=0;n<M;n++)
//							tmpMat[m][n] += LeftNullTerm[m][n] * eps;
				
				double [] tmpVector = new double[M];
				double [] inMsgVector = inMsg.getMean();
				//Compute mean vector output: m_y = A*m_x
				for(m=0;m<M;m++)
				{
					tmpVector[m] = 0;
					for(n=0;n<N;n++)
						tmpVector[m] += A_clean[m][n] * inMsgVector[n];
				}

				outMsg.setMeanAndCovariance(tmpVector,tmpMat);
				
//				System.out.println("calculated info vector");
//				outMsg.getInformationVector();
//				System.out.println("calculated means");
//				printVector(outMsg.getMeans());
				if (true)
					throw new DimpleException("is this tested?");

			}else{ //We were given an information form inMsg
				//outMsg.Type = 1; //We give the same output form (information)
				//Compute A^-T * W * A^-1
				//inMsg.getMeans();
				//printMatrix(inMsg.getInformationMatrix());
				double [][] tmpMat = MatrixMult(Transpose(A_pinv), MatrixMult(inMsg.getInformationMatrix(), A_pinv));
				//Incorporate left nullspace term: C*C^T/eps
//				if(LeftNullTerm  != null)
//					for(m=0;m<M;m++)
//						for(n=0;n<M;n++)
//							tmpMat[m][n] += LeftNullTerm[m][n] / eps;

				for (int i = 0; i < tmpMat.length; i++)
					tmpMat[i][i] += eps;

				double [] tmpVector = new double[M];
				double [] inMsgVector = inMsg.getInformationVector();
				
				//Compute information vector output: h_y = A^-T * h_x
				for(m=0;m<M;m++)
				{
					tmpVector[m] = 0;
					for(n=0;n<N;n++)
						tmpVector[m] += A_pinv[n][m] * inMsgVector[n];
				}
				outMsg.setInformation(tmpVector,tmpMat);
			}
			

		}else{ // Reverse matrix multiply

			//outMsg =  new multiGaBPMsg(N); //Output matrix is NxN

			assert(inMsg.getVectorLength() == M); //dimensions should match for it to be a valid reverse matrix multiply

			if(!inMsg.isInInformationForm()) //We were given a covariance form inMsg
			{

				//outMsg.Type = 0; //We give the same output form (covariance)
				//Compute A^-1 * V * A^-T
				double [][] tmpMat = MatrixMult(A_pinv, MatrixMult(inMsg.getCovariance(), Transpose(A_pinv)));
				//Incorporate nullspace term: B^T*B/eps
				
//				if(NullTerm  != null)
//					for(m=0;m<M;m++)
//						for(n=0;n<M;n++)
//							tmpMat[m][n] += NullTerm[m][n] / eps;

				for (int i = 0; i < tmpMat.length; i++)
					tmpMat[i][i] += eps;

				
				double [] tmpVector = new double[N];
				double [] inMsgVector = inMsg.getMean();
				//Compute mean vector: m_x = A^-1 * m_y
				for(m=0;m<N;m++)
				{
					tmpVector[m] = 0;
					for(n=0;n<M;n++)
						tmpVector[m] += A_pinv[m][n] * inMsgVector[n];
				}
				
				outMsg.setMeanAndCovariance(tmpVector,tmpMat);

				if (true)
					throw new DimpleException("is this tested?");

			}else{ //We were given an information form inMsg
				//outMsg.Type = 1; //We give the same output form (information)
				//Compute A^T*W*A
				double [][] tmpMat = MatrixMult(Transpose(A_clean), MatrixMult(inMsg.getInformationMatrix(), A_clean));
				//Incorporate nullspace term: B^T*B*eps
//				if(NullTerm != null)
//					for(m=0;m<M;m++)
//						for(n=0;n<M;n++)
//							tmpMat[m][n] += NullTerm[m][n] * eps;
				
				for (int i = 0; i < tmpMat.length; i++)
					tmpMat[i][i] += eps;

				double [] tmpVector = new double[N];
				double [] inMsgVector = inMsg.getInformationVector();
				
				//Compute information vector: h_x = A^T * h_y
				for(m=0;m<N;m++)
				{
					tmpVector[m] = 0;
					for(n=0;n<M;n++)
						tmpVector[m] += A_clean[n][m] * inMsgVector[n];
				}
				
				outMsg.setInformation(tmpVector, tmpMat);
			}

		}



	}

	public static void printVector (double [] vector)
	{
		for (int i = 0; i < vector.length; i++)
		{
			System.out.print(vector[i] + ", ");
		}
		System.out.println();
	}
	
	public static void printMatrix(double [][] matrix)
	{
		System.out.println("[");
		for (int i = 0; i < matrix.length; i++)
		{
			printVector(matrix[i]);
		}
		System.out.println("]");
	}



	//Does an actual multiplication of Matrix A * Matrix B
	public double[][] MatrixMult(double[][] A, double[][] B)
	{
		int m, n, mm;
		double [][] C = new double [A.length][B[0].length];

		assert(B.length == A[0].length); //for valid matrix multiply

		for(m=0;m<A.length;m++)
			for(n=0;n<B[0].length;n++)
			{
				C[m][n] = 0;
				for(mm=0;mm<B.length;mm++)
					C[m][n] += A[m][mm] * B[mm][n];
			}

		return C;
	}

	public double[][] Transpose(double[][] A)
	{
		int m,n;
		double [][] C = new double [A[0].length][A.length];

		for(m=0;m<A[0].length;m++)
			for(n=0;n<A.length;n++)
				C[m][n] = A[n][m];

		return C;

	}





}
