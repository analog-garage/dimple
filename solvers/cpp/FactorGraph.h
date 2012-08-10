#pragma once

#include "Variable.h"
#include "IFunctionPointer.h"
#include "Function.h"
#include <set>

using std::set;

class FactorGraph
{
public:
	FactorGraph(vector<Variable *> & args);
	FactorGraph(vector<Variable *> & args,bool isMaster);
	~FactorGraph(void);
	Variable * GetVariable(int id);
	void AddGraph(FactorGraph * graph,vector<Variable*> & graphArgs);
	FactorGraph * NewInstance(vector<Variable*> & args);
	vector<Variable*> * GetVariables();
	vector<Function*> * GetFunctions();
	Function * GetFunction(int id);
	void SetNumIterations(int num);
	void Solve();
	void Initialize();
	void Iterate(int numIter);
    int createTable(vector<vector<int> > & table, vector<double> & values);
    Function * createTableFunc(int tableId,vector<Variable*> & varIds);

private:
	Function * NewTable(CombinationTable * comboTable,vector<Variable*>  & tableArgs);
	void Freeze();
	void InitConstruct(vector<Variable*> & args, bool isMaster);
	//Stores all variables, including external, internal and nested internal
	vector<Variable*> _allVars;
	vector<Variable*> _argVars;
	vector<Variable*> _ownedVars;
	vector<Variable*> _nestedVars;

	//Stores all functions including nested functions
	vector<Function*> _allFuncs;
	vector<Function*> _ownedFuncs;
	vector<Function*> _nestedFuncs;
	set<int> _varIds;

	//Stores all directly nested graphs.
	vector<FactorGraph*> _nestedGraphs;
	int _numIterations;
	bool _isFrozen;
	bool _isMaster;
	//CombinationTableFactory * _comboFactory;
    map<int,CombinationTable*> _id2table;
};
