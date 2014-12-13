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

package com.analog.lyric.dimple.model.core;

import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.util.misc.Internal;

/**
 * Contains utility methods for manipulating node identifiers.
 * <p>
 * @since 0.08
 */
public class NodeId
{
	private static int nextId = 0;
	
	@Internal
	@Deprecated
	public static int getNextId(NodeType type)
	{
		return ++nextId | (type.ordinal() << LOCAL_ID_NODE_TYPE_OFFSET);
	}
	
	/**
	 * @deprecated instead get id from FactorGraph
	 */
	@Internal
	@Deprecated
	public static int getNextFactorId()
	{
		return ++nextId | (FACTOR_TYPE<<LOCAL_ID_NODE_TYPE_OFFSET);
	}
	
	/**
	 * @deprecated instead get id from FactorGraph
	 */
	@Internal
	@Deprecated
	public static int getNextVariableId()
	{
		return ++nextId | (VARIABLE_TYPE<<LOCAL_ID_NODE_TYPE_OFFSET);
	}

	/*-----------
	 * Constants
	 */
	
	/**
	 * Maximum value for Dimple environment identifier.
	 * <p>
	 * @since 0.08
	 * @see #ENV_ID_MIN
	 * @see DimpleEnvironment#getEnvId()
	 */
	public static final long ENV_ID_MAX = (1L << 60) - 1;

	/**
	 * Minimum value for Dimple environment identifier.
	 * <p>
	 * @since 0.08
	 * @see #ENV_ID_MAX
	 * @see DimpleEnvironment#getEnvId()
	 */
	public static final long ENV_ID_MIN = 0;

	/**
	 * UUID version value for Dimple UUIDs.
	 * <p>
	 * Note that this is not a standard UUID version value to reflect
	 * the custom UUID structure.
	 * <p>
	 * @see #makeUUID(long, long)
	 * @see UUID#version()
	 */
	public static final int DIMPLE_UUID_VERSION = 0xD;

	/**
	 * Maximum value for a Dimple factor graph identifier.
	 * <p>
	 * @since 0.08
	 * @see #GRAPH_ID_MIN
	 * @see FactorGraph#getGraphId()
	 */
	public static final int GRAPH_ID_MAX = (1<<30) - 1;

	/**
	 * Minimum value for a Dimple factor graph identifier.
	 * <p>
	 * @since 0.08
	 * @see #GRAPH_ID_MAX
	 * @see FactorGraph#getGraphId()
	 */
	public static final int GRAPH_ID_MIN = 1;
	
	/**
	 * Maximum value for a Dimple global identifier.
	 * <p>
	 * @since 0.08
	 * @see #GLOBAL_ID_MIN
	 * @see Node#getGlobalId()
	 */
	public static final long GLOBAL_ID_MAX = (1L<<62) - 1;

	/**
	 * Minimum value for a Dimple global identifier.
	 * <p>
	 * @since 0.08
	 * @see #GLOBAL_ID_MAX
	 * @see Node#getGlobalId()
	 */
	public static final long GLOBAL_ID_MIN = ((long)GRAPH_ID_MIN << 32);

	public static final int LOCAL_ID_NODE_TYPE_OFFSET = 28;
	public static final int FACTOR_TYPE =    0;
	public static final int GRAPH_TYPE =     1;
	public static final int VARIABLE_TYPE =  2;
	
	/**
	 * Maximum value for the index portion of a local identifier.
	 * <p>
	 * @since 0.08
	 * @see #LOCAL_ID_INDEX_MIN
	 * @see #indexFromLocalId(int)
	 */
	public static final int LOCAL_ID_INDEX_MAX = (1<<LOCAL_ID_NODE_TYPE_OFFSET) - 1;

	/**
	 * Minimum value for the index portion of a local identifier.
	 * <p>
	 * @since 0.08
	 * @see #LOCAL_ID_INDEX_MAX
	 * @see #indexFromLocalId(int)
	 */
	public static final int LOCAL_ID_INDEX_MIN = 0;
	
