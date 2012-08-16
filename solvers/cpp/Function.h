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
