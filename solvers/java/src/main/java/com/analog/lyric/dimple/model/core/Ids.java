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

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Constant;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.util.misc.IGetId;
import com.analog.lyric.util.misc.Internal;

/**
 * Static utility methods for manipulating factor graph child identifiers.
 * <p>
 * There are a number of different types of identifiers used to index
 * nodes and other objects belonging to factor graphs. All are represented
 * as a primitive int or long.
 * <p>
 * <ul>
 * <li><b>local id</b> - identifies a node uniquely within the factor graph
 * that contains it. The local id also identifies the type of object (e.g. factor, variable).
 * {@link FactorGraph} provides methods for retrieving nodes by their local id, such as
 * {@linkplain FactorGraph#getNodeByLocalId(int) getNodeByLocalId}.
 * 
 * <li><b>local index</b> - the index component of a local id that does not include its type tag (see internal layouts
 * below). This typically is used as a direct index into arrays of data associated with child instances.
 * 
 * <li><b>graph id</b> - uniquely identifies a {@link FactorGraph} within the
 * {@link DimpleEnvironment}'s (typically there is only one) {@linkplain DimpleEnvironment#factorGraphs() factor
 * graph registry}.
 * 
 * <li><b>global id</b> - combines the local id and graph id to uniquely identify a node within
 * the environment. The {@code getNodeByGlobalId} method in both {@linkplain DimpleEnvironment#getNodeByGlobalId(long)
 * DimpleEnvironment} and {@linkplain FactorGraph#getNodeByGlobalId(long) FactorGraph} provides relatively fast lookup
 * of a node by its global id.
 * 
 * <li><b>graph tree index</b> - is a simple index that uniquely identifies a graph within the
 * tree off graphs sharing a common root graph. All of the graphs in the tree are stored in an array
 * shared by the graphs in the same tree. This index indicates the position in the array, so lookup by
 * this index is fast.
 * 
 * <li><b>graph tree id</b> - combines the local id and graph tree index to uniquely identify
 * a node within its graph tree. This provides fast lookup of nodes within the tree using
 * {@link FactorGraph#getNodeByGraphTreeId(long)}.
 * </ul>
 * <p>
 * Note that some objects may be represented by more than one identifier. The same variable may
 * be represented by its variable id in the graph that owns it, or a boundary variable id in a graph that has
 * it as a boundary variable. The same boundary edge will have different identifiers with respect to the two
 * graphs that refer to it.
 * <p>
 * <h2>Internal layout</h2>
 * 
 * Identifiers have the following internal layouts:
 * <p>
 * <ul>
 * <li><b>local id</b>
 * <pre>
 * +------+-----------------------------+
 * | type |          index              |
 * +------+-----------------------------+
 * 31     28                            0
 * </pre>
 * 
 * <li><b>global id</b>
 * <pre>
 * +----+-------------------------------+--------------------------------------+
 * | 01 |           graph id            |               local id               |
 * +----+-------------------------------+--------------------------------------+
 * 63   61                              32                                     0
 * </pre>

 * <li><b>graph tree id</b>
 * <pre>
 * +----+-------------------------------+--------------------------------------+
 * | 00 |      graph tree index         |               local id               |
 * +----+-------------------------------+--------------------------------------+
 * 63   61                              32                                     0
 * </pre>
 * </ul>
 * 
 * @since 0.08
 */
public abstract class Ids
{
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
	public static final long GLOBAL_ID_MAX = (1L<<63) - 1;

	/**
	 * Minimum value for a Dimple global identifier.
	 * <p>
	 * @since 0.08
	 * @see #GLOBAL_ID_MAX
	 * @see Node#getGlobalId()
	 */
	public static final long GLOBAL_ID_MIN = ((long)GRAPH_ID_MIN << 32) | 1L<<62;

	/**
	 * Offset of the type index portion of a local identifier.
	 * @see #typeIndexFromLocalId(int)
	 * @category internal
	 */
	@Internal
	public static final int LOCAL_ID_TYPE_OFFSET = 28;
	
	public static final int UNKNOWN_TYPE = 0;
	
	/**
	 * Type index for factor identifiers.
	 * @see #typeIndexFromLocalId(int)
	 */
	public static final int FACTOR_TYPE = 1;
	
	/**
	 * Type index for subgraph identifiers.
	 * @see #typeIndexFromLocalId(int)
	 */
	public static final int GRAPH_TYPE = 2;
	
