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

package com.analog.lyric.dimple.test.options;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.dimple.options.DimpleOptionRegistry;
import com.analog.lyric.dimple.options.DimpleOptions;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.options.OptionKeys;
import com.analog.lyric.options.OptionRegistry;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestDimpleOptions
{
	/**
	 * Test contents of {@link DimpleOptionRegistry} and {@link DimpleOptions} classes.
	 * <p>
	 * Makes sure that options are not accidentally omitted because they have mis-matched
	 * name or are not declared "public static final".
	 * <p>
	 * @since 0.07
	 */
	@Test
	public void test() throws IllegalAccessException
	{
		OptionRegistry registry = DimpleOptionRegistry.getRegistry();
		
		Set<IOptionKey<?>> allKeys = new HashSet<IOptionKey<?>>();
		for (IOptionKey<?> key : registry)
		{
			assertSame(key, DimpleOptionRegistry.getKey(OptionKey.canonicalName(key)));
			assertSame(key, DimpleOptionRegistry.getKey(OptionKey.qualifiedName(key)));
			assertTrue(allKeys.add(key));
		}
		
		Set<IOptionKey<?>> allKeys2 = new HashSet<IOptionKey<?>>();
		for (OptionKeys keys : registry.getOptionKeys())
		{
			assertTrue(keys.size() > 0);
			for (IOptionKey<?> key : keys.values())
			{
				assertTrue(allKeys2.add(key));
			}
			
			Class<?> declaringClass = keys.declaringClass();
			for (Field field : declaringClass.getDeclaredFields())
			{
				if (IOptionKey.class.isAssignableFrom(field.getType()))
				{
					final int modifiers = field.getModifiers();
					assertTrue(Modifier.isFinal(modifiers));
					assertTrue(Modifier.isPublic(modifiers));
					assertTrue(Modifier.isStatic(modifiers));
					
					IOptionKey<?> key = (IOptionKey<?>)field.get(declaringClass);
					assertEquals(field.getName(), key.name());
					assertSame(key, DimpleOptionRegistry.getKey(OptionKey.qualifiedName(key)));
					assertSame(declaringClass, key.getDeclaringClass());
				}
			}
		}
		
		assertEquals(allKeys, allKeys2);
	}
}
