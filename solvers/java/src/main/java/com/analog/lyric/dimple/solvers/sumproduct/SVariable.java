package com.analog.lyric.dimple.solvers.sumproduct;

import com.analog.lyric.dimple.model.DiscreteDomain;
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
    double [][] _inPortMsgs;
    double [][] _logInPortMsgs;
    double [][] _outMsgArray;
    double [][] _savedOutMsgArray;
    double [] _dampingParams = new double[0];
    boolean _initCalled = true;
    protected double [] _input;
    //protected Discrete _varDiscrete;
    private int _guessIndex = 0;
    private boolean _guessWasSet = false;
    private DiscreteDomain _domain;
    
	public SVariable(VariableBase var)  
    {
		super(var);
		
		if (!var.getDomain().isDiscrete())
			throw new DimpleException("only discrete variables supported");
		
		_domain = (DiscreteDomain)var.getDomain();
		
		//_varDiscrete = (Discrete)_var;
		_input = (double[])getDefaultMessage(null);
	}
	
	public VariableBase getVariable()
	{
		return _var;
	}

	public Object getDefaultMessage(Port port) 
	{
		//TODO: both variable and factor do this.  Why doesn't factor jsut ask variable?
    	double [] retVal = new double[_domain.getElements().length];
    	double val = 1.0/retVal.length;
    	for (int i = 0; i < retVal.length; i++)
    		retVal[i] = val;
    	return retVal;	
    }
	
	public int getValueIndex()
	{
		int index = -1;
		double [] belief = (double[]) getBelief();		
		double maxBelief = -1;
		
		for (int i = 0; i < belief.length; i++)
		{
			if (belief[i] > maxBelief)
			{
				index = i;
				maxBelief = belief[i];
			}
		}
		
		return index;
	}
	
	public Object getValue()
	{
		int index = getValueIndex();
		//Discrete var = (Discrete)getVariable();
		return _domain.getElements()[index];
	}

	public int getGuessIndex()
	{
		int index = 0;
		if (_guessWasSet)
			index = _guessIndex;
		else
			index = getValueIndex();
		
		return index;
	}
	
	public Object getGuess()
	{
		//Discrete var = (Discrete)getVariable();
		int index = getGuessIndex();
		return _domain.getElements()[index];
	}
	
	public void setGuess(Object guess) 
	{
		//Discrete var = (Discrete)getVariable();
		int guessIndex = -1;
		for (int i = 0; i < _domain.getElements().length; i++)
		{
			if (_domain.getElements()[i].equals(guess))
			{
				guessIndex = i;
				break;
			}
		}
		if (guessIndex == -1)
			throw new DimpleException("guess not valid value");
		
		setGuessIndex(guessIndex);
	}
	
	public void setGuessIndex(int index) 
	{
		//Discrete var = (Discrete)getVariable();
		if (index < 0 || index >= _domain.getElements().length)
			throw new DimpleException("illegal index");
		
		_guessWasSet = true;
		_guessIndex = index;
	}
	
	public double getEnergy()
	{
		int index = getGuessIndex();
		
		double maxInput = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < _input.length; i++)
		{
			if (_input[i] >maxInput)
				maxInput = _input[i];
		}
		
		return -Math.log(_input[index]) + Math.log(maxInput);
	}
	
    public void setInput(Object priors) 
    {
    	double [] vals = (double[])priors;
    	if (vals.length != _domain.getElements().length)
    		throw new DimpleException("length of priors does not match domain");
    	
    	_input = vals;
    	
    }
	public void setDamping(int portIndex,double dampingVal)
	{
		if (portIndex >= _dampingParams.length)
		{
			double [] tmp = new double [portIndex+1];
			for (int i = 0; i < _dampingParams.length; i++)				
				tmp[i] = _dampingParams[i];
			
			_dampingParams = tmp;
		}

		_dampingParams[portIndex] = dampingVal;
	}
	
	public double getDamping(int portIndex)
	{
		if (portIndex >= _dampingParams.length)
			return 0;
		else
			return _dampingParams[portIndex];
	}
	

    public void updateEdge(int outPortNum) 
    {
    	updateCache();
    	
        final double minLog = -100;
        double[] priors = (double[])_input;
        int M = priors.length;
        int D = _var.getPorts().size();
        double maxLog = Double.NEGATIVE_INFINITY;

        double[] outMsgs = _outMsgArray[outPortNum];

        double [] saved = _savedOutMsgArray[outPortNum];
        double damping = _dampingParams[outPortNum];
        
        
        if (damping != 0)
        	for (int i = 0; i < outMsgs.length; i++)
        		saved[i] = outMsgs[i];

        
        for (int m = 0; m < M; m++)
        {
        	double prior = priors[m];
        	double out = (prior == 0) ? minLog : Math.log(prior);
        	
	        for (int d = 0; d < D; d++)
	        {
	        	if (d != outPortNum)		// For all ports except the output port
	        	{
	        		double tmp = _inPortMsgs[d][m];
	        		out += (tmp == 0) ? minLog : Math.log(tmp);
	        	}
	        }
        	if (out > maxLog) maxLog = out;
        	outMsgs[m] = out;
        }
        
        //create sum
        double sum = 0;
        for (int m = 0; m < M; m++)
        {
        	double out = Math.exp(outMsgs[m] - maxLog);
        	outMsgs[m] = out;
        	sum += out;
        }
        
        //calculate message by dividing by sum
        for (int m = 0; m < M; m++)
        	outMsgs[m] /= sum;

        if (damping != 0)
        	for (int m = 0; m < M; m++)
        		outMsgs[m] = outMsgs[m]*(1-damping) + saved[m]*damping;
	    
    }
    

    public void update() 
    {
    	updateCache();

        final double minLog = -100;
        double[] priors = (double[])_input;
        int M = priors.length;
        int D = _var.getPorts().size();
        
        
        //Compute alphas
        double[] alphas = new double[M];
        for (int m = 0; m < M; m++)
        {
        	double prior = priors[m];
        	double alpha = (prior == 0) ? minLog : Math.log(prior);

        	for (int d = 0; d < D; d++)
	        {
	        	double tmp = _inPortMsgs[d][m];
        		double logtmp = (tmp == 0) ? minLog : Math.log(tmp);
        		_logInPortMsgs[d][m] = logtmp;
        		alpha += logtmp;
	        }
	        alphas[m] = alpha;
        }
        
        //Now compute output messages for each outgoing edge
	    for (int out_d = 0; out_d < D; out_d++ )
	    {
            double[] outMsgs = _outMsgArray[out_d];
            double [] saved = _savedOutMsgArray[out_d];
            double damping = _dampingParams[out_d];
            
            
            if (damping != 0)
            	for (int i = 0; i < outMsgs.length; i++)
            		saved[i] = outMsgs[i];
            
            
            double maxLog = Double.NEGATIVE_INFINITY;
            
            //set outMsgs to alpha - mu_d,m
            //find max alpha
            double[] logInPortMsgsD = _logInPortMsgs[out_d];
            for (int m = 0; m < M; m++)
            {
            	double out = alphas[m] - logInPortMsgsD[m];
                if (out > maxLog) maxLog = out;
                outMsgs[m] = out;
            }
            
            //create sum
            double sum = 0;
            for (int m = 0; m < M; m++)
            {
                double out = Math.exp(outMsgs[m] - maxLog);
                outMsgs[m] = out;
                sum += out;
            }
            
            //calculate message by dividing by sum
            for (int m = 0; m < M; m++)
            {
            	outMsgs[m] /= sum;
            }
            
            
            if (damping != 0)
            	for (int m = 0; m < M; m++)
            		outMsgs[m] = outMsgs[m]*(1-damping) + saved[m]*damping;
            
	    }
    }
    
    public Object getBelief()
    {
    	updateCache();

        final double minLog = -100;
        double[] priors = (double[])_input;
        int M = priors.length;
        int D = _var.getPorts().size();
        double maxLog = Double.NEGATIVE_INFINITY;

        double[] outBelief = new double[M];

        for (int m = 0; m < M; m++)
        {
        	double prior = priors[m];
        	double out = (prior == 0) ? minLog : Math.log(prior);
        	
	        for (int d = 0; d < D; d++)
	        {
	        	double tmp = _inPortMsgs[d][m];
	        	out += (tmp == 0) ? minLog : Math.log(tmp);
	        }
        	if (out > maxLog) maxLog = out;
        	outBelief[m] = out;
        }
        
        //create sum
        double sum = 0;
        for (int m = 0; m < M; m++)
        {
        	double out = Math.exp(outBelief[m] - maxLog);
        	outBelief[m] = out;
        	sum += out;
        }
        
        //calculate belief by dividing by sum
        for (int m = 0; m < M; m++)
        	outBelief[m] /= sum;
        
        return outBelief;
    }
    
    

    
    protected void updateCache()
    {
    	if (_initCalled)
    	{
    		_initCalled = false;
	    	int D = _var.getPorts().size();    	
			int M = ((double[])_input).length;
						
			_inPortMsgs = new double[D][];
			_logInPortMsgs = new double[D][M];
			_outMsgArray = new double[D][];
		    _savedOutMsgArray = new double[D][];
		    
		    if (_dampingParams.length != D)
		    {
		    	double [] tmp = new double[D];
		    	for (int i = 0; i < _dampingParams.length; i++)
		    	{
		    		if (i < tmp.length)
		    			tmp[i] = _dampingParams[i];
		    	}
		    	_dampingParams = tmp;
		    }
		    		    
		    for (int d = 0; d < D; d++) 
		    	_inPortMsgs[d] = (double[])_var.getPorts().get(d).getInputMsg();
			
	        //Now compute output messages for each outgoing edge
		    for (int out_d = 0; out_d < D; out_d++ )
		    {
	            _outMsgArray[out_d] = (double[])_var.getPorts().get(out_d).getOutputMsg();
	            _savedOutMsgArray[out_d] = new double[_outMsgArray[out_d].length];
		    }
		    
    	}
    }
    

	public void initialize()
	{
		
		//Flag that init was called so that we can update the cache next time we need cached
		//values.  We can't do the same thing as the tableFunction (update the cache here)
		//because the function init gets called after variable init.  If we updated teh cache
		//here, the table function init would replace the arrays for the outgoing message
		//and our update functions would update stale messages.
		_initCalled = true;
		
		_guessWasSet = false;
	}


	public void remove(Factor factor)
	{
		_initCalled = true;
	}
	
	@Override
	public void connectPort(Port p)  
	{
		// TODO Auto-generated method stub
		_initCalled = true;
		
	}



}