	/**
	 * Type index for owned (non-boundary) variable identifiers.
	 * @see #typeIndexFromLocalId(int)
	 */
	public static final int VARIABLE_TYPE = 3;

	/**
	 * Type index for boundary variable identifiers.
	 * @see #typeIndexFromLocalId(int)
	 */
	public static final int BOUNDARY_VARIABLE_TYPE = 4;

	/**
	 * Type index for edge identifiers.
	 * @see #typeIndexFromLocalId(int)
	 */
	public static final int EDGE_TYPE = 5;
	
	/**
	 * Type index for factor port identifiers
	 * @see #typeIndexFromLocalId(int)
	 */
	public static final int FACTOR_PORT_TYPE = 6;

	/**
	 * Type index for variable port identifiers
	 * @see #typeIndexFromLocalId(int)
	 */
	public static final int VARIABLE_PORT_TYPE = 7;
	
	/**
	 * Type index for variable block identifiers.
	 * @see #typeIndexFromLocalId(int)
	 */
	public static final int VARIABLE_BLOCK_TYPE = 8;
	
	/**
	 * Type index for constant identifiers.
	 * @see #typeIndexFromLocalId(int)
	 */
	public static final int CONSTANT_TYPE = 9;
	
	/**
	 * Value of minimum type index for local identifiers
	 */
	public static final int TYPE_MIN = 0;
	
	/**
	 * Value of max type index for local identifiers
	 */
	public static final int TYPE_MAX = 9;
	
	/**
	 * This is only used as the {@link Type#instanceClass() instanceClass} of {@link Type#UNKNOWN}.
	 * <p>
	 * It cannot be instantiated.
	 * 
	 * @since 0.08
	 */
	public abstract class UnknownChild implements IFactorGraphChild
	{
		private UnknownChild() {}
	}
	
	/**
	 * Enumerates supported identifier types.
	 * <p>
	 * These correspond to the similarly named type index constants, e.g.
	 * {@link Type#FACTOR} corresponds to {@link Ids#FACTOR_TYPE}.
	 * <p>
	 * @since 0.08
	 * @author Christopher Barber
	 */
	public enum Type
	{
		// NOTE: after release 0.08 new entries should be added at the end since the type
		// index may be used in external persistent representations.
		
		UNKNOWN(UnknownChild.class),
		FACTOR(Factor.class),
		GRAPH(FactorGraph.class),
		VARIABLE(Variable.class),
		BOUNDARY_VARIABLE(Variable.class),
		EDGE(Edge.class),
		FACTOR_PORT(FactorPort.class),
		VARIABLE_PORT(VariablePort.class),
		VARIABLE_BLOCK(VariableBlock.class),
		CONSTANT(Constant.class);
		
		// Possible future types:
		//   PARAMETER - T for theta
		//   UTILITY VARIABLE - U
		//   ACTION FACTOR - A
		
		private final Class<? extends IFactorGraphChild> _instanceType;
		
		private static final Type[] _values = Type.values();
		
		/**
		 * Single character type prefixes in same order as enum instances.
		 */
		private static final String DEFAULT_NAME_PREFIX = "UFGVBEPQKC";

		/*--------------
		 * Construction
		 */
		
		private Type(Class<? extends IFactorGraphChild> instanceType)
		{
			_instanceType = instanceType;
		}
		
		/*----------------
		 * Static methods
		 */
		
		/**
		 * Returns type whose {@link #instanceClass()} matches argument.
		 * <p>
		 * @param instanceClass is a {@link IFactorGraphChild} is a subclass of the {@link #instanceClass()}
		 * of one of the enumerated types.
		 * @throws IllegalArgumentException if {@code instanceClass} does not match any of the types. This can
		 * only happen if someone has created a new implementation of {@link IFactorGraphChild} that is not
		 * registered by this class or {@code instanceClass} is too general (e.g. {@link Node}).
		 * @since 0.08
		 */
		public static Type forInstanceClass(Class<? extends IFactorGraphChild> instanceClass)
		{
			for (Type type : _values)
			{
				if (type._instanceType.isAssignableFrom(instanceClass))
				{
					return type;
				}
			}
			
			throw new IllegalArgumentException(String.format("%s does not have an identifier type",
				instanceClass.getSimpleName()));
		}
		
