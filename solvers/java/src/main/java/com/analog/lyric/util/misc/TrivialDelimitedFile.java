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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TrivialDelimitedFile 
{
	private File _dir;
	private File _file;
	private String _delimiter;
	private ArrayList<String[]> _lines = new ArrayList<String[]>();
	
	public TrivialDelimitedFile(File dir, 
								String file,
								String delimiter) throws IOException 
	{
		_dir = dir;
		_file = new File(_dir.getAbsolutePath() + "/" + file);
		_delimiter = delimiter;
		
	    FileInputStream fis = new FileInputStream(_file);
	    BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedReader d = new BufferedReader(new InputStreamReader(bis));
		
//		System.out.println(String.format("Parsing [%s]", _file.getAbsoluteFile()));
//		boolean once = false;
		for(String line = d.readLine(); line != null; line = d.readLine())
		{
			String[] row = line.split(_delimiter);
			_lines.add(row);
//			if(!once)
//			{
//				once = true;
//				System.out.println(String.format("First line, %d entries, [%s]", row.length, line));
//			}
		}
		
		d.close();
		bis.close();
		fis.close();
	}
	
	public ArrayList<String[]> getLines()	{return _lines;}	
	public File getDir()					{return _dir;}
	public File getFile()					{return _file;}
	public String getDelimiter()			{return _delimiter;}
}
