package com.analog.lyric.collect;

import java.util.Iterator;

/**
 * An {@link Iterator} that can release itself to be reused. These iterators
 * are expected to be used to avoid excessive allocation and garbage
 * collection in cases where many iterations are being performed.
 * <p>
 * Typical implementations will stash a reusable copy of the iterator
 * in a variable, which will be nulled out when the iterator has been
 * handed out but not yet released. Here is a partial example using
 * a {@link ThreadLocal} variable:
 * <p>
 * <pre>
 *     public class FooIterator implements ReleasableIterator<Foo>
 *     {
 *         private static final ThreadLocal<FooIterator> resusableInstance = new ThreadLocal<FooIterator>();
 *
 *         public static FooIterator make(FooContainer c)
 *         {
 *             FooIterator iter = (FooIterator)FooIterator.reusableInstance.get();
 *             if (iter != null) {
 *                 FooIterator.reusableInstance.set(null);
 *                 iter.reset(c);
 *             } else {
 *                 iter = new FooIterator(c);
 *             }
 *         }
 *
 *         public void release()
 *         {
 *             if (FooIterator.reusableInstance.get() == null) {
 *                 this.reset(null);
 *                 FooIterator.reusableInstance.set(this);
 *             }
 *         }
 *
 *         // Reset iterator to start of contents of container.
 *         public void reset(FooContainer c)  ...
 *     }
 * </pre>
 *
 * @see ReleasableIterable
 */
public interface ReleasableIterator<T> extends Iterator<T>
{
	/**
	 * Release this iterator so that it can be reused instead of being
	 * garbage-collected. The caller must not invoke any other methods
	 * on this object after releasing it.
	 */
	void release();
}
