#pragma once

#include "IFunctionPointer.h"
#include "DoubleDomainItem.h"
#include <vector>
using namespace std;

//This class was created to test the C++ code.  This function is not used by the Matlab API
class XorFunction : public IFunctionPointer
{
public:
	XorFunction(void);
	~XorFunction(void);

	virtual double Evaluate(vector<IDomainItem*> & parameters)
	{
		int retVal = 1;
		for (unsigned int i = 0; i < parameters.size(); i++)
		{
			retVal ^= (int)((DoubleDomainItem*)parameters[i])->GetValue();
		}
		return (double)retVal;
	}
};
