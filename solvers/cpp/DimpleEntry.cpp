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

#include "mex.h" 
#include "stdafx.h"
#include "DimpleException.h"
#include <stdio.h>
#include <string.h>
#include <vector>
#include "DimpleManager.h"
#include "MatrixDomainItem.h"

using namespace std;

#include "MexHelpers.h"

//using namespace std;

static DimpleManager _dimple;

void getVariables(int nrhs, const mxArray * prhs[],int graphIdIndex,int varIdIndex, vector<Variable*> & vars)
{
	int numGraphIds;
	int numVarIds;
	double * graphIds = getDoubleArray(nrhs,prhs,graphIdIndex,&numGraphIds);
	double * varIds = getDoubleArray(nrhs,prhs,varIdIndex,&numVarIds);
	if (numVarIds != numGraphIds)
		throw DimpleException("Graph Id and Var Id vectors don't match in size.");
	vars.clear();
	for (int i= 0; i < numVarIds; i++)
	{
		vars.push_back(_dimple.GetVariable(graphIds[i],varIds[i]));
	}
}

void getVarArgListVariables(int nrhs, const mxArray * prhs[],int firstIndex, vector<Variable*> & vars,vector<vector<int> > & dimensions)
{
	int i = firstIndex;
	
	while (i < nrhs)
	{
		vector<int> graphIdDims;
		vector<int> varIdDims;
		vector<double> graphIds;
		vector<double> varIds;
		vector<double> imagPart;
		getMatrix(nrhs,prhs,i,graphIdDims, graphIds, imagPart);
		getMatrix(nrhs,prhs,i+1,varIdDims, varIds, imagPart);
		
		dimensions.push_back(varIdDims);
		for (int j = 0; j < varIds.size(); j++)
			vars.push_back(_dimple.GetVariable(graphIds[j],varIds[j]));

		i = i + 2;
		 
	}	
}

void newgraph(int nlhs, mxArray *plhs[],
    int nrhs, const mxArray *prhs[]) 
{
	//newgraph <graphIds> <varIds>
	vector<Variable*> vars = vector<Variable*>();
	getVariables(nrhs,prhs,1,2,vars);
	int id = _dimple.NewGraph(vars);
	setReturnNoError(nlhs,plhs,0,id);
}

//<command> <num vars> <domain length>
void newvars(int nlhs, mxArray *plhs[],
    int nrhs, const mxArray *prhs[]) 
{
	//<command> <num vars>
	//output: <graph ids> <var ids>
	//vector<int> graphIds = vector<int>();
	vector<int> varIds = vector<int>();
    int domainLength = getInt(nrhs,prhs,2);

	int numVars = getInt(nrhs,prhs,1);
	for (int i = 0; i < numVars; i++)
	{
		int id = _dimple.NewVariable(domainLength);
		//graphIds.push_back(-1);
		varIds.push_back(id);
	}

	setReturnNoError(nlhs,plhs,0,varIds);
}

void newinstance(int nlhs, mxArray *plhs[],
    int nrhs, const mxArray *prhs[])
{
	//<command> <graph Id> <graph ids> <var ids>
	vector<Variable*> vars = vector<Variable*>();
	int graphId = getInt(nrhs,prhs,1);
	getVariables(nrhs,prhs,2,3,vars);
	int id = _dimple.NewInstance(graphId,vars);
	setReturnNoError(nlhs,plhs,0,id);	
}


void nestgraph(int nlhs, mxArray *plhs[],
    int nrhs, const mxArray *prhs[]) 
{
	//<command> <parent id> <child id> <graph ids> <var ids>
	vector<Variable*> vars = vector<Variable*>();
	getVariables(nrhs,prhs,3,4,vars);
	int parentId = getInt(nrhs,prhs,1);
	int childId = getInt(nrhs,prhs,2);
	_dimple.GetGraph(parentId)->AddGraph(_dimple.GetGraph(childId),vars);
}



//    DimpleEntry('setpriors',graphIds,varIds,p);
void setpriors(int nlhs, mxArray *plhs[],
    int nrhs, const mxArray *prhs[]) 
{
	//<command> <graph ids> <var Ids> <priors>
	vector<Variable*> vars = vector<Variable*>();


	getVariables(nrhs,prhs,1,2,vars);

	
	int numCols;
	int numRows;
	double * priors = getDoubleMatrix(nrhs,prhs,3,&numRows,&numCols);
	vector<double> vecPriors;


	for (int i = 0; i < vars.size(); i++)
	{
		vecPriors.clear();
		for (int j = 0; j < numCols; j++)
		{
			//mexprintf("%d %d\n",i,j);
			vecPriors.push_back(priors[j * numRows + i]);
		}
		Variable * var = vars[i];
		var->SetPriors(vecPriors);
	}
}

