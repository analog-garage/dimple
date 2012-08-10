package com.analog.lyric.dimple.model.repeated;

import com.analog.lyric.dimple.FactorFunctions.NopFactorFunction;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;

public class BlastFromThePastFactor extends Factor 
{
	
	private Object _msg;
	
	public BlastFromThePastFactor(int id, VariableBase var, Object msg) 
	{
		super(id,new NopFactorFunction("BlastFromThePast"),new VariableBase[]{var});		
		setOutputMsg(msg);

	}

	public void setOutputMsg(Object msg) 
	{
		_msg = msg;
		_ports.get(0).setOutputMsg(_msg);
	}
	
	public void initializePortMsg(Port port)
	{
		
	}
	
	
	
	@Override
	public void update()  
	{
		setOutputMsg(_msg);		
	}

	@Override
	public void updateEdge(int outPortNum)  
	{
		setOutputMsg(_msg);
		
	}
	
	
}
