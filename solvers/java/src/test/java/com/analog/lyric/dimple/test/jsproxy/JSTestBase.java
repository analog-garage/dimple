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

package com.analog.lyric.dimple.test.jsproxy;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class JSTestBase extends DimpleTestBase
{
	final DimpleAppletTestState state = new DimpleAppletTestState();
	
	/**
	 * Attempts to add deploy.jar from Java's home install dir to class path.
	 * <p>
	 * This is a big hack to dynamically add deploy.jar to the classpath.
	 * <p>
	 * It appears that deploy.jar is necessary to be able to create JSObjects, at least on Windows,
	 * but this jar does not appear to exist on Ubuntu and we do not want to create any extra difficult
	 * platform-specific dependencies just to get a few tests to run.
	 * <p>
	 * @since 0.07
	 */
	@Before
	public void addDeployJarToClasspath()
	{
		try
		{
			String javaHome = System.getProperty("java.home");
			File deployJarFile = new File(javaHome + "/lib/deploy.jar");
			
			if (deployJarFile.exists())
			{
				@SuppressWarnings("resource")
				URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
				
				URL deployJarUrl = deployJarFile.toURI().toURL();

				Class<URLClassLoader> clazz= URLClassLoader.class;

				// Use reflection
				Method method= clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
				method.setAccessible(true);
				method.invoke(classLoader, new Object[] { deployJarUrl });
			}
		}
		catch (Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
	
	/**
	 * Creates a new JSObject for testing, or null if not supported in current configuration.
	 * <p>
	 * @since 0.07
	 */
	@Nullable FakeJSObject createJSObject()
	{
		try
		{
			return new FakeJSObject();
		}
		catch (Throwable ex)
		{
			DimpleEnvironment.logWarning("Cannot create JSObject for test '%s': %s\n",
				getClass().getSimpleName(), ex.toString());
			return null;
		}
	}
}