	private static String DEFAULT_GRAPH_NAME_PREFIX = "$Graph";
	
	private static final long GLOBAL_ID_MASK = GLOBAL_ID_MAX;
	
	private static final int LOCAL_ID_INDEX_MASK = LOCAL_ID_INDEX_MAX;

	private static final int UUID_VERSION_OFFSET = 12;
	private static final int UUID_VERSION_WIDTH = 4;
	
	private static final long UUID_NON_VERSION_MASK = (1L << UUID_VERSION_OFFSET) - 1;
	private static final long UUID_NON_VERSION_SHIFT = UUID_VERSION_OFFSET + UUID_VERSION_WIDTH;
	
	/**
	 * Makes a UUID instance encoding Dimple environment and global identifiers.
	 * <p>
	 * The UUID will have the IETF {@link UUID#variant()} value, and a
	 * non-IETF {@link UUID#version()} set to {@link #DIMPLE_UUID_VERSION}.
	 * <p>
	 * @param envId a valid {@linkplain DimpleEnvironment#getEnvId() Dimple environment id}
	 * @param globalId a valid {@linkplain Node#getGlobalId() global node id}
	 * @since 0.08
	 * @see #envIdFromUUID(UUID)
	 * @see #globalIdFromUUID(UUID)
	 */
	public static UUID makeUUID(long envId, long globalId)
	{
		long msb = envId >>> UUID_VERSION_OFFSET;
		msb <<= UUID_VERSION_WIDTH;
		msb |= DIMPLE_UUID_VERSION;
		msb <<= UUID_VERSION_OFFSET;
		msb |= envId & UUID_NON_VERSION_MASK;
		final long lsb = Long.MIN_VALUE | globalId & GLOBAL_ID_MASK;
		return new UUID(msb, lsb);
	}
	
	/**
	 * Constructs default name for a graph with given graph identifier.
	 * <p>
	 * @since 0.08
	 * @see #graphIdFromDefaultName(String)
	 * @see FactorGraph#getName()
	 */
	public static String defaultNameForGraphId(int graphId)
	{
		return String.format("%s%d", DEFAULT_GRAPH_NAME_PREFIX, graphId);
	}
	
	/**
	 * Constructs default name for a node with given local identifier.
	 * <p>
	 * @since 0.08
	 * @see #localIdFromDefaultName(String)
	 * @see Node#getName()
	 */
	public static String defaultNameForLocalId(int id)
	{
		int type = id >>> LOCAL_ID_NODE_TYPE_OFFSET;
		char c = type < 3 ? "FGV".charAt(type) : 'X';
		return String.format("$%c%d", c, id & LOCAL_ID_INDEX_MASK);
	}
	
	/**
	 * Extracts environment id from Dimple UUID
	 * <p>
	 * Returns the {@linkplain DimpleEnvironment#getEnvId() Dimple environment id} encoded in the
	 * UUID or else -1 if UUID was not created by {@link #makeUUID(long, long)}.
	 * @since 0.08
	 */
	public static long envIdFromUUID(UUID uid)
	{
		if (uid.variant() != 2 || uid.version() != DIMPLE_UUID_VERSION)
		{
			return -1L;
		}
		final long msb = uid.getMostSignificantBits();
		return msb & UUID_NON_VERSION_MASK | msb >>> UUID_NON_VERSION_SHIFT;
	}
	
	/**
	 * Extracts global id from Dimple UUID
	 * <p>
	 * Returns the {@linkplain Node#getGlobalId() Dimple global id} encoded in the
	 * UUID, or else -1 if UUID was not created by {@link #makeUUID(long, long)}.
	 * @since 0.08
	 */
	public static long globalIdFromUUID(UUID nodeUid)
	{
		if (nodeUid.variant() != 2 || nodeUid.version() != DIMPLE_UUID_VERSION)
		{
			return -1L;
		}
		return nodeUid.getLeastSignificantBits() & GLOBAL_ID_MASK;
	}
	
	public static long globalIdFromParts(int graphId, int localId)
	{
		return (long)graphId << 32 | 0xFFFFFFFFL & localId;
	}
	
