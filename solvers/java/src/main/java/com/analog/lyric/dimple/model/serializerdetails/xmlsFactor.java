package com.analog.lyric.dimple.model.serializerdetails;
import java.util.ArrayList;
import java.util.UUID;

import com.analog.lyric.dimple.model.VariableBase;

class xmlsFactor
{
	private UUID _uuid = java.util.UUID.randomUUID();
	private String _explicitName = "";
	private String _modelerClass = "";
	private ArrayList<VariableBase> _variables = new ArrayList<VariableBase>();
	private int _factorTableId = -1;
	
	public xmlsFactor()
	{
	}

	public String toString()
	{
		String variables = "[";
		for(VariableBase v : _variables)
		{
			variables += v.toString();
			variables += " ";
		}
		variables += "]";
		
		String s = String.format("{%s}{%s}  %s" 
								 ,getExplicitName()
								 ,getModelerClass()
								 ,variables);
		return s;
	}
	
	public UUID getUUID()
	{
		return _uuid;
	}
	public void setUUID(UUID uuid)
	{
		_uuid = uuid;
	}
	public void setModelerClass(String modelerClass) {
		_modelerClass = modelerClass;
	}
	public String getModelerClass() {
		return _modelerClass;
	}
	public void setExplicitName(String explicitName) {
		_explicitName = explicitName;
	}
	public String getExplicitName() {
		return _explicitName;
	}
	
	public int getNumEdges()
	{
		return _variables.size();
	}
	public void setNumEdges(int numEdges)
	{
		_variables = new ArrayList<VariableBase>(numEdges);
		for(int i = 0; i < numEdges; ++i)
		{
			_variables.add(null);
		}
	}
	public ArrayList<VariableBase> getVariables()
	{
		return _variables;
	}
	
	public int getFactorTableId()
	{
		return _factorTableId;
	}
	public void setFactorTableId(int id)
	{
		_factorTableId = id;
	}
	public boolean isDiscrete()
	{
		return _factorTableId != -1;
	}
}
