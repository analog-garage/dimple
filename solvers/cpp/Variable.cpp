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
#include "Variable.h"
#include "DimpleException.h"
#include "math.h"
#include <limits>


Variable::Variable(int id,int domainLength) : _id(id), _domainLength(domainLength)
{
	_priors.resize(domainLength);
	double initVal = 1.0/_priors.size();
	for (int i = 0; i < _priors.size(); i++)
		 _priors[i] = initVal;
	//_domain = NULL;
}

Variable::~Variable(void)
{
	for (vector<Port*>::iterator it = _ports.begin(); it != _ports.end(); it++)
	{
		delete (*it);
	}
}

int Variable::GetDomainLength()
{
    return _domainLength;
}

int Variable::GetId()
{
	return _id;
}


void Variable::SetPriors(vector<double> & priors)
{

	if (priors.size() != _domainLength)
	{
		throw DimpleException("Domain and priors must be same size");
	}
	double sum = 0;
	for (unsigned int i = 0; i < priors.size(); i++)
	{
		sum += priors[i];
		_priors[i] = priors[i];
	}
	
	if (fabs(sum-1) > 1e-15)
	{
		throw DimpleException("Priors must sum to 1");
	}
}

void Variable::Connect(Port * port)
{
	//Create port
	Port * thisPort = new Port(this,port);
	vector<double> inmsgs = vector<double>(_domainLength);
	vector<double> outmsgs = vector<double>(_domainLength);
	double val = 1.0/(double)_domainLength;
	for (unsigned int i = 0; i < inmsgs.size(); i++)
	{
		inmsgs[i] = val;
		outmsgs[i] = val;
	}
	thisPort->SetInputMsgs(inmsgs);
	thisPort->SetOutputMsgs(outmsgs);
	_ports.push_back(thisPort);
	
}

vector<double> * Variable::GetPriors()
{
	return &_priors;
}


void Variable::GetBeliefs(vector<double> & outBeliefs)
{
	double minLog = -100;
	double maxLog = -std::numeric_limits<double>::infinity();
	
	//find maximum logarithm
	outBeliefs.assign(_priors.begin(),_priors.end());
	for (unsigned int i = 0; i < outBeliefs.size(); i++)
	{
		if (outBeliefs[i] == 0)
			outBeliefs[i] = minLog;
		else
			outBeliefs[i] = log(outBeliefs[i]);
	}

	
	for (int i = 0; i < outBeliefs.size(); i++)
	{
		for (unsigned int inPortNum = 0; inPortNum < _ports.size(); inPortNum++)
		{
			//outBeliefs[i] *= (*_ports[inPortNum]->GetInputMsgs())[i];		
			double tmp = (*_ports[inPortNum]->GetInputMsgs())[i];
			if (tmp == 0)
				outBeliefs[i] += minLog;
			else
				outBeliefs[i] += log(tmp);
		}
		maxLog = max(maxLog,outBeliefs[i]);
	}
	
	double sum = 0;		
	for (int i = 0; i < outBeliefs.size(); i++)
	{
		outBeliefs[i] = exp(outBeliefs[i]-maxLog);
		//outBeliefs[i] -= maxLog;
		sum += outBeliefs[i];
	}
	
	if (sum == 0)
	{
		throw DimpleException("about to divide by zero in Variable::GetBeliefs()");
	}
	
	for (int i = 0; i < outBeliefs.size(); i++)
	{
		outBeliefs[i] /= sum;
	}
	
}


void Variable::Initialize()
{
	for (int i = 0; i < _ports.size(); i++)
	{
		_ports[i]->Initialize();
	}

}

void Variable::Update()
{
	//See http://mathwiki/index.php/Dimple_Suggestions for a write-up explaining the reason for the logs.
	//D = degree
	//M = alphabet size
	//d = degree index
	//m = alphabet index
	double minLog = -100;
	int D = _ports.size();
	int M = _priors.size();
	
	//Compute alphas
	
	double * alphas = new double[M];
	for (int m = 0; m < M; m++)
	{
		if (_priors[m] == 0)
			alphas[m] = minLog;
		else
			alphas[m] = log(_priors[m]);
		
		for (int d = 0; d < _ports.size(); d++)
		{
			double tmp = (*_ports[d]->GetInputMsgs())[m];
			if (tmp == 0)
				alphas[m] += minLog;
			else
				alphas[m] += log(tmp);
		}
	}
	
	//Now compute output messages for each outgoing edge
	for (int out_d = 0; out_d < D; out_d++)
	{
		vector<double> * outMsgs = _ports[out_d]->GetOutputMsgs();
		
		double maxLog = -std::numeric_limits<double>::infinity();
		
		//set outMsgs to alpha - mu_d,m
		//find max alpha
		for (int m = 0; m < M; m++)
		{
			double tmp = (*_ports[out_d]->GetInputMsgs())[m];
			if (tmp == 0)
				tmp = minLog;
			else
				tmp = log(tmp);			
			
			(*outMsgs)[m] = alphas[m] - tmp;
			maxLog = max(maxLog,(*outMsgs)[m]);
		}
			
		//create sum
		double sum = 0;
		for (int m = 0; m < M; m++)
		{
			(*outMsgs)[m] = exp((*outMsgs)[m] - maxLog);
			sum += (*outMsgs)[m];
		}
		
		//calculate message by dividing by sum
		for (int m = 0; m < M; m++)
		{
			(*outMsgs)[m] = (*outMsgs)[m]/sum;
		}
	}

	delete [] alphas;
	
}

