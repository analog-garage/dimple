package com.analog.lyric.collect;

/**
 * An {@link Iterable} whose {@link #iterator} method returns a
 * {@link ReleasableIterator}, which may be released and reused.
 * <p>
 * If you need to perform frequent iteration with such objects,
 * it may be better to directly invoke the iterator and release
 * it when done rather than to implicitly iterate using {@code for}.
 */
public interface ReleasableIterable<T> extends Iterable<T>
{
	@Override
	ReleasableIterator<T> iterator();
}
