package com.analog.lyric.dimple.model;


public class Real extends VariableBase
{

	
	public Real(int id, String modelerClassName, RealDomain domain)
	 {
		super(id, modelerClassName, domain);
		// TODO Auto-generated constructor stub
	}

	//TODO: do I want to do that?
	public Real(int id, String modelerClassName, RealDomain domain,Object input)
	 {
		super(id, modelerClassName, domain);
		// TODO Auto-generated constructor stub
		setInputObject(input);
	}
	
	
	public Real()  
	{
		this(new RealDomain(), null);
	}

	public Real(RealDomain domain)  
	{
		this(domain, null);
	}
	public Real(Object input)  
	{
		this(new RealDomain(), input);
	}
	
	public Real(RealDomain domain, Object input) 
	{
		this(NodeId.getNext(), domain, input, "Real");
	}

	public Real(int id, RealDomain domain, Object input, String modelerClassName) 
	{
		//this(id,new RealDomain[]{domain},input,modelerClassName);
		super(id,modelerClassName,domain);
		//super(id, modelerClassName);
		//_domain = domain;
		_input = input;

	}

//	public Real(int id, RealDomain [] domains, Object input, String modelerClassName) 
//	{
//		super(id,modelerClassName,domains);
//		//super(id, modelerClassName);
//		//_domain = domain;
//		_input = input;
//	}
	
	public RealDomain getRealDomain()
	{
		return (RealDomain)getDomain();
	}
	public Object getInput() 
	{
		return getInputObject();
	}

}
