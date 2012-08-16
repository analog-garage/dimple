/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

#include "DimpleManager.h"
#include "MatlabFunctionPointer.h"
#include "DimpleException.h"
#include "mex.h"
#include "stdafx.h"

DimpleManager::DimpleManager(void)
{

}



DimpleManager::~DimpleManager(void)
{
	Clear();

}

int DimpleManager::NewVariable(int domainLength)
{
	int id = _variables.size();
	_variables.push_back(new Variable(id,domainLength));
	return id;
}


vector<FactorGraph*> * DimpleManager::GetGraphs()
{
	return &_graphs;
}

/*

Domain * DimpleManager::GetDomain(double * values,int numvalues)
{
	//TODO: look up in existing domains
	Domain * d = new Domain();
	for (int i = 0; i < numvalues; i++)
	{
		d->Add(new DoubleDomainItem(values[i]));
	}
	_domains.push_back(d);
	return d;
}

void DimpleManager::AddDomain(Domain * d)
{
	_domains.push_back(d);
}
 */

/*******************************************************************
 * Given a Matlab function name and a vector of number of variables,
 *   returns a FunctionPointer that shares that same name and a same
 *   sized vector with matching number of variables.
 *******************************************************************/
/*
IFunctionPointer * DimpleManager::GetFuncPointer(string name, vector<vector<int> > & indices)
{
	map<string,vector<pair<IFunctionPointer*,vector<vector<int> > > > >::iterator it = _nameToFunctionPointers.find(name);
	
	if (it != _nameToFunctionPointers.end())
	{
	
		vector<pair<IFunctionPointer*,vector<vector<int> > > > & vecPairs =  it->second;
		
		for (int i = 0; i < vecPairs.size(); i++)
		{
			bool found = true;
		
			pair<IFunctionPointer *, vector<vector<int> > > & p = vecPairs[i];
			vector<vector<int> > & vecIndices = p.second;
			
			if (vecIndices.size() == indices.size())
			{
				bool match = true;
				for (int i = 0; i < vecIndices.size(); i++)
				{
					if (vecIndices[i].size() != indices[i].size())
					{
						match = false;
						break;
					}
					
					for (int j = 0; j < vecIndices[i].size(); j++)
					{
							if (vecIndices[i][j] != indices[i][j])
							{
								match = false;
								break;
							}
					}
					
					if (!match)
						break;
					
				}
				if (match)
				{
					return p.first;
				}
			}
		
		}
		//Not found
		IFunctionPointer * funcPointer = new MatlabFunctionPointer(name,indices);
		pair<IFunctionPointer*,vector<vector<int> > > p = pair<IFunctionPointer*,vector<vector<int> > >(funcPointer,indices);
		vecPairs.push_back(p);
		return funcPointer;
	}
	else
	{		
		
		IFunctionPointer * funcPointer = new MatlabFunctionPointer(name,indices);
		
		pair<IFunctionPointer*,vector<vector<int> > > p = pair<IFunctionPointer*,vector<vector<int> > >(funcPointer,indices);
		vector<pair<IFunctionPointer*,vector<vector<int> > > > vec = vector<pair<IFunctionPointer*,vector<vector<int> > > >();
		vec.push_back(p);
		_nameToFunctionPointers[name] = vec;
		return funcPointer;
	}
}
 *
 */

int DimpleManager::NewInstance(int graphId,vector<Variable*> & variables)
{
	FactorGraph * graph = GetGraph(graphId)->NewInstance(variables);
	_graphs.push_back(graph);
	return _graphs.size()-1;
}


int DimpleManager::NewGraph(vector<Variable*> & vars)
{
	_graphs.push_back(new FactorGraph(vars));
	return _graphs.size()-1;
}

Variable * DimpleManager::GetVariable(int graphId, int varId)
{
	if (graphId == -1)
	{
		return _variables[varId];
	}
	else
	{
		return _graphs[graphId]->GetVariable(varId);
	}
}

void DimpleManager::Clear()
{
	for(uint i = 0; i < _graphs.size(); i++)
	{
		delete _graphs[i];
	}
	_graphs.clear();

    /*
	for (uint i = 0; i < _domains.size(); i++)
	{
		delete _domains[i];
	}
	_domains.clear();
*/
    
    /*
	for (map<string,vector<pair<IFunctionPointer*,vector<vector<int> > > > >::iterator it = _nameToFunctionPointers.begin();
		it != _nameToFunctionPointers.end(); it++)
	{
		vector<pair<IFunctionPointer*,vector<vector<int> > > > & sec = it->second;
		for (int j = 0; j < sec.size(); j++)
		{
			delete sec[j].first;
		}
	}
	_nameToFunctionPointers.clear();
     */

	for (uint i = 0; i < _variables.size(); i++)
	{
		delete _variables[i];
	}
	_variables.clear();
}

FactorGraph * DimpleManager::GetGraph(int id)
{
	return _graphs[id];

}
