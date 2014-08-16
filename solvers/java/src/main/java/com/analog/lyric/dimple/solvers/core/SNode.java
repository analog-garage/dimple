/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core;

import java.util.Objects;

import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.events.SolverEvent;
import com.analog.lyric.dimple.events.SolverEventSource;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import org.eclipse.jdt.annotation.Nullable;

public abstract class SNode extends SolverEventSource implements ISolverNode
{
	/*-----------
	 * Constants
	 */
	
	/**
	 * Bits in {@link #_flags} reserved by this class and its superclasses.
	 */
	@SuppressWarnings("hiding")
	protected static final int RESERVED_FLAGS = 0xFF000000;

	private static final int MESSAGE_EVENT_MASK = 0x03000000;

	private static final int MESSAGE_EVENT_UNKNOWN = 0x00000000;

	private static final int MESSAGE_EVENT_NONE = 0x01000000;

	private static final int MESSAGE_EVENT_ENABLED = 0x03000000;
	
	protected static final int EVENT_MASK = MESSAGE_EVENT_MASK;
	
	/*-------
	 * State
	 */

	private final Node _model;
	
	/*--------------
	 * Construction
	 */
	
	public SNode(Node n)
	{
		_model = n;
	}
	
	/*-----------------------
	 * IOptionHolder methods
	 */
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public Node getModelObject()
    {
    	return _model;
    }
	
	@Override
	public ISolverNode getSibling(int edge)
	{
		return Objects.requireNonNull(getModelObject().getSibling(edge).getSolver());
	}
	
	@Override
	public int getSiblingCount()
	{
		return getModelObject().getSiblingCount();
	}
	
	/**
	 * Initialize solver node.
	 * <p>
	 * Clears internal state flags and resets messages for edges.
	 */
	@Override
	public void initialize()
	{
		clearFlags();
		for (int i = 0, end = getModelObject().getSiblingCount(); i < end; i++)
			resetEdgeMessages(i);
	}
	
	@Override
	public void setInputMsg(int portIndex, Object obj) {
		throw new DimpleException("Not supported by " + this);
	}
	@Override
	public void setOutputMsg(int portIndex, Object obj) {
		throw new DimpleException("Not supported by " + this);
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj) {
		throw new DimpleException("Not supported by " + this);
	}
	@Override
	public void setOutputMsgValues(int portIndex, Object obj) {
		throw new DimpleException("Not supported by " + this);
	}

	@Override
	public void update()
	{
		if (raiseMessageEvents())
		{
			final IParameterizedMessage[] oldMessages = cloneMessages();
			
			doUpdate();
			
			final IParameterizedMessage[] newMessages = cloneMessages();
			if (newMessages != null)
			{
				for (int edge = 0, nEdges = newMessages.length; edge < nEdges; ++edge)
				{
					final IParameterizedMessage oldMessage = oldMessages != null ? oldMessages[edge] : null;
					final IParameterizedMessage newMessage = newMessages[edge];

					if (newMessage != null)
					{
						raiseEvent(createMessageEvent(edge, oldMessage, newMessage));
					}
				}
			}
		}
		else
		{
			doUpdate();
		}
	}

	@Override
	public void updateEdge(int edge)
	{
		if (raiseMessageEvents())
		{
			final IParameterizedMessage oldMessage = cloneMessage(edge);
			
			doUpdateEdge(edge);
			
			final IParameterizedMessage newMessage = cloneMessage(edge);
			if (newMessage != null)
			{
				raiseEvent(createMessageEvent(edge, oldMessage, newMessage));
			}
		}
		else
		{
			doUpdateEdge(edge);
		}
	}

	/*---------------------------
	 * SolverEventSource methods
	 */
	
    @Override
    protected int getEventMask()
    {
    	return EVENT_MASK;
    }

	/*-------------------------
	 * Protected SNode methods
	 */
	
