package com.analog.lyric.dimple.model.repeated;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.VariableBase;

public interface IVariableStreamSlice 
{
	//public void reset();
	public VariableBase getNext() ;
	public ArrayList<VariableBase> releaseFirst() ;
	public boolean hasNext() ;
	public void backup(double howmuch) ;
	public IVariableStreamSlice copy();
	public VariableStreamBase getStream();
}
