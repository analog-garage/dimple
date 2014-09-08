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

package com.analog.lyric.dimple.model.serializerdetails;
import java.util.ArrayList;
import java.util.UUID;

import com.analog.lyric.dimple.model.variables.Variable;

class xmlsFactor
{
	private UUID _uuid = java.util.UUID.randomUUID();
	private String _explicitName = "";
	private String _modelerClass = "";
	private ArrayList<Variable> _variables = new ArrayList<Variable>();
	private int _factorTableId = -1;
	
	public xmlsFactor()
	{
	}

	public String toString()
	{
		String variables = "[";
		for(Variable v : _variables)
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
		_variables = new ArrayList<Variable>(numEdges);
		for(int i = 0; i < numEdges; ++i)
		{
			_variables.add(null);
		}
	}
	public ArrayList<Variable> getVariables()
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