	/**
	 * Returns a clone of outgoing message for given {@code edge}.
	 * <p>
	 * @param edge index in the range [0, {@link #getSiblingCount()}-1].
	 * @return clone of message if applicable. May return null if there is no message or if subclass
	 * does not support messages. In the latter case, {@link #supportsMessageEvents()} should return false.
	 * <p>
	 * The default implementation returns null.
	 * <p>
	 * Subclasses that override this method to return a non-null message should also override
	 * {@link #supportsMessageEvents()}.
	 * <p>
	 * @since 0.06
	 */
	protected @Nullable IParameterizedMessage cloneMessage(int edge)
	{
		return null;
	}

	private final @Nullable IParameterizedMessage[] cloneMessages()
	{
		IParameterizedMessage[] messages = null;
		
		final int size = getSiblingCount();
		if (size > 0)
		{
			final IParameterizedMessage firstMessage = cloneMessage(0);
			if (firstMessage != null)
			{
				messages = new IParameterizedMessage[size];
				messages[0] = firstMessage;
				for (int i = 1; i < size; ++i)
				{
					messages[i] = cloneMessage(i);
				}
			}
		}
		
		return messages;
	}

	protected void doUpdate()
	{
		for (int i = 0, end = getSiblingCount(); i < end; i++)
		{
			doUpdateEdge(i);
		}
	}

	protected abstract void doUpdateEdge(int edge);

	/**
	 * Creates a {@link IMessageUpdateEvent} event.
	 * <p>
	 * Default implementation returns null. When this method returns null, then {@link #supportsMessageEvents()}
	 * should also return false.
	 * <p>
	 * @param edge is the outgoing edge for the messages.
	 * @param oldMessage is the previous value of the message, which may be null.
	 * @param newMessage is the new message value, which must not be null.
	 * @since 0.06
	 */
	protected @Nullable SolverEvent createMessageEvent(
		int edge,
		@Nullable IParameterizedMessage oldMessage,
		IParameterizedMessage newMessage)
	{
		return null;
	}
	
	/**
	 * If {@link #supportsMessageEvents()}, this returns the base type for all message
	 * events that can be created by this object. Default implementation returns null.
	 * @since 0.06
	 */
	protected @Nullable Class<? extends SolverEvent> messageEventType()
	{
		return null;
	}

	/**
	 * Indicate subclass has a concept of passing a {@link IParameterizedMessage} messages.
	 * <p>
	 * Should only return true if:
	 * <ul>
	 * <li>{@link #cloneMessage(int)} can return non-null message
	 * <li>{@link #messageEventType()} is non-null
	 * <li>{@link #createMessageEvent} can return a non-null event
	 * </ul>
	 * Since {@link SVariableBase} and {@link SFactorBase} implement the latter two methods, subclasses
	 * of those classes that support message events only need to implement {@link #cloneMessage(int)} and
	 * this method.
	 * <p>
	 * The default implementation returns false.
	 * <p>
	 * @since 0.06
	 */
	protected boolean supportsMessageEvents()
	{
		return false;
	}
	
	/*-----------------
	 * Private methods
	 */

	/**
	 * Indicates whether to generate {@link IMessageUpdateEvent}s.
	 * @since 0.06
	 */
	private boolean raiseMessageEvents()
	{
		final int flags = _flags & MESSAGE_EVENT_MASK;
		
		if (flags == MESSAGE_EVENT_NONE)
		{
			// Check this first to minimize overhead when known to be disabled
			return false;
		}
		else if (flags == MESSAGE_EVENT_UNKNOWN)
		{
			boolean enabled = false;
			if (supportsMessageEvents())
			{
				final IDimpleEventListener listener = getEventListener();
				if (listener != null)
				{
					enabled = listener.isListeningFor(Objects.requireNonNull(messageEventType()), this);
				}
			}
			setFlagValue(MESSAGE_EVENT_MASK, enabled ? MESSAGE_EVENT_ENABLED : MESSAGE_EVENT_NONE);
			return enabled;
		}
		else
		{
			return flags == MESSAGE_EVENT_ENABLED;
		}
	}
}
