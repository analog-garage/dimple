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
