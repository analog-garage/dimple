package com.analog.lyric.dimple.model.repeated;

public interface IDataSource 
{
	public boolean hasNext();
	public Object getNext() ;
}
