package com.analog.lyric.dimple.test;

import org.junit.* ;

import com.analog.lyric.dimple.test.Demo;

public class DemoTest {

	@Test
	public void test_DemoMain() 
	{
		Demo.main(new String[]{"quiet"});
	}
}
