#pragma once
#include "IFunctionPointer.h"
#include <vector>
#include <map>
#include "Domain.h"

using namespace std;


class FunctionEntry;
class CombinationTableFactory;

class CombinationTable
{
public:
	CombinationTable(vector<vector<int> > & table,vector<double> & values);
	~CombinationTable();

private:
	vector<vector<int> > _table;
	vector<double> _values;
public:
	vector<vector<int> > * GetTable();
	vector<double> * GetValues();

};
