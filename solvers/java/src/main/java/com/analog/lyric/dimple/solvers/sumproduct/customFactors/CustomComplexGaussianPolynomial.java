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

import org.apache.commons.math3.complex.Complex;

import Jama.CholeskyDecomposition;
import Jama.Matrix;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;

public class CustomComplexGaussianPolynomial extends MultivariateGaussianFactorBase
{
	private double [] _powers;
	private Complex [] _coeffs;
	private int _iterations;

	/*
	 * TODO: changes
	 * expect complex x and y with covariance matrices
	 * expect array of complex variables for coefficients.
	 * modify newton raphson to deal with complex coefficients
	 * modify the code that calculates means and covariance
	 */
	public CustomComplexGaussianPolynomial(Factor factor)
	{
		super(factor);

		if (factor.getSiblingCount() != 2)
			throw new DimpleException("expected two complex numbers");

		//TODO: throw error message if htis cast fails
		Object[] constants = factor.getFactorFunction().getConstants();

		//TODO: error check
		double [] powers;
		double [] rcoeffs;
		double [] icoeffs;
		
		if (constants[0] instanceof Double)
		{
			powers = new double[]{(Double)constants[0]};
			rcoeffs = new double[]{(Double)constants[1]};
		}
		else
		{
			powers = (double[])constants[0];
			rcoeffs = (double[])constants[1];
		}
		
		if (constants.length > 2)
			icoeffs = (double[])constants[2];
		else
			icoeffs = new double[rcoeffs.length];
		
		_coeffs = new Complex[rcoeffs.length];
		
		for (int i = 0; i < _coeffs.length; i++)
			_coeffs[i] = new Complex(rcoeffs[i],icoeffs[i]);

		//TODO: error check pairs are in fact pairs.

		//TODO: only allow powers of 1, 3, 5, etc...

		_powers = powers;
		_iterations = 3;



		//double [] coeffs = (double[])constants[0];
	}

	public void setNumIterations(int num)
	{
		_iterations = num;
	}
	
	@Override
	protected void doUpdate()
	{
		updateToX();
		updateToY();
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		//TODO: somehow avoid double computation.
		//Maybe this is avoided when we have multivariate gaussian

		if (outPortNum == 0)
			updateToY();
		else
			updateToX();
	}

	public void updateToX()
	{
		
		//get mu and sigma for complex numbers
		MultivariateNormalParameters y = _inputMsgs[0];
		MultivariateNormalParameters x = _outputMsgs[1];

		Complex means = new Complex(y.getMean()[0],y.getMean()[1]);
	
		//get samples
		Complex [] samples = getSamples(means,y.getCovariance());
		Complex [] results = new Complex[samples.length];

		for (int i = 0; i < samples.length; i++)
		{
			results[i] = newtonRaphson(samples[i],_iterations,_powers,_coeffs);
		}

		Object [] sums = calculateWeightedSums(results);

		means = (Complex)sums[0];
		double [][] covar = (double[][])sums[1];
		
		x.setMeanAndCovariance(new double []{means.getReal(),means.getImaginary()}, covar);

	}

	public void updateToY()
	{
		//get mu and sigma for complex numbers
		MultivariateNormalParameters y = _outputMsgs[0];
		MultivariateNormalParameters x = _inputMsgs[1];
		
		double [] xmeans = x.getMean();
		double [][] xcovar = x.getCovariance();
	

		
		//get samples
		Complex [] samples = getSamples(new Complex(xmeans[0],xmeans[1]),xcovar);
		Complex [] results = new Complex[samples.length];
		
		//for each sample
		for (int i = 0; i < samples.length; i++)
		{
			results[i] = P(samples[i],_powers,_coeffs);

		}

		Object [] sums = calculateWeightedSums(results);
		Complex means = (Complex)sums[0];
		double [][] covar = (double[][])sums[1];

		y.setMeanAndCovariance(new double[]{means.getReal(),means.getImaginary()}, covar);
	}
	
