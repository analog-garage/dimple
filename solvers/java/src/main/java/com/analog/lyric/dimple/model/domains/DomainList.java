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

package com.analog.lyric.dimple.model.domains;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;

import com.analog.lyric.collect.WeakInterner;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import com.google.common.collect.Interner;

/**
 * An immutable ordered list of {@link Domain}.
 */
public class DomainList<D extends Domain> extends AbstractList<D> implements RandomAccess, Serializable
{
	private static final long serialVersionUID = 1L;

	final D[] _domains;

	/**
	 * Responsible for weakly caching all JointDomainIndexer instances.
	 */
	private static enum DomainListInterner implements Interner<DomainList<?>>
	{
		INSTANCE;
		
		private final WeakInterner<DomainList<?>> _interner = WeakInterner.create();

		@NonNullByDefault(false)
		@Override
		public DomainList<?> intern(DomainList<?> indexer)
		{
			return _interner.intern(indexer);
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	DomainList(D[] domains)
	{
		_domains = domains;
	}
	
	/**
	 * Returns canonical interned copy of indexer.
	 * <p>
	 * Used internally for caching instances upon creation.
	 * <p>
	 * @since 0.07
	 */
	@SuppressWarnings("unchecked")
	static <DL extends DomainList<?>> DL intern(DL indexer)
	{
		return (DL) DomainListInterner.INSTANCE.intern(indexer);
	}
	
	/**
	 * Replace instance with canonical interned copy if it exists.
	 * @since 0.07
	 */
	protected Object readResolve()
	{
		return intern(this);
	}
	
	/**
	 * Creates a new domain list containing the specified domains in given order.
	 * <p>
	 * If all the domains are instances of {@link DiscreteDomain}, then this will return
	 * a {@link JointDomainIndexer}, which will be directed if {@code outputIndices} is non-null.
	 * <p>
	 * May return a previously cached value.
	 */
	@SafeVarargs
	public static <T extends Domain> DomainList<T> create(@Nullable int[] outputIndices, T ... domains)
	{
		if (allDiscrete(domains))
		{
			DiscreteDomain[] discreteDomains = Arrays.copyOf(domains, domains.length, DiscreteDomain[].class);
			@SuppressWarnings("unchecked")
			DomainList<T> domainList =
				(DomainList<T>) JointDomainIndexer.lookupOrCreate(outputIndices, discreteDomains, false);
			return domainList;
		}
	
		// TODO: do something with inputIndices for non-discrete case?
		
		return intern(new DomainList<T>(domains));
	}
	
	/**
	 * Creates a new domain list containing the specified domains in given order.
	 * <p>
	 * If all the domains are instances of {@link DiscreteDomain}, then this will return
	 * an undirected {@link JointDomainIndexer}.
	 * <p>
	 * May return a previously cached value.
	 */
	@SafeVarargs
	public static <T extends Domain> DomainList<T> create(T ... domains)
	{
		return create(null, domains);
	}
	
	/*--------------
	 * List methods
	 */
	
	@Override
	public D get(int i)
	{
		return _domains[i];
	}

	@Override
	public int size()
	{
		return _domains.length;
	}

	/*--------------------
	 * DomainList methods
	 */
	
	/**
	 * True if every domain in list is discrete.
	 */
	public static boolean allDiscrete(Domain ... domains)
	{
		if (DiscreteDomain.class.isAssignableFrom(domains.getClass().getComponentType()))
		{
			// Array type ensures all entries must be discrete, so there is no need to check.
			return true;
		}
		
		for (Domain domain : domains)
		{
			if (!domain.isDiscrete())
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Casts this object to {@link JointDomainIndexer} or else returns null.
	 */
	public @Nullable JointDomainIndexer asJointDomainIndexer()
	{
		return null;
	}
	
	/**
	 * True if domains in this and {@code that} object are the same and in the same order.
	 */
	public boolean domainsEqual(DomainList<D> that)
	{
		return Arrays.equals(_domains, that._domains);
	}
	
	/**
	 * True if all domains are discrete and therefore this is an instance of
	 * {@link JointDomainIndexer}.
	 * @see #asJointDomainIndexer()
	 */
	public boolean isDiscrete()
	{
		return false;
	}
}
