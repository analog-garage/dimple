#include "stdafx.h"
#include "CombinationTableFactory.h"
#include "DimpleException.h"



CombinationTable::CombinationTable(vector<vector<int> > & table, vector<double> & values)
{
    _table.resize(table.size());
    for (int i = 0; i < table.size(); i++)
    {
        _table[i].resize(table[i].size());
        for (int j = 0; j < table[i].size(); j++)
        {
            _table[i][j] = table[i][j];
        }
    }
    
    _values.assign(values.begin(),values.end());
}


CombinationTable::~CombinationTable()
{
}


vector<vector<int> > * CombinationTable::GetTable()
{
	return & _table;
}

vector<double> * CombinationTable::GetValues()
{
	return & _values;
}



