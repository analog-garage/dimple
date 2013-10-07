package com.analog.lyric.dimple.solvers.template;

import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.solvers.core.STableFactorDoubleArray;

public class STableFactor  extends STableFactorDoubleArray 
{

	public STableFactor(Factor factor) 
	{
		super(factor);
		
		int firstLen = -1;
		
		for (INode sibling : _factor.getSiblings())
		{
			Discrete d = ((Discrete)sibling);
			int size = d.getDiscreteDomain().size();
			
			if (firstLen == -1)
				firstLen = size;
			else
			{
				if (size != firstLen)
					throw new DimpleException("all domains must be the same size");
			}
		}
	}

	@Override
	public void updateEdge(int outPortNum) 
	{
		double [] out = _outputMsgs[outPortNum];
		for (int i = 0; i < out.length; i++)
		{
			out[i] = 0;
		}
		
		for (int i = 0; i < _inputMsgs.length; i++)
		{
			if (outPortNum != i)
			{
				for (int j = 0; j < out.length; j++)
				{
					out[j] += _inputMsgs[i][j];
				}
			}
		}
		
	}

	@Override
	protected void setTableRepresentation(IFactorTable table) 
	{
		table.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT_WITH_INDICES);
	}

}
