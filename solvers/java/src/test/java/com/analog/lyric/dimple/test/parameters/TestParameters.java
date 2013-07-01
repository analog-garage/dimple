package com.analog.lyric.dimple.test.parameters;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.parameters.IParameterKey;
import com.analog.lyric.dimple.parameters.IParameterList;
import com.analog.lyric.dimple.parameters.Parameter;
import com.analog.lyric.dimple.parameters.ParameterKey;
import com.analog.lyric.dimple.parameters.ParameterList1;
import com.analog.lyric.dimple.parameters.ParameterList2;
import com.analog.lyric.dimple.parameters.ParameterListN;
import com.analog.lyric.dimple.parameters.SharedParameterValue;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.util.test.SerializationTester;

public class TestParameters
{
	public static class ParamN extends ParameterListN<IParameterKey>
	{
		private static final long serialVersionUID = 1L;

		public ParamN(int size)
		{
			super(size);
		}
		
		public ParamN(ParamN that)
		{
			super(that);
		}
		
		public ParamN(double ... values)
		{
			super(values);
		}
		
		public ParamN(boolean fixed, double ... values)
		{
			super(fixed, values);
		}

		@Override
		public IParameterKey[] getKeys()
		{
			return null;
		}

		@Override
		public ParamN clone()
		{
			return new ParamN(this);
		}
	}
	
	public static enum Alpha implements IParameterKey
	{
		alpha(0, RealDomain.full());
		
		 private final double _defaultValue;
		 private final RealDomain _domain;

		 private Alpha(double defaultValue, RealDomain domain)
		 {
			 _defaultValue = defaultValue;
			 _domain = domain;
		 }

		 @Override
		 public Class<Double> type() { return Double.class; }
		 @Override
		 public Double defaultValue() { return _defaultValue; }
		 @Override
		public Double lookup(IOptionHolder holder) { return holder.options().lookup(this); }
		 @Override
		public void set(IOptionHolder holder, Double value) { holder.options().set(this,  value); }
		 @Override
		public void unset(IOptionHolder holder) { holder.options().unset(this); }
		 @Override
		 public RealDomain domain() { return _domain; }
	}
	
	public static class Param1 extends ParameterList1<Alpha>
	{
		private static final long serialVersionUID = 1L;

		public Param1()
		{
		}
		
		public Param1(Param1 that)
		{
			super(that);
		}
		
		@Override
		public Alpha[] getKeys()
		{
			return Alpha.values();
		}

		@Override
		public Param1 clone()
		{
			return new Param1(this);
		}
	}
	
	public static class AlphaBeta
	{
		public static final ParameterKey alpha = new ParameterKey(0, AlphaBeta.class, "alpha");
		public static final ParameterKey beta = new ParameterKey(1, AlphaBeta.class, "beta");
		
		public static ParameterKey[] values()
		{
			return new ParameterKey[] { alpha, beta };
		}
	}
	
	public static class Param2 extends ParameterList2<ParameterKey>
	{
		private static final long serialVersionUID = 1L;

		public Param2()
		{
		}
		
		public Param2(Param2 that)
		{
			super(that);
		}
		
		@Override
		public ParameterKey[] getKeys()
		{
			return AlphaBeta.values();
		}

		@Override
		public ParameterList2<ParameterKey> clone()
		{
			return new Param2(this);
		}
	}
	
