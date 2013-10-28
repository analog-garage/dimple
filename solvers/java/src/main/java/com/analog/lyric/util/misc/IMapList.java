package com.analog.lyric.util.misc;

import java.util.Collection;
import java.util.List;

public interface IMapList<T extends IGetId> extends Collection<T>
{
	public abstract void addAll(T[] nodes);

	public abstract boolean contains(IGetId node);

	public abstract void ensureCapacity(int minCapacity);

	public abstract T getByKey(int id);

	public abstract T getByIndex(int index);

	public abstract List<T> values();
}
