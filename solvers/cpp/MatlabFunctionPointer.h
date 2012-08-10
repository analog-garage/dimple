#pragma once

#include "IFunctionPointer.h"
#include "IDomainItem.h"
#include <string>
#include <vector>

using namespace std;

class MatlabFunctionPointer : public IFunctionPointer
{
public:
	MatlabFunctionPointer(string name, vector<vector<int> > & dimensions);
	~MatlabFunctionPointer(void);
	string GetName();
	virtual double Evaluate(vector<IDomainItem*> & parameters);
private:
	string _name;
	vector<vector<int> > _dimensions;
};
