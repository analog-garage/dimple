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