	/**
	 * Extracts graph id from default graph name.
	 * <p>
	 * If {@code name} was formatted using {@link #defaultNameForGraphId(int)},
	 * this will return the graph identifier used to construct the name, otherwise
	 * returns -1 (which is not a valid graph id).
	 * @since 0.08
	 */
	public static int graphIdFromDefaultName(String name)
	{
		if (name.startsWith(DEFAULT_GRAPH_NAME_PREFIX))
		{
			try
			{
				int id = Integer.parseInt(name.substring(DEFAULT_GRAPH_NAME_PREFIX.length()));
				if (GRAPH_ID_MIN <= id && id <= GRAPH_ID_MAX)
				{
					return id;
				}
			}
			catch (NumberFormatException ex)
			{
				// ignore
			}
		}
		
		return -1;
	}
	
	/**
	 * Return graph identifier from Dimple global id
	 * @since 0.08
	 * @see Node#getGlobalId()
	 */
	public static int graphIdFromGlobalId(long globalId)
	{
		return (int)(globalId >>> 32);
	}
	
	/**
	 * Extract index portion of local identifier.
	 * <p>
	 * @since 0.08
	 */
	public static int indexFromLocalId(int id)
	{
		return id & LOCAL_ID_INDEX_MASK;
	}
	
	/**
	 * Determine if string represents a Dimple UUID
	 * <p>
	 * True if {@code str} is in the format produced by {@link UUID#toString}
	 * on a UUID constructed using {@link #makeUUID(long, long)}.
	 * @since 0.08
	 */
	public static boolean isUUIDString(String str)
	{
		if (str.length() != 36)
		{
			return false;
		}

		for (int i = 0; i < 36; ++i)
		{
			final char c = str.charAt(i);
			switch (i)
			{
			case 8:
			case 13:
			case 18:
			case 23:
				if (c != '-')
				{
					return false;
				}
				break;
				
			case 14:
				if (c != 'D' && c != 'd')
				{
					return false;
				}
				break;
				
			default:
				if (c < '0' || c > 'f' || c > '9' && c < 'A' || c > 'F' && c < 'a')
				{
					return false;
				}
			}
		}

		return true;
	}
	
	/**
	 * Extracts local id from default node name.
	 * <p>
	 * If {@code name} was formatted using {@link #defaultNameForLocalId(int)},
	 * this will return the identifier used to construct the name, otherwise
	 * returns -1 (which is not a valid id).
	 * @since 0.08
	 */
	public static int localIdFromDefaultName(String name)
	{
		final int length = name.length();
		
		if (length > 2 && name.charAt(0) == '$')
		{
			char c = name.charAt(1);
			if ('A' <= c && c <= 'Z')
			{
				try
				{
					int index = Integer.parseInt(name.substring(2));
					if (LOCAL_ID_INDEX_MIN <= index && index <= LOCAL_ID_INDEX_MAX)
					{
						int type = "FGV".indexOf(c);
						return (type << LOCAL_ID_NODE_TYPE_OFFSET) | index;
					}
				}
				catch (NumberFormatException ex)
				{
					// Ignore
				}
			}
		}
		
		return -1;
	}

	/**
	 * Return local identifier from Dimple global id
	 * @since 0.08
	 * @see Node#getGlobalId()
	 */
	public static int localIdFromGlobalId(long globalId)
	{
		return (int)(globalId & 0xFFFFFFFFL);
	}
	
	/**
	 * Returns node type encoded in local identifier
	 * <p>
	 * @return {@link NodeType} encoded in identifier or null if type encoding is not valid.
	 * @since 0.08
	 */
	public static @Nullable NodeType nodeTypeFromLocalId(int localId)
	{
		switch (localId >>> LOCAL_ID_NODE_TYPE_OFFSET)
		{
		case FACTOR_TYPE:
			return NodeType.FACTOR;
		case GRAPH_TYPE:
			return NodeType.GRAPH;
		case VARIABLE_TYPE:
			return NodeType.VARIABLE;
		}
		
		return null;
	}
}
