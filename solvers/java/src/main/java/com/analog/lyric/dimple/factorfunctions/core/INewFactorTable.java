package com.analog.lyric.dimple.factorfunctions.core;

import com.analog.lyric.dimple.model.DiscreteDomainListConverter;


public interface INewFactorTable extends INewFactorTableBase, IFactorTable
{
	@Override
	public INewFactorTable convert(DiscreteDomainListConverter converter);
	
	public NewFactorTableRepresentation getRepresentation();

	public void setRepresentation(NewFactorTableRepresentation representation);
}
