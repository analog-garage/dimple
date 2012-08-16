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

package com.analog.lyric.dimple.solvers.gaussian;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SVariableBase;



public class SVariable extends SVariableBase 
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */	
	private double [] _input = (double[]) getDefaultMessage(null);
    
	public SVariable(VariableBase var) 
    {
		super(var);
	}
	

	public Object getDefaultMessage(Port port) 
	{
		return new double []{0,Double.POSITIVE_INFINITY};
		//return new double[]{0,1};
    }
	

    public void setInput(Object priors) 
    {
    	double [] vals = (double[])priors;
    	if (vals.length != 2)
    		throw new DimpleException("expect priors to be a vector of mean and sigma");
    	
    	if (vals[1] < 0)
    		throw new DimpleException("expect sigma to be >= 0");
    	
    	_input = vals.clone();
    	
    }
    
    public void updateEdge(int outPortNum) 
    {
    	ArrayList<Port> ports = _var.getPorts();
    	
    	double R = 1/(_input[1]*_input[1]);
    	double Mu = _input[0]*R;
    	
    	boolean anyRisInfinite = false;
    	double MuOfInf = 0;
    	
    	if (R == Double.POSITIVE_INFINITY)
    	{
    		anyRisInfinite = true;
    		MuOfInf = _input[0];
    	}
    	
    	
    	for (int i = 0; i < ports.size(); i++)
    	{
    		if (i != outPortNum)
    		{
    			double [] msg = (double[])ports.get(i).getInputMsg();
    			double tmpR = 1/(msg[1]*msg[1]);
    			
    			if (tmpR == Double.POSITIVE_INFINITY)
    			{
    				if (!anyRisInfinite)
    				{
	    				anyRisInfinite = true;
	    				MuOfInf = msg[0];    					
    				}
    				else
    				{
    					if (MuOfInf != msg[0])
    						throw new DimpleException("variable node failed in gaussian solver because " +
    								"two incoming messages were certain of conflicting things.");
    								
    				}
    			}
    			else
    			{	    			
	    			R += tmpR;
	    			Mu += tmpR * msg[0];
    			}
    		}
    	}
    	
    	double sigma = Math.sqrt(1/R);
    	
    	if (R == Double.POSITIVE_INFINITY && ! anyRisInfinite)
    		throw new DimpleException("this case isn't handled yet");
    	
    	if (anyRisInfinite)
    	{
    		Mu = MuOfInf;
    		sigma = 0;
    	}
    	else
    	{
	    	if (R != 0)
	    		Mu /= R;
	    	else
	    		Mu = 0;
    	}
    	
    	double [] outMsg = (double[])ports.get(outPortNum).getOutputMsg();
    	outMsg[0] = Mu;
    	outMsg[1] = sigma;
    }
    

    
    public Object getBelief() 
    {
    	ArrayList<Port> ports = _var.getPorts();
    	
    	double R = 1/(_input[1]*_input[1]);
    	double Mu = _input[0]*R;
    	
    	boolean anyRisInfinite = false;
    	double MuOfInf = 0;
    	
    	if (R == Double.POSITIVE_INFINITY)
    	{
    		anyRisInfinite = true;
    		MuOfInf = _input[0];
    	}

    	
    	for (int i = 0; i < ports.size(); i++)
    	{
			double [] msg = (double[])ports.get(i).getInputMsg();
			double tmpR = 1/(msg[1]*msg[1]);
			
			
			if (tmpR == Double.POSITIVE_INFINITY)
			{
				if (!anyRisInfinite)
				{
    				anyRisInfinite = true;
    				MuOfInf = msg[0];    					
				}
				else
				{
					if (MuOfInf != msg[0])
						throw new DimpleException("variable node failed in gaussian solver because " +
								"two incoming messages were certain of conflicting things.");
								
				}
			}
			else
			{	    			
    			R += tmpR;
    			Mu += tmpR * msg[0];
			}

			/*
			R += tmpR;
			Mu += tmpR * msg[0];
			*/
    	}

    	double sigma = Math.sqrt(1/R);

    	if (R == Double.POSITIVE_INFINITY && ! anyRisInfinite)
    		throw new DimpleException("this case isn't handled yet");

    	if (anyRisInfinite)
    	{
    		Mu = MuOfInf;
    		sigma = 0;
    	}
    	else
    	{
	    	if (R != 0)
	    		Mu /= R;
	    	else
	    		Mu = 0;
    	}
    	
    	
    	return new double []{Mu,sigma};
    
    }
    
    

	public void initialize()
	{
		
	}


	public void remove(Factor factor)
	{
	}



	/*
	@Override
	public void setDomain(RealDomain domain)  
	{
		if (domain.getLowerBound() != Double.NEGATIVE_INFINITY)
			throw new DimpleException("bounds not supported for gaussian solver");
		if (domain.getUpperBound() != Double.POSITIVE_INFINITY)
			throw new DimpleException("bounds not supported for gaussain solver");
		// TODO Auto-generated method stub
		
	}

	 */





}
