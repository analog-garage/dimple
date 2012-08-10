#pragma once
#include <vector>
#include "IFunctionPointer.h"
#include "Variable.h"
#include "CombinationTableFactory.h"

using namespace std;

class Function : public INode
{
public:
	Function(int id, CombinationTable * comboTable, vector<Variable*> & args);
	~Function(void);
	void Update(int portNum);
	void Update();
	void Initialize();
    int GetId();
	vector<Port*> * GetPorts();
	CombinationTable * GetComboTable();
private:
	vector<Port*> _ports;
	CombinationTable * _comboTable;
    int _id;

};
