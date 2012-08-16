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



