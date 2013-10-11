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

package com.analog.lyric.dimple.model;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;

public class Model
{
	IFactorGraphFactory _defaultGraphFactory;

	private Model()
	{
		try
		{
			restoreDefaultDefaultGraphFactory();
		}
		catch(Exception e)
		{
			_defaultGraphFactory = null;
		}
	}


	private static class ModelerHolder
	{
		static final Model INSTANCE = new Model();
	}

	public static Model getInstance()
	{
		return ModelerHolder.INSTANCE;
	}
	
	public void restoreDefaultDefaultGraphFactory() 
	{
		setDefaultGraphFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
	}
	
	public IFactorGraphFactory getDefaultGraphFactory()
	{
		return _defaultGraphFactory;
	}

	public void setDefaultGraphFactory(IFactorGraphFactory graphFactory) 
	{
		_defaultGraphFactory = graphFactory;
	}
	
	
	@SuppressWarnings("resource")
	public static String getVersion()
	{
		InputStream in = System.class.getResourceAsStream("/VERSION");
		if (in == null)
		{
			return "UNKNOWN";
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String version = "UNKNOWN";
		
		try
		{
			version = br.readLine();
			br.close();
		}
		catch (Exception e)
		{
			
		}
		
		return version;

	}
}