void setnumiter(int nrhs, const mxArray *prhs[]) 
{
	//int graphId = getInt(nrhs,prhs,1);
	int numIter = getInt(nrhs,prhs,1);
	//_dimple.GetGraph(graphId)->SetNumIterations(numIter);
	vector<FactorGraph*> * graphs = _dimple.GetGraphs();
	for (int i = 0; i < graphs->size(); i++)
	{
		(*graphs)[i]->SetNumIterations(numIter);
	}
}

void solve(int nrhs, const mxArray *prhs[]) 
{
	int graphId = getInt(nrhs,prhs,1);
	_dimple.GetGraph(graphId)->Solve();
}

void initgraph(int nrhs, const mxArray *prhs[]) 
{
	int graphId = getInt(nrhs,prhs,1);
	_dimple.GetGraph(graphId)->Initialize();
}

void iterate(int nlhs, mxArray *plhs[],int nrhs, const mxArray *prhs[]) 
{
	//<command> <fg id> <num iter>
	int fgId = getInt(nrhs,prhs,1);
	int numIter = getInt(nrhs,prhs,2);
	_dimple.GetGraph(fgId)->Iterate(numIter);
}


void getbeliefs(int nlhs, mxArray *plhs[],int nrhs, const mxArray *prhs[]) 
{
	if (nlhs >= 1)
	{
		//<command> <fg ids> <varIds>
		vector<Variable*> vars = vector<Variable*>();

		getVariables(nrhs,prhs,1,2,vars);

		vector<double> beliefs = vector<double>();

		int m = vars.size();
		int n = vars[0]->GetDomainLength();
		mxArray * mxArr = mxCreateDoubleMatrix(m,n,mxREAL);
		double * data = mxGetPr(mxArr);

		for (int i = 0; i < m; i++)
		{
			if (vars[i]->GetDomainLength() != n)
			{
				mexErrMsgTxt("Expected more arguments.");
			}

			vars[i]->GetBeliefs(beliefs);

			for (int j = 0; j < n; j++)
			{
				data[i+j*m] = beliefs[j];
			}
		}
		plhs[0] = mxArr;
	}
}

void getgraphvars(int nlhs, mxArray *plhs[],int nrhs, const mxArray *prhs[])
{
	//Get graph id
	//Retrieve all variables from graph
	//Package them up in array
	int graphId = getInt(nrhs,prhs,1);
	vector<Variable*> * vars = _dimple.GetGraph(graphId)->GetVariables();
	vector<int> varIds;
	for (int i = 0; i < vars->size(); i++)
	{
		varIds.push_back((*vars)[i]->GetId());
	}
	setReturnNoError(nlhs, plhs,0,varIds);

}


void getgraphfuncs(int nlhs, mxArray *plhs[],int nrhs, const mxArray *prhs[])
{
	//Get graph id
	//Retrieve all variables from graph
	//Package them up in array
	int graphId = getInt(nrhs,prhs,1);
	vector<Function*> * funcs = _dimple.GetGraph(graphId)->GetFunctions();
	vector<int> funcIds;
	for (int i = 0; i < funcs->size(); i++)
	{
		funcIds.push_back((*funcs)[i]->GetId());
	}
	setReturnNoError(nlhs, plhs,0,funcIds);
	
}

void getconnectedvariables(int nlhs, mxArray *plhs[],int nrhs, const mxArray *prhs[])
{
	//graphId, funcId
	int graphId = getInt(nrhs,prhs,1);
	int funcId = getInt(nrhs,prhs,2);
	Function * func = _dimple.GetGraph(graphId)->GetFunction(funcId);
	vector<Port*> * ports = func->GetPorts();
	vector<int> varIds;
	for (int i = 0; i < ports->size(); i++)
	{
		int id = ((Variable*)(*ports)[i]->GetConnectedNode())->GetId();
		varIds.push_back(id);
	}
	setReturnNoError(nlhs, plhs,0,varIds);
	
}


void clear()
{
	_dimple.Clear();
}

//int graphId,int tableId,int [][] table,double [] values) throws Exception;
/*
 * Args:
 * graphId
 * tableId
 * 2D array for table
 * array of values
 */
