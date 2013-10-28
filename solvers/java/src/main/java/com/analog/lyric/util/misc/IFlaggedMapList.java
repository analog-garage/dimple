package com.analog.lyric.util.misc;

public interface IFlaggedMapList<T extends IGetId> extends IMapList<T>
{
	public void clearFlags();
	
	public boolean isFlagged(T node);
	
	public boolean isFlagged(int index);
	
	public void setFlag(T node, boolean value);
	
	public void setFlag(int index, boolean value);
}
