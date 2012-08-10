#pragma once
#include <vector>
#include "IDomainItem.h"
using namespace std;

class IFunctionPointer
{
public:
	IFunctionPointer();
	virtual double Evaluate(vector<IDomainItem*> & parameters) = 0;
	virtual int GetHashValue();
private:
	int _hashValue;
	static int _currentHashValue;
};
