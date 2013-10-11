package com.analog.lyric.dimple.model.domains;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;

/**
 * An immutable ordered list of {@link Domain}.
 */
public class DomainList<D extends Domain> extends AbstractList<D> implements RandomAccess, Serializable
{
	private static final long serialVersionUID = 1L;

	final D[] _domains;

	/*--------------
	 * Construction
	 */
	
	DomainList(D[] domains)
	{
		_domains = domains;
	}
	
	/**
	 * Creates a new domain list containing the specified domains in given order.
	 * <p>
	 * If all the domains are instances of {@link DiscreteDomain}, then this will return
	 * a {@link JointDomainIndexer}, which will be directed if {@code outputIndices} is non-null.
	 * <p>
	 * May return a previously cached value.
	 */
	public static DomainList<?> create(int[] outputIndices, Domain ... domains)
	{
		if (allDiscrete(domains))
		{
			DiscreteDomain[] discreteDomains = Arrays.copyOf(domains, domains.length, DiscreteDomain[].class);
			return JointDomainIndexer.lookupOrCreate(outputIndices, discreteDomains, false);
		}
	
		// TODO: implement cache
		
		// TODO: do something with inputIndices for non-discrete case?
		
		return new DomainList<Domain>(domains);
	}
	
	/**
	 * Creates a new domain list containing the specified domains in given order.
	 * <p>
	 * If all the domains are instances of {@link DiscreteDomain}, then this will return
	 * an undirected {@link JointDomainIndexer}.
	 * <p>
	 * May return a previously cached value.
	 */
	public static DomainList<?> create(Domain ... domains)
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
	public JointDomainIndexer asJointDomainIndexer()
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
