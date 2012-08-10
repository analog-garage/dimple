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
