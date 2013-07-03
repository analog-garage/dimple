package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import java.util.HashMap;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;


public class FactorInfo extends NodeInfo
{			

	public static int [] getVarIndices(Factor f,HashMap<VariableBase,Integer> var2index )
	{
		VariableList vl = f.getVariables();
		int [] retval = new int[vl.size()];
		
		for (int i = 0; i < retval.length; i++)
		{
			retval[i] = var2index.get(vl.getByIndex(i));
		}
		
		return retval;
	}
	
	public FactorInfo(Factor f, HashMap<VariableBase,Integer> var2index)
	{
		super(getVarIndices(f, var2index));
	}		
	
}

