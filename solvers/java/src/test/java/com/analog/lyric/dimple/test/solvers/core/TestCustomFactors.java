/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.solvers.core;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static java.lang.String.format;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.And;
import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.factorfunctions.core.CustomFactorFunctionWrapper;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.solvers.core.CustomFactors;
import com.analog.lyric.dimple.solvers.core.CustomFactorsOptionKey;
import com.analog.lyric.dimple.solvers.core.ISolverFactorCreator;
import com.analog.lyric.dimple.solvers.core.SolverFactorCreationException;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.dimple.test.solvers.core.customFactors.MyCustomFactor;
import com.analog.lyric.dimple.test.solvers.core.customFactors.MyCustomXor;

/**
 * Unit test for {@link CustomFactors}
 * @since 0.08
 * @author Christopher Barber
 */
public class TestCustomFactors extends DimpleTestBase
{
	public static abstract class MyCustomFactorsBase extends CustomFactors<MyCustomFactor, ISolverFactorGraph>
	{
		private static final long serialVersionUID = 1L;

		MyCustomFactorsBase(Class<MyCustomFactor> sfactorClass, Class<ISolverFactorGraph> sgraphClass)
		{
			super(sfactorClass, sgraphClass);
		}
		
		MyCustomFactorsBase(MyCustomFactorsBase other)
		{
			super(other);
		}
	}
	
	public static class MyCustomFactors extends MyCustomFactorsBase
	{
		private static final long serialVersionUID = 1L;

		public MyCustomFactors()
		{
			super(MyCustomFactor.class, ISolverFactorGraph.class);
		}

		MyCustomFactors(MyCustomFactors other)
		{
			super(other);
		}
		
		@Override
		public CustomFactors<MyCustomFactor, ISolverFactorGraph> clone()
		{
			return new MyCustomFactors(this);
		}

		@Override
		public void addBuiltins()
		{
			add(Xor.class, MyCustomXor.class);
		}

		@Override
		public MyCustomFactor createDefault(Factor factor, ISolverFactorGraph sgraph)
		{
			return new MyCustomFactor(factor, sgraph, "default");
		}
		
		@Override
		protected void freeze()
		{
			super.freeze();
		}
		
		@Override
		public String qualifiedFactorFunctionName(String factorFunction)
		{
			return super.qualifiedFactorFunctionName(factorFunction);
		}
	}
	
	public static class MyCustomXor3 extends MyCustomXor
	{
		public MyCustomXor3(Factor factor, ISolverFactorGraph parent)
		{
			super(factor, parent, "xor3");
		}

		public MyCustomXor3(Factor factor, ISolverFactorGraph parent, String tag)
		{
			super(factor, parent, tag);
		}
	}

	public static class MyCustomXor4 extends MyCustomXor
	{
		public static @Nullable Throwable throwMe = null;
		
		public MyCustomXor4(Factor factor, ISolverFactorGraph parent) throws Throwable
		{
			this(factor, parent, "xor4");
		}

		public MyCustomXor4(Factor factor, ISolverFactorGraph parent, String tag) throws Throwable
		{
			super(factor, parent, tag);
			Throwable ex = throwMe;
			if (ex != null)
				throw ex;
		}
	}
	
	public static class MyCustomSolverGraph extends SumProductSolverGraph
	{
		public MyCustomSolverGraph(FactorGraph factorGraph)
		{
			super(factorGraph, null);
		}
		
		@Override
		public ISolverFactor createFactor(Factor factor)
		{
			return option.createFactor(factor, this);
		}
	}
	
