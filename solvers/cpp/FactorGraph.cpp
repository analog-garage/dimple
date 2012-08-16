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

#include "stdafx.h"
#include "FactorGraph.h"
#include "DimpleException.h"


FactorGraph::FactorGraph(vector<Variable*> & args) 
{
	InitConstruct(args,true);
}

FactorGraph::FactorGraph(vector<Variable*> & args,bool isMaster) 
{
	InitConstruct(args,isMaster);
}

void FactorGraph::InitConstruct(vector<Variable*> & args, bool isMaster)
{
	_numIterations = 1;
	_isFrozen = false;
	_isMaster = isMaster;


	for (uint i = 0; i < args.size(); i++)
	{
		_argVars.push_back(args[i]);
		_varIds.insert(args[i]->GetId());
		
	}
}

int FactorGraph::createTable(vector<vector<int> > & table, vector<double> & values)
{
    int tableId = _id2table.size();
    _id2table[tableId] = new CombinationTable(table,values);
    return tableId;
}


FactorGraph::~FactorGraph(void)
{
	for (uint i = 0; i < _ownedFuncs.size(); i++)
		delete _ownedFuncs[i];

	for (uint i = 0; i < _nestedGraphs.size(); i++)
		delete _nestedGraphs[i];

	if (_isMaster)
    {
        for(map<int,CombinationTable*>::iterator it = _id2table.begin();
            it != _id2table.end(); it++)
        {
            delete it->second;
        }
    }
}


void FactorGraph::Solve()
{
	Freeze();
	Initialize();

	if (_numIterations <= 0)
	{
		throw DimpleException("Num iterations must be set and greater than 0");
	}

	Iterate(_numIterations);		
}

void FactorGraph::Iterate(int numIter)
{
	Freeze();

	for (int j = 0; j < numIter; j++)
	{
		for (unsigned int i = 0; i < _allVars.size(); i++)
		{
			_allVars[i]->Update();
		}
		for (unsigned int i = 0; i < _allFuncs.size(); i++)
		{
			_allFuncs[i]->Update();
		}	
	}
}


void FactorGraph::SetNumIterations(int num)
{
	_numIterations = num;
}

void FactorGraph::Initialize()
{
	Freeze();

	for (uint i = 0; i < _allVars.size(); i++)
	{
		_allVars[i]->Initialize();
	}
	for (uint i = 0; i < _allFuncs.size(); i++)
	{
		_allFuncs[i]->Initialize();
	}

}

Function * FactorGraph:: NewTable(CombinationTable * comboTable,vector<Variable*>  & tableArgs)
{
	if (_isFrozen)
		throw DimpleException("cannot call NtewTable on frozen graph");

	for (uint i = 0; i < tableArgs.size(); i++)
	{		
		//If variable is not in list add it
		if (_varIds.find(tableArgs[i]->GetId()) == _varIds.end())
		{
			_ownedVars.push_back(tableArgs[i]);
			_varIds.insert(tableArgs[i]->GetId());
		}
	}
	
	Function * f =  new Function(_ownedFuncs.size(),comboTable,tableArgs);
	_ownedFuncs.push_back(f);
	return f;	
	
}


Function * FactorGraph::createTableFunc(int tableId, vector<Variable*>  & args)
{
	if (_isFrozen)
		throw DimpleException("cannot call NewFunc on frozen graph");

    
	CombinationTable * comboTable = _id2table[tableId];

    return NewTable(comboTable,args);
    
}



void FactorGraph::AddGraph(FactorGraph * graph, vector<Variable*> & graphArgs)
{
	if (_isFrozen)
		throw DimpleException("cannot call NewFunc on frozen graph");

	//TODO: Make sure graphArgs are part of this graph.
	for (int i = 0; i < graphArgs.size(); i++)
	{
		if (_varIds.find(graphArgs[i]->GetId()) == _varIds.end())
		{
			_varIds.insert(graphArgs[i]->GetId());
			_ownedVars.push_back(graphArgs[i]);
		}
	}
	
	FactorGraph * g = graph->NewInstance(graphArgs);
	_nestedGraphs.push_back(g);

	for (uint i = 0; i < g->_allFuncs.size(); i++)
	{
		_nestedFuncs.push_back(g->_allFuncs[i]);
	}
	for (uint i = 0; i < g->_ownedVars.size(); i++)
	{
		_nestedVars.push_back(g->_ownedVars[i]);
	}
	for (uint i = 0; i < g->_nestedVars.size(); i++)
	{
		_nestedVars.push_back(g->_nestedVars[i]);
	}

}