		/**
		 * Returns {@code Type} value with given ordinal value
		 * @since 0.08
		 */
		public static Type valueOf(int ordinal)
		{
			return _values[ordinal];
		}
		
		/*-----------------
		 * Regular methods
		 */
		
		/**
		 * {@link IFactorGraphChild} class used to represent instances of this type.
		 * @since 0.08
		 */
		public Class<? extends IFactorGraphChild> instanceClass()
		{
			return _instanceType;
		}
		
		/**
		 * Prefix character used to identify type in names produced by {@link Ids#defaultNameForLocalId(int)}.
		 * @since 0.08
		 */
		public char namePrefix()
		{
			return DEFAULT_NAME_PREFIX.charAt(ordinal());
		}
		
		/**
		 * Returns type index for this type.
		 * <p>
		 * This is the same as {@link #ordinal()}.
		 * @since 0.08
		 */
		public int typeIndex()
		{
			return ordinal();
		}
	}

	/**
	 * Maximum value for the index portion of a local identifier.
	 * <p>
	 * @since 0.08
	 * @see #LOCAL_ID_INDEX_MIN
	 * @see #indexFromLocalId(int)
	 */
	public static final int LOCAL_ID_INDEX_MAX = (1<<LOCAL_ID_TYPE_OFFSET) - 1;

	/**
	 * Minimum value for the index portion of a local identifier.
	 * <p>
	 * @since 0.08
	 * @see #LOCAL_ID_INDEX_MAX
	 * @see #indexFromLocalId(int)
	 */
	public static final int LOCAL_ID_INDEX_MIN = 0;
	
	/**
	 * Default value of local id for a factor that has not yet been added to a {@link FactorGraph}.
	 * @category internal
	 */
	@Internal
	public static final int INITIAL_FACTOR_ID   = FACTOR_TYPE   << LOCAL_ID_TYPE_OFFSET | LOCAL_ID_INDEX_MAX;

	/**
	 * Default value of local id for a subgraph that has not yet been added to a {@link FactorGraph}.
	 * @category internal
	 */
	@Internal
	public static final int INITIAL_GRAPH_ID    = GRAPH_TYPE    << LOCAL_ID_TYPE_OFFSET | LOCAL_ID_INDEX_MAX;
	
	/**
	 * Default value of local id for a variable that has not yet been added to a {@link FactorGraph}.
	 * @category internal
	 */
	@Internal
	public static final int INITIAL_VARIABLE_ID = VARIABLE_TYPE << LOCAL_ID_TYPE_OFFSET | LOCAL_ID_INDEX_MAX;
	
	private static String DEFAULT_GRAPH_NAME_PREFIX = "$Graph";
	
	/**
	 * Mask for global identifier within UUID.
	 */
	private static final long GLOBAL_ID_UUID_MASK = (1L<<62) - 1;
	
	/**
	 * High-order two bits used to distinguish global id from a graph tree id.
	 */
	private static final long GLOBAL_ID_INDICATOR = 1L<<62;
	
	/**
	 * Mask for index (non-type) portion of local identifier.
	 */
	private static final int LOCAL_ID_INDEX_MASK = LOCAL_ID_INDEX_MAX;

	private static final int UUID_VERSION_OFFSET = 12;
	private static final int UUID_VERSION_WIDTH = 4;
	
	private static final long UUID_NON_VERSION_MASK = (1L << UUID_VERSION_OFFSET) - 1;
	
	/*----------------
	 * Static methods
	 */
	
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
		final long lsb = Long.MIN_VALUE | globalId & GLOBAL_ID_UUID_MASK;
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
	 * The name will have the format "$" + "<i>type-prefix-character</i>" + "<i>index</i>"
	 * <p>
	 * @since 0.08
	 * @see #localIdFromDefaultName(String)
	 * @see Node#getName()
	 */
	public static String defaultNameForLocalId(int id)
	{
		int type = id >>> LOCAL_ID_TYPE_OFFSET;
		char c = type <= TYPE_MAX ? Type.DEFAULT_NAME_PREFIX.charAt(type) : 'X';
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
		return msb & UUID_NON_VERSION_MASK | (msb >>> UUID_VERSION_WIDTH) & ~UUID_NON_VERSION_MASK;
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
		return nodeUid.getLeastSignificantBits() & GLOBAL_ID_UUID_MASK | GLOBAL_ID_INDICATOR;
	}
	
