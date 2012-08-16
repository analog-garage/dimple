/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

package com.analog.lyric.dimple.solvers.sumproduct;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.solvers.core.SFactorBase;

//import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class FiniteFieldMult extends SFactorBase
{
	//TODO: store this here or cache somewhere else?
	//      Maybe create a multition for this?
	//TODO: Do we want double or float multiply?
	//TODO: Do we want complexForward or realForward?
	private DoubleFFT_1D _fft;
	
	
	public FiniteFieldMult(Factor factor)  
	{
		super(factor);
		if (factor.getPorts().size() != 3)
			throw new DimpleException("Only supports 3 arguments");
		
		//TODO: check all have same prim poly
		VariableList variables = factor.getVariables();
		
		//TODO: error check
		int poly = ((FiniteFieldVariable)variables.getByIndex(0).getSolver()).getTables().getPoly();
		for (int i = 1; i < 3; i++)
		{			
			if (((FiniteFieldVariable)variables.getByIndex(i).getSolver()).getTables().getPoly() != poly)
			{
				//TODO: better error message
				throw new DimpleException("polys don't match");
			}
		}
		
		//((Discrete)variables.getByIndex(0));
		_fft = new DoubleFFT_1D(((Discrete)variables.getByIndex(0)).getDiscreteDomain().getElements().length-1);

	}

	@Override
	public void update() 
	{
		updateToZ();		
		updateToX();
		updateToY();
	}
	
	public void updateToX()
	{

		double [] xOutput = (double[])_factor.getPorts().get(0).getOutputMsg();
		double [] yInput = (double[])_factor.getPorts().get(1).getInputMsg();		
		double [] zInput = (double[])_factor.getPorts().get(2).getInputMsg();
		
		updateBackward(yInput,zInput,xOutput);
	}
	
	public void updateToY()
	{
		double [] yOutput = (double[])_factor.getPorts().get(1).getOutputMsg();
		double [] xInput = (double[])_factor.getPorts().get(0).getInputMsg();		
		double [] zInput = (double[])_factor.getPorts().get(2).getInputMsg();
		
		updateBackward(xInput,zInput,yOutput);
		
	}
	
	public void updateBackward(double [] yInput,double [] zInput, double [] xOutput)
	{
		int [] dlogTable = ((FiniteFieldVariable)_factor.getVariables().getByIndex(0).getSolver()).getTables().getDlogTable();
		int [] powTable = ((FiniteFieldVariable)_factor.getVariables().getByIndex(0).getSolver()).getTables().getPowerTable();

		//Sort x, y, and z so that probs are stored in logs
		double [] dlogx = new double[(xOutput.length-1)*2];
		double [] dlogy = new double[dlogx.length];
		double [] dlogz = new double[dlogx.length];
				
		for (int i = 1; i < dlogTable.length; i++)
		{
			int dlog = dlogTable[i];
			
			int tmp = (dlogTable.length - 1 - dlog)%(dlogTable.length-1);
			dlogz[dlog*2] = zInput[i];
			dlogy[tmp*2] = yInput[i];
		}
		
		xOutput[0] = zInput[0];
		
		//perform fft on two inputs
		_fft.complexForward(dlogy);
		_fft.complexForward(dlogz);
		
		//pointwise multiply
		for (int i = 0; i < dlogz.length; i+=2)
		{
			dlogx[i] = dlogy[i]*dlogz[i] - dlogy[i+1]*dlogz[i+1];
			dlogx[i+1] = dlogy[i]*dlogz[i+1] + dlogy[i+1]*dlogz[i];
		}
		

		//compute inverse FFT
		//TODO: the scaling could be slow?  Can I avoid scaling?
		_fft.complexInverse(dlogx,true);
		
		
		//sort back
		double sum = xOutput[0];
		for (int i = 0; i < dlogx.length; i += 2)
		{
			double val = 0;
			
			//threshold negative to zero
			if (dlogx[i] > 0)
				val = dlogx[i];

			xOutput[powTable[i/2]] = val + yInput[0]*zInput[0]; 			  
			sum += val;
		}
	
		
		//normalize, considering 0
		for (int i = 0; i < xOutput.length; i++)
			xOutput[i] /= sum;
	}
	
	public void updateToZ()
	{
		//p(Z=0) = p(X=0) + p(Y=0) - p(X=0)*p(Y=0)
		//p(Z=a) = p(X*Y=a) = p(dlog(x) + dlog(y) = dlog(a))
		//       = SUM (over i) p(dlog(x) == i) * p(dlog(y) == dlog(a) - i)

		
		double [] xInput = (double[])_factor.getPorts().get(0).getInputMsg();
		double [] yInput = (double[])_factor.getPorts().get(1).getInputMsg();		
		double [] zOutput = (double[])_factor.getPorts().get(2).getOutputMsg();

		
		
		int [] dlogTable = ((FiniteFieldVariable)_factor.getVariables().getByIndex(0).getSolver()).getTables().getDlogTable();
		int [] powTable = ((FiniteFieldVariable)_factor.getVariables().getByIndex(0).getSolver()).getTables().getPowerTable();
		
		//Sort x, y, and z so that probs are stored in logs
		double [] dlogx = new double[(xInput.length-1)*2];
		double [] dlogy = new double[dlogx.length];
		double [] dlogz = new double[dlogx.length];
		
		for (int i = 1; i < dlogTable.length; i++)
		{
			int dlog = dlogTable[i];
			
			dlogx[dlog*2] = xInput[i];
			dlogy[dlog*2] = yInput[i];
		}
		
		
		//calculate p(0)
		zOutput[0] = xInput[0] + yInput[0] - xInput[0]*yInput[0];
		
		
		//perform fft on two inputs
		_fft.complexForward(dlogx);
		_fft.complexForward(dlogy);
		
		//pointwise multiply
		for (int i = 0; i < dlogz.length; i+=2)
		{
			dlogz[i] = dlogx[i]*dlogy[i] - dlogx[i+1]*dlogy[i+1];
			dlogz[i+1] = dlogx[i]*dlogy[i+1] + dlogx[i+1]*dlogy[i];
		}
		

		//compute inverse FFT
		//TODO: is scaling slower or faster?
		_fft.complexInverse(dlogz,false);
		
		//unsort back
		//double sum = zOutput[0];
		double sum = 0;
		for (int i = 0; i < dlogz.length; i += 2)
		{
			double val = 0;
			
			//threshold negative to zero
			if (dlogz[i] > 0)
				val = dlogz[i];

			zOutput[powTable[i/2]] = val; 			  
			sum += val;
		}
	
		
		//TODO: Is this enough to avoid NaNs?
		if (sum > 0)
			for (int i = 1; i < zOutput.length; i++)
				zOutput[i] = (1-zOutput[0])*zOutput[i]/sum;

	}
	
	public void updateEdge(int outPortNum)  
	{
		if (outPortNum == 0)
			updateToX();
		else if (outPortNum == 1)
			updateToY();
		else if (outPortNum == 2)
			updateToZ();
		else
			throw new DimpleException("unexpected port num");
	}
	
	@Override
	public double getEnergy()  
	{
		throw new DimpleException("getEnergy not yet supported for FiniteFieldMult");
	}
}
