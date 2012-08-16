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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class TrivialOutputFile 
{
	private StringBuilder _sb = new StringBuilder();
	private String _name;
	private File _dir;
	private File _file;
	private boolean _append;
	
	public TrivialOutputFile(File dir,
	  		 				 String name)
	{
		this(dir, name, false);
	}
	
	public TrivialOutputFile(File dir,
					  		 String name, 
					  		 boolean append)
	{
		_dir = dir;
		_name = name;
		_append = append;
		_file = new File(_dir.getAbsolutePath() + "/" + _name);
	}
	
	public String getString(){return _sb.toString();}
	public StringBuilder getStringBuilder(){return _sb;}
	public File getFile(){return _file;}
	public File getDir(){return _dir;}
	public String getName(){return _name;}
	public void clear(){_sb = new StringBuilder();}
	
	public void append(String s)
	{
		_sb.append(s);
	}
	
	public void write() throws IOException 
	{
	    Writer output = new BufferedWriter(new FileWriter(_file, _append));
	    output.write(_sb.toString());
	    output.close();		
	}
}