	/**
	 * Construct a global identifier from its {@linkplain FactorGraph#getGraphId graph id} and its
	 * {@linkplain IGetId#getLocalId() local id}.
	 * @since 0.08
	 */
	public static long globalIdFromParts(int graphId, int localId)
	{
		return (long)graphId << 32 | 0xFFFFFFFFL & localId | GLOBAL_ID_INDICATOR;
	}
	
	/**
	 * Construct a global identifier from its {@linkplain FactorGraph#getGraphId graph id}, its
	 * local id type index and its local index.
	 * @since 0.08
	 * @see #globalIdFromParts(int, int)
	 * @see #localIdFromParts(int, int)
	 */
	public static long globalIdFromParts(int graphId, int idType, int localIndex)
	{
		return globalIdFromParts(graphId, localIdFromParts(idType, localIndex));
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
		return (int)(globalId >>> 32) & 0x3FFFFFFF;
	}
	
	public static long graphTreeIdFromParts(int graphTreeIndex, int localId)
	{
		return (long)graphTreeIndex << 32 | 0xFFFFFFFFL & localId;
	}
	
	/**
	 * Returns {@linkplain FactorGraph#getGraphTreeIndex() graph tree index} portion of
	 * {@linkplain IFactorGraphChild#getGraphTreeId() graph tree identifier}.
	 * @since 0.08
	 */
	public static int graphTreeIndexFromGraphTreeId(long graphTreeId)
	{
		return (int)(graphTreeId >>> 32);
	}
	
	/**
	 * Constructs local id from type and index.
	 * @since 0.08
	 */
	public static int localIdFromParts(int nodeType, int index)
	{
		return (nodeType << LOCAL_ID_TYPE_OFFSET) | index;
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
	
	public static boolean isGlobalId(long id)
	{
		return (id >>> 62) == 1L;
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
				
			case 19:
				if (c != '8' && c != '9' && c != 'a' && c != 'b' && c != 'A' && c != 'B')
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
						int type = Type.DEFAULT_NAME_PREFIX.indexOf(c);
						if (type >= 0)
						{
							return (type << LOCAL_ID_TYPE_OFFSET) | index;
						}
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
	 * @see INode#getGlobalId()
	 */
	public static int localIdFromGlobalId(long globalId)
	{
		return (int)(globalId & 0xFFFFFFFFL);
	}
	
	/**
	 * Return local identifier from Dimple graph tree id
	 * @since 0.08
	 * @see INode#getGraphTreeId()
	 */
	public static int localIdFromGraphTreeId(long graphTreeId)
	{
		return (int)(graphTreeId & 0xFFFFFFFFL);
	}
	
	/**
	 * Returns index of type encoded in local identifier
	 * <p>
	 * @return type index portion of local identifier. Valid local ids should return a number
	 * in the range {@link #TYPE_MIN} to {@link #TYPE_MAX}.
	 * <p>
	 * @since 0.08
	 * @see #typeFromLocalId(int)
	 */
	public static int typeIndexFromLocalId(int localId)
	{
		return localId >>> LOCAL_ID_TYPE_OFFSET;
	}
	
	/**
	 * Returns index of type encoded in local identifiers of instances of given class.
	 * 
	 * @param instanceClass is a {@link IFactorGraphChild} that is specific to one id type.
	 * @throws IllegalArgumentException if {@code instanceClass} does not match any of the types. This can
	 * only happen if someone has created a new implementation of {@link IFactorGraphChild} that is not
	 * registered by this class or {@code instanceClass} is too general (e.g. {@link Node}).
	 * @since 0.08
	 */
	public static int typeIndexForInstanceClass(Class<? extends IFactorGraphChild> instanceClass)
	{
		return Type.forInstanceClass(instanceClass).typeIndex();
	}
	
	private static final Type[] _localIdTypes = Arrays.copyOf(Type.values(), 16);
	
	/**
	 * Returns type encoded in local identifier.
	 * @param localId a local identifier constructed with {@link Ids#localIdFromParts(int, int)}.
	 * @return {@link Type} of given id, or null if id has an invalid type index.
	 * @since 0.08
	 * @see #typeIndexFromLocalId(int)
	 */
	public static @Nullable Type typeFromLocalId(int localId)
	{
		return _localIdTypes[localId >>> LOCAL_ID_TYPE_OFFSET];
	}
}
