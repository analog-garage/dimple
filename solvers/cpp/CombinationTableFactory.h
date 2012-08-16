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
