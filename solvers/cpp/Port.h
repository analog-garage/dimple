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

#include "INode.h"
#include <vector>

using namespace std;

class Port
{
public:
	Port(INode * parent);
	Port(INode * parent, Port * sibling);
	~Port(void);

	void SetInputMsgs(vector<double> & values);
	void SetOutputMsgs(vector<double> & values);
	vector<double> * GetInputMsgs();
	vector<double> * GetOutputMsgs();
	INode * GetConnectedNode();
	void Initialize();
	INode * GetParent();
private:
	Port * _siblingPort;
	vector<double> _inputMsg;
	INode * _parent;
};