FactorGraph * FactorGraph::NewInstance(vector<Variable*> & args)
{
	Freeze();

	//TODO: I'm fairly certian this breaks if nested graphs are added before other stuff.
	//Should modify so that it works in any order.  use map for more stuff.

	//Create return value.
	FactorGraph * retVal = new FactorGraph(args,false);

	//We will build a map of Variable pointers to indices for use later.
	map<Variable*,int> var2index = map<Variable*,int>();

	//Error check and add argument variables.
	if (args.size() != _argVars.size())
	{
		delete retVal;
		throw DimpleException("When instantiating a graph, argument variable sizes must match");
	}
	for (uint i = 0; i < args.size(); i++)
	{
		if (!args[i]->GetDomainLength() == _argVars[i]->GetDomainLength())
		{
			delete retVal;
			throw DimpleException("When instantiating a graph, Domains for each variable must match.");
		}
		//retVal->_argVars.push_back(args[i]);
		retVal->_allVars.push_back(args[i]);
		var2index[_allVars[i]] = i;
	}

	//Error check and add internal variables.
	for (uint i = 0; i < _ownedVars.size(); i++)
	{

		Variable * v = new Variable(i,_ownedVars[i]->GetDomainLength());
		v->SetPriors(*_ownedVars[i]->GetPriors());
		retVal->_allVars.push_back(v);
		retVal->_ownedVars.push_back(v);
		var2index[_ownedVars[i]] = i + _argVars.size();
	}

	//Create functions.
	for (uint i = 0; i < _ownedFuncs.size(); i++)
	{
		vector<Variable*> vars = vector<Variable*>();
		for (uint j = 0; j < _ownedFuncs[i]->GetPorts()->size(); j++)
		{
			Variable * tmp = (Variable*)((*_ownedFuncs[i]->GetPorts())[j]->GetConnectedNode());
			map<Variable*,int>::iterator it = var2index.find(tmp);
			if (it == var2index.end())
			{
				delete retVal;
				throw DimpleException("Illegal Variable referenced by function.");
			}
			int index = it->second;
			vars.push_back(retVal->_allVars[index]);
		}
		Function * f = new Function(retVal->_ownedFuncs.size(),_ownedFuncs[i]->GetComboTable(),vars);
		retVal->_allFuncs.push_back(f);
		retVal->_ownedFuncs.push_back(f);
	}

	for (uint i = 0; i < _nestedGraphs.size(); i++)
	{
		//Get arguments for graph
		vector<Variable*> vars = vector<Variable*>();
		for (uint j = 0; j < _nestedGraphs[i]->_argVars.size(); j++)
		{
			Variable * tmp = _nestedGraphs[i]->_argVars[j];
			map<Variable*,int>::iterator it = var2index.find(tmp);
			if (it == var2index.end())
			{
				delete retVal;
				throw DimpleException("Illegal Variable referenced by function.");
			}
			int index = it->second;
			vars.push_back(retVal->_allVars[index]);
		}

		//Add Graph
		retVal->AddGraph(_nestedGraphs[i],vars);
	}

	for (uint i = 0; i < retVal->_nestedVars.size(); i++)
	{
		retVal->_allVars.push_back(retVal->_nestedVars[i]);
	}
	for (uint i = 0; i < retVal->_nestedFuncs.size(); i++)
	{
		retVal->_allFuncs.push_back(retVal->_nestedFuncs[i]);
	}

	retVal->_isFrozen = true;
	return retVal;
}


Variable * FactorGraph::GetVariable(int id)
{
	Variable * v = _ownedVars[id];
	if (v->GetId() != id)
	{
		throw DimpleException("IDs don't match");
	}

	return v;
}

Function * FactorGraph::GetFunction(int id)
{
	//TODO: there should be more efficient way.
	for (int i = 0; i < _allFuncs.size(); i++)
	{
		if (_allFuncs[i]->GetId() == id)
			return _allFuncs[i];
	}
	return NULL;
}

vector<Variable*> * FactorGraph::GetVariables()
{
	Freeze();	
	return &_allVars;
}

vector<Function*> * FactorGraph::GetFunctions()
{
	Freeze();	
	return &_allFuncs;
}

void FactorGraph::Freeze()
{
	if (!_isFrozen)
	{
		for (uint i = 0; i < _argVars.size(); i++)
		{
			_allVars.push_back(_argVars[i]);
		}
		for (uint i = 0; i < _ownedVars.size(); i++)
		{
			_allVars.push_back(_ownedVars[i]);
		}
		for (uint i = 0; i < _nestedVars.size(); i++)
		{
			_allVars.push_back(_nestedVars[i]);
		}
		for (uint i = 0; i < _ownedFuncs.size(); i++)
		{
			_allFuncs.push_back(_ownedFuncs[i]);
		}
		for (uint i = 0; i < _nestedFuncs.size(); i++)
		{
			_allFuncs.push_back(_nestedFuncs[i]);
		}
		_isFrozen = true;
	}
}
