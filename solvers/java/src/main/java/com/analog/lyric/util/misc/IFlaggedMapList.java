package com.analog.lyric.util.misc;

public interface IFlaggedMapList<T extends IGetId> extends IMapList<T>
{
	public void clearFlags();
	
	public boolean isFlagged(int index);
	
	public void setFlag(int index, boolean value);
}
