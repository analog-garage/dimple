#include "stdafx.h"
#include "Port.h"

Port::~Port(void)
{
}

Port::Port(INode * parent)
{
	_parent = parent;
}
Port::Port(INode * parent, Port * sibling)
{
	_parent = parent;
	_siblingPort = sibling;
	sibling->_siblingPort = this;
}

void Port::Initialize()
{
	double val = 1.0/_inputMsg.size();
	for (unsigned int i = 0; i < _inputMsg.size(); i++)
	{
		_inputMsg[i] = val; 
	}
}


void Port::SetInputMsgs(vector<double> & values)
{
	_inputMsg.assign(values.begin(),values.end());
}
void Port::SetOutputMsgs(vector<double> & values)
{
	_siblingPort->_inputMsg.assign(values.begin(),values.end());
}
vector<double> * Port::GetInputMsgs()
{
	return & _inputMsg;
}
vector<double> * Port::GetOutputMsgs()
{
	return & (_siblingPort->_inputMsg);
}
INode * Port::GetConnectedNode()
{
	return _siblingPort->_parent;
}

INode * Port::GetParent()
{
	return _parent;
}