	private Complex [] getSamples(Complex mean, double [][] covar)
	{
		double [] mean_array = new double[2];
		mean_array[0] = mean.getReal();
		mean_array[1] = mean.getImaginary();
		double [][] samples = new double[covar.length*2][];

		
		CholeskyDecomposition cd = new CholeskyDecomposition(new Matrix(covar));
		
		double [][] chol = cd.getL().transpose().getArray();
		
		for (int i = 0; i < chol.length; i++)
		{
			samples[i*2] = chol[i].clone();
			samples[i*2+1] = chol[i].clone();
			
			for (int j = 0; j < samples[i*2].length; j++)
			{
				samples[i*2][j] = samples[i*2][j]/Math.sqrt(covar.length) + mean_array[j];
				samples[i*2+1][j] = samples[i*2+1][j]/-Math.sqrt(covar.length) + mean_array[j];
			}
		}

		Complex [] retval = new Complex[samples.length];
		for (int i = 0; i < retval.length; i++)
			retval[i] = new Complex(samples[i][0],samples[i][1]);
		
		return retval;
	}

	private Object [] calculateWeightedSums(Complex [] in)
	{
		
		double [] means = new double[]{0,0};

		for (int i = 0; i < in.length; i++)
		{
			means[0] += in[i].getReal();
			means[1] += in[i].getImaginary();
		}
		means[0] /= in.length;
		means[1] /= in.length;
		
		
		Matrix cm = new Matrix(2,2);
		Matrix mm = new Matrix(means,1);
		
		for (int i = 0; i < in.length; i++)
		{
			Jama.Matrix m = new Matrix(new double [] {in[i].getReal(),in[i].getImaginary()},1);
			Matrix m2 = m.minus(mm);
			cm = cm.plus(m2.transpose().times(m2));
		}
		
		cm = cm.times(1.0/in.length);
		
		return new Object[]{new Complex(means[0],means[1]),cm.getArray()};
		
	}
	
	
	private static Complex P(Complex input, double [] powers, Complex [] coeffs)
	{

		double a = input.getReal();
		double b = input.getImaginary();

		double a2pb2 = a*a+b*b;

		Complex retval = new Complex(0,0);
		
		for (int i = 0; i < powers.length; i++)
		{

			Complex tmp = coeffs[i].multiply(input);
			double tmp2 = Math.pow(a2pb2,powers[i]);
			tmp = tmp.multiply(new Complex(tmp2,0));
			
			retval = retval.add(tmp);
		}

		return retval;
	}

	public static double derivativeOfP1OverA(double a, double b,double [] powers, double [] coeffs)
	{
		double sum = 0;
		
		double a2pb2 = a*a+b*b;
		
		for (int index = 0; index < powers.length; index++)
		{
			int i = (int)powers[index];
			double c_i = coeffs[index];
			
			sum += c_i*Math.pow(a2pb2,i);
			if (i >0)
				sum += i*c_i*Math.pow(a2pb2,i-1)*2*a*a;
		}
		
		if (Double.isNaN(sum))
			throw new DimpleException("derivativeOfP1OverA generated NaN");
		
		return sum;
	}
	
	private static double sharedDerivative(double a, double b, double [] powers, double [] coeffs)
	{
		//sum i >=1 i *c_i*(a^2+b^2)*2ab
		double sum = 0;
		double a2pb2 = a*a+b*b;
		
		for (int index = 0; index < powers.length; index++)
		{
			double i = powers[index];
			double c_i = coeffs[index];
			
			if (i > 0)
				sum += i*c_i*Math.pow(a2pb2,i-1)*2*a*b;
		}
		

		
		return sum;
	}
	
	public static double derivativeOfP1OverB(double a, double b, double [] powers, double [] coeffs)
	{
		double tmp =  sharedDerivative(a, b,  powers, coeffs);
		if (Double.isNaN(tmp))
			throw new DimpleException("derivativeOfP1OverB generated NaN");
		return tmp;
	}
	
	public static double derivativeOfP2OverA(double a, double b, double [] powers, double [] coeffs)
	{
		double tmp =  sharedDerivative(a, b,  powers, coeffs);
		if (Double.isNaN(tmp))
			throw new DimpleException("derivativeOfP2OverA generated NaN");
		return tmp;
	}
	
