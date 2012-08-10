package com.analog.lyric.dimple.model.serializerdetails;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.analog.lyric.dimple.model.VariableBase;

class xmlsFactorGraph
{
	public UUID _uuid = java.util.UUID.randomUUID();
	public String _explicitName = "";
	public String _solverClass = "";
	public HashMap<UUID, VariableBase> _variables = new HashMap<UUID, VariableBase>();
	public ArrayList<VariableBase> _boundaryVariables = new ArrayList<VariableBase>();
	public HashMap<UUID, xmlsFactor> _factors = new HashMap<UUID, xmlsFactor>();
	public HashMap<Integer, xmlsFactorTable> _factorTables = new HashMap<Integer, xmlsFactorTable>(); 		
	public HashMap<Integer, Integer> _ephemeralToSequentialId = new HashMap<Integer, Integer>(); 		
}
