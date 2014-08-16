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

package com.analog.lyric.util.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault(false)
public class TrivialArgs
{
	private HashMap<String, ArrayList<String>> _args = null;

	static public String getDefaultArg(){return "dflt";}
	static public String getArgName(String arg)
	{
		String argName = null;
		if(arg.startsWith("-"))
		{
			argName = arg.substring(1);
		}
		return argName;
	}
	
	public void addReplace(String[] args)
	{
		String currArg = getDefaultArg();
		_args.remove(currArg);
		_args.put(currArg, new ArrayList<String>());
		
		if(args != null)
		{
			for(String arg : args)
			{
				String argName = getArgName(arg);
				if(argName != null)
				{
					_args.remove(argName);
				}
			}
			add(args);
		}
	}
	public void addReplace(HashMap<String, ArrayList<String>> args)
	{
		String defaultArg = getDefaultArg();
		_args.remove(defaultArg);
		_args.put(defaultArg, new ArrayList<String>());

		for(Entry<String, ArrayList<String>> entry: args.entrySet())
		{
			_args.remove(entry.getKey());
			add(entry.getKey(), entry.getValue());
		}
	}
	
	public void addReplace(String arg, String value)
	{
		addReplace(arg, new String[]{value});
	}
	public void addReplace(String arg, String[] values)
	{
		addReplace(arg, new ArrayList<String>(Arrays.asList(values)));
	}
	public void addReplace(String arg, ArrayList<String> values)
	{
		_args.remove(arg);
		add(arg, values);
	}

	public void add(String arg, String value)
	{
		add(arg, new String[]{value});
	}
	public void add(String arg, String[] values)
	{
		add(arg, new ArrayList<String>(Arrays.asList(values)));
	}
	public void add(String arg, ArrayList<String> values)
	{
		if(!_args.containsKey(arg))
		{
			_args.put(arg, new ArrayList<String>());
		}

		ArrayList<String> existingValues = _args.get(arg);
		existingValues.addAll(values);
	}
	public void add(HashMap<String, ArrayList<String>> args)
	{
		for(Entry<String, ArrayList<String>> entry: args.entrySet())
		{
			add(entry.getKey(), entry.getValue());
		}
	}
	public void add(String[] args)
	{
		if(args != null)
		{
			String currArgName = getDefaultArg();
			for(String arg : args)
			{
				String argName = getArgName(arg);
				if(argName != null)
				{
					currArgName = argName;
					
					if(!_args.containsKey(currArgName))
					{
						_args.put(currArgName, new ArrayList<String>());
						
					}
				}
				else
				{
					ArrayList<String> values = _args.get(currArgName);
					values.add(arg);
				}
			}
		}
	}
	public TrivialArgs(String args, String defaults)
	{
		init(args.split(" "), defaults.split(" "));
	}
	public TrivialArgs(String[] args, String[] defaults)
	{
		init(args, defaults);
	}
	public TrivialArgs(String[] defaults)
	{
		init(new String[]{}, defaults);
	}
	public TrivialArgs(String defaults)
	{
		init(new String[]{}, defaults.split(" "));
	}
	public TrivialArgs()
	{
		init(new String[]{}, new String[]{});
	}
	public void init(String[] args, String[] defaults)
	{
		_args = new HashMap<String, ArrayList<String>>();
		addReplace(defaults);
		addReplace(args);
	}
	
	public HashMap<String, ArrayList<String>> getArgs() {return _args;}
	public ArrayList<String> getArgs(String argName) {return _args.get(argName);}
	public String getArg(String argName)
	{
		String arg = null;
		ArrayList<String> argList = getArgs(argName);
		if(argList != null)
		{
			arg = argList.get(0);
		}
		return arg;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("TrivialArgs: %d args\n", _args.size()));
		
		int i = 0;
		for(Entry<String, ArrayList<String>> e: _args.entrySet())
		{
			String argName = e.getKey();
			ArrayList<String> argVal = e.getValue();
			sb.append(String.format("%d\t%s:\t", i, argName));
			for(String arg : argVal)
			{
				sb.append(String.format("%s  ", arg));
			}
			sb.append("\n");
			i++;
		}
		return sb.toString();
	}
}