	@Test
	public void test()
	{
		FactorGraph fg = new FactorGraph();
		Bit b1 = new Bit();
		Bit b2 = new Bit();
		Factor xorFactor = fg.addFactor(new Xor(), b1, b2);
		ISolverFactorGraph sfg = requireNonNull(fg.getSolver());
		
		MyCustomFactors customFactors = new MyCustomFactors();
		assertTrue(customFactors.isMutable());
		assertTrue(customFactors.keySet().isEmpty());
		assertTrue(customFactors.get("bogus").isEmpty());
		assertTrue(customFactors.get("Xor").isEmpty());
		assertEquals("MyCustomFactors()\n", customFactors.toString());

		// Test getFactorClass helper method
		assertSame(MyCustomXor.class, customFactors.getFactorClass("MyCustomXor"));
		assertSame(MyCustomXor.class, customFactors.getFactorClass(MyCustomXor.class.getName()));
		expectThrow(IllegalArgumentException.class, ".*ClassNotFoundException.*" ,customFactors, "getFactorClass", "CustomNormal");

		// Test qualifiedFactorFunctionName helper method
		assertEquals("com.analog.lyric.dimple.factorfunctions.Xor", customFactors.qualifiedFactorFunctionName("Xor"));
		assertEquals("alias", customFactors.qualifiedFactorFunctionName("alias"));
		try
		{
			customFactors.qualifiedFactorFunctionName("foo.bar");
			fail("expected exception");
		}
		catch (IllegalArgumentException ex)
		{
		}
		
		// Test addBuiltins
		customFactors.addBuiltins();
		Set<String> keys = customFactors.keySet();
		assertEquals(1, keys.size());
		assertTrue(keys.contains(Xor.class.getName()));
		assertEquals(1, customFactors.get(Xor.class.getName()).size());
		assertEquals(format("MyCustomFactors(\n\t%s = %s)\n", Xor.class.getName(), MyCustomXor.class.getName()),
			customFactors.toString());

		// Test defaultCreator helper methods
		expectThrow(DimpleException.class, ".*NoSuchMethod.*", customFactors, "defaultCreator", getClass());
		ISolverFactorCreator<MyCustomFactor, ISolverFactorGraph> creator = customFactors.defaultCreator("MyCustomXor");
		assertEquals(MyCustomXor.class.getName(), creator.toString());
		ISolverFactor sfactor = creator.create(xorFactor, sfg);
		assertSame(xorFactor, sfactor.getModelObject());
		assertSame(MyCustomXor.class, sfactor.getClass());
		creator = customFactors.defaultCreator(MyCustomXor4.class);
		assertSame(MyCustomXor4.class, creator.create(xorFactor, sfg).getClass());
		MyCustomXor4.throwMe = new RuntimeException("xxx");
		try
		{
			creator.create(xorFactor, sfg);
			fail("expected exception");
		}
		catch (RuntimeException ex)
		{
			assertSame(MyCustomXor4.throwMe, ex);
		}
		MyCustomXor4.throwMe = new Exception("yyy");
		try
		{
			creator.create(xorFactor, sfg);
			fail("expected exception");
		}
		catch (RuntimeException ex)
		{
			assertSame(MyCustomXor4.throwMe, ex.getCause());
		}
		
		MyCustomXor4.throwMe = null;
		
		// Test add methods
		ISolverFactorCreator<MyCustomFactor,ISolverFactorGraph> xor1 =
			new ISolverFactorCreator<MyCustomFactor,ISolverFactorGraph>() {
				@Override
				public MyCustomFactor create(Factor factor, ISolverFactorGraph sgraph)
				{
					return new MyCustomXor(factor, sgraph, "xor1");
				}
		};
		customFactors.add("Xor", xor1);
		ISolverFactorCreator<MyCustomFactor,ISolverFactorGraph> xor2 =
			new ISolverFactorCreator<MyCustomFactor,ISolverFactorGraph>() {
				@Override
				public MyCustomFactor create(Factor factor, ISolverFactorGraph sgraph)
				{
					return new MyCustomXor(factor, sgraph, "xor2");
				}
		};
		customFactors.add(Xor.class, xor2);
		
		customFactors.add("Xor", MyCustomXor3.class);
		
		customFactors.add(Xor.class, MyCustomXor4.class);
		customFactors.add("xor_alias", "MyCustomXor");
		
		List<ISolverFactorCreator<MyCustomFactor,ISolverFactorGraph>> creators =
			customFactors.get(Xor.class.getName());
		assertEquals(5, creators.size());
		assertSame(xor1, creators.get(1));
		assertEquals("xor1", creators.get(1).create(xorFactor, sfg).tag);
		assertSame(xor2, creators.get(2));
		assertSame(MyCustomXor3.class, creators.get(3).create(xorFactor, sfg).getClass());
		assertSame(MyCustomXor4.class, creators.get(4).create(xorFactor, sfg).getClass());
		
		creators = customFactors.get("xor_alias");
		assertEquals(1, creators.size());
		
		// Not documented, but you can change the list directly!
		customFactors.get(Xor.class.getName()).clear();
		assertTrue(customFactors.get(Xor.class.getName()).isEmpty());
		customFactors.get("xor_alias").clear();
		assertTrue(customFactors.get("xor_alias").isEmpty());
		assertTrue(customFactors.keySet().isEmpty());

		// Test addFirst methods
		customFactors.addFirst("Xor", xor1);
		customFactors.addFirst(Xor.class, xor2);
		
		customFactors.addFirst("Xor", MyCustomXor3.class);
		
		customFactors.addFirst(Xor.class, MyCustomXor4.class);
		customFactors.addFirst("xor_alias", "MyCustomXor");
		
		creators = customFactors.get(Xor.class.getName());
		assertEquals(4, creators.size());
		assertSame(xor1, creators.get(3));
		assertEquals("xor1", creators.get(3).create(xorFactor, sfg).tag);
		assertSame(xor2, creators.get(2));
		assertSame(MyCustomXor3.class, creators.get(1).create(xorFactor, sfg).getClass());
		assertSame(MyCustomXor4.class, creators.get(0).create(xorFactor, sfg).getClass());
		
		creators = customFactors.get("xor_alias");
		assertEquals(1, creators.size());
		
		// Test freeze
		customFactors.freeze();
		assertFalse(customFactors.isMutable());
		expectThrow(UnsupportedOperationException.class, customFactors, "addBuiltins");
		expectThrow(UnsupportedOperationException.class, customFactors, "add", "foo", "MyCustomXor");
		expectThrow(UnsupportedOperationException.class, customFactors, "add", "foo", xor1);
		expectThrow(UnsupportedOperationException.class, customFactors, "add", "foo", MyCustomXor.class);
		expectThrow(UnsupportedOperationException.class, customFactors, "add", Xor.class, MyCustomXor.class);
		expectThrow(UnsupportedOperationException.class, customFactors, "add", Xor.class, xor1);
		expectThrow(UnsupportedOperationException.class, customFactors, "addFirst", "foo", "MyCustomXor");
		expectThrow(UnsupportedOperationException.class, customFactors, "addFirst", "foo", xor1);
		expectThrow(UnsupportedOperationException.class, customFactors, "addFirst", "foo", MyCustomXor.class);
		expectThrow(UnsupportedOperationException.class, customFactors, "addFirst", Xor.class, MyCustomXor.class);
		expectThrow(UnsupportedOperationException.class, customFactors, "addFirst", Xor.class, xor1);
	}
	
