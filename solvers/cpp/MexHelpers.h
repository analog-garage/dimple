/*
 *  MexHelpers.h
 *  DimpleXCode
 *
 *  Created by Shawn Hershey on 5/12/09.
 *  Copyright 2009 __MyCompanyName__. All rights reserved.
 *
 */


//These functions are used to extract and assign data from the Matlab mxArray
#ifndef _MEX_HELPERS_H_
#define _MEX_HELPERS_H_

#include "mex.h"
#include "DimpleException.h"
#include <vector>
using namespace std;

void checkEnoughArgs(int nrhs,int index);
double * getDoubleArray(int nrhs, const mxArray * prhs[],int index,int * numElements);
void getMatrix(int nrhs,const mxArray * prhs[], int index,vector<int> & vecDims, vector<double> & vecRp, vector<double> & vecIp);
mxArray * createMatrix(vector<int> & dims, vector<double> & realPart, vector<double> & imagPart);
void getDoubleVector(int nrhs, const mxArray * prhs[], int index, vector<double> & vals);
void getIntVector(int nrhs, const mxArray * prhs[], int index, vector<int> & vals);
double * getDoubleMatrix(int nrhs,const mxArray * prhs[], int index,int * numRows, int * numCols);
void getIntMatrix(int nrhs,const mxArray * prhs[], int index, vector<vector<int> > & output);
void setReturnNoError(int nlhs, mxArray * plhs[],int index,double value);
void setReturnNoError(int nlhs, mxArray * plhs[], int index, string str);
void setReturnNoError(int nlhs, mxArray * plhs[],int index,vector<int> & vec);
void setReturnNoError(int nlhs, mxArray * plhs[],int index,vector<double> & vec);
void checkProblemDefined();
string getString(int nrhs, const mxArray *prhs[],int strIndex);
int getInt(int nrhs, const mxArray *prhs[],int intIndex);
int getVarId(int nrhs, const mxArray *prhs[],int varIdIndex);

#endif