	@Test
	public void testParameterList()
	{
		Param1 n1 = new Param1();
		assertEquals(1, n1.size());
		assertParameterListInvariants(n1);
		assertFalse(n1.isShared(Alpha.alpha));
		
		Param2 n2 = new Param2();
		assertEquals(2, n2.size());
		assertParameterListInvariants(n2);
		assertFalse(n2.isShared(0));
		assertFalse(n2.isShared(AlphaBeta.beta));
		
		// Parameter sharing
		assertTrue(n1.canShare());
		assertTrue(n2.canShare());
		n2.setShared(AlphaBeta.alpha, true);
		assertNotNull(n2.getSharedValue(AlphaBeta.alpha));
		n1.setSharedValue(Alpha.alpha, n2.getSharedValue(AlphaBeta.alpha));
		assertSame(n1.getSharedValue(Alpha.alpha), n2.getSharedValue(AlphaBeta.alpha));
		n1.set(0, 42);
		assertEquals(42, n2.get(0), 0.0);
		
		n2.setFixed(AlphaBeta.beta, true);
		assertParameterListInvariants(n1);
		assertParameterListInvariants(n2);
		
		ParamN n10 = new ParamN(10);
		assertEquals(10, n10.size());
		assertParameterListInvariants(n10);
		assertFalse(n10.canShare());
	
		n10 = new ParamN(1,2,3,4,5,6,7,8,9,10);
		assertEquals(10, n10.size());
		assertParameterListInvariants(n10);
		for (int i = 0; i < n10.size(); ++i)
		{
			assertEquals(i + 1, n10.get(i), 0.0);
		}
		
		ParamN n5 = new ParamN(true, 0, 2, 4, 6, 8);
		assertEquals(5, n5.size());
		assertParameterListInvariants(n5);
		for (int i = 0; i < 5; ++i)
		{
			assertTrue(n5.isFixed(i));
			assertEquals(i * 2, n5.get(i), 0.0);
		}
	}

