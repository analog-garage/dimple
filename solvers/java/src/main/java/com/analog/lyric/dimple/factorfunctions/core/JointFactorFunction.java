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

package com.analog.lyric.dimple.factorfunctions.core;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.util.misc.Internal;
import com.google.common.cache.AbstractLoadingCache;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Internal
public class JointFactorFunction extends FactorFunction
{
	/*-------
	 * State
	 */
	
	/**
	 * @since 0.05
	 */
	@Immutable
	@Internal
	public static class Functions extends AbstractList<Tuple2<FactorFunction, int[]>>
	{
		private final Tuple2<FactorFunction,int[]>[]  _functions;
		private final int _hashCode;
		
		/*--------------
		 * Construction
		 */
		
		public Functions(List<Tuple2<FactorFunction, int[]>> functions)
		{
			_functions = functions.toArray(new Tuple2[functions.size()]);
			// The order of the functions doesn't really matter, so sort by function name.
			// Since there could be duplicate or empty names, this does not really provide
			// a canonical ordering, but it is better than nothing.
			Arrays.sort(_functions, new Comparator<Tuple2<FactorFunction, int[]>> () {
				@Override
				@NonNullByDefault(false)
				public int compare(Tuple2<FactorFunction, int[]> f1, Tuple2<FactorFunction, int[]> f2)
				{
					return f1.first.getName().compareTo(f2.first.getName());
				}
			});
			_hashCode = Arrays.hashCode(_functions);
		}
		
		/*----------------
		 * Object methods
		 */
		
		@Override
		public boolean equals(@Nullable Object other)
		{
			if (other == this)
			{
				return true;
			}
			
			if (other instanceof Functions)
			{
				final Functions that = (Functions)other;
				return this._hashCode == that._hashCode && Arrays.equals(this._functions, that._functions);
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return _hashCode;
		}
		
		/*--------------
		 * List methods
		 */

		@Override
		public Tuple2<FactorFunction, int[]> get(int index)
		{
			return _functions[index];
		}

		@Override
		public int size()
		{
			return _functions.length;
		}
	}
	
	private final Functions _functions;
	private final int _newNumInputs;
	
	@NonNullByDefault(false)
	private static class Loader extends CacheLoader<Functions, JointFactorFunction>
	{
		private static final Loader INSTANCE = new Loader();
		
		@Override
		public JointFactorFunction load(Functions functions) throws Exception
		{
			return new JointFactorFunction(functions);
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @since 0.05
	 */
	@Internal
	public JointFactorFunction(Functions functions)
	{
		this(buildName(functions), functions);
	}

	/**
	 * @since 0.05
	 */
	@Internal
	public JointFactorFunction(String name, Functions functions)
	{
		super(name);
	
		_functions = functions;

		int maxIndex = 0;
		for (Tuple2<FactorFunction, int[]> tuple : functions._functions)
		{
			for (int index : tuple.second)
			{
				maxIndex = Math.max(maxIndex, index);
			}
		}
		
		_newNumInputs = maxIndex + 1;
	}

	private static String buildName(Functions functions)
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0, end = functions._functions.length; i < end; ++i)
		{
			if (i > 0)
			{
				builder.append("+");
			}
			builder.append(functions._functions[i].first.getName());
		}
		return builder.toString();
	}
	
	private static class Cache extends AbstractLoadingCache<Functions,JointFactorFunction>
	{
		private static ConcurrentMap<Functions, JointFactorFunction> _map =
			new ConcurrentHashMap<Functions, JointFactorFunction>();
		
		@Override
		public JointFactorFunction get(@Nullable Functions key) throws ExecutionException
		{
			JointFactorFunction function = _map.get(key);
			
			if (function == null)
			{
				try
				{
					_map.putIfAbsent(key, Loader.INSTANCE.load(key));
				}
				catch (Exception ex)
				{
					throw new RuntimeException(ex);
				}
				function = _map.get(key);
			}
			
			return function;
		}

		@Override
		public JointFactorFunction getIfPresent(@Nullable Object key)
		{
			return _map.get(key);
		}
	}
	
	@Internal
	public static LoadingCache<Functions,JointFactorFunction> createCache()
	{
		// FIXME: We cannot use Guava's CacheBuilder when run from MATLAB because MATLAB's static Java class
		// path includes an ancient version of the Guava Objects class that is incompatible with the one
		// needed by CacheBuilder. <Grrrr>
//		return CacheBuilder.newBuilder().build(Loader.INSTANCE);
		return new Cache();
	}
	
	@Internal
	public static JointFactorFunction getFromCache(LoadingCache<Functions,JointFactorFunction> cache, Functions key)
	{
		try
		{
			return cache.get(key);
		}
		catch (ExecutionException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	/*------------------------
	 * FactorFunction methods
	 */
	
	@Override
	public double evalEnergy(Value[] input)
	{
		//Make sure length of inputs is correct
		if (input.length != _newNumInputs)
			throw new DimpleException("expected " + _newNumInputs + " args");
		
		double energy = 0.0;
		
		for (Tuple2<FactorFunction, int[]> tuple : _functions._functions)
		{
			final FactorFunction function = tuple.first;
			final int[] inputIndicesForFunction = tuple.second;
			// TODO: Use a cache of reusable array objects instead of allocating every time.
			energy += function.evalEnergy(ArrayUtil.copyFromIndices(input, inputIndicesForFunction));
		}
	
		return energy;
	}
	
	@Override
	public double evalEnergy(Object... input)
	{
		//Make sure length of inputs is correct
		if (input.length != _newNumInputs)
			throw new DimpleException("expected " + _newNumInputs + " args");
		
		double energy = 0.0;
		
		for (Tuple2<FactorFunction, int[]> tuple : _functions._functions)
		{
			final FactorFunction function = tuple.first;
			final int[] inputIndicesForFunction = tuple.second;
			// TODO: Use a cache of reusable array objects instead of allocating every time.
			energy += function.evalEnergy(ArrayUtil.copyFromIndices(input, inputIndicesForFunction));
		}
	
		return energy;
	}


	@Override
	protected IFactorTable createTableForDomains(JointDomainIndexer domains)
	{
		final int nFunctions = _functions.size();
		final ArrayList<Tuple2<IFactorTable,int[]>> tables = new ArrayList<Tuple2<IFactorTable,int[]>>(nFunctions);
		
		final DiscreteDomain[] domainArray = domains.toArray(new DiscreteDomain[domains.size()]);
		
		for (int i = 0; i < nFunctions; ++i)
		{
			final Tuple2<FactorFunction,int[]> functionTuple = _functions.get(i);
			final FactorFunction function = functionTuple.first;
			final int[] indices = functionTuple.second;
			
			final JointDomainIndexer factorDomains =
				JointDomainIndexer.create(ArrayUtil.copyFromIndices(domainArray, indices));
			final IFactorTable factorTable = function.createTableForDomains(factorDomains);
			
			tables.add(Tuple2.create(factorTable, indices));
		}
		
		return Objects.requireNonNull(FactorTable.product(tables));
	}
}
