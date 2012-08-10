package com.analog.lyric.dimple.model;

public class DimpleException extends RuntimeException implements IDimpleException 
{

	private static final long serialVersionUID = 1L;

	public DimpleException(String message)
	{
		super(message);
	}

	public DimpleException(Exception e)
	{
		super(e);
	}
	
	@Override
	public Exception getException() 
	{
		return this;
	}
	
	
}
