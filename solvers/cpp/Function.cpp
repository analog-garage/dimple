#include "stdafx.h"
#include "Function.h"
#include "math.h"

//#include "CombinationTableFactory.h"

Function::Function(int id, CombinationTable * comboTable, vector<Variable*> & args) : _id(id)
{
	_comboTable = comboTable;
	for (unsigned int i = 0; i < args.size(); i++)
	{
		Variable * v = args[i];

		//Connect this function to the Variable
		Port * p = new Port(this);
		_ports.push_back(p);
		v->Connect(p);
	}
}

int Function::GetId()
{
    return _id;
}

Function::~Function(void)
{
	for (vector<Port*>::iterator it = _ports.begin(); it != _ports.end(); it++)
	{
		delete (*it);
	}
}

void Function::Initialize()
{
	
	for (int i =0; i < _ports.size(); i++)
	{
		_ports[i]->Initialize();
	}
	
}

void Function::Update(int outPortNum)
{
	vector<vector<int> > & table = (*_comboTable->GetTable());
	vector<double> & values = (*_comboTable->GetValues());

	
	vector<double> * outputMsgs = _ports[outPortNum]->GetOutputMsgs();

	for (unsigned int i = 0; i < outputMsgs->size(); i++)
	{
		(*outputMsgs)[i] = 0;
	}
	
	/*
	double maxLog = -1000;
	
	for (unsigned int tableIndex = 0; tableIndex < table.size(); tableIndex++)
	{
		double prob = log(values[tableIndex]);
		
		int outputIndex = -1;
		for (unsigned int inPortNum = 0; inPortNum < _ports.size(); inPortNum++)
		{
			if (inPortNum != outPortNum)
			{
				prob = prob + log((*_ports[inPortNum]->GetInputMsgs())[table[tableIndex][inPortNum]]);
				//prob *= (*_ports[inPortNum]->GetInputMsgs())[table[tableIndex][inPortNum]];
			}
		}
		(*outputMsgs)[outputIndex] += prob;
	}
	 */
	
	
	for (unsigned int tableIndex = 0; tableIndex < table.size(); tableIndex++)
	{
		double prob = values[tableIndex];
		int outputIndex = -1;
		for (unsigned int inPortNum = 0; inPortNum < _ports.size(); inPortNum++)
		{
			if (inPortNum != outPortNum)
			{
				prob *= (*_ports[inPortNum]->GetInputMsgs())[table[tableIndex][inPortNum]];
			}
			else
			{
				outputIndex = table[tableIndex][inPortNum];
			}
		}
		(*outputMsgs)[outputIndex] += prob;
	}
	
	
	double sum = 0;
	for (unsigned int i = 0; i < (*outputMsgs).size(); i++)
	{
		sum += (*outputMsgs)[i];
	}
	//TODO: can wrap this up in above code
	for (unsigned int i = 0; i < outputMsgs->size(); i++)
	{
		(*outputMsgs)[i] /= sum;
	}
	
}


void Function::Update()
{
	//TODO: Can we optimize?

	for (int i = 0; i < _ports.size(); i++)
		Update(i);
}

vector<Port*> * Function::GetPorts()
{
	return &_ports;
}


CombinationTable * Function::GetComboTable()
{
	return _comboTable;
}
