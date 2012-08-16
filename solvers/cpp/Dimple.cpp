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

// Dimple2.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"

#include "FactorGraph.h"
#include "Domain.h"
#include "DoubleDomainItem.h"
#include "XorFunction.h"

int _tmain(int argc, _TCHAR* argv[])
{
	try
	{
		/*
		Domain * domain = new Domain();
		domain->Add(new DoubleDomainItem(1));
		domain->Add(new DoubleDomainItem(0));

		vector<Variable*> arg;
		for (int i = 0; i < 4; i++)
		{
			arg.push_back(new Variable(i));
			arg[i]->SetDomain(domain);
		}

		//graph FourBitXor(b(4,1))
		FactorGraph gd = FactorGraph(arg);

		//con = bit;
		Variable * con = gd.NewVariable();
		con->SetDomain(domain);

		//func(@xorDelta,b(1:2),con);
		IFunctionPointer * xorDelta = new XorFunction();
		vector<Variable*> funcArgs = vector<Variable*>();
		funcArgs.push_back(arg[0]);
		funcArgs.push_back(arg[1]);
		funcArgs.push_back(con);

		Function * fun1 = gd.NewFunc(xorDelta,funcArgs);

		//func(@xorDelta,b(3:4),con);
		funcArgs.clear();
		funcArgs.push_back(arg[2]);
		funcArgs.push_back(arg[3]);
		funcArgs.push_back(con);
		Function * fun2 = gd.NewFunc(xorDelta,funcArgs);

		vector<Variable*> arg2;
		for (int i = 0; i < 6; i++)
		{
			arg2.push_back(new Variable(i));
			arg2[i]->SetDomain(domain);
		}

		//graph MyGraph(c(6,1))
		FactorGraph gd2 = FactorGraph(arg2);
		funcArgs.clear();
		funcArgs.push_back(arg2[0]);
		funcArgs.push_back(arg2[1]);
		funcArgs.push_back(arg2[2]);
		funcArgs.push_back(arg2[4]);
		gd2.AddGraph(&gd,funcArgs);

		//func(FourBitXor,c(1:2),c(4),c(6));
		funcArgs.clear();
		funcArgs.push_back(arg2[0]);
		funcArgs.push_back(arg2[1]);
		funcArgs.push_back(arg2[3]);
		funcArgs.push_back(arg2[5]);
		gd2.AddGraph(&gd,funcArgs);

		//c = bit(6,1);
		//priors(b,[.75 .6 .9 .1 .2 .9]);
		vector<Variable*> b = vector<Variable*>();
		double priors[] = {.75, .6, .9, .1, .2, .9};
		vector<double> priorVec = vector<double>(2);

		for (int i = 0; i < 6; i++)
		{
			priorVec[0] = priors[i];
			priorVec[1] = 1-priors[i];
			Variable * v = new Variable(i);
			v->SetDomain(domain);
			v->SetPriors(priorVec);
			b.push_back(v);
		}

		//g = MyGraph(c);
		FactorGraph * g = gd2.NewInstance(b);
		g->SetNumIterations(20);

		//initialize(g)
		g->Initialize();

		//solve(g);
		g->Solve();

		//x = beliefs(c);
		vector<double> beliefs;
		for (int i = 0; i < 6; i++)
		{
			b[i]->GetBeliefs(beliefs);
			printf("%d: %f\n",i,beliefs[0]);
		}

		vector<FactorGraph*> graphs = vector<FactorGraph*>();
		graphs.push_back(g);
		for (uint i = 0; i < graphs.size(); i++)
			delete (graphs[i]);
		delete domain;
		*/
	}
	catch(exception e)
	{	
		printf(e.what());
	}
	return 0;
}

