package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.model.repeated.FactorGraphStream;

public class PFactorGraphStream 
{
	private FactorGraphStream _stream;
	
	public PFactorGraphStream(FactorGraphStream stream)
	{
		_stream = stream;
	}
	
	public FactorGraphStream getModelerObject()
	{
		return _stream;
	}
	
	public boolean hasNext() 
	{
		return _stream.hasNext();
	}
	
	public void advance() 
	{
		_stream.advance();
	}
	
	public void setBufferSize(int size) 
	{
		_stream.setBufferSize(size);
	}
	
	public int getBufferSize()
	{
		return _stream.getBufferSize();
	}
}
