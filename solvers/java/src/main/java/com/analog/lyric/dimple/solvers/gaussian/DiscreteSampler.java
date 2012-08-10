package com.analog.lyric.dimple.solvers.gaussian;

import java.util.Random;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.swedish.SwedishSampler;

public class DiscreteSampler extends SwedishSampler 
{

	public DiscreteSampler(Port p, Random random) 
	{
		super(p, random);
		// TODO Auto-generated constructor stub
	}

	private double [] _msg;
	private Object [] _domain;
	
	@Override
	public void initialize()  
	{
		// TODO Auto-generated method stub
		_msg = (double[])_p.getInputMsg();
		INode n = _p.getConnectedNode();
		
		if (! (n instanceof Discrete))
			throw new DimpleException("expected Discrete");
		
		Discrete d = (Discrete)n;
		
		_domain = d.getDiscreteDomain().getElements();
	}

	@Override
	public Object generateSample() 
	{
		//normalize
		double sum = 0;
		for (int i = 0; i < _msg.length; i++)
			sum += _msg[i];
		
		double d = _random.nextDouble();
		
		double cum = 0;
		
		for (int i = 0; i < _msg.length; i++)
		{
			cum += _msg[i]/sum;
			
			if (d < cum)
			{
				return _domain[i];
			}
		}
		
		
		return _domain[_domain.length-1];
	}

}
