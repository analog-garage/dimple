package com.analog.lyric.dimple.model;

import java.util.UUID;

import com.analog.lyric.util.misc.IGetId;

public interface INameable extends IGetId
{
	public int getId();	
	
	public UUID getUUID();
	public void setUUID(UUID newUUID) ;
	
	//Returns explicitly set name or, if name not set, UUID as string
	public String getName();
	
	//Returns explicitly set name or, if name not set, null 
	public String getExplicitName();
	
	//Returns name qualified by all parent graphs, if there are any
	//	Each parent is separated by the '.' character.
	public String getQualifiedName();
	
	//Disallowed values:
	//	'.' character anywhere in the name. 
	//	A name already present in the immediate parent graph
	public void setName(String name) ;

	//Does not have to be unique. Object cannot be found with this name. 
	public void setLabel(String name) ;
	
	//Returns explicitly setLabel or, if not set, explicitly set name, or if none set, 
	//	then a some shortened version of the UUID, 
	//	suitable for printing, but not guaranteed to be
	//	unique. 
	public String getLabel();

	//As getLabel, but each ancestor name is also
	//	truncated as necessary. 
	public String getQualifiedLabel();

}
