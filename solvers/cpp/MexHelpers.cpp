/*
 *  MexHelpers.cpp
 *  DimpleXCode
 *
 *  Created by Shawn Hershey on 5/12/09.
 *  Copyright 2009 __MyCompanyName__. All rights reserved.
 *
 */

#include "MexHelpers.h"

void checkEnoughArgs(int nrhs,int index)
{
	if (nrhs <= index)
	{
		mexErrMsgTxt("Expected more arguments.");
	}
}

double * getDoubleArray(int nrhs, const mxArray * prhs[],int index,int * numElements)
{
	
	checkEnoughArgs(nrhs,index);
	const mxArray * mxDomain = prhs[index];
	*numElements = mxGetNumberOfElements(mxDomain);
	double * data = (double*)mxGetData(mxDomain);
	
	return data;
}

void getMatrix(int nrhs,const mxArray * prhs[], int index,vector<int> & vecDims, vector<double> & vecRp, vector<double> & vecIp)
{
	
	checkEnoughArgs(nrhs,index);
	
	int numElements;
    int numDims;
	double * pr;
	double * pi;
    const int *dims;  
	
	
    /* Get the number of elements in the input argument */
    numElements=mxGetNumberOfElements(prhs[index]);
    numDims=mxGetNumberOfDimensions(prhs[index]);
    /* Get the number of dimensions in the input argument. */
    dims=mxGetDimensions(prhs[index]);
    pr=(double *)mxGetPr(prhs[index]);	
	pi = (double*)mxGetPi(prhs[index]);
	
	for (int i = 0; i < numDims; i++)
		vecDims.push_back(dims[i]);
	for (int i = 0; i < numElements; i++)
	{
		vecRp.push_back(pr[i]);
		if (mxIsComplex(prhs[index]))
			vecIp.push_back(pi[i]);
		else
			vecIp.push_back(0);
		
	}
}

mxArray * createMatrix(vector<int> & dims, vector<double> & realPart, vector<double> & imagPart)
{
	
	bool isreal = true;
	for (unsigned int i = 0; i < imagPart.size(); i++)
	{
		if (imagPart[i] != 0)
			isreal = false;
	}
	
	mxArray * retVal;
	
	int * dimarr = new int[dims.size()];
	for (int j= 0; j < dims.size(); j++)
		dimarr[j] = dims[j];
	
	//Create matrix
	if (isreal)
		retVal = mxCreateNumericArray(dims.size(),dimarr,mxDOUBLE_CLASS,mxREAL);
	else
		retVal = mxCreateNumericArray(dims.size(),dimarr,mxDOUBLE_CLASS,mxCOMPLEX);
	
	delete [] dimarr;

	double * pr = (double*)mxGetPr(retVal);
	double * pi;
	if (!isreal)
		pi = (double*)mxGetPi(retVal);
	
	for (int  j = 0; j < realPart.size(); j++)
	{
		
		pr[j] = realPart[j];
		if (!isreal)
			pi[j] = imagPart[j];
	}	
	return retVal;
	
}



void getDoubleVector(int nrhs, const mxArray * prhs[], int index, vector<double> & vals)
{
	int num;
	double * dVals = getDoubleArray(nrhs,prhs,index,&num);
	vals.resize(num);
	for (int i = 0; i < num; i++)
	{
		vals[i] = dVals[i];
	}
}

void getIntVector(int nrhs, const mxArray * prhs[], int index, vector<int> & vals)
{
	int num;
	double * dVals = getDoubleArray(nrhs,prhs,index,&num);
	vals.resize(num);
	for (int i = 0; i < num; i++)
	{
		vals[i] = dVals[i];
	}    
}

double * getDoubleMatrix(int nrhs,const mxArray * prhs[], int index,int * numRows, int * numCols)
{
	
	const mwSize * dims = mxGetDimensions(prhs[index]);
	const mwSize numDims = mxGetNumberOfDimensions(prhs[index]);
	
	if (numDims != 2)
		throw DimpleException("expected two dimensions");
	
	
	
	*numRows = dims[0];
	*numCols = dims[1];
	
	double *data = (double*)mxGetData(prhs[index]);
	return data;
}

