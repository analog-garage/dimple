package com.analog.lyric.dimple.model;

public class Discrete extends VariableBase
{

	public Discrete(int id,DiscreteDomain domain, String modelerClassName)
			 {
		super(id, modelerClassName, domain);
		// TODO Auto-generated constructor stub
	}
	
	public Discrete(DiscreteDomain domain) 
	{
		this(NodeId.getNext(),domain,"Discrete");
	}

	public Discrete(Object ... domain) 
	{
		this(NodeId.getNext(),new DiscreteDomain(domain),"Discrete");

		if(domain.length < 1)
			throw new DimpleException(String.format("ERROR Variable domain length %d must be at least 2", domain.length));

	}

    public double [] getInput()
    {
    	return (double[])getInputObject();
    }
    
    public DiscreteDomain getDiscreteDomain()
    {
    	return (DiscreteDomain)getDomain();
    }
    
    
    public Object getInputObject()
    {
    	if (_input == null)
    		return getDefaultPriors(getDiscreteDomain().getElements());
    	else
    		return _input;
    }
    
    public double [] getBelief() 
    {
    	return (double[])getBeliefObject();
    }
    
    public Object getBeliefObject() 
    {
    	if (_solverVariable != null)
    		return _solverVariable.getBelief();
    	else
    		return getInputObject();
    }
   
    
	private double [] getDefaultPriors(Object [] domain)
	{
		double [] retval = new double[domain.length];
		double val = 1.0/domain.length;
		for (int i = 0; i < retval.length; i++)
			retval[i] = val;
		return retval;
	}
	
	
    protected VariableBase createJointNoFactors(VariableBase otherVariable) 
    {
    	if (! (otherVariable instanceof Discrete))
    	{
    		throw new DimpleException("Not currently supported");
    		
    	}
    	
    	Discrete dOtherVar = (Discrete)otherVariable;
    	
    	//First, create domain
    	int newDomainLength = this.getDiscreteDomain().size()*dOtherVar.getDiscreteDomain().size();
    	
    	Object [] newDomain = new Object[newDomainLength];
    	double [] inputs = new double[newDomainLength];
    	
    	int index = 0;
		for (int j = 0; j <  dOtherVar.getDiscreteDomain().size(); j++)
		{
	    	for (int i = 0; i < getDiscreteDomain().size(); i++)
	    	{
    			Object [] pair = new Object[] {this.getDiscreteDomain().getElements()[i],dOtherVar.getDiscreteDomain().getElements()[j]};
    			newDomain[index] = pair;
    			inputs[index] = this.getInput()[i]*dOtherVar.getInput()[j];
    			index++;
    		}
    	}
    	
    	Discrete retval =  new Discrete(newDomain);
    	retval.setInput(inputs);
    	return retval;
    }
	
	public void setInput(double ... value) 
	{
		setInputObject((Object)value);
	}

    
}
