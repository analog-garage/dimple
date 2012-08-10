
#include "mex.h"
#include "stdafx.h"
#include "MatlabFunctionPointer.h"
//#include "DoubleDomainItem.h"
#include "MatrixDomainItem.h"
#include "DimpleException.h"
#include "MexHelpers.h"

MatlabFunctionPointer::MatlabFunctionPointer(string name,vector<vector<int> > & dimensions)
{
	_name = name;
	_dimensions.assign(dimensions.begin(), dimensions.end());

}


MatlabFunctionPointer::~MatlabFunctionPointer(void)
{
}

string MatlabFunctionPointer::GetName()
{
	return _name;
}

double MatlabFunctionPointer::Evaluate(vector<IDomainItem*> & parameters)
{

	//Return value
	mxArray *lhs[1];
	
	//1st Memory allocation
	mxArray ** rhs = (mxArray**)malloc((parameters.size()+2)*sizeof(mxArray*));
	
	//First arg should be function name
	rhs[0] = mxCreateString(_name.c_str());
	
	//Second arg should be cell array
	rhs[1] = mxCreateCellMatrix((mwSize)_dimensions.size(),1);
	for (int i = 0; i < _dimensions.size(); i++)
	{
		
		mxArray * ar = mxCreateDoubleMatrix(1,_dimensions[i].size(),mxREAL);
		double * output = mxGetPr(ar);

		if (_dimensions[i].size() > 2)
		{
			char buf[256];
			sprintf(buf,"Failed to call Matlab function: %s.  Dimple currently only supports variable matrices of two dimensions",_name.c_str());
			throw DimpleException(buf);
		}

		for (int j = 0; j < _dimensions[i].size(); j++)
		{
			output[j] = _dimensions[i][j];
		}
	
		mxSetCell(rhs[1],i,ar);
		
	}
	
	
	int current = 0;
	for (int i = 0; i < parameters.size(); i++)
	{
		//2nd Memory Allocation
		//str = mxCreateString("Enter extension:  ");
		MatrixDomainItem * mdi = (MatrixDomainItem*)parameters[i];
		rhs[i+2] = createMatrix(*mdi->GetDims(),*mdi->GetRealPart(),*mdi->GetImagPart());

	}
	
	int result = 0;
	try
	{
		result = mexCallMATLAB(1,lhs,parameters.size()+2,rhs,"DimpleCallDelta");
	}
	catch (...)
	{
		char buf[256];
		sprintf(buf,"Failed to call Matlab function: %s",_name.c_str());
		throw DimpleException(buf);
	}
	if (result != 0)
	{
		throw DimpleException("Failed to call matlab function");
	}

	double retVal = mxGetScalar(lhs[0]);
	
	//Clean up 3rd memory allocation
	mxDestroyArray(lhs[0]);

	//Clean up 2nd memory allocations
	for (unsigned int i =0; i < _dimensions.size(); i++)
	{
		mxDestroyArray(rhs[i]);
	}
	
	//Clean up 1st memory allocation.
	free(rhs);
	
	return retVal;

}
