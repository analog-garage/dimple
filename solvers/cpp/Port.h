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
