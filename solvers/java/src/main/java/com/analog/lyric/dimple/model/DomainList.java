package com.analog.lyric.dimple.model;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;

/**
 * An immutable ordered list of {@link Domain}.
 * <p>
 * If all domains are discrete, then object will be a {@link DiscreteDomainList}.
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
	
	public static DomainList<?> create(int[] outputIndices, Domain ... domains)
	{
		if (allDiscrete(domains))
		{
			DiscreteDomain[] discreteDomains = Arrays.copyOf(domains, domains.length, DiscreteDomain[].class);
			return DiscreteDomainList.lookupOrCreate(outputIndices, discreteDomains, false);
		}
	
		// TODO: implement cache
		
		// TODO: do something with inputIndices for non-discrete case?
		
		return new DomainList<Domain>(domains);
	}
	
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
	 * Casts this object to {@link DiscreteDomainList} or else returns null.
	 */
	public DiscreteDomainList asDiscreteDomainList()
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
	 * {@link DiscreteDomainList}.
	 * @see #asDiscreteDomainList()
	 */
	public boolean isDiscrete()
	{
		return false;
	}
}