	public static double derivativeOfP2OverB(double a, double b, double [] powers, double [] coeffs)
	{
		double sum = 0;
		
		double a2pb2 = a*a+b*b;
		
		for (int index = 0; index < powers.length; index++)
		{
			int i = (int)powers[index];
			double c_i = coeffs[index];
			
			sum += c_i*Math.pow(a2pb2,i);
			if (i >0)
				sum += i*c_i*Math.pow(a2pb2,i-1)*2*b*b;
		}
		
		if (Double.isNaN(sum))
			throw new DimpleException("derivativeOfP2OverB generated NaN");

		
		return sum;
	}
	
	public static double[][] invertMatrix(double [][] M)
	{
		double a = M[0][0];
		double b = M[0][1];
		double c = M[1][0];
		double d = M[1][1];
		
		double constant = 1 / (a*d - b*c);
		
		double [][] inverse = new double[][]{
			new double[] {d*constant, -b*constant},
			new double[] {-c*constant, a*constant}
		};
		
		return inverse;
		
	}
	
	public static double [] matrixMultiply(double [][] M, double [] input)
	{
		double [] output = new double[input.length];
		
		for (int row = 0; row < input.length; row++)
		{
			double sum = 0;
			for (int col = 0; col < input.length; col++)
			{
				sum += input[col]*M[row][col];
			}
			output[row] = sum;
		}
		
		return output;
	}
	
	public static double [] vectorMultiply(double [] vec, double scalar)
	{
		double [] retval = vec.clone();
		
		for (int i = 0; i < retval.length; i++)
			retval[i] *= scalar;
		
		return retval;
	}
	
	public static double [] addScalar(double [] a, double b)
	{
		double [] retval = new double [a.length];
		for (int i = 0; i < a.length; i++)
		{
			retval[i] = a[i] + b;
		}
		return retval;
	}
	
	public static double [] addVectors(double [] a, double [] b)
	{
		double [] retval = new double [a.length];
		for (int i = 0; i < a.length; i++)
			retval[i] = a[i]+b[i];
		
		return retval;
	}
	
	public static double [][] buildJacobian(Complex [] coeffs,double [] powers, Complex x)
	{
		double a = x.getReal();
		double b = x.getImaginary();
		double a2pb2 = a*a+b*b;
		
		
		Complex dyda = new Complex(0,0);
		Complex dydb = new Complex(0,0);
		
		for (int k = 0; k < coeffs.length; k++)
		{
			double pow = powers[k];
			
			if (pow == 0)
			{
				dyda = dyda.add(coeffs[k]);
				dydb = dydb.add(coeffs[k].multiply(new Complex(0,1)));
			}
			else
			{
				dyda = dyda.add(coeffs[k].multiply(
						x.multiply(new Complex(2*a*k*Math.pow(a2pb2,k-1),0)).add(
								new Complex(Math.pow(a2pb2,k),0))));
				dydb = dydb.add(coeffs[k].multiply(
						x.multiply(new Complex(2*b*k*Math.pow(a2pb2,k-1),0)).add(
								new Complex(0,Math.pow(a2pb2,k)))));


			}
		}
		
		return new double [][]
		                     {
				new double [] {dyda.getReal(),dydb.getReal()},
				new double [] {dyda.getImaginary(),dydb.getImaginary()}
		                     };
	}

	
	public static Complex newtonRaphson(Complex input, int numIterations, double [] powers,
			Complex [] coeffs)
	{
		//for some number of iterations
		//initialize x to y;
		Complex output = new Complex(input.getReal(),input.getImaginary());
				
	
		for (int i = 0; i < numIterations; i++)
		{
			double [][] J = buildJacobian(coeffs,powers,output);
			
			Complex pout = P(output,powers,coeffs);
			Complex y = input.subtract(pout);
			
			double [][] Jinv = invertMatrix(J);
			double [] tmp = matrixMultiply(Jinv,new double [] {y.getReal(),y.getImaginary()});
			
			output = output.add(new Complex(tmp[0],tmp[1]));
		}

		return output;
	}
	
}