void getIntMatrix(int nrhs,const mxArray * prhs[], int index, vector<vector<int> > & output)
{
	const mwSize * dims = mxGetDimensions(prhs[index]);
	const mwSize numDims = mxGetNumberOfDimensions(prhs[index]);
	
	if (numDims != 2)
		throw DimpleException("expected two dimensions");
	
	
	
	int numRows = dims[0];
	int numCols = dims[1];
	
	double *data = (double*)mxGetData(prhs[index]);

    output.resize(numRows);
    
    for (int i = 0; i < numRows; i++)
    {
        output[i].resize(numCols);
        for (int j = 0; j < numCols; j++)
        {
            output[i][j] = data[j*numRows+i];
        }
    }
}



void setReturnNoError(int nlhs, mxArray * plhs[],int index,double value)
{
	//Return Id
    if (nlhs >= index)
    {
        plhs[index] = mxCreateDoubleScalar(value);
    }
	
}

void setReturnNoError(int nlhs, mxArray * plhs[], int index, string str)
{
	if (nlhs > index)
	{
		plhs[index] = mxCreateString(str.c_str());
	}
	
}

//TODO: Turn into template function to allow different vector types.
void setReturnNoError(int nlhs, mxArray * plhs[],int index,vector<int> & vec)
{
	//Convert variables to mxArray
	if (nlhs > index)
	{
		plhs[index] = mxCreateDoubleMatrix(1,vec.size(),mxREAL);
		double * output = mxGetPr(plhs[index]);
		for (int i = 0; i < vec.size(); i++)
		{
			output[i] = vec[i];
		}
	}
}

//TODO: Turn into template function to allow different vector types.
void setReturnNoError(int nlhs, mxArray * plhs[],int index,vector<double> & vec)
{
	//Convert variables to mxArray
	if (nlhs > index)
	{
		plhs[index] = mxCreateDoubleMatrix(1,vec.size(),mxREAL);
		double * output = mxGetPr(plhs[index]);
		for (int i = 0; i < vec.size(); i++)
		{
			output[i] = vec[i];
		}
	}
}


void checkProblemDefined()
{
	//TODO:
	/*
	 //Make sure dimple problem exists
	 if (_problem == NULL)
	 {
	 throw DimpleException("Must call begin before setting domain.");
	 }
	 */
}

string getString(int nrhs, const mxArray *prhs[],int strIndex)
{
	checkProblemDefined();
	if (nrhs <= strIndex)
	{
		throw DimpleException("Failed to get string.  Not enough arguments specified.");
	}
	
	//TODO: Error check that it's a string
    char varName[256];
    mxGetString(prhs[strIndex],varName,256);
	return varName;
}

int getInt(int nrhs, const mxArray *prhs[],int intIndex)
{
	checkProblemDefined();
	
	if (nrhs <= intIndex)
	{
		char buf[64];
		sprintf(buf,"parameter %d must be integer",intIndex);
		throw DimpleException(buf);
	}
    
    // check to make sure the first input argument is a scalar 
    if( !mxIsDouble(prhs[intIndex]) || mxIsComplex(prhs[intIndex]) ||
	   mxGetN(prhs[intIndex])*mxGetM(prhs[intIndex])!=1 ) 
    {
        mexErrMsgTxt("expected a scalar.");
    }
	
    // get the scalar input x 
    return (int)mxGetScalar(prhs[intIndex]);  
    
}

int getVarId(int nrhs, const mxArray *prhs[],int varIdIndex)
{
	checkProblemDefined();
	
	if (nrhs <= varIdIndex)
	{
		char buf[64];
		sprintf(buf,"VarId must be parameter %d",varIdIndex);
		throw DimpleException(buf);
	}
    
    // check to make sure the first input argument is a scalar 
    if( !mxIsDouble(prhs[varIdIndex]) || mxIsComplex(prhs[varIdIndex]) ||
	   mxGetN(prhs[varIdIndex])*mxGetM(prhs[varIdIndex])!=1 ) 
    {
        mexErrMsgTxt("Variable Id must be a scalar.");
    }
	
    // get the scalar input x 
    return (int)mxGetScalar(prhs[varIdIndex]);  
    
}
