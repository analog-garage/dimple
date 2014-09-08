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
import java.util.HashMap;
import java.util.UUID;

import com.analog.lyric.dimple.model.variables.Variable;

class xmlsFactorGraph
{
	public UUID _uuid = java.util.UUID.randomUUID();
	public String _explicitName = "";
	public String _solverClass = "";
	public HashMap<UUID, Variable> _variables = new HashMap<UUID, Variable>();
	public ArrayList<Variable> _boundaryVariables = new ArrayList<Variable>();
	public HashMap<UUID, xmlsFactor> _factors = new HashMap<UUID, xmlsFactor>();
	public HashMap<Integer, xmlsFactorTable> _factorTables = new HashMap<Integer, xmlsFactorTable>(); 		
	public HashMap<Integer, Integer> _ephemeralToSequentialId = new HashMap<Integer, Integer>(); 		
}