	public static <Key extends IParameterKey> void assertParameterListInvariants(IParameterList<Key> list)
	{
		assertTrue(list.size() > 0);
		double[] values = list.getValues();
		assertEquals(list.size(), values.length);
		
		for (int i = 0; i < values.length; ++i)
		{
			double val = list.get(i);
			assertEquals(val, values[i], 0.0);
			
			SharedParameterValue sharedValue = list.getSharedValue(i);
			assertEquals(list.isShared(i), sharedValue != null);
			if (sharedValue != null)
			{
				assertEquals(val, sharedValue.get(), 0.0);
			}
			
			if (list.isFixed(i))
			{
				try
				{
					list.set(i, val + 1);
					fail("should not get here");
				}
				catch (DimpleException ex)
				{
				}
				assertEquals(val, list.get(i), 0.0);
			}
		}
		
		Key[] keys = list.getKeys();
		
		assertEquals(list.hasKeys(), keys != null);
		if (keys != null)
		{
			assertEquals(list.size(), keys.length);
			for (int i = 0; i < keys.length; ++i)
			{
				Key key = keys[i];
				double val = list.get(i);
				assertEquals(i, key.ordinal());
				assertEquals(val, list.get(key), 0.0);
				assertEquals(list.getSharedValue(i), list.getSharedValue(key));
				assertEquals(list.isFixed(i),  list.isFixed(key));
				assertEquals(list.isShared(i), list.isShared(key));
				
				if (list.isFixed(key))
				{
					try
					{
						list.set(key, val + 1);
						fail("should not get here");
					}
					catch (DimpleException ex)
					{
					}
					assertEquals(val, list.get(key), 0.0);
				}
			}
		}
		
		{
			int i = 0;
			for (Parameter<Key> param : list)
			{
				assertEquals(i, param.index());
				assertEquals(list.get(i), param.value(), 0.0);
				assertSame(list.getSharedValue(i), param.sharedValue());
				if (keys != null)
				{
					assertEquals(keys[i], param.key());
				}
				assertEquals(!Double.isNaN(list.get(i)), param.known());
				++i;
			}
		}
		
		//
		// Errors
		//
		
		try
		{
			list.get(-1);
			fail("should not get here");
		}
		catch (IndexOutOfBoundsException ex)
		{
		}
		
		try
		{
			list.get(list.size());
			fail("should not get here");
		}
		catch (IndexOutOfBoundsException ex)
		{
		}
		
		if (keys == null)
		{
			try
			{
				list.get(null);
				fail("should not get here");
			}
			catch (UnsupportedOperationException ex)
			{
			}
		}
		
		if (!list.canShare())
		{
			try
			{
				list.setShared(0, true);
				fail("should not get here");
			}
			catch (UnsupportedOperationException ex)
			{
			}
			assertFalse(list.isShared(0));
			
			list.setShared(0, false);
			
			try
			{
				list.setSharedValue(0, new SharedParameterValue());
				fail("should not get here");
			}
			catch (UnsupportedOperationException ex)
			{
			}
			
			list.setSharedValue(0, null);
		}
		
		//
		// Cloning
		//
		
		IParameterList<Key> copy1 = list.clone();
		assertNotSame(copy1, list);
		assertParameterListEquals(list, copy1);
		for (int i = 0; i < list.size(); ++i)
		{
			assertSame(list.getSharedValue(i), copy1.getSharedValue(i));
		}
		
		IParameterList<Key> copy2 = SerializationTester.clone(list);
		assertNotSame(copy2, list);
		assertParameterListEquals(list, copy2);
		for (int i = 0; i < copy2.size(); ++i)
		{
			copy2.setShared(i, false);
			assertFalse(copy2.isShared(i));
		}
		
		//
		// Test mutable cases on cloned list
		//
		
		copy2.setAllFixed(false);
		for (int i = 0; i < copy2.size(); ++i)
		{
			assertFalse(copy2.isFixed(i));
			copy2.set(i, i);
			assertEquals(i, copy2.get(i), 0.0);
		}
		
		copy2.setAllMissing();
		for (Parameter<Key> param : copy2)
		{
			assertFalse(param.known());
		}
		
		copy2.setAllToDefault();
		if (keys != null)
		{
			for (int i = 0; i <copy2.size(); ++i)
			{
				assertEquals(keys[i].defaultValue(), copy2.get(i), 0.0);
			}
		}
		
		copy2.setAll(copy1.getValues());
		
		copy2.setAllFixed(true);
		copy2.setAllToDefault(); // Does nothing if everything is fixed
		copy2.setAllMissing(); // ditto
		try
		{
			copy2.setAll(new double[list.size()]);
			fail("should not get here");
		}
		catch (DimpleException ex)
		{
		}
		for (int i = 0; i < copy2.size(); ++i)
		{
			assertTrue(copy2.isFixed(i));
			assertEquals(list.get(i), copy2.get(i), 0.0);
		}
		
		// Fix every-other parameter, and set non-fixed params.
		for (int i = 0; i < copy2.size(); i += 2)
		{
			copy2.setFixed(i, false);
		}
		copy2.setAllMissing();
		for (Parameter<Key> param : copy2)
		{
			switch (param.index() % 2)
			{
			case 0:
				assertFalse(param.fixed());
				assertFalse(param.known());
				break;
			case 1:
				assertTrue(param.fixed());
				assertEquals(copy2.get(param.index()), param.value(), 0.0);
				break;
			}
		}
		
		for (int i = 0; i < copy2.size(); ++i)
		{
			copy2.setFixed(i, !copy2.isFixed(i));
		}
		copy2.setAllToDefault();
		for (Parameter<Key> param : copy2)
		{
			switch (param.index() % 2)
			{
			case 0:
				assertTrue(param.fixed());
				assertFalse(param.known());
				break;
			case 1:
				assertFalse(param.fixed());
				if (param.key() != null)
				{
					assertEquals(param.key().defaultValue(), param.value(), 0.0);
				}
				else
				{
					assertEquals(copy2.get(param.index()), param.value(), 0.0);
				}
				break;
			}
		}
		
	} // assertParameterListInvariants
	
	public static <Key extends IParameterKey> void assertParameterListEquals(IParameterList<Key> list1,
		IParameterList<Key> list2)
	{
		assertEquals(list1.getClass(), list2.getClass());
		assertEquals(list1.size(), list2.size());
		assertArrayEquals(list1.getValues(), list2.getValues(), 0.0);
		assertArrayEquals(list1.getKeys(), list2.getKeys());
		for (int i = list1.size(); --i >=0;)
		{
			assertEquals(list1.isFixed(i), list2.isFixed(i));
			assertEquals(list1.isShared(i), list2.isShared(i));
		}
	}
}
