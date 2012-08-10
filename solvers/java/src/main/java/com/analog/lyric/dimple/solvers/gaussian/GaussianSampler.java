package com.analog.lyric.dimple.solvers.gaussian;

import java.util.Random;

import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.swedish.SwedishSampler;

public class GaussianSampler extends SwedishSampler 
{

	public GaussianSampler(Port p, Random random) 
	{
		super(p, random);
		// TODO Auto-generated constructor stub
	}

	private double [] _msg;
	
	@Override
	public void initialize() 
	{
		_msg = (double[])_p.getInputMsg();
	}

	@Override
	public Object generateSample() 
	{
		return _random.nextGaussian()*_msg[1]+_msg[0];
	}
	
}
