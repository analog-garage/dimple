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

package com.analog.lyric.util.misc;

public class Trace
{
	public static void prettyPrintArray(int [] array)
	{
		System.out.print("[");
		for (int i = 0; i < array.length; i++)
		{
			System.out.print(array[i] + " ");
		}
		System.out.print("]\n");
	}
	static public String trace(int[] data)
	{
		StringBuilder sb = new StringBuilder();
		trace(sb, "%2d", data);
		return sb.toString();
	}
	static public void trace(StringBuilder sb, int[] data)
	{
		trace(sb, "%2d", data);
	}
	static public void trace(StringBuilder sb, String intFormat, int[] data)
	{
		sb.append("[");
		for(int i = 0; i < data.length; ++i)
		{
			sb.append(String.format(intFormat + "  ", data[i]));
		}
		sb.append("]");
	}
	static public void traceMem(StringBuilder sb, int[] data)
	{
		traceMem(sb, data, false);
	}
	static public void traceMem(StringBuilder sb, int[] data, boolean lineNumbers)
	{
		if(lineNumbers)
		{
			sb.append(String.format("%03d:\t", 0));
		}
		for(int i = 0; i < data.length; ++i)
		{
			sb.append(String.format("%08X ", data[i]));
			if(lineNumbers && (i + 1) % 8 == 0)
			{
				sb.append("\n");
				sb.append(String.format("%03d:\t", i + 1));
			}
		}
	}
	static public void traceBytes(StringBuilder sb, byte[] data)
	{
		traceBytes(sb, data, false, false);
	}
	static public void traceBytes(StringBuilder sb, byte[] data, boolean multipleLines, boolean lineNumbers)
	{
		if(lineNumbers)
		{
			sb.append(String.format("%03d:\t", 0));
		}
		for(int i = 0; i < data.length; ++i)
		{
			sb.append(String.format("%02X ", data[i]));
			if(multipleLines && lineNumbers && (i + 1) % 8 == 0)
			{
				sb.append("\n");
				sb.append(String.format("%03d:\t", i + 1));
			}
		}
	}
	static public String traceBytes(byte[] data)
	{
		StringBuilder sb = new StringBuilder();
		traceBytes(sb, data, false, false);
		return sb.toString();
	}
	static public String traceBytes(byte[] data, boolean multipleLines, boolean lineNumbers)
	{
		StringBuilder sb = new StringBuilder();
		traceBytes(sb, data, multipleLines, lineNumbers);
		return sb.toString();
	}
	
	static public String traceMem(int[] data)
	{
		StringBuilder sb = new StringBuilder();
		traceMem(sb, data);
		return sb.toString();
	}
	static public String traceMem(int[][] data)
	{
		StringBuilder sb = new StringBuilder();
		traceMem(sb, data);
		return sb.toString();
		
	}
	static public void traceMem(StringBuilder sb, int[][] data)
	{
		traceMem(sb, data, false);
	}
	static public void traceMem(StringBuilder sb, int[][] data, boolean linePerRow)
	{
		for(int i = 0; i < data.length; ++i)
		{
			sb.append("[");
			traceMem(sb, data[i]);
			sb.append("]");
			
			if(linePerRow)
			{
				sb.append("\n");
			}
			else if(i < data.length - 1)
			{
				sb.append(" ");
			}
			
			
		}
	}
	static public void trace(String name, String tag, double[] data)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(tag + "\t[");
		for(int i = 0; i < data.length; ++i)
		{
			sb.append(String.format("%03f  ", data[i]));
		}
		sb.append("]  " + name);
		System.out.println(sb.toString());
	}
	static public void traceBeliefs(String tag, double[][] beliefs)
	{
		for(int i = 0; i < beliefs.length; ++i)
		{
			trace("", tag, beliefs[i]);
		}
	}
	static public int traceBeliefs(String tag, double[][] got, double[][] expected)
	{
		double epsilon = 1e-6;
		int diffCount = 0;
		for(int i = 0; i < got.length; ++i)
		{
			double[] diffs = new double[got[i].length];
			boolean someDiff = false;
			for(int d = 0; d < diffs.length; ++d)
			{
				diffs[d] = Math.abs(expected[i][d] - got[i][d]);
				someDiff = diffs[d] > epsilon;
			}
			String tempTag =  tag + Integer.toString(i);
			trace("expected", 	tempTag, expected[i]);
			trace("got", 		tempTag, got[i]);

			trace(someDiff ?
					"diff <-----------" :
					"diff",
				  tempTag, diffs);
			
			if(someDiff){diffCount++;}
			
			System.out.println();
			
		}
		return diffCount;
	}
	static public void traceBooleans(StringBuilder sb, boolean[] data)
	{
		for(int i = 0; i < data.length; ++i)
		{
			sb.append(String.format("%s ", data[i]));
			if((i + 1) % 8 == 0)
			{
				sb.append("\n");
			}
		}
	}
	static public void traceBooleans(StringBuilder sb, boolean[][] data)
	{
		for(int i = 0; i < data.length; ++i)
		{
			sb.append("[");
			traceBooleans(sb, data[i]);
			sb.append("]");
			
			if(i < data.length - 1)
			{
				sb.append(" ");
			}
		}
	}
}
