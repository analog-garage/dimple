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

package com.analog.lyric.collect.tests;

/**
 * Test classes for use by {@link TestSupers}
 * @since 0.08
 * @author Christopher Barber
 */
public class TestSupers2
{
	public static interface PublicInterface
	{
		public int foo();
		public int foo(int x);
	}
	
	public abstract static class PublicClass
	{
		public abstract int bar();
		public abstract int bar(int x);
	}
	
	private static class PrivateClass0 extends PublicClass
	{
		@Override
		public int bar()
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int bar(int x)
		{
			return bar();
		}
	}
	
	private static class PrivateClass1 extends PrivateClass0
	{
		@Override
		public int bar()
		{
			return 42;
		}
		
		@Override
		public int bar(int x)
		{
			return x;
		}
	}
	
	public static PublicClass makePrivateClass1()
	{
		return new PrivateClass1();
	}
	
	private static class PrivateClass2 implements PublicInterface
	{

		@Override
		public int foo()
		{
			return 23;
		}

		@Override
		public int foo(int x)
		{
			return -x;
		}
	}
	
	public static PublicInterface makePrivateClass2()
	{
		return new PrivateClass2();
	}
}
