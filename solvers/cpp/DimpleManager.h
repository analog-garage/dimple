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

#pragma once

#include "FactorGraph.h"
#include "DoubleDomainItem.h"
#include "IFunctionPointer.h"
#include "Variable.h"
#include <string>
//#include <multimap>

using namespace std;

class DimpleManager
{
public:
	DimpleManager(void);
	~DimpleManager(void);

	int NewVariable(int domainLength);
	Variable * GetVariable(int graphId, int varId);
	int NewGraph(vector<Variable*> & variables);
	FactorGraph * GetGraph(int id);
	vector<FactorGraph*> * GetGraphs();
	int NewInstance(int graphId,vector<Variable*> & variables);
	//Domain * GetDomain(double * values,int numvalues);	
	//void AddDomain(Domain * domain);
	//IFunctionPointer * GetFuncPointer(string name, vector<vector<int> > & indices);
	void Clear();
private:
	//vector<Domain*> _domains;
	//map<string,vector<pair<IFunctionPointer*,vector<vector<int> > > > > _nameToFunctionPointers;
	vector<Variable*> _variables;
	vector<FactorGraph*> _graphs;
};