	public static CustomFactorsOptionKey<MyCustomFactor, ISolverFactorGraph, MyCustomFactorsBase> bogusOption =
		new CustomFactorsOptionKey<>(TestCustomFactors.class, "bogusOption", MyCustomFactorsBase.class);

	public static final CustomFactorsOptionKey<MyCustomFactor, ISolverFactorGraph, MyCustomFactors> option =
		new CustomFactorsOptionKey<>(TestCustomFactors.class, "option", MyCustomFactors.class);
	
	@Test
	public void testOptionKey()
	{
		assertSame(MyCustomFactors.class, option.type());
		MyCustomFactors defaultCustomFactors = option.defaultValue();
		assertFalse(defaultCustomFactors.isMutable());

		Set<String> keys = defaultCustomFactors.keySet();
		assertEquals(1, keys.size());
		assertTrue(keys.contains(Xor.class.getName()));
		assertEquals(1, defaultCustomFactors.get(Xor.class.getName()).size());
		
		DimpleEnvironment env = DimpleEnvironment.active();
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(null);
		ISolverFactorGraph sfg = new MyCustomSolverGraph(fg);
		
		Bit b1 = new Bit();
		Bit b2 = new Bit();
		Factor xorFactor = fg.addFactor(new Xor(), b1, b2);
		Factor xorAliasFactor = fg.addFactor(new CustomFactorFunctionWrapper("xor-alias"), b1, b2);
		Factor andFactor = fg.addFactor(new And(), b1, b2);
		
		// getOrCreate
		assertNull(option.get(env));
		MyCustomFactors envCustomFactors = option.getOrCreate(env);
		assertNotNull(envCustomFactors);
		assertTrue(envCustomFactors.keySet().isEmpty());
		assertSame(envCustomFactors, option.getOrCreate(env));

		// Test a couple of unusual exception cases
		expectThrow(DimpleException.class, bogusOption, "getOrCreate", env);
		expectThrow(DimpleException.class, bogusOption, "defaultValue");
		
		//
		// Test createFactor
		//
		
		// default solver factor creation
		MyCustomFactor sfactor = option.createFactor(andFactor, sfg);
		assertSame(MyCustomFactor.class, sfactor.getClass());
		assertSame(andFactor, sfactor.getModelObject());
		assertEquals("default", sfactor.tag);
		
		// override default by registering against FactorFunction base class
		option.getOrCreate(env).addBuiltins();
		option.getOrCreate(env).add(FactorFunction.class, new ISolverFactorCreator<MyCustomFactor,ISolverFactorGraph>() {
			@Override
			public MyCustomFactor create(Factor factor, ISolverFactorGraph sgraph)
			{
				return new MyCustomFactor(factor, sgraph, "custom-default");
			}
		});
		sfactor = option.createFactor(andFactor, sfg);
		assertEquals("custom-default", sfactor.tag);
		
		// custom creation
		sfactor = option.createFactor(xorFactor, sfg);
		assertSame(MyCustomXor.class, sfactor.getClass());
		assertEquals("xor", sfactor.tag);
		
		// custom creation with alias
		expectThrow(SolverFactorCreationException.class,
			"Cannot find factor function or custom factor implementation.*",
			option, "createFactor", xorAliasFactor, sfg);
		option.getOrCreate(env).addFirst("xor-alias", MyCustomXor4.class);
		MyCustomXor4.throwMe = new RuntimeException("last failure");
		expectThrow(SolverFactorCreationException.class,
			"Cannot find factor function 'xor-alias'.*last failure",
			option, "createFactor", xorAliasFactor, sfg);
		
		option.getOrCreate(env).add("xor-alias", MyCustomXor.class);
		sfactor = option.createFactor(xorAliasFactor, sfg);
		assertSame(MyCustomXor.class, sfactor.getClass());
		
		option.getOrCreate(env).addFirst("xor-alias", MyCustomXor4.class);
		MyCustomXor4.throwMe = new RuntimeException("last failure");
		sfactor = option.createFactor(xorAliasFactor, sfg);
		assertSame(MyCustomXor.class, sfactor.getClass());
		MyCustomXor4.throwMe = null;
	}
}
