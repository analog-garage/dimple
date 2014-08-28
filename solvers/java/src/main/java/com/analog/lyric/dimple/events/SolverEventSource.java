/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.events;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.util.misc.Internal;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public abstract class SolverEventSource extends DimpleOptionHolder implements ISolverEventSource, ISolverNode
{
	/*-----------
	 * Constants
	 */
	
	/**
	 * Bits in {@link #_flags} reserved by this class.
	 */
	protected static final int RESERVED_FLAGS = 0xF0000000;
	
	/*-------
	 * State
	 */
	
	/**
	 * Temporary flags that can be used to mark the node during the execution of various algorithms
	 * or to mark non-static attributes of the node.
	 * <p>
	 * The flags are automatically cleared by {@link #initialize()}.
	 * <p>
	 * Subclasses should document which flags are reserved for use by that class and which ones
	 * are available for use by subclasses.
	 * <p>
	 * Flags should generally be get/set using method provided by this class.
	 * <p>
	 * @since 0.06
	 * @see #clearFlags()
	 * @see #clearFlags(int)
	 * @see #isFlagSet(int)
	 * @see #setFlags(int)
	 */
	protected int _flags;
	
	/*----------------------------
	 * IDimpleEventSource methods
	 */
	
    @Override
	public @Nullable FactorGraph getContainingGraph()
	{
    	final INode node = getModelObject();
    	return node != null ? node.getContainingGraph() : null;
	}

    @Override
    public @Nullable ISolverFactorGraph getEventParent()
    {
    	return getParentGraph();
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Default implementation provided by this class returns the event source name
     * of {@link #getModelEventSource()} if non-null, otherwise the result of the {@link #toString()} method.
     * <p>
     * This behavior is likely to change in the future.
     */
    @Override
	public String getEventSourceName()
	{
		IModelEventSource modelObj = getModelEventSource();
		return modelObj != null ? modelObj.getEventSourceName() : toString();
	}

    @Override
    public @Nullable IModelEventSource getModelEventSource()
    {
    	return getModelObject();
    }
    
	/*----------------------------
	 * ISolverEventSource methods
	 */
	
    @Override
	public @Nullable ISolverFactorGraph getContainingSolverGraph()
	{
    	return getParentGraph();
	}
    
    @Override
    public void notifyListenerChanged()
    {
    	clearFlags(getEventMask());
    }

    /*----------------------------
     * Protected/internal methods
     */

    /**
	 * Clear all flag values. Invoked automatically by {@link #initialize()}.
	 */
	protected final void clearFlags()
	{
		_flags = 0;
	}
	
	/**
	 * Clear flags in given mask.
	 */
	protected final void clearFlags(int mask)
	{
		_flags = BitSetUtil.clearMask(_flags, mask);
	}
	
	/**
	 * Return mask of flag bits that are used to determine whether to
	 * generate events. This is used by {@link #notifyListenerChanged()}
	 * to clear the specified flag bits. It is assumed that the value of
	 * all zeros indicates that the object needs to recompute its flags
	 * based on the listener.
	 * <p>
	 * The default implementation returns zero.
	 * <p>
	 * Overriders should usually combine local value with value from super class using bitwise or, e.g.:
	 * <pre>
	 *    return ThisClass.EVENT_MASK | super.getEventMask();
	 * </pre>
	 *
	 * @since 0.06
	 */
	protected int getEventMask()
	{
		return 0;
	}
	
	@Internal
	public final int getFlagValue(int mask)
	{
		return BitSetUtil.getMaskedValue(_flags, mask);
	}
	
	/**
	 * True if all of the bits in {@code mask} are set in the flags.
	 */
	protected final boolean isFlagSet(int mask)
	{
		return BitSetUtil.isMaskSet(_flags, mask);
	}
	
	protected final void raiseEvent(@Nullable SolverEvent event)
	{
		if (event != null)
		{
			final IDimpleEventListener listener = getEventListener();
			final boolean handled = listener != null && listener.raiseEvent(event);
			if (!handled)
			{
				// Listener configuration probably changed. Reconfigure source to
				// prevent further event creation.
				notifyListenerChanged();
			}
		}
	}
	
	/**
	 * Sets all of the bits in {@code mask} in the flags.
	 */
	protected final void setFlags(int mask)
	{
		_flags = BitSetUtil.setMask(_flags, mask);
	}

	/**
	 * Sets bits of flag specified by {@code mask} to {@code value}.
	 */
	@Internal
	public final void setFlagValue(int mask, int value)
	{
		_flags = BitSetUtil.setMaskedValue(_flags, mask, value);
	}
}