void createTable(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[])
{
	int graphId = getInt(nrhs,prhs,1);
	FactorGraph * graph = _dimple.GetGraph(graphId);
    //int tableId = getInt(nrhs,prhs,2);
    vector<vector<int> > table;
    vector<double> values;
    getIntMatrix(nrhs,prhs,2,table);
    getDoubleVector(nrhs,prhs,3,values);

    int tableid = graph->createTable(table,values);

    setReturnNoError(nlhs,plhs,0,tableid);

}
    
/*
 * This function specifies a function node and its connected variables.  The table ID references a Table
 * created with the createTable call.
 *
 * args:
 * graphid
 * tableId
 * graphIds
 * varIds
 */
//int graphId,int tableId,int [] varIds) throws Exception;
void createTableFunc(int nlhs, mxArray *plhs[],int nrhs, const mxArray *prhs[])
{
	int graphId = getInt(nrhs,prhs,1);
	FactorGraph * graph = _dimple.GetGraph(graphId);
    int tableId = getInt(nrhs,prhs,2);
        
	vector<Variable*> vars = vector<Variable*>();
	getVariables(nrhs,prhs,3,4,vars);
    
    int funcid = graph->createTableFunc(tableId,vars)->GetId();
    
    setReturnNoError(nlhs,plhs,0,funcid);
}


void mexFunction(int nlhs, mxArray *plhs[],
    int nrhs, const mxArray *prhs[]) 
{

	//This mex function has to manage a list of factorgraphs by Id.

	try
	{
		if (nrhs < 1)
		{
			mexErrMsgTxt("First argument must be string");
		}
	    
		char command[256];

		mxGetString(prhs[0],command,256);
	    
		if (strcmp(command,"newvars") == 0)
		{
			newvars(nlhs,plhs,nrhs,prhs);
		}
		
		else if(strcmp(command,"newgraph") == 0)
		{
			newgraph(nlhs,plhs,nrhs,prhs);
		}
		else if(strcmp(command,"nestgraph") == 0)
		{
			nestgraph(nlhs,plhs,nrhs,prhs);
		}
		else if(strcmp(command,"newinstance") == 0)
		{
			newinstance(nlhs,plhs,nrhs,prhs);
		}
		else if(strcmp(command,"setpriors") == 0)
		{
			setpriors(nlhs,plhs,nrhs,prhs);
		}
		else if (strcmp(command,"setnumiter") == 0)
		{
			setnumiter(nrhs,prhs);
		}
		else if (strcmp(command,"solve") == 0)
		{
			solve(nrhs,prhs);
		}
        else if (strcmp(command,"createTable") == 0)
        {
            createTable(nlhs,plhs,nrhs,prhs);
        }
        else if (strcmp(command,"createTableFunc") == 0)
        {
            createTableFunc(nlhs,plhs,nrhs,prhs);
        }
		else if (strcmp(command,"initgraph") == 0)
		{
			initgraph(nrhs,prhs);
		}
		
		else if (strcmp(command,"getbeliefs") == 0)
		{
			getbeliefs(nlhs,plhs,nrhs,prhs);
		}
		else if(strcmp(command,"iterate") == 0)
		{
			iterate(nlhs,plhs,nrhs,prhs);
		}
		else if(strcmp(command,"clear") == 0)
		{
			clear();
		}
		else if (strcmp(command,"getgraphvars") == 0)
		{
			getgraphvars(nlhs,plhs,nrhs,prhs);			
		}		
		else if (strcmp(command,"getgraphfunctions") == 0)
		{
			getgraphfuncs(nlhs,plhs,nrhs,prhs);
		}
		else if (strcmp(command,"getconnectedvariables")==0)
		{
			getconnectedvariables(nlhs,plhs,nrhs,prhs);
		}
		else
		{
			mexErrMsgTxt("Unknown command");
		}

	}
	catch (DimpleException e)
	{
		const char * msg = e.what();
		char buf[512];
		sprintf(buf,"Enounctered Dimple Exception: %s\n",msg);
		mexErrMsgTxt(buf);
	}
	catch (exception e)
	{
		const char * msg = e.what();
		char buf[512];
		sprintf(buf,"Enounctered Exception: %s\n",msg);
		mexErrMsgTxt(buf);
	}
	/*
	catch (...)
	{
		mexErrMsgTxt("Encountered Exception.");
	}
	*/
} 
