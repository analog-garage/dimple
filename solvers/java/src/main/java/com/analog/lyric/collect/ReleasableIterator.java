/*******************************************************************************
 *   Copyright 2012 Analog Devices, Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ********************************************************************************/

package com.analog.lyric.collect;

import java.util.Iterator;

/**
 * An {@link Iterator} that can release itself to be reused. These iterators
 * are expected to be used to avoid excessive allocation and garbage
 * collection in cases where many iterations are being performed.
 * <p>
 * Typical implementations will stash a reusable copy of the iterator in a variable, which will be
 * nulled out when the iterator has been handed out but not yet released. Here is a partial example
 * using a {@link ThreadLocal} variable:
 * <p>
 * 
 * <pre>
 *     public class FooIterator implements ReleasableIterator&lt;Foo&gt;
 *     {
 *         private static final ThreadLocal&lt;FooIterator&gt; resusableInstance = new ThreadLocal&lt;FooIterator&gt;();
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
 * Other possible caching techniques include use of a static
 * {@linkplain java.util.concurrent.atomic.AtomicReference AtomicReference} or to cache
 * a reusable instance in the instance of the class that is responsible for creating the iterator.
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
